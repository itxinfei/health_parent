package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告列表/头部视图（07 §3.2 GET /health/reports）
 *
 * @author xinfei
 */
@Data
public class ReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String reportNo;

    private Integer memberId;

    private String memberName;

    private String memberSex;

    private String archiveNo;

    private Integer orderId;

    private String examNo;

    private Long batchId;

    private LocalDate checkDate;

    private String doctor;

    private String source;

    private String status;

    /** 异常项数（列表色标用，18 C5） */
    private Integer abnormalCount;

    private Integer criticalCount;

    private String conclusionType;

    private String conclusionFinal;

    private String fileUrl;

    private String revokeFlag;

    private String summaryBy;

    private LocalDateTime summaryTime;

    private String finalCheckBy;

    private LocalDateTime finalCheckTime;

    private Long sourceReportId;

    private String remark;

    private LocalDateTime createTime;

}
