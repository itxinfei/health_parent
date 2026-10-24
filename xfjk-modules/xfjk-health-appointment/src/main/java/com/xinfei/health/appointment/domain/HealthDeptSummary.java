package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xinfei.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 科室小结 t_health_dept_summary（07 §2.8）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_dept_summary")
public class HealthDeptSummary extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("summary_text")
    private String summaryText;

    @TableField("print_label")
    private String printLabel;

    @TableField("last_modify_by")
    private String lastModifyBy;

    @TableField("last_modify_time")
    private LocalDateTime lastModifyTime;

    @TableField("submit_flag")
    private String submitFlag;

    @TableField("send_back_flag")
    private String sendBackFlag;

    @TableField("send_back_reason")
    private String sendBackReason;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    public static final String YES = "1";
    public static final String NO = "0";

}
