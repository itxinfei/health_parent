package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 归档附属资料入参（07 §3.2 POST /health/reports/{id}/archives、§2.10）
 *
 * @author xinfei
 */
@Data
public class ArchiveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 知情同意书/检前问卷/既往报告/影像资料/心电图/病理报告 */
    @NotBlank(message = "归档类型不能为空")
    private String type;

    private Long ossId;

    private Integer pageCount;

    private String remark;

}
