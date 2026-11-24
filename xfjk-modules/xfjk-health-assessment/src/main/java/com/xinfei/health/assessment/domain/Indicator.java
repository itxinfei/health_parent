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
 * 评估指标库 t_health_indicator（09 §2.11）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_indicator")
public class Indicator extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("unit")
    private String unit;

    @TableField("ref_min")
    private BigDecimal refMin;

    @TableField("ref_max")
    private BigDecimal refMax;

    @TableField("ref_range")
    private String refRange;

    @TableField("value_type")
    private String valueType;

    @TableField("category")
    private String category;

    @TableField("sort")
    private Integer sort;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    public static final String VALUE_NUMERIC = "numeric";
    public static final String VALUE_TEXT = "text";
    public static final String VALUE_ENUM = "enum";

}
