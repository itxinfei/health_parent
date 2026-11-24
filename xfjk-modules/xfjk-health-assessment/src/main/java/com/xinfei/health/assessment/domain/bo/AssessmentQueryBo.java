package com.xinfei.health.assessment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 评估记录查询条件（09 §6.1；统计区间入参 beginDate/endDate 由服务端解析为闭区间）
 *
 * @author xinfei
 */
@Data
public class AssessmentQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private Long questionnaireId;

    private String level;

    private String severity;

    private String status;

    private String source;

    private LocalDate beginDate;

    private LocalDate endDate;

}
