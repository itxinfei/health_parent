package com.xinfei.health.assessment.engine;

import com.xinfei.health.assessment.constant.ScaleConfigKeys;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 算分引擎单元测试（09 §10 验收：转化分、TC-A7-1/2、反向计分、严重度六组、阳性严格大于）
 *
 * @author xinfei
 */
@Tag("dev")
class ScoringEngineTest {

    // ---------- 反向计分（18 A2） ----------

    @Test
    @DisplayName("反向计分：1→5、2→4、3→3；非反向条目原样")
    void testReverse() {
        assertEquals(0, ScoringEngine.reverse(new BigDecimal("1"), BigDecimal.ONE, new BigDecimal("5"), 1)
            .compareTo(new BigDecimal("5")));
        assertEquals(0, ScoringEngine.reverse(new BigDecimal("2"), BigDecimal.ONE, new BigDecimal("5"), 1)
            .compareTo(new BigDecimal("4")));
        assertEquals(0, ScoringEngine.reverse(new BigDecimal("3"), BigDecimal.ONE, new BigDecimal("5"), 1)
            .compareTo(new BigDecimal("3")));
        assertEquals(0, ScoringEngine.reverse(new BigDecimal("3"), BigDecimal.ONE, new BigDecimal("5"), 0)
            .compareTo(new BigDecimal("3")));
        assertEquals(0, ScoringEngine.reverse(new BigDecimal("3"), BigDecimal.ONE, new BigDecimal("5"), null)
            .compareTo(new BigDecimal("3")));
    }

    // ---------- 规则区间左闭右开（09 §2.5） ----------

    @Test
    @DisplayName("规则区间左闭右开，max 为 null 无上界，同 sort 取第一条")
    void testMatchRule() {
        ScaleRule r1 = rule(ScaleRule.RULE_FACTOR_LEVEL, "somatization", "2", "3", "mild", 1);
        ScaleRule r2 = rule(ScaleRule.RULE_FACTOR_LEVEL, "somatization", "3", "4", "moderate", 1);
        assertEquals("mild", ScoringEngine.matchRule(List.of(r1, r2), ScaleRule.RULE_FACTOR_LEVEL,
            "somatization", new BigDecimal("2")).getLevelCode());
        assertEquals("moderate", ScoringEngine.matchRule(List.of(r1, r2), ScaleRule.RULE_FACTOR_LEVEL,
            "somatization", new BigDecimal("3")).getLevelCode());
        assertEquals("mild", ScoringEngine.matchRule(List.of(r1, r2), ScaleRule.RULE_FACTOR_LEVEL,
            "somatization", new BigDecimal("2.99")).getLevelCode());
        ScaleRule noMax = rule(ScaleRule.RULE_FACTOR_LEVEL, "somatization", "2", null, "mild", 1);
        assertEquals("mild", ScoringEngine.matchRule(List.of(noMax), ScaleRule.RULE_FACTOR_LEVEL,
            "somatization", new BigDecimal("99")).getLevelCode());
    }

    // ---------- 中医体质转化分（18 A3） ----------

    @Test
    @DisplayName("转化分：8 条目原始分 34 → 81.25")
    void testConvertScore8Items() {
        Questionnaire q = tcmQuestionnaire();
        List<ScaleFactor> factors = tcmFactors(8);
        List<ScoringEngine.ScoringInput> inputs = new ArrayList<>(tcmInputs("qixu", 8, 34));
        for (ScaleFactor f : factors) {
            if (!"qixu".equals(f.getCode())) {
                inputs.addAll(tcmInputs(f.getCode(), 8, 8));
            }
        }
        ScoringEngine.ScoringResult result = ScoringEngine.score(ctx(q, inputs, factors, List.of(), tcmConfig()));
        assertEquals(0, convertOf(result, "qixu").compareTo(new BigDecimal("81.25")));
    }

    @Test
    @DisplayName("转化分：7 条目原始分 22 → 53.57")
    void testConvertScore7Items() {
        Questionnaire q = tcmQuestionnaire();
        ScaleFactor f = tcmFactor("qixu", 7);
        ScoringEngine.ScoringResult result = ScoringEngine.score(
            ctx(q, tcmInputs("qixu", 7, 22), List.of(f), List.of(), tcmConfig()));
        assertEquals(0, convertOf(result, "qixu").compareTo(new BigDecimal("53.57")));
    }

    // ---------- TC-A7-1 / TC-A7-2（18 A7 标准算例，不可改数） ----------

    @Test
    @DisplayName("TC-A7-1：平和 75 / 气虚 56 / 痰湿 12 / 余 <30 → 是气虚质，平和质否")
    void testTcA7_1() {
        ScoringEngine.ScoringResult result = scoreTcm(32, 26, 12, 8);
        assertEquals("qixu", result.getTcmPrimary());
        assertEquals("yes", result.getTcmLevel());
        assertEquals("no", result.getTcmPeaceLevel());
    }

