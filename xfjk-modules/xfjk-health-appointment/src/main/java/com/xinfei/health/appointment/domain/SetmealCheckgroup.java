package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 套餐-检查组关联 t_setmeal_checkgroup（原库冻结表，联合主键）
 *
 * @author xinfei
 */
@Data
@TableName("t_setmeal_checkgroup")
public class SetmealCheckgroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐 id
     */
    @TableId(value = "setmeal_id", type = IdType.INPUT)
    private Integer setmealId;

    /**
     * 检查组 id
     */
    @TableField("checkgroup_id")
    private Integer checkgroupId;

}
