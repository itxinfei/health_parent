package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 预约名额设置 t_ordersetting（原库冻结表，禁止增删列）
 *
 * <p>reservations 可空，所有加减必须 COALESCE（08 §4.3）。
 *
 * @author xinfei
 */
@Data
@TableName("t_ordersetting")
public class Ordersetting implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 预约日期（驼峰列）
     */
    @TableField("orderDate")
    private LocalDate orderDate;

    /**
     * 可预约人数
     */
    @TableField("number")
    private Integer number;

    /**
     * 已预约人数（可空）
     */
    @TableField("reservations")
    private Integer reservations;

}
