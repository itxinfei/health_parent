package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 手工建档报告入参（07 §3.2 POST /health/reports）
 *
 * @author xinfei
 */
@Data
public class ReportSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "会员id不能为空")
    private Integer memberId;

    @NotNull(message = "体检日期不能为空")
    private LocalDate checkDate;

    /** 上传/接口/解析/团检/手工录入 */
    @NotBlank(message = "报告来源不能为空")
    private String source;

    private Integer orderId;

    private String examNo;

    private Long batchId;

    private String doctor;

    private String remark;

}
