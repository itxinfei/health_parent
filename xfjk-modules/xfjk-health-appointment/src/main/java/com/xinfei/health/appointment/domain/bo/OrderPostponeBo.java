package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 延期入参（08 §5.7、18 C6：until 必填）
 *
 * @author xinfei
 */
@Data
public class OrderPostponeBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 延期至日期（不早于今天）
     */
    @NotNull(message = "延期至日期不能为空")
    private LocalDate until;

}
