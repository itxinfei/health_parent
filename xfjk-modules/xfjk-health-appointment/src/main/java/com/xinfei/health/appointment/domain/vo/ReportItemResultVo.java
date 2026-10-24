package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单项结果录入响应（07 §6.2：判定结果即时回显）
 *
 * @author xinfei
 */
@Data
public class ReportItemResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String abnormalFlag;

    private String refRange;

    private String refConfigured;

    private String criticalFlag;

    private String abnormalGrade;

    private String majorPositive;

    private String edgeFlag;

    /** 录入提示：偏高/偏低/临界/危急值/复查建议（18 D3/D8） */
    private String tip;

}
