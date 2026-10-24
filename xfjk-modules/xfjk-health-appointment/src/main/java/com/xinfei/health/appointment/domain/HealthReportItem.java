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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报告明细 t_health_report_item（07 §2.7）
 *
 * <p>判定列（abnormal_flag / ref_configured / critical_flag / abnormal_grade / edge_flag）
 * 只由服务端判定引擎产出，禁止前端写入（07 §7）。
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_report_item")
public class HealthReportItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    @TableField("checkitem_id")
    private Integer checkitemId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("item_name")
    private String itemName;

    /** 0 数值 / 1 定性 / 2 文本 / 3 影像（18 D1） */
    @TableField("result_type")
    private String resultType;

    @TableField("result")
    private String result;

    @TableField("unit")
    private String unit;

    @TableField("ref_range")
    private String refRange;

    @TableField("ref_min")
    private BigDecimal refMin;

    @TableField("ref_max")
    private BigDecimal refMax;

    @TableField("ref_configured")
    private String refConfigured;

    @TableField("abnormal_flag")
    private String abnormalFlag;

    @TableField("qualitative")
    private String qualitative;

    @TableField("edge_flag")
    private String edgeFlag;

    @TableField("abnormal_grade")
    private String abnormalGrade;

    @TableField("critical_flag")
    private String criticalFlag;

    @TableField("major_positive")
    private String majorPositive;

    @TableField("item_status")
    private String itemStatus;

    @TableField("discard_reason")
    private String discardReason;

    @TableField("merge_group_id")
    private String mergeGroupId;

    @TableField("prev_result")
    private String prevResult;

    @TableField("prev_abnormal_flag")
    private String prevAbnormalFlag;

    @TableField("prev_ref_range")
    private String prevRefRange;

    @TableField("dual_dept_flag")
    private String dualDeptFlag;

    @TableField("peer_dept_status")
    private String peerDeptStatus;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("doctor")
    private String doctor;

    @TableField("check_time")
    private LocalDateTime checkTime;

    @TableField("check_device")
    private String checkDevice;

    @TableField("remark")
    private String remark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 结果类型（18 D1） */
    public static final String TYPE_NUMERIC = "0";
    public static final String TYPE_QUALITATIVE = "1";
    public static final String TYPE_TEXT = "2";
    public static final String TYPE_IMAGE = "3";

    /** 判定值域（18 D1，展开 07 §2.7） */
    public static final String FLAG_NORMAL = "0";
    public static final String FLAG_HIGH = "1";
    public static final String FLAG_LOW = "2";
    public static final String FLAG_ABNORMAL = "3";
    public static final String FLAG_POSITIVE = "4";
    public static final String FLAG_NEGATIVE = "5";
    public static final String FLAG_WEAK_POSITIVE = "6";

    /** 明细状态（18 C1） */
    public static final String ITEM_NOT_CHECKED = "未检";
    public static final String ITEM_CHECKED = "已检";
    public static final String ITEM_PENDING = "待查";
    public static final String ITEM_DISCARDED = "弃检";
    public static final String ITEM_DELAYED = "延期";

    public static final String YES = "1";
    public static final String NO = "0";

}
