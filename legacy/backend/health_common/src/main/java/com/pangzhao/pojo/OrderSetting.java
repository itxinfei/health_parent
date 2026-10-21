package com.pangzhao.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 预约设置
 */
@Table(name = "t_ordersetting")
public class OrderSetting implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    @Column(name = "orderDate")
    private Date orderDate;//预约设置日期
    @Column
    private Integer number;//可预约人数
    @Column
    private Integer reservations ;//已预约人数

    public OrderSetting() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getReservations() {
        return reservations;
    }

    public void setReservations(Integer reservations) {
        this.reservations = reservations;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderSetting(Date orderDate, Integer number) {
        this.orderDate = orderDate;
        this.number = number;
    }
}
