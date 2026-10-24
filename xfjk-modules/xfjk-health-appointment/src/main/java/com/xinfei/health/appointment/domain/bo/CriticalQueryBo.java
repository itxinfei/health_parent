package com.xinfei.health.appointment.domain.bo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 危急值分页查询入参（07 §3.2.1 逐字段）
 *
 * @author xinfei
 */
@Data
public class CriticalQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 派生筛选：false=未处理（handle_result 空），true=已处理，不传=全部 */
    private Boolean handled;

    /** 派生筛选：按 receiver_confirm_time 是否为空 */
    private Boolean confirmed;

    /** 六码闭集，传其它值返回空集并提示（07 §3.2.1） */
    private String ruleCode;

    private String archiveNo;

    /** 仅后台可用 */
    private Long memberId;

    private String receiver;

    private String notifyWay;

    /** 作用于 report_time，闭区间；格式 yyyy-MM-dd HH:mm:ss */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long deptId;

}
