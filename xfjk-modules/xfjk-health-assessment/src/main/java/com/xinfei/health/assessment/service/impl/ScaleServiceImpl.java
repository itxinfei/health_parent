package com.xinfei.health.assessment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleConfig;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import com.xinfei.health.assessment.mapper.ScaleConfigMapper;
import com.xinfei.health.assessment.mapper.ScaleFactorMapper;
import com.xinfei.health.assessment.mapper.ScaleRuleMapper;
import com.xinfei.health.assessment.service.IQuestionnaireService;
import com.xinfei.health.assessment.service.IScaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 量表因子/规则/阈值配置 服务层处理（09 §2.4~§2.6）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class ScaleServiceImpl implements IScaleService {

    private static final Set<String> LEVEL_CODES = Set.of(
        "yes", "basic", "no", "tendency", "normal", "sub_health", "mild", "moderate", "severe");

    private final ScaleFactorMapper factorMapper;
    private final ScaleRuleMapper ruleMapper;
    private final ScaleConfigMapper configMapper;
    private final IQuestionnaireService questionnaireService;

    @Override
    public List<ScaleFactor> listFactors(Long questionnaireId) {
        return factorMapper.selectList(Wrappers.<ScaleFactor>lambdaQuery()
            .eq(ScaleFactor::getQuestionnaireId, questionnaireId)
            .orderByAsc(ScaleFactor::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertFactor(ScaleFactor entity) {
        requireDraft(entity.getQuestionnaireId());
        factorMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFactor(ScaleFactor entity) {
        if (entity.getId() == null) {
            throw new ServiceException("因子主键不能为空");
        }
        ScaleFactor old = factorMapper.selectById(entity.getId());
        requireDraft(old == null ? null : old.getQuestionnaireId());
        return factorMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFactor(Long id) {
        ScaleFactor old = factorMapper.selectById(id);
        requireDraft(old == null ? null : old.getQuestionnaireId());
        return factorMapper.deleteById(id) > 0;
    }

    @Override
    public List<ScaleRule> listRules(Long questionnaireId) {
        return ruleMapper.selectList(Wrappers.<ScaleRule>lambdaQuery()
            .eq(ScaleRule::getQuestionnaireId, questionnaireId)
            .orderByAsc(ScaleRule::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertRule(ScaleRule entity) {
        requireDraft(entity.getQuestionnaireId());
        validateRule(entity);
        ruleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRule(ScaleRule entity) {
        if (entity.getId() == null) {
            throw new ServiceException("规则主键不能为空");
        }
        ScaleRule old = ruleMapper.selectById(entity.getId());
        requireDraft(old == null ? null : old.getQuestionnaireId());
        validateRule(entity);
        return ruleMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRule(Long id) {
        ScaleRule old = ruleMapper.selectById(id);
        requireDraft(old == null ? null : old.getQuestionnaireId());
        return ruleMapper.deleteById(id) > 0;
    }

    @Override
    public List<ScaleConfig> listConfigs(Long questionnaireId) {
        return configMapper.selectList(Wrappers.<ScaleConfig>lambdaQuery()
            .eq(ScaleConfig::getQuestionnaireId, questionnaireId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveConfigs(Long questionnaireId, List<ScaleConfig> configs) {
        requireDraft(questionnaireId);
        if (configs == null) {
            return false;
        }
        for (ScaleConfig config : configs) {
            if (config.getQuestionnaireId() == null) {
                config.setQuestionnaireId(questionnaireId);
            }
            if (StringUtils.isBlank(config.getConfigKey()) || StringUtils.isBlank(config.getConfigValue())) {
                throw new ServiceException("配置键与值不能为空", AssessmentErrorCode.SCALE_CONFIG_MISSING);
            }
            configMapper.delete(Wrappers.<ScaleConfig>lambdaQuery()
                .eq(ScaleConfig::getQuestionnaireId, questionnaireId)
                .eq(ScaleConfig::getConfigKey, config.getConfigKey()));
            configMapper.insert(config);
        }
        return true;
    }

    private void requireDraft(Long questionnaireId) {
        if (questionnaireId == null) {
            throw new ServiceException("问卷主键不能为空");
        }
        Questionnaire questionnaire = questionnaireService.requireDraft(questionnaireId);
        if (questionnaire == null) {
            throw new ServiceException("问卷不存在：" + questionnaireId, AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
    }

    /**
     * 规则区间校验（09 §2.5：左闭右开 min < max；level_code 属值域 → 7004）
     */
    private void validateRule(ScaleRule rule) {
        if (StringUtils.isBlank(rule.getRuleType())) {
            throw new ServiceException("规则类型不能为空", AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID);
        }
        if (rule.getMinValue() != null && rule.getMaxValue() != null
            && rule.getMinValue().compareTo(rule.getMaxValue()) >= 0) {
            throw new ServiceException("规则区间必须左闭右开（min < max）", AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID);
        }
        if (rule.getLevelCode() == null || !LEVEL_CODES.contains(rule.getLevelCode())) {
            throw new ServiceException("规则等级码非法：" + rule.getLevelCode(),
                AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID);
        }
    }

}
