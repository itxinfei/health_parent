package com.xinfei.health.assessment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xinfei.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估记录 t_health_assessment_record（09 §2.7）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_assessment_record")
public class AssessmentRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("record_no")
    private String recordNo;

    @TableField("member_id")
    private Long memberId;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("questionnaire_version")
    private Integer questionnaireVersion;

    @TableField("scale_type")
    private String scaleType;

    @TableField("status")
    private String status;

    @TableField("source")
    private String source;

    @TableField("answer_count")
    private Integer answerCount;

    @TableField("total_score")
    private BigDecimal totalScore;

    @TableField("avg_score")
    private BigDecimal avgScore;

    @TableField("positive_count")
    private Integer positiveCount;

    @TableField("positive_flag")
    private Integer positiveFlag;

    @TableField("severity")
    private String severity;

    @TableField("tcm_primary")
    private String tcmPrimary;

    @TableField("tcm_level")
    private String tcmLevel;

    @TableField("tcm_combine_json")
    private String tcmCombineJson;

    @TableField("conclusion")
    private String conclusion;

    @TableField("level")
    private String level;

    @TableField("factor_scores_json")
    private String factorScoresJson;

    @TableField("suggestion_json")
    private String suggestionJson;

    @TableField("crowd_ids")
    private String crowdIds;

    @TableField("cfg_version")
    private String cfgVersion;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("duration_sec")
    private Integer durationSec;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("invalid_reason")
    private String invalidReason;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 记录状态（字典 health_record_status 沿用，09 §2.7） */
    public static final String STATUS_DOING = "doing";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_SCORED = "scored";
    public static final String STATUS_INVALID = "invalid";

    /** 来源（字典 health_assessment_source） */
    public static final String SOURCE_APP = "app";
    public static final String SOURCE_ADMIN = "admin";
    public static final String SOURCE_BATCH = "batch";

}
