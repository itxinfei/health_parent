package cn.webdai.service;

import cn.webdai.entity.PageResult;
import cn.webdai.pojo.CheckGroup;
import cn.webdai.pojo.CheckItem;

import java.util.List;

/**
 * @author 戴金华
 * @date 2019-11-08 16:06
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
