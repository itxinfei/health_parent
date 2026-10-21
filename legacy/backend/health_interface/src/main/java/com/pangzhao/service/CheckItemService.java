package com.pangzhao.service;

import com.pangzhao.entity.PageResult;
import com.pangzhao.pojo.CheckItem;

import java.util.List;

/**
 * 体检检查项管理
 */
public interface CheckItemService {

    //根据查询条件分页查询检查项
    PageResult findByPage(Integer currentPage, Integer pageSize, String queryString);

    //添加检查项
    void add(CheckItem checkItem);

    //根据id查询对象
    CheckItem findById(Integer id);

    //修改checkItem
    void update(CheckItem checkItem);

    //删除
    void deleteCheckItem(Integer id);

    //查询所有
    List<CheckItem> findAll();
}
