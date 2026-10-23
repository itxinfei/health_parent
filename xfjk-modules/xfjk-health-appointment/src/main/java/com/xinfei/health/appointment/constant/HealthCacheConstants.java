package com.xinfei.health.appointment.constant;

/**
 * 预约管理缓存 Key（口径见 03 §8、08 §4.1，禁止另起键名）
 *
 * @author xinfei
 */
public final class HealthCacheConstants {

    /**
     * 已上传图片集合
     */
    public static final String IMG_UPLOADED = "health:img:uploaded";

    /**
     * 已被业务引用图片集合
     */
    public static final String IMG_USED = "health:img:used";

    /**
     * 下单短信验证码（13 §5.2：键 health:sms:order:{phone}，5 分钟一次性）
     */
    public static final String SMS_ORDER_KEY = "health:sms:order:";

    /**
     * C 端登录验证码（13 R2/12 §7.2：键 health:sms:login:{phone}，一次性，登录成功即删）
     */
    public static final String SMS_LOGIN_KEY = "health:sms:login:";

    private HealthCacheConstants() {
    }

}
