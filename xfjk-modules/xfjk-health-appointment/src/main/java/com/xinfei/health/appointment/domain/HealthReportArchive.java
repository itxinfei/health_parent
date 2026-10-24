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
 * 报告归档附属资料 t_health_report_archive（07 §2.10）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_report_archive")
public class HealthReportArchive extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    /** 知情同意书 / 检前问卷 / 既往报告 / 影像资料 / 心电图 / 病理报告 */
    @TableField("type")
    private String type;

    @TableField("oss_id")
    private Long ossId;

    @TableField("page_count")
    private Integer pageCount;

    @TableField("upload_by")
    private String uploadBy;

    @TableField("upload_time")
    private LocalDateTime uploadTime;

    @TableField("remark")
    private String remark;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
