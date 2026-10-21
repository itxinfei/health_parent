package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色
 */
@Data
@ToString
@Table(name = "t_role")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name; // 角色名称
    @Column(name = "keyword")
    private String keyword; // 角色关键字，用于权限控制
    @Column(name = "description")
    private String description; // 描述


    private Set<Permission> permissions = new HashSet<Permission>(0);


}
