package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单状态迁移入参（08 §4.5：仅允许 §3.2 合法边）
 *
 * @author xinfei
 */
@Data
public class OrderFlowBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标流程状态（必填，须为 §3.2 合法迁移边）
     */
    @NotBlank(message = "目标状态不能为空")
    private String toStatus;

    /**
     * 迁移备注（选填 ≤255）
     */
    @Size(max = 255, message = "备注不能超过 255 字符")
    private String remark;

}
