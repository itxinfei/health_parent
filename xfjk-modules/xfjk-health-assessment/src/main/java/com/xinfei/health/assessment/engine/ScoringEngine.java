package com.xinfei.health.assessment.engine;

import cn.hutool.json.JSONUtil;
import com.xinfei.health.assessment.constant.ScaleConfigKeys;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用算分引擎（09 §3、§4）。
 *
 * <p>纯函数实现，不依赖数据库：题号/阈值/记分法全部由入参数据与配置驱动，
 * 代码内不出现任何题号数组与阈值字面量（A13/A14 同源约束）。
 *
 * @author xinfei
 */
public final class ScoringEngine {

    /** 因子分保留 3 位（09 §3.4） */
    private static final int FACTOR_SCALE = 3;
    /** 总分/均分/转化分保留 2 位（09 §3.4） */
    private static final int SCORE_SCALE = 2;

    /** 中医体质平和质因子码（09 §2.4 码表） */
    private static final String TCM_PINGHE = "pinghe";

    /** 等级码（09 §2.5 值域） */
    public static final String LEVEL_YES = "yes";
    public static final String LEVEL_BASIC = "basic";
    public static final String LEVEL_NO = "no";
    public static final String LEVEL_TENDENCY = "tendency";

    private ScoringEngine() {
    }

    /**
     * 全流程算分入口，按 scoring_type 分派（09 §3.2）。
     */
    public static ScoringResult score(ScoringContext ctx) {
        String scoringType = ctx.questionnaire.getScoringType();
        if (Questionnaire.SCORING_FACTOR.equals(scoringType)) {
            return scoreFactor(ctx);
        }
        if (Questionnaire.SCORING_SCALE.equals(scoringType)) {
            return scoreTcm(ctx);
        }
        return scoreSum(ctx);
    }

    /**
     * 反向计分：score = (answer_max_score + answer_min_score) − raw_score（09 §3.3）
     */
    public static BigDecimal reverse(BigDecimal raw, BigDecimal min, BigDecimal max, Integer reverseFlag) {
        if (reverseFlag == null || reverseFlag != 1) {
            return raw;
        }
        return max.add(min).subtract(raw);
    }

    /**
     * 规则区间命中：左闭右开 [min,max)，max 为 NULL 视为无上界；同 sort 取第一条（09 §2.5）
     */
    public static ScaleRule matchRule(List<ScaleRule> rules, String ruleType, String factorCode, BigDecimal value) {
        if (rules == null || value == null) {
            return null;
        }
        return rules.stream()
            .filter(r -> ruleType.equals(r.getRuleType()))
            .filter(r -> factorCode == null ? r.getFactorCode() == null : factorCode.equals(r.getFactorCode()))
            .filter(r -> r.getMinValue() == null || value.compareTo(r.getMinValue()) >= 0)
            .filter(r -> r.getMaxValue() == null || value.compareTo(r.getMaxValue()) < 0)
            .min(Comparator.comparing(r -> r.getSort() == null ? 0 : r.getSort()))
            .orElse(null);
    }

    // ---------- sum：普通问卷/风险测评（09 §4.3） ----------

