package com.xinfei.health.appointment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 报告分页查询入参（07 §3.2）
 *
 * @author xinfei
 */
@Data
public class ReportQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 档案号（精确，一号制） */
    private String archiveNo;

    private Integer memberId;

    private Long batchId;

    private String examNo;

    private String status;

    private String source;

    /** 体检日期起（闭区间） */
    private LocalDate beginDate;

    /** 体检日期止（闭区间） */
    private LocalDate endDate;

}
