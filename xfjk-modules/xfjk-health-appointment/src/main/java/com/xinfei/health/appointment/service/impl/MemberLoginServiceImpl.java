package com.xinfei.health.appointment.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.redis.utils.RedisUtils;
import com.xinfei.health.appointment.constant.HealthCacheConstants;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.Member;
import com.xinfei.health.appointment.domain.vo.AppLoginVo;
import com.xinfei.health.appointment.mapper.MemberMapper;
import com.xinfei.health.appointment.service.IMemberLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * C 端会员登录 服务层处理（13 R2：业务侧自实现，不占框架 smsAuthStrategy bean 名）
 *
 * <p>会话标识 loginId = app_user:{memberId}，与后台 sys_user 体系隔离；
 * 登录仅签发 token 不建会员（13 §4.1，避免用登录接口做会员枚举）。
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class MemberLoginServiceImpl implements IMemberLoginService {

    /**
     * C 端会话前缀（13 R2）
     */
    public static final String LOGIN_ID_PREFIX = "app_user:";

    private final MemberMapper memberMapper;

    @Override
    public AppLoginVo login(String clientId, String phone, String smsCode) {
        // 登录验证码：一次性，校验并删除（05 §6）
        String key = HealthCacheConstants.SMS_LOGIN_KEY + phone;
        String cached = RedisUtils.getCacheObject(key);
        if (cached == null) {
            throw new ServiceException("短信验证码已过期", HealthErrorCode.SMS_CODE_INVALID);
        }
        RedisUtils.deleteObject(key);
        if (!cached.equals(smsCode)) {
            throw new ServiceException("短信验证码错误", HealthErrorCode.SMS_CODE_INVALID);
        }

        // 按手机号查会员；不存在返回 2001（13 §4.1：不自动建会员，防手机号枚举）
        Member member = memberMapper.selectOne(Wrappers.<Member>lambdaQuery()
            .eq(Member::getPhoneNumber, phone)
            .last("LIMIT 1"));
        if (ObjectUtil.isNull(member)) {
            throw new ServiceException("该手机号尚未注册，请先预约", HealthErrorCode.SMS_CODE_INVALID);
        }

        // 签发 token：loginId = app_user:{memberId}
        StpUtil.login(LOGIN_ID_PREFIX + member.getId());
        AppLoginVo vo = new AppLoginVo();
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        vo.setClientId(clientId);
        return vo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 从当前会话解析会员 id（"仅本人"数据隔离的取值入口，13 R3）
     */
    public static Integer currentMemberId() {
        String loginId = StpUtil.getLoginIdAsString();
        if (loginId == null || !loginId.startsWith(LOGIN_ID_PREFIX)) {
            throw new ServiceException("非会员会话，禁止访问", HealthErrorCode.SMS_CODE_INVALID);
        }
        return Integer.valueOf(loginId.substring(LOGIN_ID_PREFIX.length()));
    }

}
