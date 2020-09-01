package com.pangzhao.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pangzhao.entity.PageResult;
import com.pangzhao.mapper.*;
import com.pangzhao.pojo.*;
import com.pangzhao.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Service(interfaceClass = SetMealService.class)
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private CheckGroupMapper checkGroupMapper;
    @Autowired
    private SetMealAndCheckgroupMapper setMealAndCheckgroupMapper;
    @Autowired
    private CheckGroupAndCheckItemMapper checkGroupAndCheckItemMapper;
    @Autowired
    private CheckItemMapper checkItemMapper;

    @Override
    public PageResult findByPage(Integer currentPage, Integer pageSize, String queryString) {

        Page<Setmeal> setmeals;

        if (queryString != null && queryString != "") {
            //如果查询条件不为空
            Example example = new Example(Setmeal.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.orLike("name", "%" + queryString + "%").orLike("code", "%" + queryString + "%").orLike("helpCode", "%" + queryString + "%");
            PageHelper.startPage(currentPage, pageSize);
            setmeals = (Page<Setmeal>) setMealMapper.selectByExample(example);
        } else {
            PageHelper.startPage(currentPage, pageSize);
            setmeals = (Page<Setmeal>) setMealMapper.selectAll();
        }
        return new PageResult(setmeals.getTotal(), setmeals.getResult());
    }

    //添加套餐
    @Transactional
    @Override
    public void add(Setmeal setmeal, Integer[] ids) {
        //添加套餐
        setMealMapper.insert(setmeal);
        //添加套餐所属的检查项
        Setmeal setMealFinal = setMealMapper.selectOne(setmeal);
        Integer setMealId = setMealFinal.getId();
        for (Integer id : ids) {
            setMealAndCheckgroupMapper.insert(new SetMealAndCheckgroup(setMealId, id));
        }
    }

    //根据id查询
    @Override
    public Setmeal findById(Integer id) {
        Setmeal setmeal = setMealMapper.selectByPrimaryKey(id);
        //根据id查询套餐对应的检查组
        Example example = new Example(SetMealAndCheckgroup.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("setMealId", id);
        List<SetMealAndCheckgroup> setMealAndCheckgroups = setMealAndCheckgroupMapper.selectByExample(example);
        ArrayList<Integer> checkGroupIds = new ArrayList<>();
        for (SetMealAndCheckgroup setMealAndCheckgroup : setMealAndCheckgroups) {
            checkGroupIds.add(setMealAndCheckgroup.getCheckGroupId());
        }
        ArrayList<CheckGroup> checkGroups = new ArrayList<>();
        ArrayList<Integer> checkItemsIds = new ArrayList<>();   //检查项的id
        ArrayList<CheckItem> checkItems = new ArrayList<>();
        //根据id查询检查组成
        for (Integer checkgroupId : checkGroupIds) {
            CheckGroup checkGroup = checkGroupMapper.selectByPrimaryKey(checkgroupId);
            Example example1 = new Example(CheckGroupAndCheckItem.class);
            Example.Criteria criteria1 = example1.createCriteria();
            criteria1.andEqualTo("checkGroupId", checkgroupId);
            //根据id查询检查项
            List<CheckGroupAndCheckItem> checkGroupAndCheckItems = checkGroupAndCheckItemMapper.selectByExample(example1);
            for (CheckGroupAndCheckItem checkGroupAndCheckItem : checkGroupAndCheckItems) {
                checkItemsIds.add(checkGroupAndCheckItem.getCheckItemId());
            }

            for (Integer checkItemsId : checkItemsIds) {
                checkItems.add(checkItemMapper.selectByPrimaryKey(checkItemsId));
            }

            checkGroup.setCheckItemList(checkItems);

            checkGroups.add(checkGroup);
        }

        setmeal.setCheckGroups(checkGroups);

        return setmeal;
    }

    @Transactional
    //根据id查询检查组
    @Override
    public List<CheckGroup> findCheckGroupById(Integer id) {
        Example example = new Example(SetMealAndCheckgroup.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("setMealId", id);
        List<SetMealAndCheckgroup> setMealAndCheckgroups = setMealAndCheckgroupMapper.selectByExample(example);
        ArrayList<Integer> ids = new ArrayList<>();

        for (SetMealAndCheckgroup setMealAndCheckgroup : setMealAndCheckgroups) {
            ids.add(setMealAndCheckgroup.getCheckGroupId());
        }

        ArrayList<CheckGroup> checkGroups = new ArrayList<>();
        for (Integer integer : ids) {
            CheckGroup checkGroup = checkGroupMapper.selectByPrimaryKey(integer);
            checkGroups.add(checkGroup);
        }
        return checkGroups;
    }

    //更新套餐
    @Transactional
    @Override
    public void update(Setmeal setmeal, Integer[] ids) {

        //更新套餐
        setMealMapper.updateByPrimaryKey(setmeal);

        //更新套餐所属检查组
        Example example = new Example(SetMealAndCheckgroup.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("setMealId", setmeal.getId()).andNotIn("checkGroupId", Arrays.asList(ids));
        List<SetMealAndCheckgroup> setMealAndCheckgroups = setMealAndCheckgroupMapper.selectByExample(example);
        for (SetMealAndCheckgroup setMealAndCheckgroup : setMealAndCheckgroups) {
            setMealAndCheckgroupMapper.delete(setMealAndCheckgroup);
        }
    }

    //删除套餐
    @Transactional
    @Override
    public void delete(Integer id) {
        //先删除中间表
        Example example = new Example(SetMealAndCheckgroup.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("setMealId", id);
        List<SetMealAndCheckgroup> setMealAndCheckgroups = setMealAndCheckgroupMapper.selectByExample(example);
        for (SetMealAndCheckgroup setMealAndCheckgroup : setMealAndCheckgroups) {
            setMealAndCheckgroupMapper.delete(setMealAndCheckgroup);
        }

        //再删除套餐表
        setMealMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Setmeal> findAll() {
        return setMealMapper.selectAll();
    }

}