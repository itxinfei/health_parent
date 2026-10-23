package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.redis.utils.RedisUtils;
import com.xinfei.health.appointment.constant.HealthCacheConstants;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.bo.ForbidCheckBo;
import com.xinfei.health.appointment.domain.vo.AppOrderVo;
import com.xinfei.health.appointment.domain.vo.ForbidCheckVo;
import com.xinfei.health.appointment.domain.HealthOrderExt;
import com.xinfei.health.appointment.domain.HealthOrderLog;
import com.xinfei.health.appointment.domain.Member;
import com.xinfei.health.appointment.domain.Order;
import com.xinfei.health.appointment.domain.Setmeal;
import com.xinfei.health.appointment.domain.bo.AppOrderBo;
import com.xinfei.health.appointment.mapper.HealthOrderExtMapper;
import com.xinfei.health.appointment.mapper.HealthOrderLogMapper;
import com.xinfei.health.appointment.mapper.MemberMapper;
import com.xinfei.health.appointment.mapper.OrderMapper;
import com.xinfei.health.appointment.mapper.SetmealMapper;
import com.xinfei.health.appointment.service.IAppOrderService;
import com.xinfei.health.appointment.service.IForbidService;
import com.xinfei.health.appointment.service.IOrderService;
import com.xinfei.health.appointment.service.IOrdersettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_BOOKED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.ORDER_STATUS_UNARRIVED;

