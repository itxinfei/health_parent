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
 * 预约订单 t_order（原库冻结表）
 *
 * <p>本切片仅用于套餐删除前的订单引用校验（08 §4.1 错误码 4001），
 * 订单查询与到诊登记由 08 §3.5 的后续切片补齐。
 *
 * @author xinfei
 */
@Data
@TableName("t_order")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 会员 id
     */
    @TableField("member_id")
    private Integer memberId;

    /**
     * 预约日期（驼峰列）
     */
    @TableField("orderDate")
    private LocalDate orderDate;

    /**
     * 预约类型：电话预约 / 微信预约
     */
    @TableField("orderType")
    private String orderType;

    /**
     * 预约状态：已到诊 / 未到诊
     */
    @TableField("orderStatus")
    private String orderStatus;

    /**
     * 套餐 id
     */
    @TableField("setmeal_id")
    private Integer setmealId;

}
