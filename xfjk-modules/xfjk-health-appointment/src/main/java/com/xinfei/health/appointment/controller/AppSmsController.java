package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.RandomUtil;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.ratelimiter.annotation.RateLimiter;
import com.xinfei.common.ratelimiter.enums.LimitType;
import com.xinfei.common.redis.utils.RedisUtils;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.constant.HealthCacheConstants;
import com.xinfei.health.appointment.sms.SmsSender;
import com.xinfei.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * C 端业务短信（05 §6、12 §7.2：业务侧自实现，禁止复用框架 /resource/sms/code）
 *
 * <p>验证码位数与有效期读 sys_config 键 health:sms:code_length / health:sms:expire_minutes，
 * 5 分钟一次性，消费在下单事务内（13 R5）。
 *
 * @author xinfei
 */
@Validated
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/sms")
@Tag(name = "C端-业务短信")
public class AppSmsController extends BaseController {

    /**
     * 同一来源发码限流 60 秒 1 次（05 §7；框架 LimitType 仅 IP/CLUSTER，按 IP 粒度）
     */
    private static final int RATE_LIMIT_COUNT = 1;
    private static final int RATE_LIMIT_SECONDS = 60;

    /**
     * sys_config 缺省兜底：位数 4、有效期 5 分钟（12 §4.3）
     */
    private static final int DEFAULT_CODE_LENGTH = 4;
    private static final int DEFAULT_EXPIRE_MINUTES = 5;

    private final SmsSender smsSender;
    private final ISysConfigService configService;

    /**
     * 发送下单验证码（键 health:sms:order:{phone}）
     */
    @Operation(summary = "发送下单验证码")
    @RateLimiter(count = RATE_LIMIT_COUNT, time = RATE_LIMIT_SECONDS, limitType = LimitType.IP)
    @PostMapping("/order")
    public R<Void> sendOrderCode(@NotBlank(message = "手机号不能为空")
                                 @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String phone) {
        sendCode("order", HealthCacheConstants.SMS_ORDER_KEY, phone);
        return R.ok();
    }

    /**
     * 发送登录验证码（键 health:sms:login:{phone}；13 §4.1 /app/sms/login）
     */
    @Operation(summary = "发送登录验证码")
    @RateLimiter(count = RATE_LIMIT_COUNT, time = RATE_LIMIT_SECONDS, limitType = LimitType.IP)
    @PostMapping("/login")
    public R<Void> sendLoginCode(@NotBlank(message = "手机号不能为空")
                                 @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String phone) {
        sendCode("login", HealthCacheConstants.SMS_LOGIN_KEY, phone);
        return R.ok();
    }

    /**
     * 发码公共链路：读配置生成验证码 → Redis 一次性 → 发送器（开发环境日志出码）
     */
    private void sendCode(String scene, String keyPrefix, String phone) {
        int length = readIntConfig("health:sms:code_length", DEFAULT_CODE_LENGTH);
        int expireMinutes = readIntConfig("health:sms:expire_minutes", DEFAULT_EXPIRE_MINUTES);
        String code = RandomUtil.randomNumbers(length);
        RedisUtils.setCacheObject(keyPrefix + phone, code, Duration.ofMinutes(expireMinutes));
        smsSender.sendCode(scene, phone, code);
    }

    /**
     * 读 sys_config 数值键，缺失或非法时回落默认值
     */
    private int readIntConfig(String key, int defaultValue) {
        try {
            String value = configService.selectConfigByKey(key);
            return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
