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
 * 题目选项 t_health_question_option（09 §2.3；分值须落 answer_min~max，越界 7011）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_question_option")
public class QuestionOption extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("question_id")
    private Long questionId;

    @TableField("seq")
    private Integer seq;

    @TableField("content")
    private String content;

    @TableField("score")
    private BigDecimal score;

    @TableField("factor_code")
    private String factorCode;

    @TableField("is_other")
    private Integer isOther;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
