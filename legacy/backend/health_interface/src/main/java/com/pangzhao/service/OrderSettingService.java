package com.pangzhao.service;

import com.pangzhao.entity.Result;
import com.pangzhao.pojo.OrderSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface OrderSettingService {

    //将预约设置的集合存入数据库中
    void add(ArrayList<OrderSetting> orderSettings);

    //查询所有预约
    List<Map> findAll(String currentYear, String currentMonth);

    //更新可预约人数
    void update(OrderSetting orderSetting);

    //判断输入的预约日期是否还有剩余
    Result findByDate(String orderDate);
}
