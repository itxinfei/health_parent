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
 * 项目重置与数据重收留痕 t_health_item_reset_log（04 §5.2、08 §2.8、18 D11，只插入不更新）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_item_reset_log")
public class HealthItemResetLog implements Serializable {

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
     * 关联 t_checkitem.id
     */
    @TableField("checkitem_id")
    private Integer checkitemId;

    /**
     * 动作：重置/数据重收/复查登记/延期/延期恢复/弃检/弃检撤销（08 §2.8）
     */
    @TableField("action")
    private String action;

    /**
     * 原因（复查必须记，18 C7）
     */
    @TableField("reason")
    private String reason;

    /**
     * 变更前值（重置为「前状态:前结果」快照）
     */
    @TableField("before_value")
    private String beforeValue;

    /**
     * 变更后值
     */
    @TableField("after_value")
    private String afterValue;

    /**
     * 复查申请人（18 C7，本期留空）
     */
    @TableField("apply_by")
    private String applyBy;

    /**
     * 总检审核人（18 C7，本期留空）
     */
    @TableField("audit_by")
    private String auditBy;

    /**
     * 审核状态（18 C7，本期留空）
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 操作人（sys_user.id）
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

}
