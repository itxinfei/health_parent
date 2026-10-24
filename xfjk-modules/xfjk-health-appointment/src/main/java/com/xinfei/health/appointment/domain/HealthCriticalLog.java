package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xinfei.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 危急值登记 t_health_critical_log（07 §2.11，18 D4~D5）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_critical_log")
public class HealthCriticalLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_item_id")
    private Long reportItemId;

    @TableField("report_id")
    private Long reportId;

    @TableField("member_id")
    private Integer memberId;

    @TableField("archive_no")
    private String archiveNo;

    @TableField("item_name")
    private String itemName;

    @TableField("critical_value")
    private String criticalValue;

    @TableField("rule_code")
    private String ruleCode;

    @TableField("report_time")
    private LocalDateTime reportTime;

    @TableField("receiver")
    private String receiver;

    @TableField("receiver_confirm_time")
    private LocalDateTime receiverConfirmTime;

    @TableField("notify_way")
    private String notifyWay;

    @TableField("handle_result")
    private String handleResult;

    @TableField("track_by")
    private String trackBy;

    @TableField("track_time")
    private LocalDateTime trackTime;

    @TableField("track_remark")
    private String trackRemark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 危急值规则码闭集（12 §4.2 注，禁止另造） */
    public static final String RULE_WBC_LOW = "D4-WBC-LOW";
    public static final String RULE_WBC_HIGH = "D4-WBC-HIGH";
    public static final String RULE_HB_LOW = "D4-HB-LOW";
    public static final String RULE_HB_HIGH = "D4-HB-HIGH";
    public static final String RULE_PLT_LOW = "D4-PLT-LOW";
    public static final String RULE_PLT_HIGH = "D4-PLT-HIGH";

    public static final java.util.Set<String> RULE_CODES = java.util.Set.of(
        RULE_WBC_LOW, RULE_WBC_HIGH, RULE_HB_LOW, RULE_HB_HIGH, RULE_PLT_LOW, RULE_PLT_HIGH);

    /** 通知方式字典 health_notify_way 三值，默认第三种（18 D5） */
    public static final String NOTIFY_PHONE = "电话";
    public static final String NOTIFY_SYSTEM = "系统推送";
    public static final String NOTIFY_BOTH = "电话+系统推送";

    public static final java.util.Set<String> NOTIFY_WAYS = java.util.Set.of(NOTIFY_PHONE, NOTIFY_SYSTEM, NOTIFY_BOTH);

}
