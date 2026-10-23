package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单流水 t_health_order_log（08 §2.9，只插入不更新）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_order_log")
public class HealthOrderLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 雪花主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_order.id
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 动作：预约/到诊/取消/改期/延期/延期恢复/备单/打印/发放/换人
     */
    @TableField("action")
    private String action;

    /**
     * 迁移前流程状态
     */
    @TableField("from_status")
    private String fromStatus;

    /**
     * 迁移后流程状态
     */
    @TableField("to_status")
    private String toStatus;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operator;

    /**
     * 操作人角色
     */
    @TableField("operator_role")
    private String operatorRole;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

}
