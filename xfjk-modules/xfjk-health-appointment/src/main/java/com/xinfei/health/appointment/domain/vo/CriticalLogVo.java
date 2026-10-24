package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 危急值清单行（07 §6.5 出参；handled/confirmed 由服务端派生）
 *
 * @author xinfei
 */
@Data
public class CriticalLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reportId;

    private Long reportItemId;

    private Integer memberId;

    private String memberName;

    private String archiveNo;

    private String itemName;

    private String criticalValue;

    private String ruleCode;

    private Long deptId;

    private LocalDateTime reportTime;

    private String receiver;

    private LocalDateTime receiverConfirmTime;

    private String notifyWay;

    private String handleResult;

    /** 派生：handleResult 非空 */
    private Boolean handled;

    /** 派生：receiverConfirmTime 非空（18 D5 无确认视为未送达） */
    private Boolean confirmed;

    private String trackBy;

    private LocalDateTime trackTime;

    private String trackRemark;

}
