package com.xinfei.health.appointment.service;

import com.xinfei.health.appointment.domain.vo.AppLoginVo;

/**
 * C 端会员登录 服务层（13 §4.1、R2）
 *
 * @author xinfei
 */
public interface IMemberLoginService {

    /**
     * 手机号 + 验证码登录：不存在该会员返回 2001（不自动建会员）
     */
    AppLoginVo login(String clientId, String phone, String smsCode);

    /**
     * 退出登录
     */
    void logout();

}
