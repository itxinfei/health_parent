package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 禁忌规则新增/编辑入参（08 §4.5：/health/forbids）
 *
 * @author xinfei
 */
@Data
public class ForbidBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（编辑时必填）
     */
    private Long id;

    /**
     * 规则编码，全局唯一，锚定 18 E 组
     */
    @NotBlank(message = "规则编码不能为空")
    @Size(max = 32, message = "规则编码不能超过 32 字符")
    private String ruleCode;

    /**
     * 作用范围：CHECKITEM/CHECKGROUP/SETMEAL
     */
    @NotBlank(message = "作用范围不能为空")
    private String scope;

    /**
     * 按 scope 关联冻结表主键
     */
    @NotNull(message = "目标 id 不能为空")
    private Integer targetId;

    /**
     * 触发条件 JSON，键固定 8 个（08 §2.7），未知键 5013
     */
    @NotBlank(message = "触发条件不能为空")
    @Size(max = 500, message = "触发条件不能超过 500 字符")
    private String conditionJson;

    /**
     * 处置档：0 拦截 / 1 提示 / 2 延后补检
     */
    @NotBlank(message = "处置档不能为空")
    private String action;

    /**
     * 可读原因，必须能直接展示给用户
     */
    @NotBlank(message = "提示文案不能为空")
    @Size(max = 255, message = "提示文案不能超过 255 字符")
    private String tipText;

    /**
     * 优先级，冲突时小者生效
     */
    private Integer priority;

    /**
     * 是否启用：1 启用 / 0 停用
     */
    private String enabled;

}
