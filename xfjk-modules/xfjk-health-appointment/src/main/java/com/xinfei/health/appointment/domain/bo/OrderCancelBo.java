package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 取消订单入参（08 §5.6：reason 必填 ≤255）
 *
 * @author xinfei
 */
@Data
public class OrderCancelBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 取消原因（必填）
     */
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 255, message = "取消原因不能超过 255 字符")
    private String reason;

}
