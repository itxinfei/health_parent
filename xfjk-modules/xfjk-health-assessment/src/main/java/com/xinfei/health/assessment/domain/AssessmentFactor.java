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
 * 评估因子得分 t_health_assessment_factor（09 §2.9）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_assessment_factor")
public class AssessmentFactor extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("factor_code")
    private String factorCode;

    @TableField("factor_name")
    private String factorName;

    @TableField("item_count")
    private Integer itemCount;

    @TableField("raw_sum")
    private BigDecimal rawSum;

    @TableField("factor_score")
    private BigDecimal factorScore;

    @TableField("convert_score")
    private BigDecimal convertScore;

    @TableField("level_code")
    private String levelCode;

    @TableField("conclusion")
    private String conclusion;

    @TableField("norm_upper")
    private BigDecimal normUpper;

    @TableField("norm_exceed_flag")
    private Integer normExceedFlag;

    @TableField("sort")
    private Integer sort;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
