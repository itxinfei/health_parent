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

/**
 * 分级/判定规则 t_health_scale_rule（09 §2.5；区间一律左闭右开 [min,max)）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_scale_rule")
public class ScaleRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("rule_type")
    private String ruleType;

    @TableField("factor_code")
    private String factorCode;

    @TableField("min_value")
    private BigDecimal minValue;

    @TableField("max_value")
    private BigDecimal maxValue;

    @TableField("level_code")
    private String levelCode;

    @TableField("conclusion")
    private String conclusion;

    @TableField("suggestion_id")
    private Long suggestionId;

    @TableField("sort")
    private Integer sort;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 规则类型（字典 health_scale_rule_type） */
    public static final String RULE_FACTOR_LEVEL = "factor_level";
    public static final String RULE_TOTAL_LEVEL = "total_level";
    public static final String RULE_TCM_CONVERT = "tcm_convert";
    public static final String RULE_SCREEN = "screen";
    public static final String RULE_SEVERITY = "severity";

}
