package com.pangzhao.service.impl;

import com.pangzhao.entity.Result;
import com.pangzhao.mapper.OrderSettingMapper;
import com.pangzhao.pojo.OrderSetting;
import com.pangzhao.service.OrderSettingService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 *
 */
@Service
public class OrderSettingServiceImpl implements OrderSettingService {

    @Autowired
    private OrderSettingMapper orderSettingMapper;

    @Override
    public void add(ArrayList<OrderSetting> orderSettings) {
        for (OrderSetting orderSetting : orderSettings) {
            orderSetting.setReservations(0);//设置已预约人数为0
            Date orderDate = orderSetting.getOrderDate();
            Example example = new Example(OrderSetting.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("orderDate",orderDate);
            List<OrderSetting> orderSettings1 = orderSettingMapper.selectByExample(example);
            if (orderSettings1.size()>0&&orderSettings1!=null){
                //如果数据库中已经存在该日期的预约设置 则更新
                orderSettingMapper.updateByPrimaryKeySelective(orderSetting);
            }else{
                //如果数据库中不存在该日期的预约设置 则插入
                orderSettingMapper.insert(orderSetting);
            }
        }
    }

    //查询所有预约
    @Override
    public List<Map> findAll(String currentYear, String currentMonth) {
        String beginDate = currentYear+"-"+currentMonth+"-1";
        String endDate = currentYear+"-"+currentMonth+"-31";

        Example example = new Example(OrderSetting.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andBetween("orderDate",beginDate,endDate);
        List<OrderSetting> orderSettings = orderSettingMapper.selectByExample(example);
        List<Map> mapList = new ArrayList<>();
        for (OrderSetting orderSetting : orderSettings) {
            Map map = new HashMap();
            int date = orderSetting.getOrderDate().getDate();
            map.put("date",date);
            map.put("number",orderSetting.getNumber());
            map.put("reservations",orderSetting.getReservations());

            mapList.add(map);
        }
        return mapList;
    }

    //更新可预约人数
    @Override
    public void update(OrderSetting orderSetting) {
        orderSetting.setReservations(0);
        Date orderDate = orderSetting.getOrderDate();
        String orderDateStr = orderDate.getYear()+1900+"-"+(orderDate.getMonth()+1)+"-"+orderDate.getDate();
//        String beginDate = orderDate.getYear()+1900+"-"+(orderDate.getMonth()+1)+"-"+(orderDate.getDate()-1);
//        String endDate = orderDate.getYear()+1900+"-"+(orderDate.getMonth()+1)+"-"+(orderDate.getDate()+1);
        Example example = new Example(OrderSetting.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderDate",orderDateStr);
        List<OrderSetting> orderSettings = orderSettingMapper.selectByExample(example);
        if (orderSettings.size()>0&&orderSettings!=null){
            //如果数据库存在该日期 则更新
            orderSettingMapper.updateByExample(orderSetting,example);
        }else{
            //如果数据库不存在该日期 则插入
            orderSettingMapper.insert(orderSetting);
        }
    }

    //判断输入的预约日期是否还有剩余
    @Override
    public Result findByDate(String orderDate) {
        Result result = new Result();
        Example example = new Example(OrderSetting.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderDate",orderDate);
        OrderSetting orderSetting = orderSettingMapper.selectOneByExample(example);
        if(orderSetting ==null){
            //如果预约日期没有设置 则返回false
            result.setFlag(false);
            result.setMessage("该预约日期尚未设置,请选择其他日期");
            return result;
        }
        Integer total = orderSetting.getNumber();
        Integer used = orderSetting.getReservations();
        if (total-used>0){
            //该预约日期已经设置 且还有剩余
            result.setFlag(true);
            return result;
        }else{
            result.setFlag(false);
            result.setMessage("该预约日期预约人数已满,请选择其他日期进行预约");
            return result;
        }
    }
}
