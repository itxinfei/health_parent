package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 禁忌规则 t_health_checkitem_forbid（08 §2.7）
 *
 * <p>rule_code 语义锚定 18 §3.E；处置档 action 只有 0/1/2 三值，禁止新增。
 * E10（入职禁查乙肝两对半）不入本表，作为知识规则存在。
 *
 * @author xinfei
 */
@Data
@TableName("t_health_checkitem_forbid")
public class HealthCheckitemForbid implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作用范围常量（08 §2.7）
     */
    public static final String SCOPE_CHECKITEM = "CHECKITEM";
    public static final String SCOPE_CHECKGROUP = "CHECKGROUP";
    public static final String SCOPE_SETMEAL = "SETMEAL";

    /**
     * 雪花主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则编码，全局唯一，锚定 18 E 组
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 作用范围：CHECKITEM/CHECKGROUP/SETMEAL
     */
    @TableField("scope")
    private String scope;

    /**
     * 按 scope 关联冻结表主键
     */
    @TableField("target_id")
    private Integer targetId;

    /**
     * 触发条件 JSON，键固定 8 个（08 §2.7），未知键 5013
     */
    @TableField("condition_json")
    private String conditionJson;

    /**
     * 处置档：0 拦截 / 1 提示 / 2 延后补检
     */
    @TableField("action")
    private String action;

    /**
     * 可读原因，必须能直接展示给用户
     */
    @TableField("tip_text")
    private String tipText;

    /**
     * 优先级，冲突时小者生效
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 是否启用：1 启用 / 0 停用
     */
    @TableField("enabled")
    private String enabled;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}
