package com.pangzhao.controller;

import com.pangzhao.constant.MessageConstant;
import com.pangzhao.constant.RedisMessageConstant;
import com.pangzhao.entity.Result;
import com.pangzhao.pojo.Member;
import com.pangzhao.pojo.Order;
import com.pangzhao.pojo.Setmeal;
import com.pangzhao.service.MemberService;
import com.pangzhao.service.OrderService;
import com.pangzhao.service.OrderSettingService;
import com.pangzhao.service.SetMealService;
import com.pangzhao.util.DateUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 戴金华
 * @date 2019-11-13 18:27
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Reference
    private OrderSettingService orderSettingService;
    @Reference
    private MemberService memberService;
    @Reference
    private SetMealService setMealService;


    @RequestMapping("/checkValidateCode.do")
    public Result checkValidateCode(String validateCode, String phone){
        //获取缓存中的验证码
        String realCode = (String) redisTemplate.boundValueOps(phone + RedisMessageConstant.SENDTYPE_ORDER).get();
        try {
            if (validateCode.equals(realCode)){
                //如果验证码输入正确
                return new Result(true,"验证码输入正确");
            }else{
                return new Result(false,"验证码输入错误");
            }
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
    }

    @RequestMapping("/checkOrderDate.do")
    public Result checkOrderDate(String orderDate){
        try {
            Result result = orderSettingService.findByDate(orderDate);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"该日期预约已满");
        }
    }


    @RequestMapping("/postOrder.do")
    public Result postOrder(@RequestBody Map map){
        try {
            String orderDate = (String) map.get("orderDate");
            String setmealIdStr = (String) map.get("setmealId");
            Integer setmealId = Integer.parseInt(setmealIdStr);
            String idCard = (String) map.get("idCard");
            String name = (String) map.get("name");
            String sex = (String) map.get("sex");
            String phoneNumber = (String) map.get("telephone");
            Member member = new Member();
            member.setIdCard(idCard);
            member.setName(name);
            member.setSex(sex);
            member.setPhoneNumber(phoneNumber);

            Order order = new Order();
            order.setOrderDate(DateUtils.parseString2Date(orderDate));
            order.setSetmealId(setmealId);
            order.setOrderType("微信预约");
            order.setOrderStatus("未到诊");
            //判断该用户是否在同一天预定过该套餐
            Result result = orderService.hasOrderedSetMeal(member,orderDate,setmealId);
            if (result.isFlag()){
                //如果未订购过
                //判断用户是否是会员 如果不是会员 注册 如果是会员 直接预定
                return orderService.postOrder(order,member);
            }else{
                return result;
            }

        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, MessageConstant.ORDERSETTING_FAIL);
        }
    }


    @RequestMapping("/findById.do")
    public Map findById(Integer id) throws Exception {
        Map map = new HashMap();

        Order order = orderService.findById(id);
        Member member = memberService.findById(order.getMemberId());
        Setmeal setmeal = setMealService.findById(order.getSetmealId());

        map.put("member",member.getName());
        map.put("setmeal",setmeal.getName());
        map.put("orderDate", DateUtils.parseDate2String(order.getOrderDate()));
        map.put("orderType",order.getOrderType());
        return map;
    }
}
