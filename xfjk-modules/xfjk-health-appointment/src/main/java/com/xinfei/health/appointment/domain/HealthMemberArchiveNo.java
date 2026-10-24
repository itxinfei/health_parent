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
 * 会员档案号 t_health_member_archive_no（一号制，07 §2.2）
 *
 * <p>档案号唯一性只认本表；t_member.fileNumber 仅作冗余展示。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_member_archive_no")
public class HealthMemberArchiveNo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 t_member.id（唯一索引）
     */
    @TableField("member_id")
    private Integer memberId;

    /**
     * 档案号：FA + 6位区划 + yyyyMMdd + 5位序号
     */
    @TableField("file_number")
    private String fileNumber;

    /**
     * 建档来源（字典 health_bind_source）：注册 / 团检导入 / 人工建档
     */
    @TableField("bind_source")
    private String bindSource;

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

    /**
     * 建档来源常量（字典 health_bind_source）
     */
    public static final String SOURCE_REGISTER = "注册";
    public static final String SOURCE_BATCH_IMPORT = "团检导入";
    public static final String SOURCE_MANUAL = "人工建档";

}
