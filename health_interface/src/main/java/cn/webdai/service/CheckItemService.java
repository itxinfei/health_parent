package cn.webdai.service;

import cn.webdai.entity.PageResult;
import cn.webdai.pojo.CheckItem;

import java.util.List;

/**
 * @author 戴金华
 * @date 2019-11-07 18:55
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
