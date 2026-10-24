package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 总检撤销入参（07 §3.2 PUT /health/reports/{id}/revoke，18 C9）
 *
 * @author xinfei
 */
@Data
public class ReportRevokeBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "撤销原因不能为空")
    private String reason;

}
