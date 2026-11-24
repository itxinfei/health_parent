package com.xinfei.health.assessment.engine;

import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.constant.ScaleConfigKeys;
import com.xinfei.health.assessment.domain.Question;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleConfig;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 发布前六项校验（09 §5.2、§2.4、§2.5、§7），返回逐项错误清单（形态同 20 §6）。
 *
 * <p>本类内置的 A14 因子分配表仅用于发布校验比对，不参与算分——
 * 算分引擎的因子归属完全由 t_health_question.factor_code 数据驱动。
 *
 * @author xinfei
 */
public final class PublishValidator {

    /** 心理量表因子等级码值域（09 §2.5） */
    private static final Set<String> LEVEL_CODES = Set.of(
        "yes", "basic", "no", "tendency", "normal", "sub_health", "mild", "moderate", "severe");

    /** SCL-90 因子项目分配表（09 §4.1.1 / 18 A14，程序校验 1~90 无重无漏） */
    private static final Map<String, List<Integer>> A14 = new LinkedHashMap<>();

    static {
        A14.put("somatization", List.of(1, 4, 12, 27, 40, 42, 48, 49, 52, 53, 56, 58));
        A14.put("obsessive", List.of(3, 9, 10, 28, 38, 45, 46, 51, 55, 65));
        A14.put("interpersonal", List.of(6, 21, 34, 36, 37, 41, 61, 69, 73));
        A14.put("depression", List.of(5, 14, 15, 20, 22, 26, 29, 30, 31, 32, 54, 71, 79));
        A14.put("anxiety", List.of(2, 17, 23, 33, 39, 57, 72, 78, 80, 86));
        A14.put("hostility", List.of(11, 24, 63, 67, 74, 81));
        A14.put("phobic", List.of(13, 25, 47, 50, 70, 75, 82));
        A14.put("paranoid", List.of(8, 18, 43, 68, 76, 83));
        A14.put("psychoticism", List.of(7, 16, 35, 62, 77, 84, 85, 87, 88, 90));
        A14.put("additional", List.of(19, 44, 59, 60, 64, 66, 89));
    }

    private PublishValidator() {
    }

    /**
     * 发布校验：任一不过即失败，错误码取最严重类别（7004 &gt; 7005 &gt; 7006）。
     */
    public static PublishResult validate(Questionnaire questionnaire, List<Question> questions,
                                         List<ScaleFactor> factors, List<ScaleRule> rules,
                                         List<ScaleConfig> configs) {
        List<PublishError> errors = new ArrayList<>();
        boolean structureBroken = validateStructure(questionnaire, questions, factors, rules, errors);
        boolean factorBroken = validateFactors(questionnaire, factors, errors);
        boolean configBroken = validateConfigs(questionnaire, configs, errors);
        PublishResult result = new PublishResult();
        result.setErrors(errors);
        result.setSuccess(errors.isEmpty());
        if (!result.isSuccess()) {
            result.setErrorCode(structureBroken ? AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID
                : factorBroken ? AssessmentErrorCode.FACTOR_CONFIG_INVALID
                : AssessmentErrorCode.SCALE_CONFIG_MISSING);
        }
        return result;
    }

