package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员动态信息 t_health_member_profile（1:1 t_member，07 §2.3）
 *
 * <p>member_status 是会员停用/恢复标记的唯一落点（07 §5.8）；
 * bmi 由服务端计算，不信任前端值。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_profile")
public class HealthMemberProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_member.id（唯一索引）
     */
    @TableField("member_id")
    private Integer memberId;

    // ---------- 体格 ----------
    @TableField("height")
    private BigDecimal height;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("bmi")
    private BigDecimal bmi;

    @TableField("waist")
    private BigDecimal waist;

    @TableField("hip")
    private BigDecimal hip;

    @TableField("whtr")
    private BigDecimal whtr;

    @TableField("blood_pressure_high")
    private Integer bloodPressureHigh;

    @TableField("blood_pressure_low")
    private Integer bloodPressureLow;

    @TableField("heart_rate")
    private Integer heartRate;

    @TableField("vision_left")
    private BigDecimal visionLeft;

    @TableField("vision_right")
    private BigDecimal visionRight;

    @TableField("color_vision")
    private String colorVision;

    // ---------- 基本 ----------
    @TableField("blood_type")
    private String bloodType;

    @TableField("marital_status")
    private String maritalStatus;

    @TableField("sex_history")
    private String sexHistory;

    @TableField("pregnancy_status")
    private String pregnancyStatus;

    @TableField("menstruation_flag")
    private String menstruationFlag;

    @TableField("menstruation_date")
    private LocalDate menstruationDate;

    @TableField("occupation")
    private String occupation;

    @TableField("edu_level")
    private String eduLevel;

    @TableField("job_category")
    private String jobCategory;

    @TableField("address")
    private String address;

    @TableField("phone_self")
    private String phoneSelf;

    @TableField("phone_contact")
    private String phoneContact;

    @TableField("contact_name")
    private String contactName;

    // ---------- 生活方式 ----------
    @TableField("smoking")
    private String smoking;

    @TableField("smoking_count")
    private Integer smokingCount;

    @TableField("drinking")
    private String drinking;

    @TableField("drinking_count")
    private Integer drinkingCount;

    @TableField("exercise_freq")
    private Integer exerciseFreq;

    @TableField("exercise_time")
    private Integer exerciseTime;

    @TableField("diet_habit")
    private String dietHabit;

    @TableField("sleep_hours")
    private BigDecimal sleepHours;

    @TableField("salt_level")
    private String saltLevel;

    @TableField("psych_adjust")
    private String psychAdjust;

    @TableField("compliance")
    private String compliance;

    // ---------- 既往 ----------
    @TableField("surgeon_history_flag")
    private String surgeonHistoryFlag;

    @TableField("trauma_flag")
    private String traumaFlag;

    @TableField("transfusion_flag")
    private String transfusionFlag;

    @TableField("drug_allergy_flag")
    private String drugAllergyFlag;

    @TableField("menarche_age")
    private Integer menarcheAge;

    @TableField("menopause_age")
    private Integer menopauseAge;

    @TableField("parity_count")
    private Integer parityCount;

    // ---------- 状态与时效 ----------

    /**
     * 会员状态（sys_normal_disable）：0 正常 / 1 停用（07 §5.8 唯一落点）
     */
    @TableField("member_status")
    private String memberStatus;

    /**
     * 本条档案数据采集时间（≠建档时间，18 D11 历次对比依赖）
     */
    @TableField("data_time")
    private LocalDateTime dataTime;

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
     * 会员状态常量（sys_normal_disable）
     */
    public static final String STATUS_NORMAL = "0";
    public static final String STATUS_DISABLED = "1";

}