    @Test
    @DisplayName("TC-A7-2：平和 75 / 痰湿 32 / 余 <30 → 平和质基本是，有痰湿质倾向")
    void testTcA7_2() {
        ScoringEngine.ScoringResult result = scoreTcm(32, 8, 18, 8);
        assertEquals("basic", result.getTcmPeaceLevel());
        assertEquals("tanshi", result.getTcmPrimary());
        assertEquals("tendency", result.getTcmLevel());
        assertTrue(result.getConclusion() != null && result.getConclusion().contains("痰湿"),
            "结论应含痰湿倾向文案，实际：" + result.getConclusion());
    }

    /**
     * 构造 9 亚量表作答并算分；平和/气虚/痰湿原始分可调，其余 6 偏颇全给 1 分（raw=8）
     */
    private ScoringEngine.ScoringResult scoreTcm(int pingheRaw, int qixuRaw, int tanshiRaw, int otherRaw) {
        Questionnaire q = tcmQuestionnaire();
        List<ScaleFactor> factors = tcmFactors(8);
        List<ScoringEngine.ScoringInput> inputs = new ArrayList<>(tcmInputs("pinghe", 8, pingheRaw));
        inputs.addAll(tcmInputs("qixu", 8, qixuRaw));
        inputs.addAll(tcmInputs("tanshi", 8, tanshiRaw));
        for (String code : List.of("yangxu", "yinxu", "shire", "xueyu", "qiyu", "tebing")) {
            inputs.addAll(tcmInputs(code, 8, otherRaw));
        }
        List<ScaleRule> rules = List.of(
            rule(ScaleRule.RULE_TCM_CONVERT, "tanshi", "30", "40", "tendency", "有痰湿质倾向", 1));
        return ScoringEngine.score(ctx(q, inputs, factors, rules, tcmConfig()));
    }

    // ---------- 心理严重度六组（09 §4.1.2，A12 最高档短路） ----------

    @Test
    @DisplayName("严重度六组：n4=1/n3=3/n3=2/n2=3/n2=2/仅均分 1.6 → severe/severe/moderate/moderate/mild/sub_health")
    void testSeveritySixGroups() {
        // 组 1：n4=1（躯体化因子分 4.0）
        assertEquals("severe", severityOf("somatization", "4"));
        // 组 2：n3=3（躯体化/强迫/人际 3.0）
        assertEquals("severe", severityOf(List.of("somatization", "obsessive", "interpersonal"), "3"));
        // 组 3：n3=2
        assertEquals("moderate", severityOf(List.of("somatization", "obsessive"), "3"));
        // 组 4：n2=3
        assertEquals("moderate", severityOf(List.of("somatization", "obsessive", "interpersonal"), "2"));
        // 组 5：n2=2
        assertEquals("mild", severityOf(List.of("somatization", "obsessive"), "2"));
        // 组 6：全部因子分 1.6（总均分 1.6）且无 ≥2 因子 → 心理亚健康
        List<ScoringEngine.ScoringInput> inputs = flatScl90(scl90Factors(), "1.6");
        assertEquals("sub_health", scoreFactor(inputs).getSeverity());
        // 组 7：全部 1 分 → normal
        assertEquals("normal", scoreFactor(flatScl90(scl90Factors(), "1")).getSeverity());
    }

    /** 指定若干因子给满分值，其余因子 1 分 */
    private String severityOf(String factorCode, String score) {
        return severityOf(List.of(factorCode), score);
    }

    private String severityOf(List<String> factorCodes, String score) {
        List<ScoringEngine.ScoringInput> inputs = flatScl90(scl90Factors(), "1");
        for (ScoringEngine.ScoringInput in : inputs) {
            if (factorCodes.contains(in.getFactorCode())) {
                in.setScore(new BigDecimal(score));
            }
        }
        return scoreFactor(inputs).getSeverity();
    }

    private ScoringEngine.ScoringResult scoreFactor(List<ScoringEngine.ScoringInput> inputs) {
        return ScoringEngine.score(ctx(scl90Questionnaire(), inputs, scl90Factors(), List.of(), scl90Config()));
    }

    // ---------- 筛查阳性三条件严格大于（18 A10） ----------

