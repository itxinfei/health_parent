package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 戴金华
 * @date 2019-12-02 9:55
 */
@Data
@ToString
@Table(name = "t_user_role")
public class UserAndRole {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "role_id")
    private Integer roleId;
}
