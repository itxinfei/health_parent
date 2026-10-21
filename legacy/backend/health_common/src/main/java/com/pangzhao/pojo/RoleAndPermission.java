package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 戴金华
 * @date 2019-12-02 10:21
 */
@Data
@ToString
@Table(name = "t_role_permission")
public class RoleAndPermission {

    @Id
    @Column(name = "role_id")
    private Integer roleId;
    @Id
    @Column(name = "permission_id")
    private Integer permissionId;
}
