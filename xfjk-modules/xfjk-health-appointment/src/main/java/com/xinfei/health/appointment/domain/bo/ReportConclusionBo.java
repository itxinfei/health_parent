package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 结论条目入参（07 §2.9、§5.4）
 *
 * @author xinfei
 */
@Data
public class ReportConclusionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 引用 t_health_conclusion_dict.id（6011） */
    @NotNull(message = "结论词不能为空")
    private Long conclusionDictId;

    /** 疾病诊断 / 阳性发现 / 其他结论 */
    private String conclusionType;

    /** 来源明细 id 列表 */
    private List<Long> itemIds;

    /** 关联处理意见模板 id 列表（11 建议模板库，B8 启用） */
    private List<Long> adviceIds;

    private String grade;

    /** 合并组（18 D9） */
    private String mergeGroupId;

    private Integer sortNo;

}
