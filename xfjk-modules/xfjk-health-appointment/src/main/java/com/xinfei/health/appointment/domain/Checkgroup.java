package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 检查组 t_checkgroup（原库冻结表，禁止增删列）
 *
 * @author xinfei
 */
@Data
@TableName("t_checkgroup")
public class Checkgroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（原库 int auto_increment）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 检查组编码（唯一）
     */
    @TableField("code")
    private String code;

    /**
     * 检查组名称
     */
    @TableField("name")
    private String name;

    /**
     * 助记码（驼峰列）
     */
    @TableField("helpCode")
    private String helpCode;

    /**
     * 性别限制（0 不限 1 男 2 女）
     */
    @TableField("sex")
    private String sex;

    /**
     * 说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 注意事项
     */
    @TableField("attention")
    private String attention;

}
