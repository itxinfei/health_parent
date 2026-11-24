package com.xinfei.health.assessment.domain.vo;

import com.xinfei.health.assessment.domain.Question;
import com.xinfei.health.assessment.domain.QuestionOption;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目 VO（含选项，题目 CRUD 一次提交，09 §6.1）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionVo extends Question {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 选项列表 */
    private List<QuestionOption> options = new ArrayList<>();

}
