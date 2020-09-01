package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 会员
 */
@Data
@ToString
@Table(name = "t_member")
public class Member implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;//主键

    @Column(name = "fileNumber")
    private String fileNumber;//档案号
    @Column(name = "name")
    private String name;//姓名
    @Column(name = "sex")
    private String sex;//性别
    @Column(name = "idCard")
    private String idCard;//身份证号
    @Column(name = "phoneNumber")
    private String phoneNumber;//手机号
    @Column(name = "regTime")
    private Date regTime;//注册时间
    @Column(name = "password")
    private String password;//登录密码
    @Column(name = "email")
    private String email;//邮箱
    @Column(name = "birthday")
    private Date birthday;//出生日期
    @Column(name = "remark")
    private String remark;//备注



}
