package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 会员病史 t_health_member_history（07 §2.5）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_history")
public class HealthMemberHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Integer memberId;

    /**
     * 病史类型：disease / family / allergy / surgery / medication
     */
    @TableField("type")
    private String type;

    @TableField("content")
    private String content;

    @TableField("disease_code")
    private String diseaseCode;

    @TableField("relation")
    private String relation;

    @TableField("allergen")
    private String allergen;

    @TableField("allergy_type")
    private String allergyType;

    @TableField("severity")
    private String severity;

    @TableField("surgery_name")
    private String surgeryName;

    @TableField("surgery_date")
    private LocalDate surgeryDate;

    @TableField("hospital")
    private String hospital;

    @TableField("drug_name")
    private String drugName;

    @TableField("drug_dose")
    private String drugDose;

    @TableField("drug_freq")
    private String drugFreq;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("happen_date")
    private LocalDate happenDate;

    /**
     * 1 已复核 / 0 待复核（C 端自填默认 0）
     */
    @TableField("confirm_flag")
    private String confirmFlag;

    @TableField("remark")
    private String remark;

    @TableField("create_dept")
    private Long createDept;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    /**
     * 病史类型合法值（07 §2.5，闭集）
     */
    public static final Set<String> TYPES = Set.of("disease", "family", "allergy", "surgery", "medication");

}
