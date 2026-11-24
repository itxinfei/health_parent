package com.xinfei.health.assessment.domain.vo;

import com.xinfei.health.assessment.domain.AssessmentAnswer;
import com.xinfei.health.assessment.domain.AssessmentFactor;
import com.xinfei.health.assessment.domain.AssessmentRecord;
import com.xinfei.health.assessment.domain.Crowd;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 评估记录 VO（列表展示附会员/问卷名称，详情附作答/因子/人群，09 §6.1）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentRecordVo extends AssessmentRecord {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会员姓名（07 档案） */
    private String memberName;

    /** 问卷名称 */
    private String questionnaireName;

    /** 作答明细（详情） */
    private List<AssessmentAnswer> answers;

    /** 因子得分（详情） */
    private List<AssessmentFactor> factors;

    /** 命中人群（详情） */
    private List<Crowd> crowds;

}
