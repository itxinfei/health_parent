package com.xinfei.health.assessment.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.redis.utils.RedisUtils;
import com.xinfei.common.satoken.utils.LoginHelper;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.mapper.MemberMapper;
import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.constant.ScaleConfigKeys;
import com.xinfei.health.assessment.domain.AssessmentAnswer;
import com.xinfei.health.assessment.domain.AssessmentFactor;
import com.xinfei.health.assessment.domain.AssessmentRecord;
import com.xinfei.health.assessment.domain.Crowd;
import com.xinfei.health.assessment.domain.CrowdRule;
import com.xinfei.health.assessment.domain.MemberCrowd;
import com.xinfei.health.assessment.domain.Question;
import com.xinfei.health.assessment.domain.QuestionOption;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleConfig;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import com.xinfei.health.assessment.domain.Suggestion;
import com.xinfei.health.assessment.domain.bo.AnswerItemBo;
import com.xinfei.health.assessment.domain.bo.AssessmentQueryBo;
import com.xinfei.health.assessment.domain.bo.AssessmentSubmitBo;
import com.xinfei.health.assessment.domain.vo.AssessmentRecordVo;
import com.xinfei.health.assessment.domain.vo.QuestionnaireVo;
import com.xinfei.health.assessment.engine.ScoringEngine;
import com.xinfei.health.assessment.engine.ScoringEngine.FactorLine;
import com.xinfei.health.assessment.engine.ScoringEngine.ScoringContext;
import com.xinfei.health.assessment.engine.ScoringEngine.ScoringInput;
import com.xinfei.health.assessment.engine.ScoringEngine.ScoringResult;
import com.xinfei.health.assessment.mapper.AssessmentAnswerMapper;
import com.xinfei.health.assessment.mapper.AssessmentFactorMapper;
import com.xinfei.health.assessment.mapper.AssessmentRecordMapper;
import com.xinfei.health.assessment.mapper.CrowdMapper;
import com.xinfei.health.assessment.mapper.CrowdRuleMapper;
import com.xinfei.health.assessment.mapper.MemberCrowdMapper;
import com.xinfei.health.assessment.mapper.QuestionMapper;
import com.xinfei.health.assessment.mapper.QuestionOptionMapper;
import com.xinfei.health.assessment.mapper.QuestionnaireMapper;
import com.xinfei.health.assessment.mapper.ScaleConfigMapper;
import com.xinfei.health.assessment.mapper.ScaleFactorMapper;
import com.xinfei.health.assessment.mapper.ScaleRuleMapper;
import com.xinfei.health.assessment.mapper.SuggestionMapper;
import com.xinfei.health.assessment.service.IAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评估记录 服务层处理（09 §5：提交作答 → 算分 → 结论建议 → 人群归类，单事务）
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AssessmentServiceImpl implements IAssessmentService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 记录号 Redis 自增键前缀（EV + yyyyMMdd + 6 位序列，09 §2.7） */
    private static final String RECORD_NO_KEY = "health:assessment:recordno:";

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final ScaleFactorMapper factorMapper;
    private final ScaleRuleMapper ruleMapper;
    private final ScaleConfigMapper configMapper;
    private final AssessmentRecordMapper recordMapper;
    private final AssessmentAnswerMapper answerMapper;
    private final AssessmentFactorMapper assessmentFactorMapper;
    private final SuggestionMapper suggestionMapper;
    private final CrowdMapper crowdMapper;
    private final CrowdRuleMapper crowdRuleMapper;
    private final MemberCrowdMapper memberCrowdMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentRecordVo submit(AssessmentSubmitBo bo, Long memberId, String source) {
        // 步骤 1：问卷须已发布（7001）
        Questionnaire questionnaire = questionnaireMapper.selectById(bo.getQuestionnaireId());
        if (questionnaire == null || !Questionnaire.STATUS_PUBLISHED.equals(questionnaire.getStatus())) {
            throw new ServiceException("量表/问卷不存在或未发布", AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        // 步骤 2：会员存在（6001，07 §7）
        if (memberId == null || !memberMapper.exists(Wrappers.<com.xinfei.health.appointment.domain.Member>lambdaQuery()
            .eq(com.xinfei.health.appointment.domain.Member::getId, Math.toIntExact(memberId)))) {
            throw new ServiceException("会员不存在", HealthErrorCode.MEMBER_NOT_FOUND);
        }
        // 步骤 3：幂等——doing 复用覆盖，scored 禁止重复（7010）
        AssessmentRecord record = recordMapper.selectOne(Wrappers.<AssessmentRecord>lambdaQuery()
            .eq(AssessmentRecord::getMemberId, memberId)
            .eq(AssessmentRecord::getQuestionnaireId, questionnaire.getId())
            .orderByDesc(AssessmentRecord::getCreateTime)
            .last("LIMIT 1"));
        if (record != null && AssessmentRecord.STATUS_SCORED.equals(record.getStatus())) {
            throw new ServiceException("该问卷已有已完成记录，禁止重复提交", AssessmentErrorCode.DUPLICATE_SUBMIT);
        }
        if (record == null) {
            record = new AssessmentRecord();
            record.setRecordNo(generateRecordNo());
            record.setMemberId(memberId);
            record.setQuestionnaireId(questionnaire.getId());
            record.setQuestionnaireVersion(questionnaire.getVersion());
            record.setScaleType(questionnaire.getScaleType());
            record.setStatus(AssessmentRecord.STATUS_DOING);
            record.setSource(source);
            record.setStartTime(bo.getStartTime() == null ? LocalDateTime.now() : bo.getStartTime());
            recordMapper.insert(record);
        } else {
            // 复用 doing 记录：清除旧答案后覆盖（09 §5.1 步骤 3）
            answerMapper.delete(Wrappers.<AssessmentAnswer>lambdaQuery()
                .eq(AssessmentAnswer::getRecordId, record.getId()));
            assessmentFactorMapper.delete(Wrappers.<AssessmentFactor>lambdaQuery()
                .eq(AssessmentFactor::getRecordId, record.getId()));
        }

        // 步骤 4：完整性校验（7002/7011/7004）
        List<Question> questions = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
            .eq(Question::getQuestionnaireId, questionnaire.getId()));
        Map<Long, Question> questionById = questions.stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));
        List<AnswerItemBo> answers = bo.getAnswers() == null ? List.of() : bo.getAnswers();
        validateCompleteness(questionnaire, questions, answers);
        Map<Long, BigDecimal> rawScores = resolveRawScores(questionnaire, questionById, answers);

        // 步骤 5：算分（09 §4，引擎数据驱动，零题号/零阈值字面量）
        List<ScaleFactor> factors = factorMapper.selectList(Wrappers.<ScaleFactor>lambdaQuery()
            .eq(ScaleFactor::getQuestionnaireId, questionnaire.getId()));
        List<ScaleRule> rules = ruleMapper.selectList(Wrappers.<ScaleRule>lambdaQuery()
            .eq(ScaleRule::getQuestionnaireId, questionnaire.getId()));
        List<ScaleConfig> configs = configMapper.selectList(Wrappers.<ScaleConfig>lambdaQuery()
            .eq(ScaleConfig::getQuestionnaireId, questionnaire.getId()));
        Map<String, String> resolvedConfig = resolveConfig(configs);

        ScoringContext ctx = new ScoringContext();
        ctx.setQuestionnaire(questionnaire);
        List<ScoringInput> inputs = new ArrayList<>();
        for (Question question : questions) {
            ScoringInput input = new ScoringInput();
            input.setQuestionId(question.getId());
            input.setFactorCode(question.getFactorCode());
            BigDecimal raw = rawScores.get(question.getId());
            if (raw != null) {
                input.setScore(ScoringEngine.reverse(raw, questionnaire.getAnswerMinScore(),
                    questionnaire.getAnswerMaxScore(), question.getReverseFlag()));
            }
            inputs.add(input);
        }
        ctx.setInputs(inputs);
        ctx.setFactors(factors);
        ctx.setRules(rules);
        ctx.setConfig(resolvedConfig);
        ScoringResult result = ScoringEngine.score(ctx);

        // 步骤 6：写作答明细 + 因子得分 + 记录（快照字段）
        writeAnswers(record.getId(), questionnaire, questions, answers, rawScores);
        writeFactors(record.getId(), result);
        fillRecord(record, questionnaire, result, configs);
        record.setAnswerCount(result.getAnswerCount());
        record.setSubmitTime(LocalDateTime.now());
        if (record.getStartTime() != null) {
            record.setDurationSec(Math.toIntExact(
                ChronoUnit.SECONDS.between(record.getStartTime(), record.getSubmitTime())));
        }
        record.setStatus(AssessmentRecord.STATUS_SCORED);
        if (AssessmentRecord.SOURCE_ADMIN.equals(source)) {
            record.setOperatorId(LoginHelper.getUserId());
            record.setOperatorName(LoginHelper.getUsername());
        }
        recordMapper.updateById(record);

        // 步骤 7：结论与建议匹配（09 §5.3，快照落 suggestion_json）
        record.setSuggestionJson(matchSuggestions(questionnaire, result));
        recordMapper.updateById(record);

        // 步骤 8：人群归类（09 §5.4，source_record_id 必填）
        List<Long> crowdIds = classifyCrowds(record.getId(), memberId, result, record);
        record.setCrowdIds(crowdIds.isEmpty() ? null
            : crowdIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        recordMapper.updateById(record);

        // 步骤 9：返回结构化结果
        return queryById(record.getId());
    }

    // ---------- 查询 ----------

    @Override
    public TableDataInfo<AssessmentRecordVo> queryPageList(AssessmentQueryBo bo, PageQuery pageQuery) {
        Page<AssessmentRecordVo> page = recordMapper.selectVoPage(pageQuery.build(),
            Wrappers.<AssessmentRecord>lambdaQuery()
                .eq(bo.getMemberId() != null, AssessmentRecord::getMemberId, bo.getMemberId())
                .eq(bo.getQuestionnaireId() != null, AssessmentRecord::getQuestionnaireId, bo.getQuestionnaireId())
                .eq(StringUtils.isNotBlank(bo.getLevel()), AssessmentRecord::getLevel, bo.getLevel())
                .eq(StringUtils.isNotBlank(bo.getSeverity()), AssessmentRecord::getSeverity, bo.getSeverity())
                .eq(StringUtils.isNotBlank(bo.getStatus()), AssessmentRecord::getStatus, bo.getStatus())
                .eq(StringUtils.isNotBlank(bo.getSource()), AssessmentRecord::getSource, bo.getSource())
                .ge(bo.getBeginDate() != null, AssessmentRecord::getCreateTime,
                    bo.getBeginDate() == null ? null : bo.getBeginDate().atStartOfDay())
                .le(bo.getEndDate() != null, AssessmentRecord::getCreateTime,
                    bo.getEndDate() == null ? null : bo.getEndDate().atTime(LocalTime.MAX))
                .orderByDesc(AssessmentRecord::getCreateTime));
        attachNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public AssessmentRecordVo queryById(Long id) {
        AssessmentRecordVo vo = recordMapper.selectVoById(id);
        if (vo == null) {
            throw new ServiceException("评估记录不存在或无权查看", AssessmentErrorCode.RECORD_NOT_FOUND_OR_DENIED);
        }
        attachNames(List.of(vo));
        vo.setAnswers(answerMapper.selectList(Wrappers.<AssessmentAnswer>lambdaQuery()
            .eq(AssessmentAnswer::getRecordId, id)));
        vo.setFactors(assessmentFactorMapper.selectList(Wrappers.<AssessmentFactor>lambdaQuery()
            .eq(AssessmentFactor::getRecordId, id).orderByAsc(AssessmentFactor::getSort)));
        if (StringUtils.isNotBlank(vo.getCrowdIds())) {
            List<Long> crowdIds = java.util.Arrays.stream(vo.getCrowdIds().split(","))
                .map(Long::valueOf).toList();
            vo.setCrowds(crowdMapper.selectList(Wrappers.<Crowd>lambdaQuery().in(Crowd::getId, crowdIds)));
        }
        return vo;
    }

    @Override
    public Map<String, Object> statistics(AssessmentQueryBo bo) {
        List<AssessmentRecord> records = recordMapper.selectList(Wrappers.<AssessmentRecord>lambdaQuery()
            .ge(bo.getBeginDate() != null, AssessmentRecord::getCreateTime,
                bo.getBeginDate() == null ? null : bo.getBeginDate().atStartOfDay())
            .le(bo.getEndDate() != null, AssessmentRecord::getCreateTime,
                bo.getEndDate() == null ? null : bo.getEndDate().atTime(LocalTime.MAX))
            .eq(AssessmentRecord::getStatus, AssessmentRecord.STATUS_SCORED));
        Map<String, Object> stat = new LinkedHashMap<>();
        long memberCount = records.stream().map(AssessmentRecord::getMemberId)
            .filter(Objects::nonNull).distinct().count();
        stat.put("memberCount", memberCount);
        stat.put("recordCount", records.size());
        List<AssessmentRecord> withFlag = records.stream()
            .filter(r -> r.getPositiveFlag() != null).toList();
        stat.put("positiveRate", withFlag.isEmpty() ? null
            : BigDecimal.valueOf(withFlag.stream().filter(r -> r.getPositiveFlag() == 1).count())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(withFlag.size()), 2, RoundingMode.HALF_UP));
        stat.put("severityDist", records.stream()
            .filter(r -> StringUtils.isNotBlank(r.getSeverity()))
            .collect(Collectors.groupingBy(AssessmentRecord::getSeverity, Collectors.counting())));
        stat.put("levelDist", records.stream()
            .filter(r -> StringUtils.isNotBlank(r.getLevel()))
            .collect(Collectors.groupingBy(AssessmentRecord::getLevel, Collectors.counting())));
        return stat;
    }

    @Override
    public Boolean deleteById(Long id) {
        AssessmentRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new ServiceException("评估记录不存在或无权查看", AssessmentErrorCode.RECORD_NOT_FOUND_OR_DENIED);
        }
        return recordMapper.deleteById(id) > 0;
    }

    // ---------- C 端 ----------

    @Override
    public List<QuestionnaireVo> appQuestionnaireList() {
        List<QuestionnaireVo> vos = questionnaireMapper.selectVoList(Wrappers.<Questionnaire>lambdaQuery()
            .eq(Questionnaire::getStatus, Questionnaire.STATUS_PUBLISHED)
            .eq(Questionnaire::getIsPublic, 1)
            .orderByAsc(Questionnaire::getSort));
        vos.forEach(vo -> vo.setQuestionNum(Math.toIntExact(questionMapper.selectCount(
            Wrappers.<Question>lambdaQuery().eq(Question::getQuestionnaireId, vo.getId())))));
        return vos;
    }

    @Override
    public QuestionnaireVo appQuestionnaireDetail(Long id) {
        QuestionnaireVo vo = questionnaireMapper.selectVoById(id);
        if (vo == null || !Questionnaire.STATUS_PUBLISHED.equals(vo.getStatus()) || vo.getIsPublic() != 1) {
            throw new ServiceException("量表/问卷不存在或未发布", AssessmentErrorCode.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        List<Question> questions = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
            .eq(Question::getQuestionnaireId, id).orderByAsc(Question::getSeq));
        // 09 §6.2：不返回 factor_code 等算分内部字段
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        List<QuestionOption> options = CollUtil.isEmpty(questionIds) ? List.of()
            : optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
                .in(QuestionOption::getQuestionId, questionIds).orderByAsc(QuestionOption::getSeq));
        questions.forEach(q -> {
            q.setFactorCode(null);
            q.setReverseFlag(null);
        });
        options.forEach(o -> o.setFactorCode(null));
        vo.setQuestions(questions.stream().map(q -> {
            com.xinfei.health.assessment.domain.vo.QuestionVo qv =
                com.xinfei.common.core.utils.MapstructUtils.convert(q,
                    com.xinfei.health.assessment.domain.vo.QuestionVo.class);
            qv.setOptions(options.stream().filter(o -> o.getQuestionId().equals(q.getId())).toList());
            return qv;
        }).toList());
        return vo;
    }

    @Override
    public TableDataInfo<AssessmentRecordVo> appRecords(Long memberId, PageQuery pageQuery) {
        Page<AssessmentRecordVo> page = recordMapper.selectVoPage(pageQuery.build(),
            Wrappers.<AssessmentRecord>lambdaQuery()
                .eq(AssessmentRecord::getMemberId, memberId)
                .eq(AssessmentRecord::getStatus, AssessmentRecord.STATUS_SCORED)
                .orderByDesc(AssessmentRecord::getCreateTime));
        attachNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public AssessmentRecordVo appDetail(Long memberId, Long id) {
        AssessmentRecordVo vo = recordMapper.selectVoById(id);
        if (vo == null || !memberId.equals(vo.getMemberId())) {
            throw new ServiceException("评估记录不存在或无权查看", AssessmentErrorCode.RECORD_NOT_FOUND_OR_DENIED);
        }
        return queryById(id);
    }

    // ---------- 私有：提交流程各步 ----------

    /**
     * 步骤 4：必答全覆盖、单选单选项、多选至少一项、选项须属该题（7002/7011/7004，09 §7）
     */
    private void validateCompleteness(Questionnaire questionnaire, List<Question> questions,
                                      List<AnswerItemBo> answers) {
        Map<Long, AnswerItemBo> answerByQuestion = answers.stream()
            .filter(a -> a.getQuestionId() != null)
            .collect(Collectors.toMap(AnswerItemBo::getQuestionId, Function.identity(), (a, b) -> b));
        Map<Long, List<QuestionOption>> optionsByQuestion = optionMapper.selectList(
                Wrappers.<QuestionOption>lambdaQuery()
                    .in(QuestionOption::getQuestionId,
                        questions.stream().map(Question::getId).toList()))
            .stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));

        for (Question question : questions) {
            AnswerItemBo answer = answerByQuestion.get(question.getId());
            boolean required = question.getRequired() == null || question.getRequired() == 1;
            if (answer == null || isEmptyAnswer(answer)) {
                if (required) {
                    throw new ServiceException("必答题未作答：第 " + question.getSeq() + " 题",
                        AssessmentErrorCode.ANSWER_INCOMPLETE);
                }
                continue;
            }
            List<QuestionOption> options = optionsByQuestion.getOrDefault(question.getId(), List.of());
            Set<Long> optionIds = options.stream().map(QuestionOption::getId).collect(Collectors.toSet());
            List<Long> chosen = answer.getOptionIds() == null ? List.of() : answer.getOptionIds();
            if (Question.TYPE_SINGLE.equals(question.getType()) && chosen.size() != 1) {
                throw new ServiceException("单选题必须且只能选 1 项：第 " + question.getSeq() + " 题",
                    AssessmentErrorCode.ANSWER_INCOMPLETE);
            }
            if (Question.TYPE_MULTI.equals(question.getType()) && chosen.isEmpty()) {
                throw new ServiceException("多选题至少选择 1 项：第 " + question.getSeq() + " 题",
                    AssessmentErrorCode.ANSWER_INCOMPLETE);
            }
            if (Question.TYPE_TEXT.equals(question.getType()) && StringUtils.isBlank(answer.getAnswerText())) {
                throw new ServiceException("填空题必须填写文本：第 " + question.getSeq() + " 题",
                    AssessmentErrorCode.ANSWER_INCOMPLETE);
            }
            for (Long optionId : chosen) {
                if (!optionIds.contains(optionId)) {
                    throw new ServiceException("选项不属于该题：第 " + question.getSeq() + " 题",
                        AssessmentErrorCode.ANSWER_INCOMPLETE);
                }
            }
        }

        // 量表类 item_no 覆盖不完整（7004，09 §5.1 步骤 4）
        boolean scale = Questionnaire.CATEGORY_SCALE.equals(questionnaire.getCategory())
            && questionnaire.getScaleType() != null
            && !Questionnaire.SCALE_CUSTOM.equals(questionnaire.getScaleType());
        if (scale) {
            TreeSet<Integer> itemNos = questions.stream().map(Question::getItemNo)
                .filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
            if (itemNos.size() != questions.size() || itemNos.isEmpty()
                || !itemNos.first().equals(1) || !itemNos.last().equals(questions.size())) {
                throw new ServiceException("量表条目覆盖不完整", AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID);
            }
        }
    }

    private boolean isEmptyAnswer(AnswerItemBo answer) {
        return (answer.getOptionIds() == null || answer.getOptionIds().isEmpty())
            && StringUtils.isBlank(answer.getAnswerText());
    }

    /**
     * 步骤 4：选项分值解析与越界校验（7011；多选累加，09 §4.3）
     */
    private Map<Long, BigDecimal> resolveRawScores(Questionnaire questionnaire,
                                                   Map<Long, Question> questionById,
                                                   List<AnswerItemBo> answers) {
        List<Long> questionIds = questionById.values().stream().map(Question::getId).toList();
        Map<Long, QuestionOption> optionById = CollUtil.isEmpty(questionIds) ? Map.of()
            : optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
                    .in(QuestionOption::getQuestionId, questionIds))
                .stream().collect(Collectors.toMap(QuestionOption::getId, Function.identity()));
        Map<Long, BigDecimal> rawScores = new HashMap<>();
        for (AnswerItemBo answer : answers) {
            if (answer.getOptionIds() == null || answer.getOptionIds().isEmpty()) {
                continue;
            }
            Question question = questionById.get(answer.getQuestionId());
            if (question == null || Question.TYPE_TEXT.equals(question.getType())) {
                continue;
            }
            BigDecimal raw = BigDecimal.ZERO;
            for (Long optionId : answer.getOptionIds()) {
                QuestionOption option = optionById.get(optionId);
                if (option == null || option.getScore() == null) {
                    continue;
                }
                raw = raw.add(option.getScore());
            }
            if (questionnaire.getAnswerMinScore() != null && questionnaire.getAnswerMaxScore() != null) {
                for (Long optionId : answer.getOptionIds()) {
                    QuestionOption option = optionById.get(optionId);
                    if (option != null && option.getScore() != null
                        && (option.getScore().compareTo(questionnaire.getAnswerMinScore()) < 0
                        || option.getScore().compareTo(questionnaire.getAnswerMaxScore()) > 0)) {
                        throw new ServiceException("作答分值不在允许域", AssessmentErrorCode.ANSWER_SCORE_OUT_OF_RANGE);
                    }
                }
            }
            rawScores.put(answer.getQuestionId(), raw);
        }
        return rawScores;
    }

    /**
     * A13 三级配置解析：问卷级 t_health_scale_config &gt; sys_config（health:scale: 前缀）&gt; 内置兜底
     */
    private Map<String, String> resolveConfig(List<ScaleConfig> configs) {
        Map<String, String> resolved = new LinkedHashMap<>(ScaleConfigKeys.BUILTIN_DEFAULTS);
        // sys_config 全局默认（09 §2.6 优先级 2）
        List<String> sysKeys = ScaleConfigKeys.BUILTIN_DEFAULTS.keySet().stream()
            .map(k -> ScaleConfigKeys.SYS_CONFIG_PREFIX + k).toList();
        String inClause = sysKeys.stream().map(k -> "'" + k + "'").collect(Collectors.joining(","));
        try {
            jdbcTemplate.query("SELECT config_key, config_value FROM sys_config WHERE config_key IN ("
                    + inClause + ")",
                rs -> {
                    resolved.put(rs.getString("config_key").substring(ScaleConfigKeys.SYS_CONFIG_PREFIX.length()),
                        rs.getString("config_value"));
                });
        } catch (Exception e) {
            log.warn("sys_config 全局阈值读取失败，回退内置兜底：{}", e.getMessage());
        }
        // 问卷级配置（优先级最高）
        if (configs != null) {
            for (ScaleConfig config : configs) {
                resolved.put(config.getConfigKey(), config.getConfigValue());
            }
        }
        return resolved;
    }

    /**
     * 步骤 6：作答明细落库（rawScore 与 score 双列，09 §2.8）
     */
    private void writeAnswers(Long recordId, Questionnaire questionnaire, List<Question> questions,
                              List<AnswerItemBo> answers, Map<Long, BigDecimal> rawScores) {
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        Map<Long, QuestionOption> optionById = CollUtil.isEmpty(questionIds) ? Map.of()
            : optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
                    .in(QuestionOption::getQuestionId, questionIds))
                .stream().collect(Collectors.toMap(QuestionOption::getId, Function.identity()));
        Map<Long, Question> questionById = questions.stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));
        for (AnswerItemBo answer : answers) {
            Question question = questionById.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }
            AssessmentAnswer row = new AssessmentAnswer();
            row.setRecordId(recordId);
            row.setQuestionId(question.getId());
            row.setItemNo(question.getItemNo());
            row.setOptionIds(answer.getOptionIds() == null ? null
                : answer.getOptionIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
            row.setOptionCount(answer.getOptionIds() == null ? 0 : answer.getOptionIds().size());
            row.setAnswerText(answer.getAnswerText());
            BigDecimal raw = rawScores.get(question.getId());
            if (raw != null) {
                row.setRawScore(raw);
                // 反向映射后计入总分（09 §3.3 先反向再求和）
                int reverseFlag = question.getReverseFlag() == null ? 0 : question.getReverseFlag();
                row.setReverseFlag(reverseFlag);
                if (reverseFlag == 1
                    && (questionnaire.getAnswerMinScore() == null || questionnaire.getAnswerMaxScore() == null)) {
                    throw new ServiceException("反向计分题缺少分值上下界配置",
                        AssessmentErrorCode.SCALE_CONFIG_MISSING);
                }
                row.setScore(ScoringEngine.reverse(raw, questionnaire.getAnswerMinScore(),
                    questionnaire.getAnswerMaxScore(), reverseFlag));
            }
            answerMapper.insert(row);
        }
    }

    /**
     * 步骤 6：因子得分落库（09 §2.9）
     */
    private void writeFactors(Long recordId, ScoringResult result) {
        int sort = 0;
        for (FactorLine line : result.getFactorLines()) {
            AssessmentFactor row = new AssessmentFactor();
            row.setRecordId(recordId);
            row.setFactorCode(line.getFactorCode());
            row.setFactorName(line.getFactorName());
            row.setItemCount(line.getItemCount());
            row.setRawSum(line.getRawSum());
            row.setFactorScore(line.getFactorScore());
            row.setConvertScore(line.getConvertScore());
            row.setLevelCode(line.getLevelCode());
            row.setConclusion(line.getConclusion());
            row.setNormUpper(line.getNormUpper());
            row.setNormExceedFlag(line.getNormExceedFlag());
            row.setSort(line.getSort() == null ? sort++ : line.getSort());
            assessmentFactorMapper.insert(row);
        }
    }

    /**
     * 步骤 6：记录字段快照
     */
    private void fillRecord(AssessmentRecord record, Questionnaire questionnaire, ScoringResult result,
                            List<ScaleConfig> configs) {
        record.setTotalScore(result.getTotalScore());
        record.setAvgScore(result.getAvgScore());
        record.setPositiveCount(result.getPositiveCount());
        record.setPositiveFlag(result.getPositiveFlag());
        record.setSeverity(result.getSeverity());
        record.setTcmPrimary(result.getTcmPrimary());
        record.setTcmLevel(result.getTcmLevel());
        record.setTcmCombineJson(result.getTcmCombineJson());
        record.setConclusion(result.getConclusion());
        record.setLevel(result.getLevel());
        record.setFactorScoresJson(JSONUtil.toJsonStr(result.getFactorLines()));
        String cfgVersion = configs == null ? null : configs.stream()
            .map(ScaleConfig::getCfgVersion).filter(StringUtils::isNotBlank).findFirst().orElse(null);
        record.setCfgVersion(cfgVersion);
    }

    /**
     * 步骤 7：建议匹配（09 §5.3 优先级：(问卷,因子,等级) &gt; (问卷,因子) &gt; (问卷,等级) &gt; (通用,因子) &gt; (通用)）
     */
    private String matchSuggestions(Questionnaire questionnaire, ScoringResult result) {
        List<Suggestion> all = suggestionMapper.selectList(Wrappers.<Suggestion>lambdaQuery()
            .eq(questionnaire.getId() != null, Suggestion::getQuestionnaireId, questionnaire.getId()));
        // 兼容通用建议（questionnaire_id 为空）与问卷级建议并存场景
        List<Suggestion> generic = suggestionMapper.selectList(Wrappers.<Suggestion>lambdaQuery()
            .isNull(Suggestion::getQuestionnaireId));
        List<Map<String, String>> picked = new ArrayList<>();
        List<String> levels = new ArrayList<>();
        if (result.getLevel() != null) {
            levels.add(result.getLevel());
        }
        if (result.getFactorLines() != null) {
            for (FactorLine line : result.getFactorLines()) {
                if (line.getLevelCode() != null && !levels.contains(line.getLevelCode())) {
                    levels.add(line.getLevelCode());
                }
            }
        }
        // 因子维度
        if (result.getFactorLines() != null) {
            for (FactorLine line : result.getFactorLines()) {
                List<Suggestion> hit = firstHit(all, generic, line.getFactorCode(), line.getLevelCode());
                appendSuggestions(picked, hit, all);
            }
        }
        // 总分维度
        List<Suggestion> totalHit = firstHit(all, generic, null, result.getLevel());
        appendSuggestions(picked, totalHit, all);
        return picked.isEmpty() ? null : JSONUtil.toJsonStr(picked);
    }

    /**
     * 五级优先命中即取（同优先级按 sort）
     */
    private List<Suggestion> firstHit(List<Suggestion> qScoped, List<Suggestion> generic,
                                      String factorCode, String levelCode) {
        List<List<Suggestion>> groups = List.of(
            filter(qScoped, factorCode, levelCode),
            filter(qScoped, factorCode, null),
            filter(qScoped, null, levelCode),
            filter(generic, factorCode, null),
            filter(generic, null, null));
        for (List<Suggestion> group : groups) {
            if (CollUtil.isNotEmpty(group)) {
                return group;
            }
        }
        return List.of();
    }

    private List<Suggestion> filter(List<Suggestion> source, String factorCode, String levelCode) {
        return source.stream()
            .filter(s -> factorCode == null ? s.getFactorCode() == null : factorCode.equals(s.getFactorCode()))
            .filter(s -> levelCode == null ? s.getLevelCode() == null : levelCode.equals(s.getLevelCode()))
            .sorted((a, b) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(),
                b.getSort() == null ? 0 : b.getSort()))
            .toList();
    }

    private void appendSuggestions(List<Map<String, String>> picked, List<Suggestion> hit,
                                   List<Suggestion> scoped) {
        Set<Long> used = picked.stream()
            .map(m -> m.get("id")).filter(Objects::nonNull).map(Long::valueOf).collect(Collectors.toSet());
        for (Suggestion suggestion : hit) {
            if (used.contains(suggestion.getId())) {
                continue;
            }
            used.add(suggestion.getId());
            Map<String, String> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(suggestion.getId()));
            item.put("type", suggestion.getType());
            item.put("content", suggestion.getContent());
            picked.add(item);
        }
    }

    /**
     * 步骤 8：人群归类（09 §5.4；factor/assessment 两类源本期求值，
     * indicator/report/profile 源留后续批次——17 §1.17 登记）
     */
    private List<Long> classifyCrowds(Long recordId, Long memberId, ScoringResult result,
                                      AssessmentRecord record) {
        List<Crowd> crowds = crowdMapper.selectList(Wrappers.<Crowd>lambdaQuery()
            .eq(Crowd::getStatus, Crowd.STATUS_ENABLED));
        if (CollUtil.isEmpty(crowds)) {
            return List.of();
        }
        Map<String, FactorLine> factorLines = result.getFactorLines() == null ? Map.of()
            : result.getFactorLines().stream()
                .collect(Collectors.toMap(FactorLine::getFactorCode, Function.identity(), (a, b) -> a));
        Map<String, Object> valueSource = new HashMap<>();
        valueSource.put("total_score", record.getTotalScore());
        valueSource.put("avg_score", record.getAvgScore());
        valueSource.put("positive_count", record.getPositiveCount());
        valueSource.put("positive_flag", record.getPositiveFlag());
        valueSource.put("severity", record.getSeverity());
        valueSource.put("level", record.getLevel());
        valueSource.put("tcm_level", record.getTcmLevel());

        List<Long> hitCrowdIds = new ArrayList<>();
        for (Crowd crowd : crowds) {
            List<CrowdRule> rules = crowdRuleMapper.selectList(Wrappers.<CrowdRule>lambdaQuery()
                .eq(CrowdRule::getCrowdId, crowd.getId())
                .orderByAsc(CrowdRule::getGroupNo)
                .orderByAsc(CrowdRule::getSort));
            if (CollUtil.isEmpty(rules)) {
                continue;
            }
            if (evaluateCrowd(rules, factorLines, valueSource)) {
                hitCrowdIds.add(crowd.getId());
                boolean exists = memberCrowdMapper.exists(Wrappers.<MemberCrowd>lambdaQuery()
                    .eq(MemberCrowd::getMemberId, memberId)
                    .eq(MemberCrowd::getCrowdId, crowd.getId())
                    .eq(MemberCrowd::getSourceRecordId, recordId));
                if (!exists) {
                    MemberCrowd relation = new MemberCrowd();
                    relation.setMemberId(memberId);
                    relation.setCrowdId(crowd.getId());
                    relation.setSourceRecordId(recordId);
                    relation.setJoinTime(LocalDateTime.now());
                    memberCrowdMapper.insert(relation);
                }
                // member_count 冗余重算（09 §2.12）
                Long count = memberCrowdMapper.selectCount(Wrappers.<MemberCrowd>lambdaQuery()
                    .eq(MemberCrowd::getCrowdId, crowd.getId()));
                crowd.setMemberCount(count.intValue());
                crowdMapper.updateById(crowd);
            }
        }
        return hitCrowdIds;
    }

    /**
     * 人群规则求值：同 group_no 内按 logic 组合，组间固定 OR（09 §2.12）
     */
    private boolean evaluateCrowd(List<CrowdRule> rules, Map<String, FactorLine> factorLines,
                                  Map<String, Object> valueSource) {
        Map<Integer, List<CrowdRule>> byGroup = rules.stream()
            .collect(Collectors.groupingBy(r -> r.getGroupNo() == null ? 1 : r.getGroupNo(),
                LinkedHashMap::new, Collectors.toList()));
        for (List<CrowdRule> group : byGroup.values()) {
            // 组内从左到右按每条规则的 logic 与前序结果组合（09 §2.12）
            boolean groupResult = false;
            boolean first = true;
            for (CrowdRule rule : group) {
                boolean matched = evaluateRule(rule, factorLines, valueSource);
                if (first) {
                    groupResult = matched;
                    first = false;
                } else if (CrowdRule.LOGIC_OR.equals(rule.getLogic())) {
                    groupResult = groupResult || matched;
                } else {
                    groupResult = groupResult && matched;
                }
            }
            if (groupResult) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateRule(CrowdRule rule, Map<String, FactorLine> factorLines,
                                 Map<String, Object> valueSource) {
        Object resolved = resolveRuleValue(rule, factorLines, valueSource);
        if (resolved == null) {
            return false;
        }
        // eq/ne：value_text 优先文本相等，否则数值比较（09 §2.12）
        if (CrowdRule.OP_EQ.equals(rule.getOperator())) {
            return eqValue(resolved, rule);
        }
        if (CrowdRule.OP_NE.equals(rule.getOperator())) {
            return !eqValue(resolved, rule);
        }
        BigDecimal value = toDecimal(resolved);
        if (value == null) {
            return false;
        }
        return switch (rule.getOperator()) {
            case CrowdRule.OP_GT -> rule.getValueMin() == null || value.compareTo(rule.getValueMin()) > 0;
            case CrowdRule.OP_GE -> rule.getValueMin() == null || value.compareTo(rule.getValueMin()) >= 0;
            case CrowdRule.OP_LT -> rule.getValueMin() == null || value.compareTo(rule.getValueMin()) < 0;
            case CrowdRule.OP_LE -> rule.getValueMin() == null || value.compareTo(rule.getValueMin()) <= 0;
            case CrowdRule.OP_BETWEEN -> rule.getValueMin() != null && rule.getValueMax() != null
                && value.compareTo(rule.getValueMin()) >= 0 && value.compareTo(rule.getValueMax()) <= 0;
            default -> false;
        };
    }

    /**
     * 解析规则条件值（factor/assessment 两类源；indicator/report/profile 本期不求值，17 §1.17 登记）
     */
    private Object resolveRuleValue(CrowdRule rule, Map<String, FactorLine> factorLines,
                                    Map<String, Object> valueSource) {
        if (CrowdRule.SOURCE_FACTOR.equals(rule.getSourceType())) {
            FactorLine line = factorLines.get(rule.getSourceCode());
            return line != null ? line.getFactorScore() : valueSource.get(rule.getSourceCode());
        }
        if (CrowdRule.SOURCE_ASSESSMENT.equals(rule.getSourceType())) {
            return valueSource.get(rule.getSourceCode());
        }
        return null;
    }

    private boolean eqValue(Object resolved, CrowdRule rule) {
        if (StringUtils.isNotBlank(rule.getValueText())) {
            return rule.getValueText().equals(String.valueOf(resolved));
        }
        BigDecimal value = toDecimal(resolved);
        return value != null && rule.getValueMin() != null && value.compareTo(rule.getValueMin()) == 0;
    }

    private BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Integer i) {
            return BigDecimal.valueOf(i);
        }
        if (value instanceof Long l) {
            return BigDecimal.valueOf(l);
        }
        if (value instanceof Double d) {
            return BigDecimal.valueOf(d);
        }
        return null;
    }

    // ---------- 私有：工具 ----------

    /**
     * 记录号生成：EV + yyyyMMdd + 6 位序列（Redis 自增，09 §2.7）
     */
    private String generateRecordNo() {
        String day = LocalDate.now().format(DAY);
        long seq = RedisUtils.incrAtomicValue(RECORD_NO_KEY + day);
        return "EV" + day + String.format("%06d", seq);
    }

    private void attachNames(List<AssessmentRecordVo> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        List<Long> memberIds = records.stream().map(AssessmentRecordVo::getMemberId)
            .filter(Objects::nonNull).distinct().toList();
        List<Long> questionnaireIds = records.stream().map(AssessmentRecordVo::getQuestionnaireId)
            .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> memberNames = CollUtil.isEmpty(memberIds) ? Map.of()
            : memberMapper.selectList(Wrappers.<com.xinfei.health.appointment.domain.Member>lambdaQuery()
                    .in(com.xinfei.health.appointment.domain.Member::getId,
                        memberIds.stream().map(Math::toIntExact).toList()))
                .stream().collect(Collectors.toMap(
                    m -> m.getId().longValue(),
                    com.xinfei.health.appointment.domain.Member::getName, (a, b) -> a));
        Map<Long, String> questionnaireNames = CollUtil.isEmpty(questionnaireIds) ? Map.of()
            : questionnaireMapper.selectList(Wrappers.<Questionnaire>lambdaQuery()
                    .in(Questionnaire::getId, questionnaireIds))
                .stream().collect(Collectors.toMap(Questionnaire::getId, Questionnaire::getName, (a, b) -> a));
        records.forEach(vo -> {
            vo.setMemberName(memberNames.get(vo.getMemberId()));
            vo.setQuestionnaireName(questionnaireNames.get(vo.getQuestionnaireId()));
        });
    }

}
