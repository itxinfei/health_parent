package com.xinfei.health.assessment.constant;

/**
 * 健康评估业务错误码 7001~7011（口径见 09 §8，唯一登记处）
 *
 * @author xinfei
 */
public final class AssessmentErrorCode {

    /**
     * 量表/问卷不存在或未发布
     */
    public static final int QUESTIONNAIRE_NOT_PUBLISHED = 7001;

    /**
     * 评估作答不完整/无效
     */
    public static final int ANSWER_INCOMPLETE = 7002;

    /**
     * 问卷未配置计分类型或判定规则缺失
     */
    public static final int SCORING_RULE_MISSING = 7003;

    /**
     * 问卷结构配置非法（题数、因子归属、规则区间不符）
     */
    public static final int QUESTIONNAIRE_STRUCTURE_INVALID = 7004;

    /**
     * 量表因子配置非法（条目数、常模缺失或越界）
     */
    public static final int FACTOR_CONFIG_INVALID = 7005;

    /**
     * 阈值配置缺失或非法（scale_config 必填键）
     */
    public static final int SCALE_CONFIG_MISSING = 7006;

    /**
     * 已发布问卷禁止原地修改，须复制新版本
     */
    public static final int PUBLISHED_LOCKED = 7007;

    /**
     * 评估记录不存在或无权查看（非本人 / 问卷被引用）
     */
    public static final int RECORD_NOT_FOUND_OR_DENIED = 7008;

    /**
     * 人群规则条件非法
     */
    public static final int CROWD_RULE_INVALID = 7009;

    /**
     * 评估重复提交（该问卷已有已完成记录）
     */
    public static final int DUPLICATE_SUBMIT = 7010;

    /**
     * 作答分值不在允许域（记分法与配置不一致）
     */
    public static final int ANSWER_SCORE_OUT_OF_RANGE = 7011;

    private AssessmentErrorCode() {
    }

}
