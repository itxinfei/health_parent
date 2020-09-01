package com.pangzhao.service;

import com.pangzhao.entity.PageResult;
import com.pangzhao.pojo.CheckGroup;
import com.pangzhao.pojo.Setmeal;

import java.util.List;
/**
 *
 */
public interface SetMealService {

    //分页查找套餐
    PageResult findByPage(Integer currentPage, Integer pageSize, String queryString);

    //添加套餐
    void add(Setmeal setmeal, Integer[] ids);

    //根据id查询
    Setmeal findById(Integer id);

    //根据id查询检查组
    List<CheckGroup> findCheckGroupById(Integer id);

    //更新套餐
    void update(Setmeal setmeal, Integer[] ids);

    //删除套餐
    void delete(Integer id);

    //查询所有
    List<Setmeal> findAll();

}
