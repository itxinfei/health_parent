package com.pangzhao.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/findUsername")
    public Map findUsername(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        Map map = new HashMap();
        map.put("username",username);
        return map;
    }
}
