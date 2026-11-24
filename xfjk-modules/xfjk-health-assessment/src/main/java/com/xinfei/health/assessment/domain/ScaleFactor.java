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
 * 量表因子 t_health_scale_factor（09 §2.4；item_count 为 A8 因子分分母，常模 18 A11）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_scale_factor")
public class ScaleFactor extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("item_count")
    private Integer itemCount;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("norm_mean")
    private BigDecimal normMean;

    @TableField("norm_sd")
    private BigDecimal normSd;

    @TableField("extra_flag")
    private Integer extraFlag;

    @TableField("sort")
    private Integer sort;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
