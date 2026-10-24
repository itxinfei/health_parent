package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员停用/恢复入参（07 §5.8，member_status 0↔1）
 *
 * @author xinfei
 */
@Data
public class MemberStatusBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标状态（sys_normal_disable）：0 正常 / 1 停用
     */
    @NotNull(message = "会员状态不能为空")
    private String memberStatus;

}
