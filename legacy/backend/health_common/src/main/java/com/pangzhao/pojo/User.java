package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户
 */
@Data
@ToString
@Table(name = "t_user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 主键
    @Column(name = "birthday")
    private Date birthday; // 生日
    @Column(name = "gender")
    private String gender; // 性别
    @Column(name = "username")
    private String username; // 用户名，唯一
    @Column(name = "password")
    private String password; // 密码
    @Column(name = "remark")
    private String remark; // 备注
    @Column(name = "station")
    private String station; // 状态
    @Column(name = "telephone")
    private String telephone; // 联系电话
    private Set<Role> roles = new HashSet<Role>(0);//对应角色集合
}
