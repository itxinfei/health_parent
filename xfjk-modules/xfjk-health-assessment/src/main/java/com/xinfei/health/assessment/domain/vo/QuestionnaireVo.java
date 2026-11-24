package com.xinfei.health.assessment.domain.vo;

import com.xinfei.health.assessment.domain.Questionnaire;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 问卷详情 VO（附题目数/因子数/规则数统计，09 §6.1；C 端详情附题目列表，09 §6.2）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireVo extends Questionnaire {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 实际题目数 */
    private Integer questionNum;

    /** 因子数 */
    private Integer factorNum;

    /** 规则数 */
    private Integer ruleNum;

    /** 题目与选项（C 端问卷详情，不含算分内部字段） */
    private List<QuestionVo> questions;

}
