package com.xinfei.health.appointment.domain.vo;

import com.xinfei.health.appointment.domain.HealthOrderExt;
import com.xinfei.health.appointment.domain.HealthOrderLog;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单视图（08 §6.4：主表 + ext + 流水）
 *
 * @author xinfei
 */
@Data
public class OrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer memberId;

    private LocalDate orderDate;

    private String orderType;

    private String orderStatus;

    private Integer setmealId;

    /**
     * 流转扩展（1:1）
     */
    private HealthOrderExt ext;

    /**
     * 订单流水
     */
    private List<HealthOrderLog> logs;

}
