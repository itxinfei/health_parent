package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限
 */
@Data
@ToString
@Table(name = "t_permission")
public class Permission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name; // 权限名称
    @Column(name = "keyword")
    private String keyword; // 权限关键字，用于权限控制
    @Column(name = "description")
    private String description; // 描述

}
