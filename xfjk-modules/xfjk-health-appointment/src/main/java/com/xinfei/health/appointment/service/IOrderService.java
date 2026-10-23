package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.bo.OrderCancelBo;
import com.xinfei.health.appointment.domain.bo.OrderPostponeBo;
import com.xinfei.health.appointment.domain.bo.OrderQueryBo;
import com.xinfei.health.appointment.domain.bo.OrderRescheduleBo;
import com.xinfei.health.appointment.domain.vo.OrderVo;

import java.time.LocalDate;

/**
 * 预约订单与流转 服务层（08 §4.5、§5.3~§5.6）
 *
 * @author xinfei
 */
public interface IOrderService {

    /**
     * 分页查询订单（日期区间/orderStatus/flowStatus/会员/套餐/批次）
     */
    TableDataInfo<OrderVo> queryPageList(OrderQueryBo bo, PageQuery pageQuery);

    /**
     * 订单详情（含 ext 与流水）
     */
    OrderVo queryById(Integer id);

    /**
     * 到诊登记：生成 exam_no、flow_status=已到诊、orderStatus=已到诊、写流水（08 §5.5）
     *
     * @return 生成的体检号
     */
    String register(Integer orderId, String remark);

    /**
     * 取消：必填 reason；回退当日名额（条件更新）；仅 已预约 可取消（08 §5.6）
     */
    Boolean cancel(Integer orderId, OrderCancelBo bo);

    /**
     * 改期：目标日 +1 成功 → 原日 -1，原子双更新（08 §5.4）
     */
    Boolean reschedule(Integer orderId, OrderRescheduleBo bo);

    /**
     * 延期：已到诊/检查中 → 延期，postpone_count +1（08 §5.7）
     */
    Boolean postpone(Integer orderId, OrderPostponeBo bo);

    /**
     * 延期恢复：延期 → 已到诊，写流水（08 §5.7）
     */
    Boolean postponeRestore(Integer orderId);

    /**
     * 状态迁移：仅允许 §3.2 合法边，非法迁移返回 5006（08 §4.5）
     */
    Boolean transitFlow(Integer orderId, String toStatus, String remark);

}
