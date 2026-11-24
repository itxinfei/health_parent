package com.xinfei.health.assessment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 会员-人群关联 t_health_member_crowd（09 §2.12，无审计列）
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_crowd")
public class MemberCrowd {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("crowd_id")
    private Long crowdId;

    @TableField("source_record_id")
    private Long sourceRecordId;

    @TableField("join_time")
    private LocalDateTime joinTime;

}
