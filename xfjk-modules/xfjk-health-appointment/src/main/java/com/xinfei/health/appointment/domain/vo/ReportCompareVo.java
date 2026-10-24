package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 历年同项趋势行（07 §3.1 GET /health/members/{id}/compare，18 D11/G1）
 *
 * @author xinfei
 */
@Data
public class ReportCompareVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long reportId;

    private String reportNo;

    private LocalDate checkDate;

    private Integer checkitemId;

    private String itemName;

    private String result;

    private String unit;

    private String refRange;

    private String abnormalFlag;

}
