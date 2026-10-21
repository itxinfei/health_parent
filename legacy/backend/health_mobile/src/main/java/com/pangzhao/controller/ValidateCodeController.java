package com.pangzhao.controller;

import com.pangzhao.constant.RedisMessageConstant;
import com.pangzhao.entity.Result;
import com.pangzhao.util.SMSUtils;
import com.pangzhao.util.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author 戴金华
 * @date 2019-11-13 15:47
 */
@RestController
@RequestMapping("/validateCode")
public class ValidateCodeController {


    @Autowired
    private RedisTemplate  redisTemplate;

    @RequestMapping("/send4order.do")
    public Result send4order(String telephone){
        Integer code = ValidateCodeUtils.generateValidateCode(4);   //生成4位数字验证码
//        Integer code = 4097;
        try {
            //发送短信
            SMSUtils.sendShortMessage(SMSUtils.ORDER_NOTICE,telephone,code.toString());
            //将验证码缓存到redis中
            redisTemplate.opsForValue().set(telephone+RedisMessageConstant.SENDTYPE_ORDER,code.toString(),5*60,TimeUnit.SECONDS);
            return new Result(true,"发送成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
    }


    @RequestMapping("/send4Login.do")
    public Result send4Login(String telephone){
        Integer code = ValidateCodeUtils.generateValidateCode(4);//生成4位数字验证码
        try {
            //发送短信
            SMSUtils.sendShortMessage(SMSUtils.ORDER_NOTICE,telephone,code.toString());
            //将验证码存储在缓存中
            redisTemplate.expire(telephone+RedisMessageConstant.SENDTYPE_LOGIN,60,TimeUnit.MILLISECONDS);
            return new Result(true,"短信发送成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
    }
}
