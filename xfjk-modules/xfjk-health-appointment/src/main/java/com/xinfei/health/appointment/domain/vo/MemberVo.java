package com.xinfei.health.appointment.domain.vo;

import com.xinfei.health.appointment.domain.HealthMemberTag;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员视图（07 §3.1：基础信息 + 状态 + 档案号 + 标签；身份证/手机号脱敏输出）
 *
 * @author xinfei
 */
@Data
public class MemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 档案号（冗余展示，真值在 t_health_member_archive_no）
     */
    private String fileNumber;

    private String name;

    private String sex;

    /**
     * 身份证号（脱敏后输出，07 §9）
     */
    private String idCard;

    /**
     * 手机号（脱敏后输出）
     */
    private String phoneNumber;

    private LocalDate birthday;

    private String email;

    private String remark;

    private LocalDate regTime;

    /**
     * 会员状态（sys_normal_disable）：0 正常 / 1 停用；无 profile 行按 0
     */
    private String memberStatus;

    /**
     * 完整度「已填/应填」（07 §5.2 实时计算）
     */
    private String completeness;

    private LocalDateTime createTime;

    /**
     * 标签（详情页填充；列表页可空）
     */
    private List<HealthMemberTag> tags;

}
