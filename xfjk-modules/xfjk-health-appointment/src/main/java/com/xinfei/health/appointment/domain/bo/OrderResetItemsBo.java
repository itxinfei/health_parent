package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 项目重置入参（08 §5.7、18 D11）
 *
 * @author xinfei
 */
@Data
public class OrderResetItemsBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 明细 id 列表；为空则重置报告全部明细 */
    private List<Long> itemIds;

    @NotBlank(message = "重置原因不能为空")
    private String reason;

}
