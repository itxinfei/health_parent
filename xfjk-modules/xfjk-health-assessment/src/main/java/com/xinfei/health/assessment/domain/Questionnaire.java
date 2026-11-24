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
 * 问卷/量表定义 t_health_questionnaire（09 §2.1）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_questionnaire")
public class Questionnaire extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("category")
    private String category;

    @TableField("scale_type")
    private String scaleType;

    @TableField("version")
    private Integer version;

    @TableField("intro")
    private String intro;

    @TableField("instruction")
    private String instruction;

    @TableField("question_count")
    private Integer questionCount;

    @TableField("scoring_type")
    private String scoringType;

    @TableField("answer_min_score")
    private BigDecimal answerMinScore;

    @TableField("answer_max_score")
    private BigDecimal answerMaxScore;

    @TableField("status")
    private String status;

    @TableField("is_public")
    private Integer isPublic;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("sort")
    private Integer sort;

    @TableField("remark")
    private String remark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 类别（字典 health_questionnaire_category） */
    public static final String CATEGORY_SCALE = "scale";
    public static final String CATEGORY_QUESTION = "question";

    /** 量表类别（字典 health_scale_type） */
    public static final String SCALE_SCL90 = "scl90";
    public static final String SCALE_TCM9 = "tcm9";
    public static final String SCALE_CUSTOM = "custom";

    /** 计分类型（09 §3.2） */
    public static final String SCORING_SUM = "sum";
    public static final String SCORING_FACTOR = "factor";
    public static final String SCORING_SCALE = "scale";

    /** 状态 */
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_DISABLED = "disabled";

}
