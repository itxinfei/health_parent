package com.xinfei.health.appointment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 危急值处置入参（07 §3.2 PUT /health/criticals/{id}/handle、§6.4）
 *
 * @author xinfei
 */
@Data
public class CriticalHandleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String receiver;

    private LocalDateTime receiverConfirmTime;

    private String notifyWay;

    private String handleResult;

    private String trackBy;

    private LocalDateTime trackTime;

    private String trackRemark;

}
