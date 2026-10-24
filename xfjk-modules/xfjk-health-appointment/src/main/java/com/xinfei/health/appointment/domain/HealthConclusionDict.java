package com.xinfei.health.appointment.domain;

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
 * 结论词库 t_health_conclusion_dict（11 §2.1 的 07 必需子集）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_conclusion_dict")
public class HealthConclusionDict extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("term")
    private String term;

    /** disease 疾病诊断 / positive 阳性发现 / other 其他结论 */
    @TableField("conclusion_type")
    private String conclusionType;

    @TableField("dept_scope")
    private String deptScope;

    @TableField("abnormal_grade")
    private String abnormalGrade;

    @TableField("normal_flag")
    private Integer normalFlag;

    @TableField("major_positive_flag")
    private Integer majorPositiveFlag;

    @TableField("merge_group_code")
    private String mergeGroupCode;

    @TableField("merge_target_code")
    private String mergeTargetCode;

    @TableField("synonyms")
    private String synonyms;

    @TableField("basis")
    private String basis;

    @TableField("icd_code")
    private String icdCode;

    @TableField("health_education_id")
    private Long healthEducationId;

    @TableField("default_advice_id")
    private Long defaultAdviceId;

    @TableField("critical_note")
    private String criticalNote;

    @TableField("usage_count")
    private Integer usageCount;

    /** draft / published / disabled */
    @TableField("status")
    private String status;

    @TableField("sort")
    private Integer sort;

    @TableField("remark")
    private String remark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    public static final String STATUS_PUBLISHED = "published";

}
