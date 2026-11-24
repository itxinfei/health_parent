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
 * 问卷题目 t_health_question（09 §2.2；标准量表题干禁止改写，18 A14/A16）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_question")
public class Question extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("item_no")
    private Integer itemNo;

    @TableField("seq")
    private Integer seq;

    @TableField("type")
    private String type;

    @TableField("content")
    private String content;

    @TableField("required")
    private Integer required;

    @TableField("factor_code")
    private String factorCode;

    @TableField("reverse_flag")
    private Integer reverseFlag;

    @TableField("jump_rule")
    private String jumpRule;

    @TableField("sort")
    private Integer sort;

    @TableField("remark")
    private String remark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /** 题型 */
    public static final String TYPE_SINGLE = "single";
    public static final String TYPE_MULTI = "multi";
    public static final String TYPE_TEXT = "text";

}
