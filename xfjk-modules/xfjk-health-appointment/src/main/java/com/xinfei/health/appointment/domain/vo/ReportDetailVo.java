package com.xinfei.health.appointment.domain.vo;

import com.xinfei.health.appointment.domain.HealthCriticalLog;
import com.xinfei.health.appointment.domain.HealthDeptSummary;
import com.xinfei.health.appointment.domain.HealthReportArchive;
import com.xinfei.health.appointment.domain.HealthReportConclusion;
import com.xinfei.health.appointment.domain.HealthReportItem;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告详情（07 §3.2 GET /health/reports/{id}：五段 + 结论条目 + 明细 + 小结 + 危急值）
 *
 * @author xinfei
 */
@Data
public class ReportDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ReportVo report;

    /** 五段正文 */
    private String deptSummaryText;

    private String abnormalSummary;

    private String conclusion;

    private String basis;

    private String advice;

    private String summary;

    private String conclusionJson;

    /** 质控链（18 C8） */
    private String firstCheckBy;

    private LocalDateTime firstCheckTime;

    private String summaryBy;

    private LocalDateTime summaryTime;

    private String revokeReason;

    private LocalDateTime revokeTime;

    private List<HealthReportItem> items;

    private List<HealthDeptSummary> deptSummaries;

    private List<HealthReportConclusion> conclusions;

    private List<HealthCriticalLog> criticals;

    private List<HealthReportArchive> archives;

    /** 上一次报告（18 D11） */
    private ReportVo prevReport;

}
