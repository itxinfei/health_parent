package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 预约名额视图（08 §5.2 月历口径：date/number/reservations/remain）
 *
 * @author xinfei
 */
@Data
public class OrdersettingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预约日期
     */
    private LocalDate orderDate;

    /**
     * 可预约人数
     */
    private Integer number;

    /**
     * 已预约人数
     */
    private Integer reservations;

    /**
     * 剩余名额（COALESCE 计算）
     */
    private Integer remain;

}
