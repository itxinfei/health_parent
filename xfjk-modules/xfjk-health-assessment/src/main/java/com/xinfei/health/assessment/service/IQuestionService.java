package com.xinfei.health.assessment.service;

import com.xinfei.health.assessment.domain.vo.QuestionImportVo;
import com.xinfei.health.assessment.domain.vo.QuestionVo;

import java.util.List;

/**
 * 题目 服务层（09 §2.2、§2.3；含选项一次提交）
 *
 * @author xinfei
 */
public interface IQuestionService {

    /**
     * 查询问卷下全部题目（含选项，按 seq 排序）
     */
    List<QuestionVo> queryByQuestionnaire(Long questionnaireId);

    /**
     * 题目详情（含选项）
     */
    QuestionVo queryById(Long id);

    /**
     * 新增题目（含选项一次提交；选项分值越界 7011；问卷已发布 7007）
     */
    Long insert(QuestionVo vo);

    /**
     * 编辑题目（选项差异更新；已发布 7007）
     */
    Boolean update(QuestionVo vo);

    /**
     * 删除题目（级联删选项；已发布 7007）
     */
    Boolean deleteById(Long id);

    /**
     * 题库导入（JSON 行数组，逐行标错，形态同 20 §6 行级回执）
     */
    QuestionImportVo importQuestions(Long questionnaireId, List<QuestionVo> rows);

}
