package com.xinfei.health.assessment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.bo.QuestionnaireBo;
import com.xinfei.health.assessment.domain.vo.QuestionnaireVo;
import com.xinfei.health.assessment.engine.PublishValidator.PublishResult;

/**
 * 问卷/量表定义 服务层（09 §2.1、§5.2）
 *
 * @author xinfei
 */
public interface IQuestionnaireService {

    /**
     * 分页查询（name/code/category/scaleType/status，09 §6.1）
     */
    TableDataInfo<QuestionnaireVo> queryPageList(QuestionnaireBo bo, PageQuery pageQuery);

    /**
     * 详情（含题目数/因子数/规则数统计）
     */
    QuestionnaireVo queryById(Long id);

    /**
     * 新增（草稿，version=1，code 全局唯一 7004）
     */
    Long insert(Questionnaire entity);

    /**
     * 编辑元信息（不含题目；已发布仅元信息可改）
     */
    Boolean update(Questionnaire entity);

    /**
     * 删除：存在评估记录则禁止（7008）
     */
    Boolean deleteById(Long id);

    /**
     * 复制为新版本草稿（version+1，含题目/选项/因子/规则/配置，09 §5.2）
     */
    Long copyVersion(Long id);

    /**
     * 发布：六项校验通过才发布，否则 7004/7005/7006 + 逐项错误清单（09 §5.2）
     */
    PublishResult publish(Long id);

    /**
     * 停用
     */
    Boolean disable(Long id);

    /**
     * 校验问卷存在且为草稿（编辑题目/因子/规则的前置，已发布 7007）
     */
    Questionnaire requireDraft(Long id);

}
