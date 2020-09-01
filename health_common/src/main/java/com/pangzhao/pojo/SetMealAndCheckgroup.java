package com.pangzhao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 戴金华
 * @date 2019-11-10 16:12
 */
@Table(name = "t_setmeal_checkgroup")
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SetMealAndCheckgroup {


    @Id
    @Column(name = "setmeal_id")
    private Integer setMealId;
    @Id
    @Column(name = "checkgroup_id")
    private Integer checkGroupId;
}
