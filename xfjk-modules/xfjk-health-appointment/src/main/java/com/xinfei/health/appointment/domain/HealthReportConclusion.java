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

/**
 * 报告结论条目 t_health_report_conclusion（07 §2.9）
 *
 * <p>末次语义（18 F7）：同一报告仅一组 final_flag=1，改结论时旧组置 0、新组置 1。
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_report_conclusion")
public class HealthReportConclusion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    @TableField("conclusion_dict_id")
    private Long conclusionDictId;

    @TableField("conclusion_type")
    private String conclusionType;

    @TableField("item_ids")
    private String itemIds;

    @TableField("advice_ids")
    private String adviceIds;

    @TableField("grade")
    private String grade;

    @TableField("merge_group_id")
    private String mergeGroupId;

    @TableField("final_flag")
    private String finalFlag;

    @TableField("sort_no")
    private Integer sortNo;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

}
