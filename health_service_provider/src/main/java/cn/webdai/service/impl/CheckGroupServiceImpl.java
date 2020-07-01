package cn.webdai.service.impl;

import cn.webdai.entity.PageResult;
import cn.webdai.mapper.CheckGroupAndCheckItemMapper;
import cn.webdai.mapper.CheckGroupMapper;
import cn.webdai.mapper.CheckItemMapper;
import cn.webdai.mapper.SetMealAndCheckgroupMapper;
import cn.webdai.pojo.CheckGroup;
import cn.webdai.pojo.CheckGroupAndCheckItem;
import cn.webdai.pojo.CheckItem;
import cn.webdai.pojo.SetMealAndCheckgroup;
import cn.webdai.service.CheckGroupService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author 戴金华
 * @date 2019-11-08 16:11
 */
@Service(interfaceClass = CheckGroupService.class)
public class CheckGroupServiceImpl implements CheckGroupService {

    @Autowired
    private CheckGroupMapper checkGroupMapper;
    @Autowired
    private CheckGroupAndCheckItemMapper checkGroupAndCheckItemMapper;
    @Autowired
    private CheckItemMapper checkItemMapper;
    @Autowired
    private SetMealAndCheckgroupMapper setMealAndCheckgroupMapper;

    @Override
    public PageResult findByPage(Integer currentPage, Integer pageSize, String queryString) {

        Page<CheckGroup> checkGroups;

        Example example = new Example(CheckGroup.class);
        Example.Criteria criteria = example.createCriteria();

        if (queryString!=null&&queryString!=""){
            criteria.orLike("code","%"+queryString+"%").orLike("name","%"+queryString+"%").orLike("helpCode","%"+queryString+"%");
            PageHelper.startPage(currentPage,pageSize);
            checkGroups = (Page<CheckGroup>) checkGroupMapper.selectByExample(example);
        }else{
            PageHelper.startPage(currentPage,pageSize);
            checkGroups = (Page<CheckGroup>) checkGroupMapper.selectAll();
        }

        return new PageResult(checkGroups.getTotal(),checkGroups.getResult());
    }

    //根据id查询检查项
    @Transactional
    @Override
    public List<CheckItem> findById(Integer id) {
        Example example = new Example(CheckGroupAndCheckItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("checkGroupId",id);

        List<CheckGroupAndCheckItem> checkGroupAndCheckItems = checkGroupAndCheckItemMapper.selectByExample(example);
        ArrayList<Integer> ids = new ArrayList<>();
        for (CheckGroupAndCheckItem groupAndCheckItem : checkGroupAndCheckItems) {
            ids.add(groupAndCheckItem.getCheckItemId());
        }

        ArrayList<CheckItem> checkItems = new ArrayList<>();
        for (Integer checkItemId : ids) {
            CheckItem checkItem = checkItemMapper.selectByPrimaryKey(checkItemId);
            checkItems.add(checkItem);
        }

        return checkItems;
    }

    //获取检查组
    @Override
    public CheckGroup findCheckGroupById(Integer id) {
        return checkGroupMapper.selectByPrimaryKey(id);
    }

    //添加检查组
    @Transactional
    @Override
    public void addCheckGroup(CheckGroup checkGroup, Integer[] ids) {
        //插入数据
        checkGroupMapper.insert(checkGroup);

        //根据checkGroup查找
        CheckGroup checkGroup2 = checkGroupMapper.selectOne(checkGroup);
        Integer checkGroupId = checkGroup2.getId();

        for (Integer id : ids) {
            CheckGroupAndCheckItem checkGroupAndCheckItem = new CheckGroupAndCheckItem(checkGroupId, id);
            checkGroupAndCheckItemMapper.insert(checkGroupAndCheckItem);
        }
    }

    //更新检查组
    @Transactional
    @Override
    public void updateCheckGroup(CheckGroup checkGroup, Integer[] ids) {
        checkGroupMapper.updateByPrimaryKey(checkGroup);
        Integer checkGroupId = checkGroup.getId();

        //根据检查组id查询其下的所有检查项id
        Example example = new Example(CheckGroupAndCheckItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("checkGroupId",checkGroupId);
        criteria.andNotIn("checkItemId", Arrays.asList(ids));
        List<CheckGroupAndCheckItem> checkGroupAndCheckItems = checkGroupAndCheckItemMapper.selectByExample(example);
        for (CheckGroupAndCheckItem checkGroupAndCheckItem : checkGroupAndCheckItems) {
            checkGroupAndCheckItemMapper.delete(checkGroupAndCheckItem);
        }
    }

    //删除检查组
    @Transactional
    @Override
    public void deleteCheckGroup(Integer id) {

        //判断该检查组是否属于某一个检查套餐
        Example example1 = new Example(SetMealAndCheckgroup.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andEqualTo("checkGroupId",id);
        int count = setMealAndCheckgroupMapper.selectCountByExample(example1);
        if (count>0){
            throw new RuntimeException("该检查组是某一套餐的一部分,无法删除");
        }

        //先删除中间表中的数据
        Example example = new Example(CheckGroupAndCheckItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("checkGroupId",id);
        List<CheckGroupAndCheckItem> checkGroupAndCheckItems = checkGroupAndCheckItemMapper.selectByExample(example);

        for (CheckGroupAndCheckItem checkGroupAndCheckItem : checkGroupAndCheckItems) {
            checkGroupAndCheckItemMapper.delete(checkGroupAndCheckItem);
        }

        //再删除检查组
        checkGroupMapper.deleteByPrimaryKey(id);
    }

    //查找所有
    @Override
    public List<CheckGroup> findAll() {
        return checkGroupMapper.selectAll();
    }

}
