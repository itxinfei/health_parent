package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * C 端登录返回（05 §4 / 13 §4.1：accessToken/expireIn/clientId）
 *
 * @author xinfei
 */
@Data
public class AppLoginVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权令牌
     */
    private String accessToken;

    /**
     * 令牌有效期（秒）
     */
    private Long expireIn;

    /**
     * 应用 id（原样返回）
     */
    private String clientId;

}
