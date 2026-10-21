package com.pangzhao.service;

import com.pangzhao.pojo.User;
/**
 *
 */
public interface UserService {

    //根据用户名查找用户
    User findByName(String s);
}
