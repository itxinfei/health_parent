package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 检查组-检查项关联 t_checkgroup_checkitem（原库冻结表，联合主键）
 *
 * <p>MP 不支持联合主键，这里只把 checkgroup_id 声明为 INPUT 主键以满足插入语义，
 * 任何按主键的更新/删除都不使用，一律走条件构造器。
 *
 * @author xinfei
 */
@Data
@TableName("t_checkgroup_checkitem")
public class CheckgroupCheckitem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 检查组 id
     */
    @TableId(value = "checkgroup_id", type = IdType.INPUT)
    private Integer checkgroupId;

    /**
     * 检查项 id
     */
    @TableField("checkitem_id")
    private Integer checkitemId;

}
