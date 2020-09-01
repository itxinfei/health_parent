package com.pangzhao.service.impl;

import com.pangzhao.mapper.MemberMapper;
import com.pangzhao.mapper.OrderMapper;
import com.pangzhao.pojo.Member;
import com.pangzhao.pojo.Order;
import com.pangzhao.service.ReportService;
import com.pangzhao.util.DateUtils;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 */
@Service(interfaceClass = ReportService.class)
public class ReportServiceImpl implements ReportService {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private OrderMapper orderMapper;

    /**
     *    reportData:{
     *                     reportDate:null,
     *                     todayNewMember :0,
     *                     totalMember :0,
     *                     thisWeekNewMember :0,
     *                     thisMonthNewMember :0,
     *                     todayOrderNumber :0,
     *                     todayVisitsNumber :0,
     *                     thisWeekOrderNumber :0,
     *                     thisWeekVisitsNumber :0,
     *                     thisMonthOrderNumber :0,
     *                     thisMonthVisitsNumber :0,
     *                     hotSetmeal :[
     *      *                         {name:'阳光爸妈升级肿瘤12项筛查（男女单人）体检套餐',setmeal_count:200,proportion:0.222},
     *      *                         {name:'阳光爸妈升级肿瘤12项筛查体检套餐',setmeal_count:200,proportion:0.222}
     *      *                     ]
     *                 }
     * @return
     */
    @Override
    public Map getBusinessReportData() throws Exception {

        //获取当天日期
        String reportDate = DateUtils.parseDate2String(DateUtils.getToday());

        //获取本周第一天和本周的最后一天
        String firstDayOfthisWeek =  DateUtils.parseDate2String(DateUtils.getThisWeekMonday());
        String lastDayOfthisWeek = DateUtils.parseDate2String(DateUtils.getSundayOfThisWeek());


        //获取当月的第一天和最后一天
        String firstDayofCurrentMonth = DateUtils.parseDate2String(DateUtils.getFirstDay4ThisMonth());
        String lastDayofCurrentMonth = DateUtils.parseDate2String(DateUtils.getMonthLastDay());


        //获取当天新增会员人数
        int todayNewMember = memberMapper.selectCountByExample(getExample(Member.class,"regTime",reportDate));

        //获取总会员数
        int totalMember = memberMapper.selectCountByExample(getExample(Member.class,"",""));

        //获取本周新增会员
        int thisWeekNewMember = memberMapper.selectCountByExample(getExample2(Member.class,"regTime",firstDayOfthisWeek,lastDayOfthisWeek));

        //获取本月新增会员人数
        int thisMonthNewMember = memberMapper.selectCountByExample(getExample2(Member.class,"regTime",firstDayofCurrentMonth,lastDayofCurrentMonth));

        //获取当日预约人数
        int todayOrderNumber = orderMapper.selectCountByExample(getExample(Order.class,"orderDate",reportDate));

        //获取本周预约人数
        int thisWeekOrderNumber = orderMapper.selectCountByExample(getExample2(Order.class,"orderDate",firstDayOfthisWeek,lastDayOfthisWeek));

        //获取本月预约人数
        int thisMonthOrderNumber = orderMapper.selectCountByExample(getExample2(Order.class,"orderDate",firstDayofCurrentMonth,lastDayofCurrentMonth));

        //获取今日到诊人数
        int todayVisitsNumber = orderMapper.selectCountByExample(getExample4(Order.class,"orderDate",reportDate,"orderStatus","到诊"));


        //获取本周到诊人数
        int thisWeekVisitsNumber = orderMapper.selectCountByExample(getExample3(Order.class,"orderDate",firstDayOfthisWeek,lastDayOfthisWeek,"orderStatus","到诊"));

        //获取本月到诊人数
        int thisMonthVisitsNumber = orderMapper.selectCountByExample(getExample3(Order.class,"orderDate",firstDayofCurrentMonth,lastDayofCurrentMonth,"orderStatus","到诊"));

        //获取热门套餐的数据集合
        List<Map> hotSetmeal = orderMapper.findHotSetMeal();

        Map map = new HashMap();
        map.put("reportDate",reportDate);
        map.put("todayNewMember",todayNewMember);
        map.put("totalMember",totalMember);
        map.put("thisWeekNewMember",thisWeekNewMember);
        map.put("thisMonthNewMember",thisMonthNewMember);
        map.put("todayOrderNumber",todayOrderNumber);
        map.put("thisWeekOrderNumber",thisWeekOrderNumber);
        map.put("thisMonthOrderNumber",thisMonthOrderNumber);
        map.put("todayVisitsNumber",todayVisitsNumber);
        map.put("thisWeekVisitsNumber",thisWeekVisitsNumber);
        map.put("thisMonthVisitsNumber",thisMonthVisitsNumber);
        map.put("hotSetmeal",hotSetmeal);
        return map;
    }



    //获取条件 相等(1)
    public Example getExample(Class c,String properties,String properties2){
        Example example = new Example(c);
        Example.Criteria criteria = example.createCriteria();
        if (!"".equals(properties)){
            criteria.andEqualTo(properties,properties2);
        }
        return example;
    }



    //获取条件2 区间
    public Example getExample2(Class c,String properties,String min,String max){
        Example example = new Example(c);
        Example.Criteria criteria = example.createCriteria();
        if (!"".equals(properties)){
            criteria.andBetween(properties,min,max);
        }
        return example;
    }

    //获取条件2 区间+相等
    public Example getExample3(Class c,String properties1,String min,String max,String properties2,String properties3){
        Example example = new Example(c);
        Example.Criteria criteria = example.createCriteria();
        if (!"".equals(properties1)){
            criteria.andBetween(properties1,min,max);
        }
        if (!"".equals(properties2)){
            criteria.andEqualTo(properties2,properties3);
        }
        return example;
    }

    //获取条件 相等(2)
    public Example getExample4(Class c,String properties1,String properties2,String properties3,String properties4){
        Example example = new Example(c);
        Example.Criteria criteria = example.createCriteria();
        if (!"".equals(properties1)){
            criteria.andEqualTo(properties1,properties2);
        }
        if (!"".equals(properties3)){
            criteria.andEqualTo(properties3,properties4);
        }
        return example;
    }
}
