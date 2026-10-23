package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * C 端下单返回（13 §4.2：orderId/orderDate/setmealName/memberName/noticeText）
 *
 * @author xinfei
 */
@Data
public class AppOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer orderId;

    /**
     * 会员 id
     */
    private Integer memberId;

    private LocalDate orderDate;

    private String setmealName;

    private String memberName;

    /**
     * 检前须知（套餐 attention，禁忌须知待扩展表落地后并入）
     */
    private String noticeText;

    /**
     * 流程状态（P08 列表展示）
     */
    private String flowStatus;

    /**
     * 延期至日期（P08 列表展示）
     */
    private LocalDate postponeUntil;

}
