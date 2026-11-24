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
 * 评估作答明细 t_health_assessment_answer（09 §2.8）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_assessment_answer")
public class AssessmentAnswer extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("question_id")
    private Long questionId;

    @TableField("item_no")
    private Integer itemNo;

    @TableField("option_ids")
    private String optionIds;

    @TableField("option_count")
    private Integer optionCount;

    @TableField("answer_text")
    private String answerText;

    @TableField("raw_score")
    private BigDecimal rawScore;

    @TableField("score")
    private BigDecimal score;

    @TableField("reverse_flag")
    private Integer reverseFlag;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
