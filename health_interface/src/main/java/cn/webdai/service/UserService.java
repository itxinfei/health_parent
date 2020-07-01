package cn.webdai.service;

import cn.webdai.pojo.User;

/**
 * @author 戴金华
 * @date 2019-11-24 9:02
 */
public interface UserService {

    //根据用户名查找用户
    User findByName(String s);
}
