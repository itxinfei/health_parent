package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 禁忌试算入参（08 §6.2：setmealId + 人员四维；13 §4.2 forbid-preview 同构）
 *
 * @author xinfei
 */
@Data
public class ForbidCheckBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐 id
     */
    @NotNull(message = "套餐不能为空")
    private Integer setmealId;

    /**
     * 性别 0 不限 / 1 男 / 2 女
     */
    private String sex;

    /**
     * 婚况 0 不限 / 1 已婚 / 2 未婚
     */
    private String married;

    /**
     * 性生活史 0 不限 / 1 有 / 2 无
     */
    private String hasSexHistory;

    /**
     * 妊娠状态 0 未孕 / 1 可能妊娠 / 3 妊娠
     */
    private String pregnancy;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 经期中
     */
    private Boolean inMenstruation;

    /**
     * 体内金属植入物
     */
    private Boolean metalImplant;

}
