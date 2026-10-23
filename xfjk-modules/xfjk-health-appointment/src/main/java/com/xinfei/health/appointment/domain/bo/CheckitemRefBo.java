package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 参考区间入参（08 §2.3、§4.1 refs 子资源）
 *
 * @author xinfei
 */
@Data
public class CheckitemRefBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（编辑区间时必填）
     */
    private Long id;

    /**
     * 0 通用 / 1 男 / 2 女
     */
    private String sex;

    /**
     * 年龄下限（闭区间，NULL 不限）
     */
    private Integer ageMin;

    /**
     * 年龄上限（闭区间，NULL 不限）
     */
    private Integer ageMax;

    /**
     * 0 通用 / 1 妊娠 / 2 备孕
     */
    private String physioState;

    /**
     * 区间原文（报告直接展示）
     */
    @NotBlank(message = "区间原文不能为空")
    private String refText;

    /**
     * 下限
     */
    private BigDecimal refMin;

    /**
     * 上限
     */
    private BigDecimal refMax;

    /**
     * 优先级，多条命中取最小值
     */
    private Integer priority;

}
