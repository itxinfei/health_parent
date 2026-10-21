package com.pangzhao.service.impl;

import com.pangzhao.entity.PageResult;
import com.pangzhao.mapper.CheckGroupAndCheckItemMapper;
import com.pangzhao.mapper.CheckItemMapper;
import com.pangzhao.pojo.CheckGroupAndCheckItem;
import com.pangzhao.pojo.CheckItem;
import com.pangzhao.service.CheckItemService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


/**
 * 体检检查项管理
 */
@Service(interfaceClass = CheckItemService.class)
public class CheckItemServiceImpl implements CheckItemService {

    @Autowired
    private CheckItemMapper checkItemMapper;

    @Autowired
    private CheckGroupAndCheckItemMapper checkGroupAndCheckItemMapper;

    @Override
    public PageResult findByPage(Integer currentPage, Integer pageSize, String queryString) {

        Page<CheckItem> checkItems;

        if (queryString != "" && queryString != null) {
            //如果查询参数不为空
            Example example = new Example(CheckItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.orLike("code", "%" + queryString + "%").orLike("name", "%" + queryString + "%");
            PageHelper.startPage(currentPage, pageSize);
            checkItems = (Page<CheckItem>) checkItemMapper.selectByExample(example);
        } else {
            //如果参数为空
            PageHelper.startPage(currentPage, pageSize);
            checkItems = (Page<CheckItem>) checkItemMapper.selectAll();
        }
        return new PageResult(checkItems.getTotal(), checkItems.getResult());
    }

    /**
     * 添加检查项
     *
     * @param checkItem
     */
    @Override
    public void add(CheckItem checkItem) {
        checkItemMapper.insert(checkItem);
    }

    /**
     * 根据id查询对象
     *
     * @param id
     * @return
     */
    @Override
    public CheckItem findById(Integer id) {
        return checkItemMapper.selectByPrimaryKey(id);
    }

    /**
     * 修改checkItem
     *
     * @param checkItem
     */
    @Override
    public void update(CheckItem checkItem) {
        checkItemMapper.updateByPrimaryKeySelective(checkItem);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void deleteCheckItem(Integer id) {
        Example example = new Example(CheckGroupAndCheckItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("checkItemId", id);
        int count = checkGroupAndCheckItemMapper.selectCountByExample(example);
        if (count > 0) {
            throw new RuntimeException("该检查项是某一检查组的内容,不可删除");
        }
        checkItemMapper.deleteByPrimaryKey(id);
    }

    //查询所有
    @Override
    public List<CheckItem> findAll() {
        return checkItemMapper.selectAll();
    }
}
