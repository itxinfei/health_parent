package com.pangzhao.controller;

import com.pangzhao.constant.MessageConstant;
import com.pangzhao.entity.Result;
import com.pangzhao.pojo.OrderSetting;
import com.pangzhao.service.OrderSettingService;
import com.pangzhao.util.POIUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("/ordersetting")
public class OrderSettingController {

    @Reference
    private OrderSettingService orderSettingService;

    @RequestMapping("/upload.do")
    public Result upload(@RequestParam("excelFile")MultipartFile excelFile){

        try {
            List<String[]> strings = POIUtils.readExcel(excelFile);
            if (strings.size()>0&&strings!=null){
                ArrayList<OrderSetting> orderSettings = new ArrayList<>();
                //如果上传的文件不为空
                for (String[] string : strings) {
                    if (new Date().getTime()>=new Date(string[0]).getTime()){
                        //如果预约设置日期比当前日期小 则跳过不存储
                        continue;
                    }else{
                        OrderSetting orderSetting = new OrderSetting(new Date(string[0]), Integer.parseInt(string[1]));
                        orderSettings.add(orderSetting);
                    }
                }
                //将预约设置集合存入数据库中
                orderSettingService.add(orderSettings);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.IMPORT_ORDERSETTING_FAIL);
        }
        return new Result(true, MessageConstant.IMPORT_ORDERSETTING_SUCCESS);
    }


    @RequestMapping("/findAll.do")
    public List<Map> findAll(String currentYear, String currentMonth){
        return orderSettingService.findAll(currentYear,currentMonth);
    }

    @RequestMapping("/update.do")
    public Result update(@RequestBody OrderSetting orderSetting){
        try {
            orderSettingService.update(orderSetting);
            return new Result(true, MessageConstant.ORDERSETTING_SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, MessageConstant.ORDERSETTING_FAIL);
        }
    }
}
