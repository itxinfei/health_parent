package com.xinfei.health.appointment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员动态信息保存入参（07 §2.3，1:1 upsert；bmi 服务端计算，前端值忽略）
 *
 * @author xinfei
 */
@Data
public class MemberProfileBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ---------- 体格 ----------
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal waist;
    private BigDecimal hip;
    private Integer bloodPressureHigh;
    private Integer bloodPressureLow;
    private Integer heartRate;
    private BigDecimal visionLeft;
    private BigDecimal visionRight;
    private String colorVision;

    // ---------- 基本 ----------
    private String bloodType;
    private String maritalStatus;
    private String sexHistory;
    private String pregnancyStatus;
    private String menstruationFlag;
    private LocalDate menstruationDate;
    private String occupation;
    private String eduLevel;
    private String jobCategory;
    private String address;
    private String phoneSelf;
    private String phoneContact;
    private String contactName;

    // ---------- 生活方式 ----------
    private String smoking;
    private Integer smokingCount;
    private String drinking;
    private Integer drinkingCount;
    private Integer exerciseFreq;
    private Integer exerciseTime;
    private String dietHabit;
    private BigDecimal sleepHours;
    private String saltLevel;
    private String psychAdjust;
    private String compliance;

    // ---------- 既往 ----------
    private String surgeonHistoryFlag;
    private String traumaFlag;
    private String transfusionFlag;
    private String drugAllergyFlag;
    private Integer menarcheAge;
    private Integer menopauseAge;
    private Integer parityCount;

    // ---------- 时效 ----------
    private LocalDateTime dataTime;
    private String remark;

}
