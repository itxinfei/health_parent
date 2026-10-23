package com.xinfei.health.appointment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会员 t_member（原库冻结表，禁止增删列）
 *
 * <p>本模块仅用于下单时的「姓名 + 身份证」识别与注册（08 §5.3 步骤 4）；
 * 会员管理主入口在 07 号文档模块，创建后如迁移以该处为准。
 *
 * @author xinfei
 */
@Data
@TableName("t_member")
public class Member implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 档案号（驼峰列）
     */
    @TableField("fileNumber")
    private String fileNumber;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 性别
     */
    @TableField("sex")
    private String sex;

    /**
     * 身份证号（驼峰列）
     */
    @TableField("idCard")
    private String idCard;

    /**
     * 手机号（驼峰列）
     */
    @TableField("phoneNumber")
    private String phoneNumber;

    /**
     * 注册时间（驼峰列）
     */
    @TableField("regTime")
    private LocalDate regTime;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("birthday")
    private LocalDate birthday;

    @TableField("remark")
    private String remark;

}
