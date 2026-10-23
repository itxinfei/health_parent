package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单流转扩展 t_health_order_ext（1:1 t_order，08 §2.6）
 *
 * <p>流程状态九值见 08 §3.2；t_order.orderStatus 保持原语义不动。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_order_ext")
public class HealthOrderExt implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 雪花主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_order.id（唯一索引）
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 流程状态：已预约/已到诊/检查中/待总检/已总检/报告已打印/报告已发放/已取消/延期
     */
    @TableField("flow_status")
    private String flowStatus;

    /**
     * 体检号：EX + yyyyMMdd + 4 位序号，到诊登记时生成
     */
    @TableField("exam_no")
    private String examNo;

    /**
     * 团检批次 id（20 §5.2）
     */
    @TableField("batch_id")
    private Long batchId;

    /**
     * 团检名单行 id
     */
    @TableField("batch_member_id")
    private Long batchMemberId;

    /**
     * 取消原因（取消时必填）
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 取消操作人
     */
    @TableField("cancel_by")
    private String cancelBy;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 延期次数，默认 0
     */
    @TableField("postpone_count")
    private Integer postponeCount;

    /**
     * 延期至日期
     */
    @TableField("postpone_until")
    private LocalDate postponeUntil;

    /**
     * 报告打印时间
     */
    @TableField("print_time")
    private LocalDateTime printTime;

    /**
     * 报告发放时间
     */
    @TableField("issue_time")
    private LocalDateTime issueTime;

    /**
     * 到诊登记人
     */
    @TableField("register_by")
    private String registerBy;

    /**
     * 到诊登记时间
     */
    @TableField("register_time")
    private LocalDateTime registerTime;

    /**
     * 关联 t_health_report.id（07，报告草稿）
     */
    @TableField("report_id")
    private Long reportId;

    /**
     * 流程状态常量（08 §3.2）
     */
    public static final String FLOW_BOOKED = "已预约";
    public static final String FLOW_REGISTERED = "已到诊";
    public static final String FLOW_IN_EXAM = "检查中";
    public static final String FLOW_PENDING_FINAL = "待总检";
    public static final String FLOW_FINALIZED = "已总检";
    public static final String FLOW_PRINTED = "报告已打印";
    public static final String FLOW_ISSUED = "报告已发放";
    public static final String FLOW_CANCELLED = "已取消";
    public static final String FLOW_POSTPONED = "延期";

    /**
     * 订单原语义状态（禁止自造，08 §12）
     */
    public static final String ORDER_STATUS_UNARRIVED = "未到诊";
    public static final String ORDER_STATUS_ARRIVED = "已到诊";

}
