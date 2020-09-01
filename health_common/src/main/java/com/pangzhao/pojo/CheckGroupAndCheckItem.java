package com.pangzhao.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 戴金华
 * @date 2019-11-08 18:34
 */
@Table(name = "t_checkgroup_checkitem" )
public class CheckGroupAndCheckItem {

    @Id
    @Column(name="checkgroup_id")
    private Integer checkGroupId;
    @Id
    @Column(name = "checkitem_id")
    private Integer checkItemId;

    public Integer getCheckGroupId() {
        return checkGroupId;
    }

    public void setCheckGroupId(Integer checkGroupId) {
        this.checkGroupId = checkGroupId;
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    @Override
    public String toString() {
        return "CheckGroupAndCheckItem{" + "checkGroupId=" + checkGroupId + ", checkItemId=" + checkItemId + '}';
    }

    public CheckGroupAndCheckItem(Integer checkGroupId, Integer checkItemId) {
        this.checkGroupId = checkGroupId;
        this.checkItemId = checkItemId;
    }
}