    @Test
    @DisplayName("总分 160 不阳性、160.5 阳性（严格大于）")
    void testPositiveTotalStrictlyGreater() {
        List<ScoringEngine.ScoringInput> inputs = flatScl90(scl90Factors(), "1.75");
        // 每因子第 1 条改为 2 分（共 10 条），总分恰 160.0
        List<Integer> firstIndexes = List.of(0, 12, 22, 31, 44, 54, 60, 67, 73, 83);
        for (int idx : firstIndexes) {
            inputs.get(idx).setScore(new BigDecimal("2"));
        }
        assertEquals(0, inputs.stream().map(ScoringEngine.ScoringInput::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(new BigDecimal("160.0")));
        assertEquals(0, scoreFactor(inputs).getPositiveFlag());
        // 躯体化第 2 条 +0.5 → 总分 160.5 → 阳性
        inputs.get(1).setScore(new BigDecimal("2.25"));
        assertEquals(1, scoreFactor(inputs).getPositiveFlag());
    }

    @Test
    @DisplayName("阳性项目数 43 不阳性、44 阳性（严格大于）")
    void testPositiveCountStrictlyGreater() {
        assertEquals(0, scoreFactor(scl90WithTwos(43)).getPositiveFlag());
        assertEquals(1, scoreFactor(scl90WithTwos(44)).getPositiveFlag());
    }

    @Test
    @DisplayName("因子分 2.0 不阳性、2.05 阳性（严格大于）")
    void testPositiveFactorStrictlyGreater() {
        // 抑郁因子全 2.0
        List<ScoringEngine.ScoringInput> inputs = flatScl90(scl90Factors(), "1");
        for (int i = 31; i <= 43; i++) {
            inputs.get(i).setScore(new BigDecimal("2.0"));
        }
        assertEquals(0, scoreFactor(inputs).getPositiveFlag());
        // 抑郁因子全 2.05
        for (int i = 31; i <= 43; i++) {
            inputs.get(i).setScore(new BigDecimal("2.05"));
        }
        assertEquals(1, scoreFactor(inputs).getPositiveFlag());
    }

    // ---------- 构造工具 ----------

    private static Questionnaire scl90Questionnaire() {
        Questionnaire q = new Questionnaire();
        q.setCategory(Questionnaire.CATEGORY_SCALE);
        q.setScaleType(Questionnaire.SCALE_SCL90);
        q.setScoringType(Questionnaire.SCORING_FACTOR);
        q.setQuestionCount(90);
        return q;
    }

    private static Questionnaire tcmQuestionnaire() {
        Questionnaire q = new Questionnaire();
        q.setCategory(Questionnaire.CATEGORY_SCALE);
        q.setScaleType(Questionnaire.SCALE_TCM9);
        q.setScoringType(Questionnaire.SCORING_SCALE);
        q.setQuestionCount(60);
        return q;
    }

    private static Map<String, String> scl90Config() {
        Map<String, String> cfg = new LinkedHashMap<>();
        cfg.put(ScaleConfigKeys.SCL90_SCORING_METHOD, "1to5");
        cfg.put(ScaleConfigKeys.SCL90_TOTAL_MAX, "450");
        cfg.put(ScaleConfigKeys.SCL90_SCREEN_TOTAL_GT, "160");
        cfg.put(ScaleConfigKeys.SCL90_SCREEN_POSITIVE_COUNT_GT, "43");
        cfg.put(ScaleConfigKeys.SCL90_SCREEN_FACTOR_GT, "2");
        cfg.put(ScaleConfigKeys.SCL90_POSITIVE_ITEM_GE, "2");
        cfg.put(ScaleConfigKeys.SCL90_TOTAL_AVG_DIVISOR, "90");
        cfg.put(ScaleConfigKeys.SCL90_NORM_EXCEED, "upper");
        return cfg;
    }

    private static Map<String, String> tcmConfig() {
        Map<String, String> cfg = new LinkedHashMap<>();
        cfg.put(ScaleConfigKeys.TCM_ITEMS_PER_SUBSCALE, "7,8");
        cfg.put(ScaleConfigKeys.TCM_SCALE_LEVELS, "5");
        cfg.put(ScaleConfigKeys.TCM_PEACE_IS_GE, "60");
        cfg.put(ScaleConfigKeys.TCM_PEACE_ALL_LT, "30");
        cfg.put(ScaleConfigKeys.TCM_PEACE_BASIC_LT, "40");
        cfg.put(ScaleConfigKeys.TCM_DEVIATE_IS_GE, "40");
        cfg.put(ScaleConfigKeys.TCM_DEVIATE_TENDENCY_GE, "30");
        cfg.put(ScaleConfigKeys.TCM_COMBINE_COUNT_GE, "2");
        cfg.put(ScaleConfigKeys.TCM_COMBINE_SCORE_GE, "30");
        return cfg;
    }

    /** SCL-90 十因子（18 A11 常模；additional 附加因子无常模） */
    private static List<ScaleFactor> scl90Factors() {
        List<ScaleFactor> factors = new ArrayList<>();
        factors.add(scl90Factor("somatization", 12, "1.37", "0.48"));
        factors.add(scl90Factor("obsessive", 10, "1.62", "0.58"));
        factors.add(scl90Factor("interpersonal", 9, "1.65", "0.61"));
        factors.add(scl90Factor("depression", 13, "1.50", "0.59"));
        factors.add(scl90Factor("anxiety", 10, "1.39", "0.43"));
        factors.add(scl90Factor("hostility", 6, "1.46", "0.55"));
        factors.add(scl90Factor("phobic", 7, "1.23", "0.41"));
        factors.add(scl90Factor("paranoid", 6, "1.43", "0.57"));
        factors.add(scl90Factor("psychoticism", 10, "1.29", "0.42"));
        factors.add(scl90Factor("additional", 7, null, null));
        factors.get(9).setExtraFlag(1);
        return factors;
    }

    private static ScaleFactor scl90Factor(String code, int itemCount, String mean, String sd) {
        ScaleFactor f = new ScaleFactor();
        f.setCode(code);
        f.setItemCount(itemCount);
        if (mean != null) {
            f.setNormMean(new BigDecimal(mean));
        }
        if (sd != null) {
            f.setNormSd(new BigDecimal(sd));
        }
        return f;
    }

    private static List<ScaleFactor> tcmFactors(int itemCount) {
        List<ScaleFactor> factors = new ArrayList<>();
        for (String code : List.of("pinghe", "qixu", "yangxu", "yinxu", "tanshi", "shire",
            "xueyu", "qiyu", "tebing")) {
            factors.add(tcmFactor(code, itemCount));
        }
        return factors;
    }

    private static ScaleFactor tcmFactor(String code, int itemCount) {
        ScaleFactor f = new ScaleFactor();
        f.setCode(code);
        f.setItemCount(itemCount);
        return f;
    }

    /** 生成 n 条 1~5 整数分且总和恰为 raw 的作答 */
    private static List<ScoringEngine.ScoringInput> tcmInputs(String factorCode, int n, int raw) {
        List<ScoringEngine.ScoringInput> list = new ArrayList<>();
        int remaining = raw - n;
        for (int i = 0; i < n; i++) {
            int add = Math.min(4, remaining);
            remaining -= add;
            list.add(input(factorCode, BigDecimal.valueOf(1 + add)));
        }
        return list;
    }

    private static ScoringEngine.ScoringInput input(String factorCode, BigDecimal score) {
        ScoringEngine.ScoringInput in = new ScoringEngine.ScoringInput();
        in.setFactorCode(factorCode);
        in.setScore(score);
        return in;
    }

    /** 每因子 itemCount 条同分作答 */
    private static List<ScoringEngine.ScoringInput> flatScl90(List<ScaleFactor> factors, String score) {
        List<ScoringEngine.ScoringInput> inputs = new ArrayList<>();
        for (ScaleFactor f : factors) {
            for (int i = 0; i < f.getItemCount(); i++) {
                inputs.add(input(f.getCode(), new BigDecimal(score)));
            }
        }
        return inputs;
    }

    /** 90 条 1 分作答，自前向后把 twos 条改为 2 分 */
    private static List<ScoringEngine.ScoringInput> scl90WithTwos(int twos) {
        List<ScoringEngine.ScoringInput> inputs = flatScl90(scl90Factors(), "1");
        for (int i = 0; i < twos; i++) {
            inputs.get(i).setScore(new BigDecimal("2"));
        }
        return inputs;
    }

    private static ScaleRule rule(String ruleType, String factorCode, String min, String max,
                                  String levelCode, int sort) {
        return rule(ruleType, factorCode, min, max, levelCode, null, sort);
    }

    private static ScaleRule rule(String ruleType, String factorCode, String min, String max,
                                  String levelCode, String conclusion, int sort) {
        ScaleRule r = new ScaleRule();
        r.setRuleType(ruleType);
        r.setFactorCode(factorCode);
        r.setMinValue(min == null ? null : new BigDecimal(min));
        r.setMaxValue(max == null ? null : new BigDecimal(max));
        r.setLevelCode(levelCode);
        r.setConclusion(conclusion);
        r.setSort(sort);
        return r;
    }

    private static ScoringEngine.ScoringContext ctx(Questionnaire q, List<ScoringEngine.ScoringInput> inputs,
                                                    List<ScaleFactor> factors, List<ScaleRule> rules,
                                                    Map<String, String> config) {
        ScoringEngine.ScoringContext ctx = new ScoringEngine.ScoringContext();
        ctx.setQuestionnaire(q);
        ctx.setInputs(inputs);
        ctx.setFactors(factors);
        ctx.setRules(rules);
        ctx.setConfig(config);
        return ctx;
    }

    private static BigDecimal convertOf(ScoringEngine.ScoringResult result, String factorCode) {
        return result.getFactorLines().stream()
            .filter(l -> factorCode.equals(l.getFactorCode()))
            .findFirst().orElseThrow().getConvertScore();
    }

}
