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
 * 会员-标签关联 t_health_member_tag_rel（07 §2.12，唯一 (tag_id, member_id)）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_tag_rel")
public class HealthMemberTagRel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("tag_id")
    private Long tagId;

    @TableField("member_id")
    private Integer memberId;

    /**
     * 评估 / 报告 / 人工（B6 只写「人工」）
     */
    @TableField("source")
    private String source;

    @TableField("source_id")
    private Long sourceId;

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

    public static final String SOURCE_MANUAL = "人工";

}
