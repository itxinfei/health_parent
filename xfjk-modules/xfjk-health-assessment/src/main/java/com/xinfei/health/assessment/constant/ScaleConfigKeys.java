package com.xinfei.health.assessment.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 量表阈值配置键与内置兜底默认值（09 §2.6，18 A13）
 *
 * <p>优先级：问卷级 t_health_scale_config &gt; sys_config（health:scale: 前缀）&gt; 本类兜底默认。
 * 兜底默认只用于防 NPE，发布校验（7006）仍要求问卷级配置齐备，不得短路。
 *
 * @author xinfei
 */
public final class ScaleConfigKeys {

    /** SCL-90 项目内唯一记分法（18 A8，禁 0~4 混用） */
    public static final String SCL90_SCORING_METHOD = "scl90.scoring.method";
    /** SCL-90 总分上限 90×5（A15 推导） */
    public static final String SCL90_TOTAL_MAX = "scl90.total.max";
    /** 筛查阳性：总分严格大于（A10） */
    public static final String SCL90_SCREEN_TOTAL_GT = "scl90.screen.total_gt";
    /** 筛查阳性：阳性项目数严格大于（A10） */
    public static final String SCL90_SCREEN_POSITIVE_COUNT_GT = "scl90.screen.positive_count_gt";
    /** 筛查阳性：任一因子分严格大于（A10） */
    public static final String SCL90_SCREEN_FACTOR_GT = "scl90.screen.factor_gt";
    /** 阳性项目：单项 ≥ 该值（A15） */
    public static final String SCL90_POSITIVE_ITEM_GE = "scl90.positive.item_ge";
    /** 总均分分母（A15） */
    public static final String SCL90_TOTAL_AVG_DIVISOR = "scl90.total.avg_divisor";
    /** 超常模方向：upper = norm_mean + norm_sd（09 §4.1 定稿） */
    public static final String SCL90_NORM_EXCEED = "scl90.norm.exceed";
    /** 中医体质允许的子量表条目数（A1） */
    public static final String TCM_ITEMS_PER_SUBSCALE = "tcm.items.per_subscale";
    /** 中医体质五级评分（A1），公式分母 条数×(级数−1) */
    public static final String TCM_SCALE_LEVELS = "tcm.scale.levels";
    /** 平和质「是」转化分下界（A4） */
    public static final String TCM_PEACE_IS_GE = "tcm.peace.is_ge";
    /** 平和质「是」要求其余 8 种均 < 该值（A4） */
    public static final String TCM_PEACE_ALL_LT = "tcm.peace.all_lt";
    /** 平和质「基本是」的其余上界不含（A4） */
    public static final String TCM_PEACE_BASIC_LT = "tcm.peace.basic_lt";
    /** 偏颇质「是」（A5） */
    public static final String TCM_DEVIATE_IS_GE = "tcm.deviate.is_ge";
    /** 偏颇质「倾向是」下界（A5） */
    public static final String TCM_DEVIATE_TENDENCY_GE = "tcm.deviate.tendency_ge";
    /** 兼夹倾向的体质种数下界（A6） */
    public static final String TCM_COMBINE_COUNT_GE = "tcm.combine.count_ge";
    /** 兼夹计入的转化分下界（A6） */
    public static final String TCM_COMBINE_SCORE_GE = "tcm.combine.score_ge";

    /** sys_config 全局默认键前缀（09 §2.6） */
    public static final String SYS_CONFIG_PREFIX = "health:scale:";

    /** 内置兜底默认值（仅防 NPE，禁止参与判定短路） */
    public static final Map<String, String> BUILTIN_DEFAULTS = new LinkedHashMap<>();

    static {
        BUILTIN_DEFAULTS.put(SCL90_SCORING_METHOD, "1to5");
        BUILTIN_DEFAULTS.put(SCL90_TOTAL_MAX, "450");
        BUILTIN_DEFAULTS.put(SCL90_SCREEN_TOTAL_GT, "160");
        BUILTIN_DEFAULTS.put(SCL90_SCREEN_POSITIVE_COUNT_GT, "43");
        BUILTIN_DEFAULTS.put(SCL90_SCREEN_FACTOR_GT, "2");
        BUILTIN_DEFAULTS.put(SCL90_POSITIVE_ITEM_GE, "2");
        BUILTIN_DEFAULTS.put(SCL90_TOTAL_AVG_DIVISOR, "90");
        BUILTIN_DEFAULTS.put(SCL90_NORM_EXCEED, "upper");
        BUILTIN_DEFAULTS.put(TCM_ITEMS_PER_SUBSCALE, "7,8");
        BUILTIN_DEFAULTS.put(TCM_SCALE_LEVELS, "5");
        BUILTIN_DEFAULTS.put(TCM_PEACE_IS_GE, "60");
        BUILTIN_DEFAULTS.put(TCM_PEACE_ALL_LT, "30");
        BUILTIN_DEFAULTS.put(TCM_PEACE_BASIC_LT, "40");
        BUILTIN_DEFAULTS.put(TCM_DEVIATE_IS_GE, "40");
        BUILTIN_DEFAULTS.put(TCM_DEVIATE_TENDENCY_GE, "30");
        BUILTIN_DEFAULTS.put(TCM_COMBINE_COUNT_GE, "2");
        BUILTIN_DEFAULTS.put(TCM_COMBINE_SCORE_GE, "30");
    }

    /** SCL-90 算分必需的配置键（09 §2.6，缺任一 → 7006） */
    public static final String[] SCL90_REQUIRED_KEYS = {
        SCL90_SCORING_METHOD, SCL90_TOTAL_MAX, SCL90_SCREEN_TOTAL_GT,
        SCL90_SCREEN_POSITIVE_COUNT_GT, SCL90_SCREEN_FACTOR_GT,
        SCL90_POSITIVE_ITEM_GE, SCL90_TOTAL_AVG_DIVISOR, SCL90_NORM_EXCEED
    };

    /** 中医体质算分必需的配置键 */
    public static final String[] TCM_REQUIRED_KEYS = {
        TCM_ITEMS_PER_SUBSCALE, TCM_SCALE_LEVELS, TCM_PEACE_IS_GE, TCM_PEACE_ALL_LT,
        TCM_PEACE_BASIC_LT, TCM_DEVIATE_IS_GE, TCM_DEVIATE_TENDENCY_GE,
        TCM_COMBINE_COUNT_GE, TCM_COMBINE_SCORE_GE
    };

    private ScaleConfigKeys() {
    }

}
