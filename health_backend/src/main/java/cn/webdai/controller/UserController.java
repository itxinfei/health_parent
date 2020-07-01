package cn.webdai.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 戴金华
 * @date 2019-12-02 11:43
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