/**
 * C 端预约下单 服务层处理（08 §5.3、13 §5.2）
 *
 * <p>事务内顺序：验证码 → 名额 → 重复预约 → 识别/注册会员 → 落单 → 条件扣减 → 流水。
 * 禁忌校验（§5.3 步骤 5）待 §2.7 规则表落地后在扣减名额之前叠加。
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class AppOrderServiceImpl implements IAppOrderService {

    /**
     * 下单验证码 TTL 5 分钟（13 §5.2）
     */
    private static final Duration SMS_CODE_TTL = Duration.ofMinutes(5);

    /**
     * 微信预约（08 §12 禁止自造取值）
     */
    private static final String ORDER_TYPE_WECHAT = "微信预约";

    private final OrderMapper orderMapper;
    private final MemberMapper memberMapper;
    private final SetmealMapper setmealMapper;
    private final HealthOrderExtMapper extMapper;
    private final HealthOrderLogMapper logMapper;
    private final IOrdersettingService ordersettingService;
    private final IOrderService orderService;
    private final IForbidService forbidService;

    @Override
    public boolean checkDate(LocalDate orderDate) {
        checkQuota(orderDate);
        return true;
    }

    @Override
    public boolean checkCode(String phone, String smsCode) {
        String cached = RedisUtils.getCacheObject(smsKey(phone));
        return smsCode != null && smsCode.equals(cached);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppOrderVo submitOrder(AppOrderBo bo) {
        // 步骤 1：最终校验并删除验证码（一次性，13 R5）
        String key = smsKey(bo.getPhone());
        String cached = RedisUtils.getCacheObject(key);
        if (cached == null || !cached.equals(bo.getSmsCode())) {
            throw new ServiceException("短信验证码错误或已过期", HealthErrorCode.SMS_CODE_INVALID);
        }
        RedisUtils.deleteObject(key);

        // 步骤 2：日期已设置且剩余名额 > 0（先校验，真正的条件扣减在步骤 7）
        checkQuota(bo.getOrderDate());

        // 步骤 3：重复预约（同会员/同日/同套餐）→ 5003；身份证先识别会员再判重
        Member member = findOrRegisterMember(bo);
        boolean duplicated = orderMapper.exists(Wrappers.<Order>lambdaQuery()
            .eq(Order::getMemberId, member.getId())
            .eq(Order::getOrderDate, bo.getOrderDate())
            .eq(Order::getSetmealId, bo.getSetmealId()));
        if (duplicated) {
            throw new ServiceException("该会员当日已预约此套餐", HealthErrorCode.ORDER_DUPLICATE);
        }

        // 套餐存在性复查（13 §5.2：禁止信任前端传入的套餐）
        Setmeal setmeal = setmealMapper.selectById(bo.getSetmealId());
        if (setmeal == null) {
            throw new ServiceException("套餐不存在：" + bo.getSetmealId(), HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }

        // 步骤 5：禁忌校验（08 §5.3：必须在步骤 7 扣减名额之前；拦截不得占用名额）
        ForbidCheckVo forbid = forbidService.trialCalc(toForbidCheckBo(bo));
        if (!forbid.isPass()) {
            String tips = forbid.getHits().stream()
                .map(h -> h.getTipText() + "（" + h.getItemName() + "）")
                .collect(Collectors.joining("；"));
            throw new ServiceException("命中禁忌，禁止预约：" + tips, HealthErrorCode.FORBID_BLOCKED);
        }
        // 延后补检档须用户二次确认（13 §5.3 action=2 处置）
        if (!forbid.getDelays().isEmpty() && !Boolean.TRUE.equals(bo.getConfirmedDelay())) {
            String tips = forbid.getDelays().stream().map(ForbidCheckVo.ForbidHitVo::getTipText)
                .collect(Collectors.joining("；"));
            throw new ServiceException("建议延后补检：" + tips);
        }
        // 提示档文案并入订单备注与返回体（08 §5.3 步骤 5 action=1 处置）
        String warnTips = forbid.getWarns().stream().map(ForbidCheckVo.ForbidHitVo::getTipText)
            .collect(Collectors.joining("；"));

        // 步骤 6：插入订单 + 扩展（flow_status=已预约）
        Order order = new Order();
        order.setMemberId(member.getId());
        order.setOrderDate(bo.getOrderDate());
        order.setOrderType(ORDER_TYPE_WECHAT);
        order.setOrderStatus(ORDER_STATUS_UNARRIVED);
        order.setSetmealId(bo.getSetmealId());
        orderMapper.insert(order);

        HealthOrderExt ext = new HealthOrderExt();
        ext.setOrderId(order.getId());
        ext.setFlowStatus(FLOW_BOOKED);
        ext.setPostponeCount(0);
        extMapper.insert(ext);

        // 步骤 7：条件更新扣减名额，影响 0 行回滚 → 5002（防超卖）
        if (!ordersettingService.occupy(bo.getOrderDate())) {
            throw new ServiceException("名额已满", HealthErrorCode.ORDER_QUOTA_FULL);
        }

        // 步骤 8：写流水（动作=预约；提示档文案记入备注，t_order 冻结表无备注列）
        HealthOrderLog log = new HealthOrderLog();
        log.setOrderId(order.getId());
        log.setAction("预约");
        log.setFromStatus(null);
        log.setToStatus(FLOW_BOOKED);
        log.setOperator(member.getName());
        log.setOperatorRole("member");
        log.setRemark(joinRemark(bo.getMember().getRemark(), warnTips));
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);

        AppOrderVo vo = new AppOrderVo();
        vo.setOrderId(order.getId());
        vo.setOrderDate(order.getOrderDate());
        vo.setSetmealName(setmeal.getName());
        vo.setMemberName(member.getName());
        vo.setNoticeText(joinRemark(setmeal.getAttention(), warnTips));
        return vo;
    }

    /**
     * 步骤 2/4 共用的名额预检（reservations 可空须 COALESCE 由 SQL 保证）
     */
    private void checkQuota(LocalDate orderDate) {
        if (orderDate.isBefore(LocalDate.now())) {
            throw new ServiceException("预约日期不能早于今天", HealthErrorCode.ORDERSETTING_NOT_FOUND);
        }
        var setting = ordersettingService.queryByRange(orderDate, orderDate);
        if (setting.isEmpty()) {
            throw new ServiceException("该日期未开放预约", HealthErrorCode.ORDERSETTING_NOT_FOUND);
        }
        if (setting.get(0).getRemain() <= 0) {
            throw new ServiceException("该日期名额已满", HealthErrorCode.ORDER_QUOTA_FULL);
        }
    }

    /**
     * 步骤 4：会员按姓名 + 身份证识别，未匹配则注册（regTime=now）
     */
    private Member findOrRegisterMember(AppOrderBo bo) {
        AppOrderBo.AppOrderMember m = bo.getMember();
        Member member = memberMapper.selectOne(Wrappers.<Member>lambdaQuery()
            .eq(Member::getName, m.getName())
            .eq(Member::getIdCard, m.getIdCard())
            .last("LIMIT 1"));
        if (ObjectUtil.isNotNull(member)) {
            return member;
        }
        member = new Member();
        member.setName(m.getName());
        member.setSex(m.getSex());
        member.setIdCard(m.getIdCard());
        member.setPhoneNumber(StringUtils.isNotBlank(m.getPhone()) ? m.getPhone() : bo.getPhone());
        member.setEmail(m.getEmail());
        member.setRemark(m.getRemark());
        member.setRegTime(LocalDate.now());
        memberMapper.insert(member);
        return member;
    }

    /**
     * 禁忌试算入参：套餐 + 人员四维（sex 取体检人，其余取 ext）
     */
    private ForbidCheckBo toForbidCheckBo(AppOrderBo bo) {
        ForbidCheckBo checkBo = new ForbidCheckBo();
        checkBo.setSetmealId(bo.getSetmealId());
        AppOrderBo.AppOrderMember m = bo.getMember();
        checkBo.setSex(m == null ? null : m.getSex());
        if (bo.getExt() != null) {
            checkBo.setMarried(bo.getExt().getMarried());
            checkBo.setHasSexHistory(bo.getExt().getHasSexHistory());
            checkBo.setPregnancy(bo.getExt().getPregnancy());
            checkBo.setInMenstruation(bo.getExt().getInMenstruation());
            checkBo.setMetalImplant(bo.getExt().getMetalImplant());
        }
        return checkBo;
    }

    /**
     * 文案拼接（空值跳过，避免 "null" 落库）
     */
    private String joinRemark(String base, String append) {
        if (StringUtils.isBlank(append)) {
            return base;
        }
        if (StringUtils.isBlank(base)) {
            return append;
        }
        return base + "；" + append;
    }

    private String smsKey(String phone) {
        return HealthCacheConstants.SMS_ORDER_KEY + phone;
    }

    /**
     * P08 tab → flow_status 唯一映射（13 §3.2）
     */
    private static final Map<String, List<String>> TAB_FLOW = Map.of(
        "待检查", List.of("已预约", "已到诊", "检查中"),
        "已完成", List.of("待总检", "已总检", "报告已打印", "报告已发放"),
        "已取消", List.of("已取消")
    );

    @Override
    public TableDataInfo<AppOrderVo> myOrders(Integer memberId, String tab, PageQuery pageQuery) {
        List<String> flows = TAB_FLOW.get(tab);
        Page<Order> page = orderMapper.selectPage(pageQuery.build(), Wrappers.<Order>lambdaQuery()
            .eq(Order::getMemberId, memberId)
            .in(flows != null, Order::getId, memberOrderIds(memberId, flows))
            .orderByDesc(Order::getOrderDate));
        // flows 为空集合时 in 空列表会丢条件，先解出 id 集合再过滤
        if (flows != null && flows.isEmpty()) {
            return TableDataInfo.build(new Page<>(page.getCurrent(), page.getSize(), 0));
        }
        Page<AppOrderVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(o -> toAppVo(o, memberId)).toList());
        return TableDataInfo.build(voPage);
    }

    @Override
    public AppOrderVo myOrder(Integer memberId, Integer orderId) {
        return toAppVo(requireOwnedOrder(memberId, orderId), memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rescheduleMine(Integer memberId, Integer orderId, LocalDate targetDate) {
        Order order = requireOwnedOrder(memberId, orderId);
        // C 端改期仅限已预约（13 §5.4）；复用后台原子双更新
        HealthOrderExt ext = extMapper.selectOne(Wrappers.<HealthOrderExt>lambdaQuery()
            .eq(HealthOrderExt::getOrderId, orderId));
        if (ext == null || !"已预约".equals(ext.getFlowStatus())) {
            throw new ServiceException("仅已预约的订单可改期", HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        orderService.reschedule(orderId, rescheduleBo(targetDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMine(Integer memberId, Integer orderId, String reason) {
        requireOwnedOrder(memberId, orderId);
        com.xinfei.health.appointment.domain.bo.OrderCancelBo bo =
            new com.xinfei.health.appointment.domain.bo.OrderCancelBo();
        bo.setReason(reason);
        orderService.cancel(orderId, bo);
    }

    /**
     * 按会员与流程状态集合解出订单 id（tab 过滤）
     */
    private List<Integer> memberOrderIds(Integer memberId, List<String> flows) {
        if (flows == null) {
            return List.of();
        }
        List<Integer> ids = extMapper.selectList(Wrappers.<HealthOrderExt>lambdaQuery()
                .in(HealthOrderExt::getFlowStatus, flows)
                .select(HealthOrderExt::getOrderId))
            .stream().map(HealthOrderExt::getOrderId).toList();
        return ids.isEmpty() ? List.of(-1) : ids;
    }

    /**
     * 属主校验：仅本人可查改取消（13 R3）
     */
    private Order requireOwnedOrder(Integer memberId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("预约订单不存在：" + orderId, HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        if (!memberId.equals(order.getMemberId())) {
            throw new ServiceException("无权访问他人订单", HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        return order;
    }

    private com.xinfei.health.appointment.domain.bo.OrderRescheduleBo rescheduleBo(LocalDate targetDate) {
        com.xinfei.health.appointment.domain.bo.OrderRescheduleBo bo =
            new com.xinfei.health.appointment.domain.bo.OrderRescheduleBo();
        bo.setTargetDate(targetDate);
        return bo;
    }

    /**
     * 组装 C 端视图（含套餐名与流程状态）
     */
    private AppOrderVo toAppVo(Order order, Integer memberId) {
        AppOrderVo vo = new AppOrderVo();
        vo.setOrderId(order.getId());
        vo.setOrderDate(order.getOrderDate());
        vo.setMemberId(order.getMemberId());
        Setmeal setmeal = setmealMapper.selectById(order.getSetmealId());
        vo.setSetmealName(setmeal == null ? null : setmeal.getName());
        vo.setNoticeText(setmeal == null ? null : setmeal.getAttention());
        HealthOrderExt ext = extMapper.selectOne(Wrappers.<HealthOrderExt>lambdaQuery()
            .eq(HealthOrderExt::getOrderId, order.getId()));
        if (ext != null) {
            vo.setFlowStatus(ext.getFlowStatus());
            vo.setPostponeUntil(ext.getPostponeUntil());
        }
        return vo;
    }

}
