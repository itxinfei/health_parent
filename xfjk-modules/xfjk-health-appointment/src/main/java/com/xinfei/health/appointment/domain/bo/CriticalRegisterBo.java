package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 危急值登记入参（07 §3.2 POST /health/reports/{id}/critical、§6.4）
 *
 * @author xinfei
 */
@Data
public class CriticalRegisterBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 必须存在且该明细 critical_flag=1（6012） */
    @NotNull(message = "触发项不能为空")
    private Long reportItemId;

    @NotBlank(message = "接收医生不能为空")
    private String receiver;

    /** 未传按默认第三种「电话+系统推送」（18 D5） */
    private String notifyWay;

    private LocalDateTime receiverConfirmTime;

    private String handleResult;

    private LocalDateTime reportTime;

}