    private static ScoringResult scoreSum(ScoringContext ctx) {
        ScoringResult result = new ScoringResult();
        result.answerCount = ctx.inputs.size();
        result.totalScore = ctx.inputs.stream()
            .map(ScoringInput::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(SCORE_SCALE, RoundingMode.HALF_UP);
        ScaleRule totalRule = matchRule(ctx.rules, ScaleRule.RULE_TOTAL_LEVEL, null, result.totalScore);
        if (totalRule != null) {
            result.level = totalRule.getLevelCode();
            result.conclusion = totalRule.getConclusion();
        }
        return result;
    }

    // ---------- factor：SCL-90（09 §4.1，18 A8~A15） ----------

    private static ScoringResult scoreFactor(ScoringContext ctx) {
        ScoringResult result = new ScoringResult();
        result.answerCount = ctx.inputs.size();
        Map<String, String> cfg = ctx.config;

        // 步骤 3/4：总分与总均分（A15）
        result.totalScore = ctx.inputs.stream()
            .map(ScoringInput::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(SCORE_SCALE, RoundingMode.HALF_UP);
        BigDecimal avgDivisor = decimal(cfg, ScaleConfigKeys.SCL90_TOTAL_AVG_DIVISOR);
        result.avgScore = divide(result.totalScore, avgDivisor, SCORE_SCALE);

        // 步骤 5：阳性项目数（A15，配置 scl90.positive.item_ge）
        BigDecimal itemGe = decimal(cfg, ScaleConfigKeys.SCL90_POSITIVE_ITEM_GE);
        result.positiveCount = (int) ctx.inputs.stream()
            .filter(i -> i.getScore().compareTo(itemGe) >= 0)
            .count();

        // 步骤 6/7：因子原始分与因子分（A8/A14，归属由 question.factor_code 数据驱动）
        Map<String, List<ScoringInput>> byFactor = new LinkedHashMap<>();
        for (ScoringInput input : ctx.inputs) {
            byFactor.computeIfAbsent(input.getFactorCode(), k -> new ArrayList<>()).add(input);
        }
        List<FactorLine> lines = new ArrayList<>();
        for (ScaleFactor factor : ctx.factors) {
            List<ScoringInput> items = byFactor.getOrDefault(factor.getCode(), List.of());
            BigDecimal rawSum = items.stream().map(ScoringInput::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            FactorLine line = new FactorLine();
            line.factorCode = factor.getCode();
            line.factorName = factor.getName();
            line.itemCount = items.size();
            line.rawSum = rawSum.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
            if (!items.isEmpty()) {
                line.factorScore = divide(rawSum, BigDecimal.valueOf(items.size()), FACTOR_SCALE);
            }
            // 步骤 8：超常模标记（A11 + 09 §4.1 定稿 upper = norm_mean + norm_sd；additional 无常模不参与）
            if (factor.getNormMean() != null && factor.getNormSd() != null && line.factorScore != null) {
                line.normUpper = factor.getNormMean().add(factor.getNormSd()).setScale(4, RoundingMode.HALF_UP);
                line.normExceedFlag = line.factorScore.compareTo(line.normUpper) > 0 ? 1 : 0;
            }
            // 因子分级规则命中（§2.9 level_code/conclusion）
            ScaleRule factorRule = matchRule(ctx.rules, ScaleRule.RULE_FACTOR_LEVEL, factor.getCode(), line.factorScore);
            if (factorRule != null) {
                line.levelCode = factorRule.getLevelCode();
                line.conclusion = factorRule.getConclusion();
            }
            line.sort = factor.getSort();
            lines.add(line);
        }
        result.factorLines = lines;

        // 步骤 9：筛查阳性（A10，三条件任一，均为严格大于）
        boolean exceedTotal = result.totalScore.compareTo(decimal(cfg, ScaleConfigKeys.SCL90_SCREEN_TOTAL_GT)) > 0;
        boolean exceedPositiveCount = BigDecimal.valueOf(result.positiveCount)
            .compareTo(decimal(cfg, ScaleConfigKeys.SCL90_SCREEN_POSITIVE_COUNT_GT)) > 0;
        BigDecimal factorGt = decimal(cfg, ScaleConfigKeys.SCL90_SCREEN_FACTOR_GT);
        boolean exceedFactor = lines.stream()
            .anyMatch(l -> l.factorScore != null && l.factorScore.compareTo(factorGt) > 0);
        result.positiveFlag = (exceedTotal || exceedPositiveCount || exceedFactor) ? 1 : 0;

        // 步骤 10：心理严重度分级（09 §4.1.2，A12，自上而下短路）
        result.severity = severityLevel(lines, result.avgScore);
        result.level = result.severity;

        // 步骤 11：总分分级规则命中（§2.5 total_level）
        ScaleRule totalRule = matchRule(ctx.rules, ScaleRule.RULE_TOTAL_LEVEL, null, result.totalScore);
        if (totalRule != null) {
            result.level = totalRule.getLevelCode();
            if (result.conclusion == null) {
                result.conclusion = totalRule.getConclusion();
            }
        }
        return result;
    }

    /**
     * 心理严重度分级（09 §4.1.2，A12 定稿算法，非 A13 配置阈值）
     */
    private static String severityLevel(List<FactorLine> lines, BigDecimal avgScore) {
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (FactorLine line : lines) {
            BigDecimal fs = line.factorScore;
            if (fs == null) {
                continue;
            }
            if (fs.compareTo(BigDecimal.valueOf(4)) >= 0) {
                n4++;
            } else if (fs.compareTo(BigDecimal.valueOf(3)) >= 0) {
                n3++;
            } else if (fs.compareTo(BigDecimal.valueOf(2)) >= 0) {
                n2++;
            }
        }
        if (n4 >= 1 || n3 >= 3) {
            return "severe";
        }
        if ((n3 >= 1 && n3 <= 2) || n2 >= 3) {
            return "moderate";
        }
        if (n2 >= 1 && n2 <= 2) {
            return "mild";
        }
        if (avgScore != null && avgScore.compareTo(BigDecimal.valueOf(1.5)) >= 0
            && avgScore.compareTo(BigDecimal.valueOf(2)) < 0) {
            return "sub_health";
        }
        return "normal";
    }

    // ---------- scale：中医体质（09 §4.2，18 A1~A7） ----------

    private static ScoringResult scoreTcm(ScoringContext ctx) {
        ScoringResult result = new ScoringResult();
        result.answerCount = ctx.inputs.size();
        Map<String, String> cfg = ctx.config;
        BigDecimal levels = decimal(cfg, ScaleConfigKeys.TCM_SCALE_LEVELS);
        BigDecimal levelMinusOne = levels.subtract(BigDecimal.ONE);

        // 每亚量表：先反向再求和（§3.3），转化分 C_k = (raw − n) / (n × (levels−1)) × 100（A3）
        Map<String, List<ScoringInput>> byFactor = new LinkedHashMap<>();
        for (ScoringInput input : ctx.inputs) {
            byFactor.computeIfAbsent(input.getFactorCode(), k -> new ArrayList<>()).add(input);
        }
        Map<String, BigDecimal> convert = new LinkedHashMap<>();
        List<FactorLine> lines = new ArrayList<>();
        for (ScaleFactor factor : ctx.factors) {
            List<ScoringInput> items = byFactor.getOrDefault(factor.getCode(), List.of());
            BigDecimal rawSum = items.stream().map(ScoringInput::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            FactorLine line = new FactorLine();
            line.factorCode = factor.getCode();
            line.factorName = factor.getName();
            line.itemCount = items.size();
            line.rawSum = rawSum.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
            if (!items.isEmpty()) {
                BigDecimal n = BigDecimal.valueOf(items.size());
                BigDecimal c = rawSum.subtract(n)
                    .divide(n.multiply(levelMinusOne), 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(SCORE_SCALE, RoundingMode.HALF_UP);
                line.convertScore = c;
                convert.put(factor.getCode(), c);
            }
            line.sort = factor.getSort();
            lines.add(line);
        }
        result.factorLines = lines;

        BigDecimal deviateIsGe = decimal(cfg, ScaleConfigKeys.TCM_DEVIATE_IS_GE);
        BigDecimal deviateTendencyGe = decimal(cfg, ScaleConfigKeys.TCM_DEVIATE_TENDENCY_GE);

        // 步骤 1：对 8 种偏颇逐一分档（A5），先于平和判定（§4.2 顺序不可交换）
        Map<String, String> deviateLevel = new LinkedHashMap<>();
        for (FactorLine line : lines) {
            if (TCM_PINGHE.equals(line.factorCode) || line.convertScore == null) {
                continue;
            }
            if (line.convertScore.compareTo(deviateIsGe) >= 0) {
                deviateLevel.put(line.factorCode, LEVEL_YES);
            } else if (line.convertScore.compareTo(deviateTendencyGe) >= 0) {
                deviateLevel.put(line.factorCode, LEVEL_TENDENCY);
            } else {
                deviateLevel.put(line.factorCode, LEVEL_NO);
            }
        }

        // 步骤 2：平和质判定（A4）
        BigDecimal peaceIsGe = decimal(cfg, ScaleConfigKeys.TCM_PEACE_IS_GE);
        BigDecimal peaceAllLt = decimal(cfg, ScaleConfigKeys.TCM_PEACE_ALL_LT);
        BigDecimal peaceBasicLt = decimal(cfg, ScaleConfigKeys.TCM_PEACE_BASIC_LT);
        BigDecimal peaceScore = convert.get(TCM_PINGHE);
        boolean allDeviateLt30 = lines.stream()
            .filter(l -> !TCM_PINGHE.equals(l.factorCode) && l.convertScore != null)
            .allMatch(l -> l.convertScore.compareTo(peaceAllLt) < 0);
        boolean allDeviateLt40 = lines.stream()
            .filter(l -> !TCM_PINGHE.equals(l.factorCode) && l.convertScore != null)
            .allMatch(l -> l.convertScore.compareTo(peaceBasicLt) < 0);
        if (peaceScore != null && peaceScore.compareTo(peaceIsGe) >= 0 && allDeviateLt30) {
            result.tcmPeaceLevel = LEVEL_YES;
        } else if (peaceScore != null && peaceScore.compareTo(peaceIsGe) >= 0 && allDeviateLt40) {
            result.tcmPeaceLevel = LEVEL_BASIC;
        } else {
            result.tcmPeaceLevel = LEVEL_NO;
        }

        // 步骤 4：主导体质（§4.2）
        FactorLine topDeviate = lines.stream()
            .filter(l -> !TCM_PINGHE.equals(l.factorCode) && l.convertScore != null)
            .max(Comparator.comparing(FactorLine::getConvertScore))
            .orElse(null);
        if (LEVEL_NO.equals(result.tcmPeaceLevel) || topDeviate == null
            || topDeviate.getConvertScore().compareTo(peaceAllLt) >= 0) {
            // 主体质为偏颇（存在 ≥30 的偏颇，或平和质不成立）
            if (topDeviate != null) {
                result.tcmPrimary = topDeviate.factorCode;
                result.tcmLevel = deviateLevel.get(topDeviate.factorCode);
            }
        } else {
            // 8 种偏颇全 < 30 且平和质为是/基本是 → 主体质为平和质
            result.tcmPrimary = TCM_PINGHE;
            result.tcmLevel = result.tcmPeaceLevel;
        }

        // 步骤 3：兼夹倾向（A6）——计数含主导、清单不含主导（09 §4.2 定稿语义）
        BigDecimal combineScoreGe = decimal(cfg, ScaleConfigKeys.TCM_COMBINE_SCORE_GE);
        int combineCountGe = decimal(cfg, ScaleConfigKeys.TCM_COMBINE_COUNT_GE).intValue();
        List<String> aboveThreshold = lines.stream()
            .filter(l -> !TCM_PINGHE.equals(l.factorCode) && l.convertScore != null
                && l.convertScore.compareTo(combineScoreGe) >= 0)
            .map(l -> l.factorCode)
            .toList();
        if (aboveThreshold.size() >= combineCountGe) {
            List<String> combineList = aboveThreshold.stream()
                .filter(code -> !code.equals(result.tcmPrimary))
                .toList();
            result.tcmCombineJson = JSONUtil.toJsonStr(combineList);
        }

        // 因子级 tcm_convert 规则命中（§4.2 步骤 5：结论文案来源）
        for (FactorLine line : lines) {
            if (line.convertScore == null) {
                continue;
            }
            ScaleRule rule = matchRule(ctx.rules, ScaleRule.RULE_TCM_CONVERT, line.factorCode, line.convertScore);
            if (rule != null) {
                line.levelCode = rule.getLevelCode();
                line.conclusion = rule.getConclusion();
                if (line.factorCode.equals(result.tcmPrimary)) {
                    result.conclusion = rule.getConclusion();
                }
            }
        }
        return result;
    }

    private static BigDecimal decimal(Map<String, String> cfg, String key) {
        return new BigDecimal(cfg.get(key));
    }

    private static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
        return dividend.divide(divisor, scale, RoundingMode.HALF_UP);
    }

    // ---------- 入参/结果模型 ----------

    /**
     * 算分上下文（全部由数据驱动）
     */
    @Data
    public static class ScoringContext {
        private Questionnaire questionnaire;
        /** 逐题输入（含反向映射后的计分分） */
        private List<ScoringInput> inputs;
        private List<ScaleFactor> factors;
        private List<ScaleRule> rules;
        /** 已按三级优先级解析的配置（键 → 值） */
        private Map<String, String> config;
    }

    /**
     * 单题计分输入
     */
    @Data
    public static class ScoringInput {
        private Long questionId;
        private String factorCode;
        private BigDecimal score;
    }

    /**
     * 算分结果
     */
    @Data
    public static class ScoringResult {
        private Integer answerCount;
        private BigDecimal totalScore;
        private BigDecimal avgScore;
        private Integer positiveCount;
        private Integer positiveFlag;
        private String severity;
        private String level;
        private String conclusion;
        private List<FactorLine> factorLines = new ArrayList<>();
        /** 平和质判定 yes/basic/no（A4） */
        private String tcmPeaceLevel;
        private String tcmPrimary;
        private String tcmLevel;
        private String tcmCombineJson;
    }

    /**
     * 因子得分行（09 §2.9）
     */
    @Data
    public static class FactorLine {
        private String factorCode;
        private String factorName;
        private Integer itemCount;
        private BigDecimal rawSum;
        private BigDecimal factorScore;
        private BigDecimal convertScore;
        private String levelCode;
        private String conclusion;
        private BigDecimal normUpper;
        private Integer normExceedFlag;
        private Integer sort;
    }

}
