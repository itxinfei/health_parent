package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 总检打回某科室入参（07 §3.2 PUT /health/reports/{id}/send-back，18 C9）
 *
 * @author xinfei
 */
@Data
public class SendBackBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "科室不能为空")
    private Long deptId;

    @NotBlank(message = "打回原因不能为空")
    private String reason;

}
