package com.pangzhao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 体检预约信息
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_order")
public class Order implements Serializable {
    public static final String ORDERTYPE_TELEPHONE = "电话预约";
    public static final String ORDERTYPE_WEIXIN = "微信预约";
    public static final String ORDERSTATUS_YES = "已到诊";
    public static final String ORDERSTATUS_NO = "未到诊";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "member_id")
    private Integer memberId;//会员id
    @Column(name = "orderDate")
    private Date orderDate;//预约日期
    @Column(name = "orderType")
    private String orderType;//预约类型 电话预约/微信预约
    @Column(name = "orderStatus")
    private String orderStatus;//预约状态（是否到诊）
    @Column(name = "setmeal_id")
    private Integer setmealId;//体检套餐id



}