    /**
     * 结构校验：题目数、因子条目数求和、A14 比对、规则区间（7004）
     */
    private static boolean validateStructure(Questionnaire q, List<Question> questions,
                                             List<ScaleFactor> factors, List<ScaleRule> rules,
                                             List<PublishError> errors) {
        boolean broken = false;

        // 题目数与 question_count 强校验（09 §2.1）
        int questionNum = questions == null ? 0 : questions.size();
        if (q.getQuestionCount() == null || q.getQuestionCount() != questionNum) {
            errors.add(new PublishError("questionCount", String.valueOf(q.getQuestionCount()),
                String.valueOf(questionNum), "题目数与配置不一致"));
            broken = true;
        }

        boolean scl90 = Questionnaire.SCALE_SCL90.equals(q.getScaleType());
        boolean tcm9 = Questionnaire.SCALE_TCM9.equals(q.getScaleType());

        if (scl90) {
            // 题 90 + item_no 覆盖 1~90 无重无漏
            TreeSet<Integer> itemNos = questions == null ? new TreeSet<>()
                : questions.stream().map(Question::getItemNo).filter(java.util.Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));
            if (questionNum != 90 || itemNos.size() != 90
                || !itemNos.first().equals(1) || !itemNos.last().equals(90)) {
                errors.add(new PublishError("scl90.itemNo", "1~90 全覆盖", String.valueOf(itemNos.size()),
                    "SCL-90 题目必须 90 且 item_no 覆盖 1~90"));
                broken = true;
            }
        }
        if (tcm9) {
            if (questionNum != 60) {
                errors.add(new PublishError("tcm9.questionCount", "60", String.valueOf(questionNum),
                    "中医体质题目必须 60"));
                broken = true;
            }
            if (factors == null || factors.size() != 9) {
                errors.add(new PublishError("tcm9.factorCount", "9", String.valueOf(factors == null ? 0 : factors.size()),
                    "中医体质必须 9 个亚量表因子"));
                broken = true;
            }
        }

        // 因子条目数求和 = 题目数（09 §2.4 发布校验 1）
        if (factors != null) {
            int factorItemSum = factors.stream().mapToInt(f -> f.getItemCount() == null ? 0 : f.getItemCount()).sum();
            if (questionNum > 0 && factorItemSum != questionNum) {
                errors.add(new PublishError("factor.itemCountSum", String.valueOf(questionNum),
                    String.valueOf(factorItemSum), "因子条目数求和与题目数不一致"));
                broken = true;
            }
            if (tcm9) {
                for (ScaleFactor f : factors) {
                    int n = f.getItemCount() == null ? 0 : f.getItemCount();
                    if (n != 7 && n != 8) {
                        errors.add(new PublishError("factor." + f.getCode() + ".itemCount", "7 或 8",
                            String.valueOf(n), "中医体质每亚量表条目数必须为 7 或 8"));
                        broken = true;
                    }
                }
            }
        }

        // SCL-90 与 A14 逐因子完全一致（无重、无漏、不错配，09 §2.4 发布校验 2）
        if (scl90 && questions != null) {
            Map<String, List<Integer>> actual = new LinkedHashMap<>();
            for (Question question : questions) {
                if (question.getItemNo() == null || question.getFactorCode() == null) {
                    errors.add(new PublishError("question.itemNo", "必填", "空",
                        "量表题目 item_no 与 factor_code 必填"));
                    broken = true;
                    continue;
                }
                actual.computeIfAbsent(question.getFactorCode(), k -> new ArrayList<>()).add(question.getItemNo());
            }
            for (Map.Entry<String, List<Integer>> entry : A14.entrySet()) {
                List<Integer> expect = entry.getValue();
                List<Integer> got = actual.get(entry.getKey());
                if (got == null || !new TreeSet<>(got).equals(new TreeSet<>(expect))) {
                    String expectStr = expect.stream().map(String::valueOf).collect(Collectors.joining(","));
                    String gotStr = got == null ? "缺整个因子" : got.stream().sorted()
                        .map(String::valueOf).collect(Collectors.joining(","));
                    errors.add(new PublishError("factor." + entry.getKey(), expectStr, gotStr,
                        "因子项目分配与 18 A14 不符"));
                    broken = true;
                }
            }
            for (String factorCode : actual.keySet()) {
                if (!A14.containsKey(factorCode)) {
                    errors.add(new PublishError("factor." + factorCode, "非 A14 因子", "已配置",
                        "存在 18 A14 未登记的因子归属"));
                    broken = true;
                }
            }
        }

