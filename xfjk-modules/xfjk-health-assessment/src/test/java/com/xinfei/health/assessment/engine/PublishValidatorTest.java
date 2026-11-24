package com.xinfei.health.assessment.engine;

import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.constant.ScaleConfigKeys;
import com.xinfei.health.assessment.domain.Question;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.ScaleConfig;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 发布校验单元测试（09 §5.2 六项校验、§10 验收：7004/7005/7006 坏数据）
 *
 * @author xinfei
 */
@Tag("dev")
class PublishValidatorTest {

    /** 18 A14 因子项目分配（仅测试数据来源，与 PublishValidator 内置表一致） */
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

    @Test
    @DisplayName("合法 SCL-90 问卷：六项校验全过")
    void testValidScl90Passes() {
        Questionnaire q = scl90Questionnaire();
        PublishValidator.PublishResult result = PublishValidator.validate(
            q, questions90(), factors(), rules(), configs());
        assertTrue(result.isSuccess(), "合法问卷应通过：" + result.getErrors());
    }

    @Test
    @DisplayName("7004 结构：题目数 89 ≠ 90")
    void testStructureQuestionCount() {
        List<Question> questions = questions90();
        questions.remove(0);
        PublishValidator.PublishResult result = PublishValidator.validate(
            scl90Questionnaire(), questions, factors(), rules(), configs());
        assertFalse(result.isSuccess());
        assertEquals(AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID, result.getErrorCode());
    }

    @Test
    @DisplayName("7004 结构：A14 因子归属错配（躯体化多一题、强迫少一题）")
    void testStructureA14Mismatch() {
        List<Question> questions = questions90();
        // 把 58 题（躯体化）的因子码改为 obsessive → 躯体化少一、强迫多一
        questions.stream().filter(q -> q.getItemNo() == 58)
            .findFirst().orElseThrow().setFactorCode("obsessive");
        PublishValidator.PublishResult result = PublishValidator.validate(
            scl90Questionnaire(), questions, factors(), rules(), configs());
        assertFalse(result.isSuccess());
        assertEquals(AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID, result.getErrorCode());
    }

    @Test
    @DisplayName("7004 结构：规则区间 min ≥ max")
    void testStructureRuleInterval() {
        List<ScaleRule> rules = rules();
        rules.get(0).setMinValue(new BigDecimal("3"));
        rules.get(0).setMaxValue(new BigDecimal("3"));
        PublishValidator.PublishResult result = PublishValidator.validate(
            scl90Questionnaire(), questions90(), factors(), rules, configs());
        assertFalse(result.isSuccess());
        assertEquals(AssessmentErrorCode.QUESTIONNAIRE_STRUCTURE_INVALID, result.getErrorCode());
    }

    @Test
    @DisplayName("7005 常模：躯体化 norm_mean 缺失")
    void testFactorNormMissing() {
        List<ScaleFactor> factors = factors();
        factors.get(0).setNormMean(null);
        PublishValidator.PublishResult result = PublishValidator.validate(
            scl90Questionnaire(), questions90(), factors, rules(), configs());
        assertFalse(result.isSuccess());
        assertEquals(AssessmentErrorCode.FACTOR_CONFIG_INVALID, result.getErrorCode());
    }

    @Test
    @DisplayName("7006 配置：缺 scl90.screen.total_gt")
    void testConfigMissing() {
        List<ScaleConfig> configs = configs();
        configs.removeIf(c -> ScaleConfigKeys.SCL90_SCREEN_TOTAL_GT.equals(c.getConfigKey()));
        PublishValidator.PublishResult result = PublishValidator.validate(
            scl90Questionnaire(), questions90(), factors(), rules(), configs);
        assertFalse(result.isSuccess());
        assertEquals(AssessmentErrorCode.SCALE_CONFIG_MISSING, result.getErrorCode());
    }

    // ---------- 构造工具 ----------

    private static Questionnaire scl90Questionnaire() {
        Questionnaire q = new Questionnaire();
        q.setScaleType(Questionnaire.SCALE_SCL90);
        q.setQuestionCount(90);
        return q;
    }

    /** 按 A14 生成 90 题（item_no 1~90，factor_code 归属正确） */
    private static List<Question> questions90() {
        List<Question> questions = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : A14.entrySet()) {
            for (int itemNo : entry.getValue()) {
                Question q = new Question();
                q.setItemNo(itemNo);
                q.setFactorCode(entry.getKey());
                q.setType("single");
                q.setContent("题 " + itemNo);
                questions.add(q);
            }
        }
        return questions;
    }

    private static List<ScaleFactor> factors() {
        List<ScaleFactor> factors = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : A14.entrySet()) {
            ScaleFactor f = new ScaleFactor();
            f.setCode(entry.getKey());
            f.setItemCount(entry.getValue().size());
            if (!"additional".equals(entry.getKey())) {
                f.setNormMean(new BigDecimal("1.5"));
                f.setNormSd(new BigDecimal("0.5"));
            } else {
                f.setExtraFlag(1);
            }
            factors.add(f);
        }
        return factors;
    }

    private static List<ScaleRule> rules() {
        ScaleRule r = new ScaleRule();
        r.setRuleType(ScaleRule.RULE_TOTAL_LEVEL);
        r.setMinValue(BigDecimal.ZERO);
        r.setMaxValue(new BigDecimal("450"));
        r.setLevelCode("normal");
        return new ArrayList<>(List.of(r));
    }

    private static List<ScaleConfig> configs() {
        List<ScaleConfig> configs = new ArrayList<>();
        for (String key : ScaleConfigKeys.SCL90_REQUIRED_KEYS) {
            ScaleConfig c = new ScaleConfig();
            c.setConfigKey(key);
            c.setConfigValue("1");
            configs.add(c);
        }
        return configs;
    }

}
