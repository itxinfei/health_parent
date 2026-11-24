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

/**
 * 评估建议 t_health_suggestion（09 §2.10）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_suggestion")
public class Suggestion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("factor_code")
    private String factorCode;

    @TableField("level_code")
    private String levelCode;

    @TableField("type")
    private String type;

    @TableField("content")
    private String content;

    @TableField("ref_disease_id")
    private Long refDiseaseId;

    @TableField("sort")
    private Integer sort;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 建议类型（09 §2.10） */
    public static final String TYPE_DIET = "diet";
    public static final String TYPE_EXERCISE = "exercise";
    public static final String TYPE_PSYCH = "psych";
    public static final String TYPE_FOLLOWUP = "followup";
    public static final String TYPE_GENERAL = "general";

}
