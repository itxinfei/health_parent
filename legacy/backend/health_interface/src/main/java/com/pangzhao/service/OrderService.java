package com.pangzhao.service;

import com.pangzhao.entity.Result;
import com.pangzhao.pojo.Member;
import com.pangzhao.pojo.Order;

import java.util.Map;

/**
 *
 */
public interface OrderService {

    //提交预约
    Result postOrder(Order order, Member member);

    //判断该用户是否在同一天预定过该套餐
    Result hasOrderedSetMeal(Member member, String orderDate, Integer setmealId);

    //根据id获取订单
    Order findById(Integer id);

    //根据套餐查找数量 封装成map结合
    Map findBySetmeal();
}
