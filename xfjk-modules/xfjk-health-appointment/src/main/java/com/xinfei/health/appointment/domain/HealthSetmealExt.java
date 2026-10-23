package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 套餐扩展 t_health_setmeal_ext（1:1 t_setmeal，08 §2.5）
 *
 * <p>suitable_desc 仅展示文案，不得作为校验依据（18 E11）；
 * 适用人群四维以 married_limit / has_sex_history 等结构化字段为准（18 E11）。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_setmeal_ext")
public class HealthSetmealExt implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_setmeal.id（唯一）
     */
    @TableField("setmeal_id")
    private Integer setmealId;

    /**
     * 0 不限 / 1 男 / 2 女
     */
    @TableField("sex")
    private String sex;

    /**
     * 适用年龄下限
     */
    @TableField("age_min")
    private Integer ageMin;

    /**
     * 适用年龄上限
     */
    @TableField("age_max")
    private Integer ageMax;

    /**
     * 0 不限 / 1 限已婚 / 2 限未婚
     */
    @TableField("married_limit")
    private String marriedLimit;

    /**
     * 0 不限 / 1 需有性生活史 / 2 需无性生活史（18 E3）
     */
    @TableField("has_sex_history")
    private String hasSexHistory;

    /**
     * 1 含放射/妊娠禁忌项，下单须警示（18 E1）
     */
    @TableField("pregnancy_warn")
    private String pregnancyWarn;

    /**
     * 仅展示文案，不得作为校验依据（18 E11）
     */
    @TableField("suitable_desc")
    private String suitableDesc;

    /**
     * 默认「正常结论」词（18 F9）
     */
    @TableField("default_conclusion_id")
    private Long defaultConclusionId;

    /**
     * 默认「正常处理意见」模板
     */
    @TableField("default_advice_id")
    private Long defaultAdviceId;

    /**
     * 检前须知（13 C 端展示）
     */
    @TableField("notice_text")
    private String noticeText;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}
