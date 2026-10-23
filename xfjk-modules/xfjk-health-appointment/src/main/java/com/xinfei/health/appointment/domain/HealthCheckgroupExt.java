package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 检查组元数据扩展 t_health_checkgroup_ext（1:1 t_checkgroup，08 §2.4）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_checkgroup_ext")
public class HealthCheckgroupExt implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_checkgroup.id（唯一）
     */
    @TableField("checkgroup_id")
    private Integer checkgroupId;

    /**
     * 承检科室
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 标本
     */
    @TableField("specimen")
    private String specimen;

    /**
     * 1 需双科室分检（18 C3）
     */
    @TableField("dual_dept_flag")
    private String dualDeptFlag;

    /**
     * 组级空腹标记
     */
    @TableField("is_fasting")
    private String isFasting;

    /**
     * 组级默认结论词
     */
    @TableField("default_conclusion_id")
    private Long defaultConclusionId;

    /**
     * 套餐内顺序（指引单打印序）
     */
    @TableField("sort_no")
    private Integer sortNo;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}
