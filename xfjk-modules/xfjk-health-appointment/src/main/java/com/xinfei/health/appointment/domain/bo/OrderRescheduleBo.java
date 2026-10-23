package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 改期入参（08 §5.4）
 *
 * @author xinfei
 */
@Data
public class OrderRescheduleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标日期（须在名额表内且有余量）
     */
    @NotNull(message = "目标日期不能为空")
    private LocalDate targetDate;

}
