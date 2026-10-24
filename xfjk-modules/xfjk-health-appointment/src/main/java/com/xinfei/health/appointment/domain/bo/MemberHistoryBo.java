package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会员病史保存入参（07 §2.5；整组替换语义，见 §3.1 histories 接口）
 *
 * @author xinfei
 */
@Data
public class MemberHistoryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 病史类型：disease / family / allergy / surgery / medication（闭集）
     */
    @NotBlank(message = "病史类型不能为空")
    private String type;

    @NotBlank(message = "病史描述不能为空")
    @Size(max = 500, message = "病史描述长度不能超过500个字符")
    private String content;

    private String diseaseCode;
    private String relation;
    private String allergen;
    private String allergyType;
    private String severity;
    private String surgeryName;
    private LocalDate surgeryDate;
    private String hospital;
    private String drugName;
    private String drugDose;
    private String drugFreq;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate happenDate;

    /**
     * 1 已复核 / 0 待复核；后台维护默认 1
     */
    private String confirmFlag;

    private String remark;

}
