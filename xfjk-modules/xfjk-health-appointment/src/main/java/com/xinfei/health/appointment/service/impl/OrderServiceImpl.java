package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.satoken.utils.LoginHelper;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.HealthOrderExt;
import com.xinfei.health.appointment.domain.HealthOrderLog;
import com.xinfei.health.appointment.domain.Order;
import com.xinfei.health.appointment.domain.bo.OrderCancelBo;
import com.xinfei.health.appointment.domain.bo.OrderPostponeBo;
import com.xinfei.health.appointment.domain.bo.OrderQueryBo;
import com.xinfei.health.appointment.domain.bo.OrderRescheduleBo;
import com.xinfei.health.appointment.domain.vo.OrderVo;
import com.xinfei.health.appointment.mapper.HealthOrderExtMapper;
import com.xinfei.health.appointment.mapper.HealthOrderLogMapper;
import com.xinfei.health.appointment.mapper.OrderMapper;
import com.xinfei.health.appointment.service.IOrderService;
import com.xinfei.health.appointment.service.IOrdersettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.xinfei.health.appointment.domain.HealthOrderExt.*;
import static com.xinfei.health.appointment.service.impl.OrderFlowStateMachine.*;

/**
 * 预约订单与流转 服务层处理（08 §4.5、§5.3~§5.7）
 *
 * <p>名额增减一律走条件更新（08 §12 禁止先查后改）。
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private static final DateTimeFormatter EXAM_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderMapper orderMapper;
    private final HealthOrderExtMapper extMapper;
    private final HealthOrderLogMapper logMapper;
    private final IOrdersettingService ordersettingService;

    @Override
    public TableDataInfo<OrderVo> queryPageList(OrderQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Order> lqw = Wrappers.<Order>lambdaQuery()
            .ge(ObjectUtil.isNotNull(bo.getOrderDateBegin()), Order::getOrderDate, bo.getOrderDateBegin())
            .le(ObjectUtil.isNotNull(bo.getOrderDateEnd()), Order::getOrderDate, bo.getOrderDateEnd())
            .eq(ObjectUtil.isNotNull(bo.getMemberId()), Order::getMemberId, bo.getMemberId())
            .eq(ObjectUtil.isNotNull(bo.getSetmealId()), Order::getSetmealId, bo.getSetmealId())
            .eq(ObjectUtil.isNotNull(bo.getOrderStatus()), Order::getOrderStatus, bo.getOrderStatus());
        // 流程状态/批次在 ext 表，先解出 order_id 集合（08 §6.4 筛选口径）
        if (ObjectUtil.isNotNull(bo.getFlowStatus()) || ObjectUtil.isNotNull(bo.getBatchId())) {
            List<Integer> ids = extMapper.selectList(Wrappers.<HealthOrderExt>lambdaQuery()
                    .eq(ObjectUtil.isNotNull(bo.getFlowStatus()), HealthOrderExt::getFlowStatus, bo.getFlowStatus())
                    .eq(ObjectUtil.isNotNull(bo.getBatchId()), HealthOrderExt::getBatchId, bo.getBatchId())
                    .select(HealthOrderExt::getOrderId))
                .stream().map(HealthOrderExt::getOrderId).toList();
            if (ids.isEmpty()) {
                return TableDataInfo.build(new Page<>());
            }
            lqw.in(Order::getId, ids);
        }
        Page<Order> page = orderMapper.selectPage(pageQuery.build(), lqw.orderByDesc(Order::getOrderDate));
        Page<OrderVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return TableDataInfo.build(voPage);
    }

    @Override
    public OrderVo queryById(Integer id) {
        OrderVo vo = toVo(orderMapper.selectById(id));
        if (vo != null) {
            vo.setLogs(logMapper.selectList(Wrappers.<HealthOrderLog>lambdaQuery()
                .eq(HealthOrderLog::getOrderId, id)
                .orderByDesc(HealthOrderLog::getCreateTime)));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(Integer orderId, String remark) {
        HealthOrderExt ext = requireExt(orderId);
        Order order = requireOrder(orderId);
        if (!canRegister(ext.getFlowStatus())) {
            throw new ServiceException("当前状态不允许到诊登记：" + ext.getFlowStatus(),
                HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        if (order.getOrderDate().isAfter(LocalDate.now())) {
            throw new ServiceException("预约日期未到，须先改期后登记", HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        String examNo = nextExamNo();
        ext.setFlowStatus(FLOW_REGISTERED);
        ext.setExamNo(examNo);
        ext.setRegisterBy(LoginHelper.getUsername());
        ext.setRegisterTime(LocalDateTime.now());
        // 报告草稿由 07 健康评估模块建（08 §5.5），本切片仅落 ext.report_id 预留
        extMapper.updateById(ext);
        order.setOrderStatus(ORDER_STATUS_ARRIVED);
        orderMapper.updateById(order);
        // 名额不回退（08 §4.5 register）
        writeLog(orderId, "到诊", FLOW_BOOKED, FLOW_REGISTERED, remark);
        return examNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Integer orderId, OrderCancelBo bo) {
        HealthOrderExt ext = requireExt(orderId);
        Order order = requireOrder(orderId);
        if (!canCancel(ext.getFlowStatus())) {
            throw new ServiceException("已到诊及之后的状态取消须先撤销登记", HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        ext.setFlowStatus(FLOW_CANCELLED);
        ext.setCancelReason(bo.getReason());
        ext.setCancelBy(LoginHelper.getUsername());
        ext.setCancelTime(LocalDateTime.now());
        extMapper.updateById(ext);
        order.setOrderStatus(ORDER_STATUS_UNARRIVED);
        orderMapper.updateById(order);
        // 名额回退：条件更新，下限 0（08 §5.6）
        ordersettingService.release(order.getOrderDate());
        writeLog(orderId, "取消", FLOW_BOOKED, FLOW_CANCELLED, bo.getReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reschedule(Integer orderId, OrderRescheduleBo bo) {
        Order order = requireOrder(orderId);
        requireExt(orderId);
        if (bo.getTargetDate().equals(order.getOrderDate())) {
            return true;
        }
        // 原子双更新：先目标日 +1（成功才有余量），成功后再原日 -1（08 §5.4）
        if (!ordersettingService.occupy(bo.getTargetDate())) {
            throw new ServiceException("目标日期未设置或名额已满", HealthErrorCode.ORDER_QUOTA_FULL);
        }
        ordersettingService.release(order.getOrderDate());
        order.setOrderDate(bo.getTargetDate());
        orderMapper.updateById(order);
        writeLog(orderId, "改期", null, null, "改期至 " + bo.getTargetDate());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postpone(Integer orderId, OrderPostponeBo bo) {
        HealthOrderExt ext = requireExt(orderId);
        if (!canPostpone(ext.getFlowStatus())) {
            throw new ServiceException("当前状态不允许延期：" + ext.getFlowStatus(),
                HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        if (bo.getUntil().isBefore(LocalDate.now())) {
            throw new ServiceException("延期日期不能早于今天", HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        ext.setFlowStatus(FLOW_POSTPONED);
        ext.setPostponeUntil(bo.getUntil());
        ext.setPostponeCount(ObjectUtil.defaultIfNull(ext.getPostponeCount(), 0) + 1);
        extMapper.updateById(ext);
        writeLog(orderId, "延期", ext.getFlowStatus(), FLOW_POSTPONED, "延期至 " + bo.getUntil());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postponeRestore(Integer orderId) {
        HealthOrderExt ext = requireExt(orderId);
        if (!FLOW_POSTPONED.equals(ext.getFlowStatus())) {
            throw new ServiceException("当前状态不是延期，无法恢复", HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        ext.setFlowStatus(FLOW_REGISTERED);
        extMapper.updateById(ext);
        writeLog(orderId, "延期恢复", FLOW_POSTPONED, FLOW_REGISTERED, null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transitFlow(Integer orderId, String toStatus, String remark) {
        HealthOrderExt ext = requireExt(orderId);
        String from = ext.getFlowStatus();
        if (!canTransit(from, toStatus)) {
            throw new ServiceException("非法状态迁移：" + from + " -> " + toStatus,
                HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        ext.setFlowStatus(toStatus);
        extMapper.updateById(ext);
        // 到诊登记同步 t_order.orderStatus（08 §5.5 其余边不动 orderStatus）
        if (FLOW_REGISTERED.equals(toStatus)) {
            Order order = requireOrder(orderId);
            order.setOrderStatus(ORDER_STATUS_ARRIVED);
            orderMapper.updateById(order);
        }
        writeLog(orderId, "流转", from, toStatus, remark);
        return true;
    }

    /**
     * 生成体检号：EX + yyyyMMdd + 4 位序号（08 §2.6）
     */
    private String nextExamNo() {
        String prefix = "EX" + LocalDate.now().format(EXAM_NO_DATE);
        Long count = extMapper.selectCount(Wrappers.<HealthOrderExt>lambdaQuery()
            .likeRight(HealthOrderExt::getExamNo, prefix));
        return prefix + String.format("%04d", count + 1);
    }

    private Order requireOrder(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("预约订单不存在：" + orderId, HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        return order;
    }

    private HealthOrderExt requireExt(Integer orderId) {
        HealthOrderExt ext = extMapper.selectOne(Wrappers.<HealthOrderExt>lambdaQuery()
            .eq(HealthOrderExt::getOrderId, orderId));
        if (ext == null) {
            throw new ServiceException("订单缺少流转扩展记录：" + orderId, HealthErrorCode.ORDER_FLOW_ILLEGAL);
        }
        return ext;
    }

    /**
     * 写流水（只插入不更新，08 §2.9）
     */
    private void writeLog(Integer orderId, String action, String from, String to, String remark) {
        HealthOrderLog log = new HealthOrderLog();
        log.setOrderId(orderId);
        log.setAction(action);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperator(LoginHelper.getUsername());
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    private OrderVo toVo(Order order) {
        OrderVo vo = new OrderVo();
        vo.setId(order.getId());
        vo.setMemberId(order.getMemberId());
        vo.setOrderDate(order.getOrderDate());
        vo.setOrderType(order.getOrderType());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setSetmealId(order.getSetmealId());
        HealthOrderExt ext = extMapper.selectOne(Wrappers.<HealthOrderExt>lambdaQuery()
            .eq(HealthOrderExt::getOrderId, order.getId()));
        vo.setExt(ext);
        return vo;
    }

}
