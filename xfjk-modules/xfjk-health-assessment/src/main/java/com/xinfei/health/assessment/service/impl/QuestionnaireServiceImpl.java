package com.xinfei.health.assessment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.domain.AssessmentRecord;
import com.xinfei.health.assessment.domain.Question;
import com.xinfei.health.assessment.domain.QuestionOption;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleConfig;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import com.xinfei.health.assessment.domain.bo.QuestionnaireBo;
import com.xinfei.health.assessment.domain.vo.QuestionnaireVo;
import com.xinfei.health.assessment.engine.PublishValidator;
import com.xinfei.health.assessment.mapper.AssessmentRecordMapper;
import com.xinfei.health.assessment.mapper.QuestionMapper;
import com.xinfei.health.assessment.mapper.QuestionOptionMapper;
import com.xinfei.health.assessment.mapper.QuestionnaireMapper;
import com.xinfei.health.assessment.mapper.ScaleConfigMapper;
import com.xinfei.health.assessment.mapper.ScaleFactorMapper;
import com.xinfei.health.assessment.mapper.ScaleRuleMapper;
import com.xinfei.health.assessment.service.IQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷/量表定义 服务层处理（09 §2.1、§5.2）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class QuestionnaireServiceImpl implements IQuestionnaireService {

    private final QuestionnaireMapper baseMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final ScaleFactorMapper factorMapper;
    private final ScaleRuleMapper ruleMapper;
    private final ScaleConfigMapper configMapper;
    private final AssessmentRecordMapper recordMapper;

    @Override
    public TableDataInfo<QuestionnaireVo> queryPageList(QuestionnaireBo bo, PageQuery pageQuery) {
        Page<QuestionnaireVo> page = baseMapper.selectVoPage(pageQuery.build(), Wrappers.<Questionnaire>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getName()), Questionnaire::getName, bo.getName())
            .like(StringUtils.isNotBlank(bo.getCode()), Questionnaire::getCode, bo.getCode())
            .eq(StringUtils.isNotBlank(bo.getCategory()), Questionnaire::getCategory, bo.getCategory())
            .eq(StringUtils.isNotBlank(bo.getScaleType()), Questionnaire::getScaleType, bo.getScaleType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), Questionnaire::getStatus, bo.getStatus())
            .orderByDesc(Questionnaire::getCreateTime));
        page.getRecords().forEach(vo -> vo.setQuestionNum(Math.toIntExact(
            questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getQuestionnaireId, vo.getId())))));
        return TableDataInfo.build(page);
    }

    @Override
    public QuestionnaireVo queryById(Long id) {
        QuestionnaireVo vo = baseMapper.selectVoById(id);
        if (vo == null) {
            throw new ServiceException("问卷不存在：" + id, AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        vo.setQuestionNum(Math.toIntExact(questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
            .eq(Question::getQuestionnaireId, id))));
        vo.setFactorNum(Math.toIntExact(factorMapper.selectCount(Wrappers.<ScaleFactor>lambdaQuery()
            .eq(ScaleFactor::getQuestionnaireId, id))));
        vo.setRuleNum(Math.toIntExact(ruleMapper.selectCount(Wrappers.<ScaleRule>lambdaQuery()
            .eq(ScaleRule::getQuestionnaireId, id))));
        return vo;
    }

    @Override
    public Long insert(Questionnaire entity) {
        checkCodeUnique(entity.getCode(), null);
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus(Questionnaire.STATUS_DRAFT);
        }
        if (entity.getVersion() == null) {
            entity.setVersion(1);
        }
        if (entity.getIsPublic() == null) {
            entity.setIsPublic(0);
        }
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean update(Questionnaire entity) {
        if (entity.getId() == null) {
            throw new ServiceException("问卷主键不能为空");
        }
        checkCodeUnique(entity.getCode(), entity.getId());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        if (recordMapper.exists(Wrappers.<AssessmentRecord>lambdaQuery()
            .eq(AssessmentRecord::getQuestionnaireId, id))) {
            throw new ServiceException("该问卷已存在评估记录，不能删除", AssessmentErrorCode.RECORD_NOT_FOUND_OR_DENIED);
        }
        // 先取该问卷下题目 id，再按 id 删选项：不用 inSql 拼子查询字符串（DENY-004）
        List<Long> questionIds = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                .select(Question::getId)
                .eq(Question::getQuestionnaireId, id))
            .stream().map(Question::getId).toList();
        if (!questionIds.isEmpty()) {
            optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery()
                .in(QuestionOption::getQuestionId, questionIds));
        }
        questionMapper.delete(Wrappers.<Question>lambdaQuery().eq(Question::getQuestionnaireId, id));
        factorMapper.delete(Wrappers.<ScaleFactor>lambdaQuery().eq(ScaleFactor::getQuestionnaireId, id));
        ruleMapper.delete(Wrappers.<ScaleRule>lambdaQuery().eq(ScaleRule::getQuestionnaireId, id));
        configMapper.delete(Wrappers.<ScaleConfig>lambdaQuery().eq(ScaleConfig::getQuestionnaireId, id));
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyVersion(Long id) {
        Questionnaire source = baseMapper.selectById(id);
        if (source == null) {
            throw new ServiceException("问卷不存在：" + id, AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        // 新版本草稿（09 §5.2：复制生成 version+1 草稿）
        Questionnaire copy = new Questionnaire();
        copy.setCode(source.getCode());
        copy.setName(source.getName());
        copy.setCategory(source.getCategory());
        copy.setScaleType(source.getScaleType());
        copy.setVersion(source.getVersion() + 1);
        copy.setIntro(source.getIntro());
        copy.setInstruction(source.getInstruction());
        copy.setQuestionCount(source.getQuestionCount());
        copy.setScoringType(source.getScoringType());
        copy.setAnswerMinScore(source.getAnswerMinScore());
        copy.setAnswerMaxScore(source.getAnswerMaxScore());
        copy.setStatus(Questionnaire.STATUS_DRAFT);
        copy.setIsPublic(source.getIsPublic());
        copy.setSort(source.getSort());
        copy.setRemark(source.getRemark());
        baseMapper.insert(copy);

        // 题目 + 选项
        for (Question question : questionMapper.selectList(Wrappers.<Question>lambdaQuery()
            .eq(Question::getQuestionnaireId, id).orderByAsc(Question::getSeq))) {
            Long oldQuestionId = question.getId();
            question.setId(null);
            question.setQuestionnaireId(copy.getId());
            questionMapper.insert(question);
            for (QuestionOption option : optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
                .eq(QuestionOption::getQuestionId, oldQuestionId).orderByAsc(QuestionOption::getSeq))) {
                option.setId(null);
                option.setQuestionId(question.getId());
                optionMapper.insert(option);
            }
        }
        // 因子 / 规则 / 配置
        for (ScaleFactor factor : factorMapper.selectList(Wrappers.<ScaleFactor>lambdaQuery()
            .eq(ScaleFactor::getQuestionnaireId, id))) {
            factor.setId(null);
            factor.setQuestionnaireId(copy.getId());
            factorMapper.insert(factor);
        }
        for (ScaleRule rule : ruleMapper.selectList(Wrappers.<ScaleRule>lambdaQuery()
            .eq(ScaleRule::getQuestionnaireId, id))) {
            rule.setId(null);
            rule.setQuestionnaireId(copy.getId());
            ruleMapper.insert(rule);
        }
        for (ScaleConfig config : configMapper.selectList(Wrappers.<ScaleConfig>lambdaQuery()
            .eq(ScaleConfig::getQuestionnaireId, id))) {
            config.setId(null);
            config.setQuestionnaireId(copy.getId());
            configMapper.insert(config);
        }
        return copy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublishValidator.PublishResult publish(Long id) {
        Questionnaire questionnaire = baseMapper.selectById(id);
        if (questionnaire == null) {
            throw new ServiceException("问卷不存在：" + id, AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        List<Question> questions = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
            .eq(Question::getQuestionnaireId, id));
        List<ScaleFactor> factors = factorMapper.selectList(Wrappers.<ScaleFactor>lambdaQuery()
            .eq(ScaleFactor::getQuestionnaireId, id));
        List<ScaleRule> rules = ruleMapper.selectList(Wrappers.<ScaleRule>lambdaQuery()
            .eq(ScaleRule::getQuestionnaireId, id));
        List<ScaleConfig> configs = configMapper.selectList(Wrappers.<ScaleConfig>lambdaQuery()
            .eq(ScaleConfig::getQuestionnaireId, id));

        PublishValidator.PublishResult result =
            PublishValidator.validate(questionnaire, questions, factors, rules, configs);
        if (result.isSuccess()) {
            questionnaire.setStatus(Questionnaire.STATUS_PUBLISHED);
            if (questionnaire.getPublishTime() == null) {
                questionnaire.setPublishTime(LocalDateTime.now());
            }
            baseMapper.updateById(questionnaire);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disable(Long id) {
        Questionnaire questionnaire = baseMapper.selectById(id);
        if (questionnaire == null) {
            throw new ServiceException("问卷不存在：" + id, AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        questionnaire.setStatus(Questionnaire.STATUS_DISABLED);
        return baseMapper.updateById(questionnaire) > 0;
    }

    @Override
    public Questionnaire requireDraft(Long id) {
        Questionnaire questionnaire = baseMapper.selectById(id);
        if (questionnaire == null) {
            throw new ServiceException("问卷不存在：" + id, AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        if (Questionnaire.STATUS_PUBLISHED.equals(questionnaire.getStatus())) {
            throw new ServiceException("已发布问卷禁止原地修改，须复制新版本", AssessmentErrorCode.PUBLISHED_LOCKED);
        }
        return questionnaire;
    }

    private void checkCodeUnique(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            throw new ServiceException("问卷编码不能为空");
        }
        boolean exists = baseMapper.exists(Wrappers.<Questionnaire>lambdaQuery()
            .eq(Questionnaire::getCode, code)
            .ne(excludeId != null, Questionnaire::getId, excludeId));
        if (exists) {
            throw new ServiceException("问卷编码已存在：" + code, AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID);
        }
    }

}
