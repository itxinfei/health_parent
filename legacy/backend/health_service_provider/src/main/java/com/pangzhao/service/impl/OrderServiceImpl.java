package com.pangzhao.service.impl;

import com.pangzhao.entity.Result;
import com.pangzhao.mapper.MemberMapper;
import com.pangzhao.mapper.OrderMapper;
import com.pangzhao.mapper.OrderSettingMapper;
import com.pangzhao.mapper.SetMealMapper;
import com.pangzhao.pojo.Member;
import com.pangzhao.pojo.Order;
import com.pangzhao.pojo.OrderSetting;
import com.pangzhao.service.OrderService;
import com.pangzhao.util.DateUtils;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 *
 */
@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderSettingMapper orderSettingMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private SetMealMapper setMealMapper;

    @Transactional
    @Override
    public Result postOrder(Order order, Member member) {

        //从数据库中获取member的id
        Example example1 = new Example(Member.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andEqualTo("name",member.getName()).andEqualTo("idCard",member.getIdCard());
        Member member1 = memberMapper.selectOneByExample(example1);
        order.setMemberId(member1.getId());
        //将表单数据插入表单表中
        orderMapper.insert(order);
        //预约人数+1
        Example example = new Example(OrderSetting.class);
        Example.Criteria criteria = example.createCriteria();
        try {
            criteria.andEqualTo("orderDate", DateUtils.parseDate2String(order.getOrderDate()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        OrderSetting orderSetting = orderSettingMapper.selectOneByExample(example);
        int reservations = orderSetting.getReservations();
        orderSetting.setReservations(reservations+1);
        orderSettingMapper.updateByPrimaryKeySelective(orderSetting);


        Order order1 = orderMapper.selectOne(order);
        return new Result(true,"success",order1.getId());
    }

    //判断该用户是否在同一天预定过该套餐
    @Transactional
    @Override
    public Result hasOrderedSetMeal(Member member, String orderDate, Integer setmealId) {
        Result result = new Result();
        //先根据姓名查找id
        Example example1 = new Example(Member.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andEqualTo("name",member.getName()).andEqualTo("idCard",member.getIdCard());
        Member member1 = memberMapper.selectOneByExample(example1);
        if (member1==null){
            //如果不存在此用户 说明尚未在该日期订购过套餐
            //注册该用户
            member.setRegTime(new Date());
            memberMapper.insert(member);
            result.setFlag(true);
            return result;
        }

        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("memberId",member.getId()).andEqualTo("orderDate",orderDate).andEqualTo("setmealId",setmealId);
        Order order = orderMapper.selectOneByExample(example);

        if (order==null){
            //如果用户未在当天订购过该套餐
            result.setFlag(true);
            return result;
        }else{
            //如果用户订购该套餐
            result.setFlag(false);
            result.setMessage("您已在"+orderDate+"订购过该套餐,请不要重复订购");
            return result;
        }
    }


    //根据id获取订单
    @Override
    public Order findById(Integer id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        return order;
    }

    //根据套餐查找数量 封装成map结合
    @Transactional
    @Override
    public Map findBySetmeal() {
        List<Map> orders = orderMapper.findBySetmeal();
        Map map = new HashMap();
        ArrayList<Long> totalList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();

        for (Map order : orders) {
            String name = setMealMapper.selectByPrimaryKey(order.get("setmealId")).getName();
            nameList.add(name);
            Long total = (Long) order.get("total");
            totalList.add(total);
        }
        map.put("setmealNames",nameList);
        map.put("setmealCount",totalList);
        return map;
    }
}
