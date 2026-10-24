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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 体检报告 t_health_report（07 §2.6）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_report")
public class HealthReport extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_no")
    private String reportNo;

    @TableField("member_id")
    private Integer memberId;

    @TableField("archive_no")
    private String archiveNo;

    @TableField("order_id")
    private Integer orderId;

    @TableField("exam_no")
    private String examNo;

    @TableField("batch_id")
    private Long batchId;

    @TableField("check_date")
    private LocalDate checkDate;

    @TableField("dept_summary_text")
    private String deptSummaryText;

    @TableField("abnormal_summary")
    private String abnormalSummary;

    @TableField("conclusion")
    private String conclusion;

    @TableField("basis")
    private String basis;

    @TableField("advice")
    private String advice;

    @TableField("conclusion_type")
    private String conclusionType;

    @TableField("conclusion_final")
    private String conclusionFinal;

    @TableField("conclusion_json")
    private String conclusionJson;

    @TableField("summary")
    private String summary;

    @TableField("doctor")
    private String doctor;

    @TableField("file_url")
    private String fileUrl;

    @TableField("source")
    private String source;

    @TableField("status")
    private String status;

    @TableField("first_check_by")
    private String firstCheckBy;

    @TableField("first_check_time")
    private LocalDateTime firstCheckTime;

    @TableField("summary_by")
    private String summaryBy;

    @TableField("summary_time")
    private LocalDateTime summaryTime;

    @TableField("final_check_by")
    private String finalCheckBy;

    @TableField("final_check_time")
    private LocalDateTime finalCheckTime;

    @TableField("revoke_flag")
    private String revokeFlag;

    @TableField("revoke_reason")
    private String revokeReason;

    @TableField("revoke_by")
    private String revokeBy;

    @TableField("revoke_time")
    private LocalDateTime revokeTime;

    @TableField("sign_file_id")
    private String signFileId;

    @TableField("watermark_flag")
    private String watermarkFlag;

    @TableField("source_report_id")
    private Long sourceReportId;

    @TableField("remark")
    private String remark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 报告状态（07 §2.6，字典 health_report_status） */
    public static final String STATUS_WAIT_CREATE = "待创建";
    public static final String STATUS_IN_EXAM = "检查中";
    public static final String STATUS_PENDING_FINAL = "待总检";
    public static final String STATUS_FINALIZED = "已总检";
    public static final String STATUS_PRINTED = "已打印";
    public static final String STATUS_ISSUED = "已发放";

    /** 报告来源（字典 health_report_source） */
    public static final String SOURCE_UPLOAD = "上传";
    public static final String SOURCE_INTERFACE = "接口";
    public static final String SOURCE_PARSE = "解析";
    public static final String SOURCE_BATCH = "团检";
    public static final String SOURCE_MANUAL = "手工录入";

    public static final Set<String> SOURCES = Set.of(SOURCE_UPLOAD, SOURCE_INTERFACE, SOURCE_PARSE, SOURCE_BATCH, SOURCE_MANUAL);

    /** 允许科室改结果的报告状态（已总检后须先撤销，18 C9） */
    public static final Set<String> EDITABLE_STATUS = Set.of(STATUS_WAIT_CREATE, STATUS_IN_EXAM, STATUS_PENDING_FINAL);

}
