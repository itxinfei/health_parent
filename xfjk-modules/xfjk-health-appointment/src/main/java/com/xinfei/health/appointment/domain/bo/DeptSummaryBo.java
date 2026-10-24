package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科室小结提交入参（07 §3.2 PUT /health/reports/{id}/dept-summary）
 *
 * @author xinfei
 */
@Data
public class DeptSummaryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "科室不能为空")
    private Long deptId;

    private String summaryText;

    private String printLabel;

    /** true 提交（submit_flag=1）；false 仅暂存 */
    private Boolean submit;

}
