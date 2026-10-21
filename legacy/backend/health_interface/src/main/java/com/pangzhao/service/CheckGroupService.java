package com.pangzhao.service;

import com.pangzhao.entity.PageResult;
import com.pangzhao.pojo.CheckGroup;
import com.pangzhao.pojo.CheckItem;

import java.util.List;

/**
 *
 */
public interface CheckGroupService {

    //分页查找
    PageResult findByPage(Integer currentPage, Integer pageSize, String queryString);

    //根据id查询检查项
    List<CheckItem> findById(Integer id);

    //获取检查组
    CheckGroup findCheckGroupById(Integer id);

    //添加检查组
    void addCheckGroup(CheckGroup checkGroup, Integer[] ids);

    //更新检查组
    void updateCheckGroup(CheckGroup checkGroup, Integer[] ids);

    //删除检查组
    void deleteCheckGroup(Integer id);

    List<CheckGroup> findAll();
}
