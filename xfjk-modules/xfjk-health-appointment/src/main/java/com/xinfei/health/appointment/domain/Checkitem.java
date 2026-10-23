package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 检查项 t_checkitem（原库冻结表，禁止增删列）
 *
 * <p>列名全小写但表内混用驼峰列，按 03 §2.1 每个字段显式标注 @TableField。
 *
 * @author xinfei
 */
@Data
@TableName("t_checkitem")
public class Checkitem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（原库 int auto_increment）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 检查项编码（唯一）
     */
    @TableField("code")
    private String code;

    /**
     * 检查项名称
     */
    @TableField("name")
    private String name;

    /**
     * 性别限制（0 不限 1 男 2 女）
     */
    @TableField("sex")
    private String sex;

    /**
     * 年龄范围
     */
    @TableField("age")
    private String age;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 类型（1 检查 2 检验）
     */
    @TableField("type")
    private String type;

    /**
     * 注意事项
     */
    @TableField("attention")
    private String attention;

    /**
     * 说明
     */
    @TableField("remark")
    private String remark;

}
