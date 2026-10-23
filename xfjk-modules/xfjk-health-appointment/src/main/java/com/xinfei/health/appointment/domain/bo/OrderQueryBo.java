package com.xinfei.health.appointment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 订单分页查询入参（08 §4.5）
 *
 * @author xinfei
 */
@Data
public class OrderQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预约日期起
     */
    private LocalDate orderDateBegin;

    /**
     * 预约日期止
     */
    private LocalDate orderDateEnd;

    /**
     * 预约状态：未到诊 / 已到诊（原语义，08 §12）
     */
    private String orderStatus;

    /**
     * 流程状态（t_health_order_ext.flow_status，08 §3.2）
     */
    private String flowStatus;

    /**
     * 会员 id
     */
    private Integer memberId;

    /**
     * 套餐 id
     */
    private Integer setmealId;

    /**
     * 团检批次 id
     */
    private Long batchId;

}
