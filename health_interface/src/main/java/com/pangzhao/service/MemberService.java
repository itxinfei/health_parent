package com.pangzhao.service;

import com.pangzhao.pojo.Member;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface MemberService {


    //根据id获取会员
    Member findById(Integer id);

    //
    Map<String, List> findByDate();

    //验证是否存在该会员 即手机号是否存在于数据库表中 不存在 则将其存入到数据库中
    void checkLogin(String telephone);
}
