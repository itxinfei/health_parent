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
 * 档案缺项标记 t_health_member_missing（07 §2.4）
 *
 * <p>留空 = 未采集；本表行 = 已确认缺失，二者在档案页分别显示，禁止混同。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_missing")
public class HealthMemberMissing implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Integer memberId;

    /**
     * 缺失字段名或病史类型（如 history.family）
     */
    @TableField("field_key")
    private String fieldKey;

    @TableField("reason")
    private String reason;

    @TableField("marked_by")
    private String markedBy;

    @TableField("mark_time")
    private LocalDateTime markTime;

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
