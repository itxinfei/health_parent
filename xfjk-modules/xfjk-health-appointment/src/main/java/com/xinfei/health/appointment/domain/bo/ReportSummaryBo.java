package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.Valid;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 总检提交入参（07 §3.2 PUT /health/reports/{id}/summary、§6.3）
 *
 * @author xinfei
 */
@Data
public class ReportSummaryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 报告级结论归类（18 F14） */
    private String conclusionType;

    @Valid
    private List<ReportConclusionBo> conclusions;

    private String basis;

    private String advice;

    /** 异常汇总（自动生成后医生可微调，18 D8） */
    private String abnormalSummary;

    /** 科室小结汇总（可改文，18 F6） */
    private String deptSummaryText;

    private String doctor;

}
