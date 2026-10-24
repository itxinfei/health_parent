package com.xinfei.health.appointment.domain.vo;

import com.xinfei.health.appointment.domain.HealthMemberHistory;
import com.xinfei.health.appointment.domain.HealthMemberProfile;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 会员详情视图（07 §3.1：基础 + profile + 病史 + 完整度 + 标签）
 *
 * <p>报告概览段随 B7 报告侧表落地后补充。
 *
 * @author xinfei
 */
@Data
public class MemberDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private MemberVo member;

    private HealthMemberProfile profile;

    private List<HealthMemberHistory> histories;

    private MemberCompletenessVo completeness;

}
