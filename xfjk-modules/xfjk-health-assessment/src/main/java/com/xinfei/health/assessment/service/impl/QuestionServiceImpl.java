package com.xinfei.health.assessment.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.domain.Question;
import com.xinfei.health.assessment.domain.QuestionOption;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.vo.QuestionImportVo;
import com.xinfei.health.assessment.domain.vo.QuestionVo;
import com.xinfei.health.assessment.mapper.QuestionMapper;
import com.xinfei.health.assessment.mapper.QuestionOptionMapper;
import com.xinfei.health.assessment.service.IQuestionService;
import com.xinfei.health.assessment.service.IQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 题目 服务层处理（09 §2.2、§2.3；含选项一次提交）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class QuestionServiceImpl implements IQuestionService {

    private static final Set<String> TYPES = Set.of(Question.TYPE_SINGLE, Question.TYPE_MULTI, Question.TYPE_TEXT);

    private final QuestionMapper baseMapper;
    private final QuestionOptionMapper optionMapper;
    private final IQuestionnaireService questionnaireService;

    @Override
    public List<QuestionVo> queryByQuestionnaire(Long questionnaireId) {
        List<QuestionVo> vos = baseMapper.selectVoList(Wrappers.<Question>lambdaQuery()
            .eq(Question::getQuestionnaireId, questionnaireId)
            .orderByAsc(Question::getSeq));
        if (CollUtil.isNotEmpty(vos)) {
            List<Long> questionIds = vos.stream().map(QuestionVo::getId).toList();
            List<QuestionOption> options = optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
                .in(QuestionOption::getQuestionId, questionIds)
                .orderByAsc(QuestionOption::getSeq));
            vos.forEach(vo -> vo.setOptions(options.stream()
                .filter(o -> o.getQuestionId().equals(vo.getId())).toList()));
        }
        return vos;
    }

    @Override
    public QuestionVo queryById(Long id) {
        QuestionVo vo = baseMapper.selectVoById(id);
        if (vo != null) {
            vo.setOptions(optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
                .eq(QuestionOption::getQuestionId, id)
                .orderByAsc(QuestionOption::getSeq)));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(QuestionVo vo) {
        Questionnaire questionnaire = questionnaireService.requireDraft(vo.getQuestionnaireId());
        validateRow(questionnaire, vo);
        baseMapper.insert(vo);
        insertOptions(vo.getId(), vo.getOptions());
        return vo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(QuestionVo vo) {
        if (vo.getId() == null) {
            throw new ServiceException("题目主键不能为空");
        }
        Question old = baseMapper.selectById(vo.getId());
        if (old == null) {
            throw new ServiceException("题目不存在：" + vo.getId());
        }
        Questionnaire questionnaire = questionnaireService.requireDraft(old.getQuestionnaireId());
        validateRow(questionnaire, vo);
        vo.setQuestionnaireId(old.getQuestionnaireId());
        baseMapper.updateById(vo);
        // 选项差异更新：删旧插新（09 §6.1 题目 CRUD 含选项一次提交）
        optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery().eq(QuestionOption::getQuestionId, vo.getId()));
        insertOptions(vo.getId(), vo.getOptions());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        Question old = baseMapper.selectById(id);
        if (old != null) {
            questionnaireService.requireDraft(old.getQuestionnaireId());
        }
        optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery().eq(QuestionOption::getQuestionId, id));
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionImportVo importQuestions(Long questionnaireId, List<QuestionVo> rows) {
        Questionnaire questionnaire = questionnaireService.requireDraft(questionnaireId);
        QuestionImportVo receipt = new QuestionImportVo();
        receipt.setTotal(rows == null ? 0 : rows.size());
        if (CollUtil.isEmpty(rows)) {
            return receipt;
        }
        int rowNo = 0;
        for (QuestionVo row : rows) {
            rowNo++;
            String error = validateRowMessage(questionnaire, row);
            if (error != null) {
                receipt.getErrors().add(new QuestionImportVo.RowError(rowNo, error));
                continue;
            }
            row.setQuestionnaireId(questionnaireId);
            baseMapper.insert(row);
            insertOptions(row.getId(), row.getOptions());
            receipt.setSuccess(receipt.getSuccess() + 1);
        }
        return receipt;
    }

    /**
     * 单行校验（新增/编辑共用），违规抛异常
     */
    private void validateRow(Questionnaire questionnaire, QuestionVo vo) {
        String error = validateRowMessage(questionnaire, vo);
        if (error != null) {
            throw new ServiceException(error, AssessmentErrorCode.ANSWER_SCORE_OUT_OF_RANGE);
        }
    }

    /**
     * 单行校验（导入共用），返回错误文案或 null。
     *
     * <p>覆盖：题干非空、类型值域、量表类 item_no/factor_code 必填、选项分值落
     * [answer_min_score, answer_max_score]（7011，09 §7）。
     */
    private String validateRowMessage(Questionnaire questionnaire, QuestionVo vo) {
        if (StringUtils.isBlank(vo.getContent())) {
            return "题干不能为空";
        }
        if (!TYPES.contains(vo.getType())) {
            return "题目类型非法：" + vo.getType();
        }
        boolean scale = Questionnaire.CATEGORY_SCALE.equals(questionnaire.getCategory())
            && questionnaire.getScaleType() != null
            && !Questionnaire.SCALE_CUSTOM.equals(questionnaire.getScaleType());
        if (scale && vo.getItemNo() == null) {
            return "标准量表题目必须填写条目序号 item_no";
        }
        if (scale && StringUtils.isBlank(vo.getFactorCode())) {
            return "标准量表题目必须填写因子归属 factor_code";
        }
        if (vo.getRequired() == null) {
            vo.setRequired(1);
        }
        if (vo.getReverseFlag() == null) {
            vo.setReverseFlag(0);
        }
        if (CollUtil.isNotEmpty(vo.getOptions())) {
            BigDecimal min = questionnaire.getAnswerMinScore();
            BigDecimal max = questionnaire.getAnswerMaxScore();
            if (min != null && max != null) {
                for (QuestionOption option : vo.getOptions()) {
                    if (option.getScore() == null
                        || option.getScore().compareTo(min) < 0
                        || option.getScore().compareTo(max) > 0) {
                        return "选项分值必须落在 " + min + " ~ " + max + " 内";
                    }
                }
            }
        }
        return null;
    }

    private void insertOptions(Long questionId, List<QuestionOption> options) {
        if (CollUtil.isEmpty(options)) {
            return;
        }
        for (QuestionOption option : options) {
            option.setId(null);
            option.setQuestionId(questionId);
            if (option.getIsOther() == null) {
                option.setIsOther(0);
            }
            optionMapper.insert(option);
        }
    }

}