        // 规则区间左闭右开且 min < max；level_code 属值域（09 §2.5、§7）
        if (rules != null) {
            for (ScaleRule rule : rules) {
                if (rule.getMinValue() != null && rule.getMaxValue() != null
                    && rule.getMinValue().compareTo(rule.getMaxValue()) >= 0) {
                    errors.add(new PublishError("rule." + rule.getRuleType() + "." + rule.getFactorCode(),
                        "min < max", rule.getMinValue() + " >= " + rule.getMaxValue(), "规则区间必须左闭右开"));
                    broken = true;
                }
                if (rule.getLevelCode() == null || !LEVEL_CODES.contains(rule.getLevelCode())) {
                    errors.add(new PublishError("rule.levelCode", LEVEL_CODES.toString(),
                        String.valueOf(rule.getLevelCode()), "规则等级码非法"));
                    broken = true;
                }
            }
        }
        return broken;
    }

    /**
     * 因子常模校验：心理量表因子 norm_mean/norm_sd 齐备且 &gt; 0、item_count &gt; 0（7005）
     */
    private static boolean validateFactors(Questionnaire q, List<ScaleFactor> factors, List<PublishError> errors) {
        if (factors == null) {
            return false;
        }
        boolean broken = false;
        boolean psych = Questionnaire.SCALE_SCL90.equals(q.getScaleType());
        for (ScaleFactor f : factors) {
            if (f.getItemCount() == null || f.getItemCount() <= 0) {
                errors.add(new PublishError("factor." + f.getCode() + ".itemCount", "> 0",
                    String.valueOf(f.getItemCount()), "因子条目数必须大于 0"));
                broken = true;
            }
            if (!psych) {
                continue;
            }
            if (f.getExtraFlag() != null && f.getExtraFlag() == 1) {
                continue;
            }
            boolean hasMean = f.getNormMean() != null && f.getNormMean().compareTo(BigDecimal.ZERO) > 0;
            boolean hasSd = f.getNormSd() != null && f.getNormSd().compareTo(BigDecimal.ZERO) > 0;
            if (!hasMean || !hasSd) {
                errors.add(new PublishError("factor." + f.getCode() + ".norm",
                    "norm_mean 与 norm_sd 均 > 0", "缺失或越界", "心理量表因子常模必须齐备"));
                broken = true;
            }
        }
        return broken;
    }

    /**
     * 配置校验：必填键齐备、数值可解析（7006，09 §2.6）
     */
    private static boolean validateConfigs(Questionnaire q, List<ScaleConfig> configs, List<PublishError> errors) {
        boolean psych = Questionnaire.SCALE_SCL90.equals(q.getScaleType());
        boolean tcm = Questionnaire.SCALE_TCM9.equals(q.getScaleType());
        if (!psych && !tcm) {
            return false;
        }
        Map<String, String> present = new LinkedHashMap<>();
        if (configs != null) {
            for (ScaleConfig config : configs) {
                present.put(config.getConfigKey(), config.getConfigValue());
            }
        }
        String[] required = psych ? ScaleConfigKeys.SCL90_REQUIRED_KEYS : ScaleConfigKeys.TCM_REQUIRED_KEYS;
        boolean broken = false;
        for (String key : required) {
            String value = present.get(key);
            if (value == null || value.isBlank()) {
                errors.add(new PublishError("config." + key, "必填", "缺失", "阈值配置键缺失"));
                broken = true;
                continue;
            }
            try {
                new BigDecimal(value.trim());
            } catch (NumberFormatException e) {
                errors.add(new PublishError("config." + key, "可解析数值", value, "阈值配置值无法解析"));
                broken = true;
            }
        }
        return broken;
    }

    /**
     * 发布校验结果（错误清单形态同 20 §6 逐行标错）
     */
    @Data
    public static class PublishResult {
        private boolean success;
        private Integer errorCode;
        private List<PublishError> errors = new ArrayList<>();
    }

    /**
     * 逐项错误
     */
    @Data
    public static class PublishError {
        private String item;
        private String expect;
        private String actual;
        private String message;

        public PublishError(String item, String expect, String actual, String message) {
            this.item = item;
            this.expect = expect;
            this.actual = actual;
            this.message = message;
        }
    }

}
