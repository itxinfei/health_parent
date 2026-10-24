package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员标签 t_health_member_tag（07 §2.12）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_tag")
public class HealthMemberTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("name")
    private String name;

    /**
     * manual / auto / crowd（B6 只启用 manual，自动标签条件留 B8+）
     */
    @TableField("type")
    private String type;

    @TableField("color")
    private String color;

    /**
     * 自动标签条件（B6 只存不解析）
     */
    @TableField("rule_json")
    private String ruleJson;

    /**
     * 1 启用 / 0 停用
     */
    @TableField("enabled")
    private String enabled;

    @TableField("remark")
    private String remark;

    @TableField("create_dept")
    private Long createDept;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
