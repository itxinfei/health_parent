package com.xinfei.health.appointment.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 业务短信发送器（05 §6、12 §7.2）
 *
 * <p>真实短信触达待厂商密钥（17 待确认）；开发/测试环境从服务日志取码完成闭环，
 * 禁止复用框架 /resource/sms/code 接口充当业务验证码。
 *
 * @author xinfei
 */
@Slf4j
@Component
public class SmsSender {

    /**
     * 发送业务短信；厂商接入后在此分场景对接真实通道
     *
     * @param scene 场景（order / login）
     * @param phone 收件手机号
     * @param code  验证码
     */
    public void sendCode(String scene, String phone, String code) {
        // TODO 厂商密钥到位后替换为真实通道（12 §7.2）
        log.info("[业务短信-模拟] scene={} phone={} code={}", scene, phone, code);
    }

}
