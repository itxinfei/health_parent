package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.vo.AppLoginVo;
import com.xinfei.health.appointment.service.IMemberLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端认证（13 §4.1、R2：独立 controller，不占框架 smsAuthStrategy bean 名）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/auth")
@Tag(name = "C端-认证")
public class AppAuthController extends BaseController {

    private final IMemberLoginService memberLoginService;

    /**
     * 登录（手机号 + 验证码；grantType 仅支持 sms）
     */
    @Operation(summary = "会员短信登录")
    @SaIgnore
    @PostMapping("/login")
    public R<AppLoginVo> login(@RequestBody AppLoginBody body) {
        return R.ok(memberLoginService.login(body.getClientId(), body.getPhone(), body.getSmsCode()));
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @Log(title = "C端认证", businessType = BusinessType.OTHER)
    @SaCheckLogin
    @PostMapping("/logout")
    public R<Void> logout() {
        memberLoginService.logout();
        return R.ok();
    }

    /**
     * 登录入参（13 §4.1：{clientId, grantType:"sms", phone, smsCode}）
     */
    @Data
    public static class AppLoginBody {

        private String clientId;

        /**
         * 本期仅支持 sms（13 §4.1）
         */
        private String grantType;

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        private String phone;

        @NotBlank(message = "短信验证码不能为空")
        private String smsCode;

    }

}
