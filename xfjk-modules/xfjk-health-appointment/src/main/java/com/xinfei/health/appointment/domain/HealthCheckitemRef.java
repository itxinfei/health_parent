package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检查项参考区间 t_health_checkitem_ref（1:N，08 §2.3）
 *
 * <p>存在理由：参考区间按性别 + 年龄段 + 生理状态多套匹配（18 D2）。
 * 匹配不到任何区间时仅存原值并标记「区间未配置」，不得默认判正常。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_checkitem_ref")
public class HealthCheckitemRef implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("checkitem_id")
    private Integer checkitemId;

    /**
     * 0 通用 / 1 男 / 2 女
     */
    @TableField("sex")
    private String sex;

    /**
     * 年龄下限（闭区间，NULL 不限）
     */
    @TableField("age_min")
    private Integer ageMin;

    /**
     * 年龄上限（闭区间，NULL 不限）
     */
    @TableField("age_max")
    private Integer ageMax;

    /**
     * 0 通用 / 1 妊娠 / 2 备孕
     */
    @TableField("physio_state")
    private String physioState;

    /**
     * 区间原文（如 3.5~9.5×10⁹/L），报告直接展示
     */
    @TableField("ref_text")
    private String refText;

    /**
     * 下限（定性项可空）
     */
    @TableField("ref_min")
    private BigDecimal refMin;

    /**
     * 上限（定性项可空）
     */
    @TableField("ref_max")
    private BigDecimal refMax;

    /**
     * 多条命中时取最小值（精确匹配优先于通用）
     */
    @TableField("priority")
    private Integer priority;

    @TableField("create_time")
    private LocalDateTime createTime;

}
