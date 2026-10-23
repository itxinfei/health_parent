package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 套餐图片上传结果
 *
 * <p>ossId 落库到 t_setmeal.img，url 供页面立即回显。
 *
 * @author xinfei
 */
@Data
public class SetmealImageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * OSS 对象主键，保存到套餐时回填 img
     */
    private String ossId;

    /**
     * 可直接访问的图片地址
     */
    private String url;

}
