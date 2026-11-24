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
 * 人群规则条件 t_health_crowd_rule（09 §2.12）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_crowd_rule")
public class CrowdRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("crowd_id")
    private Long crowdId;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_code")
    private String sourceCode;

    @TableField("operator")
    private String operator;

    @TableField("value_min")
    private BigDecimal valueMin;

    @TableField("value_max")
    private BigDecimal valueMax;

    @TableField("value_text")
    private String valueText;

    @TableField("group_no")
    private Integer groupNo;

    @TableField("logic")
    private String logic;

    @TableField("sort")
    private Integer sort;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 条件值来源（字典 health_crowd_source_type） */
    public static final String SOURCE_FACTOR = "factor";
    public static final String SOURCE_INDICATOR = "indicator";
    public static final String SOURCE_REPORT = "report";
    public static final String SOURCE_PROFILE = "profile";
    public static final String SOURCE_ASSESSMENT = "assessment";

    /** 运算符（09 §2.12） */
    public static final String OP_GT = "gt";
    public static final String OP_GE = "ge";
    public static final String OP_LT = "lt";
    public static final String OP_LE = "le";
    public static final String OP_EQ = "eq";
    public static final String OP_NE = "ne";
    public static final String OP_IN = "in";
    public static final String OP_BETWEEN = "between";

    public static final String LOGIC_AND = "AND";
    public static final String LOGIC_OR = "OR";

}
