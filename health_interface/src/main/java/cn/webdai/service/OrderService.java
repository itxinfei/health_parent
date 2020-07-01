package cn.webdai.service;

import cn.webdai.entity.Result;
import cn.webdai.pojo.Member;
import cn.webdai.pojo.Order;

import java.util.Map;

/**
 * @author 戴金华
 * @date 2019-11-13 18:28
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
