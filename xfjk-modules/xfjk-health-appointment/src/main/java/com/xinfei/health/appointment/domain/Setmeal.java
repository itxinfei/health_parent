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
 * 套餐 t_setmeal（原库冻结表，禁止增删列）
 *
 * <p>img 列原存七牛文件名，新系统存框架 OSS 对象主键 ossId 字符串；
 * 列宽 varchar(128) 足够，语义等价，不改结构（03 §10、04 §2）。
 *
 * @author xinfei
 */
@Data
@TableName("t_setmeal")
public class Setmeal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（原库 int auto_increment）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 套餐名称
     */
    @TableField("name")
    private String name;

    /**
     * 套餐编码（唯一）
     */
    @TableField("code")
    private String code;

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
     * 说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 注意事项
     */
    @TableField("attention")
    private String attention;

    /**
     * 图片：OSS 对象主键 ossId 字符串
     */
    @TableField("img")
    private String img;

}
