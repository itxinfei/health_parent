package com.pangzhao.controller;

import com.pangzhao.constant.RedisMessageConstant;
import com.pangzhao.entity.Result;
import com.pangzhao.service.MemberService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author 戴金华
 * @date 2019-11-15 20:00
 */
@RestController
@RequestMapping("/login")
public class UserController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private MemberService memberService;
    @RequestMapping("/check.do")
    public Result check(@RequestBody Map map, HttpServletResponse response, HttpServletRequest request){
        String telephone = (String) map.get("telephone");
        String validateCode = (String) map.get("validateCode");
        //从缓存中读取真实验证码
        String real_code = (String) redisTemplate.boundValueOps(telephone + RedisMessageConstant.SENDTYPE_LOGIN).get();

        if (!validateCode.equals(real_code)){
            return new Result(false,"验证码输入错误");
        }


        Cookie[] cookies = request.getCookies();
        //判断cookies是否存在该用户的登录信息
        for (Cookie cookie : cookies) {
            String member_cookie = cookie.getValue();
            if (member_cookie.equals(telephone)){
                //如果存在 直接登录
                return new Result();
            }
        }

        try {
            memberService.checkLogin(telephone);
            Cookie cookie = new Cookie("member_" + telephone, telephone);
            cookie.setMaxAge(60*60*30);
            response.addCookie(cookie);
            return new Result();
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
    }
}
