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
 * 检查项元数据扩展 t_health_checkitem_ext（1:1 t_checkitem，08 §2.2）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_checkitem_ext")
public class HealthCheckitemExt implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_checkitem.id（唯一）
     */
    @TableField("checkitem_id")
    private Integer checkitemId;

    /**
     * 结果类型：0 数值 / 1 定性（阴阳）/ 2 文本描述 / 3 影像结论（18 D1）
     */
    @TableField("result_type")
    private String resultType;

    /**
     * 单位；result_type=0 时必填
     */
    @TableField("unit")
    private String unit;

    /**
     * 所属科室，关联 sys_dept（除分检与工作量统计，18 G3）
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 标本类型（字典 health_specimen）
     */
    @TableField("specimen")
    private String specimen;

    /**
     * 1 需空腹（18 E6）
     */
    @TableField("is_fasting")
    private String isFasting;

    /**
     * 1 含射线（18 E1/E8）
     */
    @TableField("is_radiation")
    private String isRadiation;

    /**
     * 1 必检，不允许弃检
     */
    @TableField("is_required")
    private String isRequired;

    /**
     * 1 需两个科室分别完成分检（18 C3）
     */
    @TableField("dual_dept_flag")
    private String dualDeptFlag;

    /**
     * 组内排序
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 默认结论词，引用 t_health_conclusion_dict.id（18 F9）
     */
    @TableField("default_conclusion_id")
    private Long defaultConclusionId;

    /**
     * 检前须知条目，供 13 展示
     */
    @TableField("notice_text")
    private String noticeText;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}
