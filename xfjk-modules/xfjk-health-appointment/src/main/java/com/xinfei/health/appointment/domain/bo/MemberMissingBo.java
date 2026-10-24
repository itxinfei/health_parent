package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 档案缺项标记入参（07 §2.4：标记=已确认缺失，与「留空=未采集」区分）
 *
 * @author xinfei
 */
@Data
public class MemberMissingBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缺失字段名或病史类型（如 history.family）
     */
    @NotBlank(message = "缺项字段名不能为空")
    private String fieldKey;

    /**
     * 缺失原因：受检者不知 / 拒绝提供 / 历史遗失
     */
    private String reason;

}
