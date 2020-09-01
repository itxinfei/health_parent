package com.pangzhao.controller;

import com.pangzhao.entity.PageResult;
import com.pangzhao.entity.QueryPageBean;
import com.pangzhao.entity.Result;
import com.pangzhao.pojo.CheckGroup;
import com.pangzhao.pojo.CheckItem;
import com.pangzhao.service.CheckGroupService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("/checkGroup")
public class CheckGroupController {

    @Reference
    private CheckGroupService checkGroupService;

    @RequestMapping("/findByPage.do")
    public PageResult findByPage(@RequestBody QueryPageBean queryPageBean){
        return checkGroupService.findByPage(queryPageBean.getCurrentPage(),queryPageBean.getPageSize(),queryPageBean.getQueryString());
    }

    //获取检查项
    @RequestMapping("/findById.do")
    public List<CheckItem> findById(Integer id){
        return checkGroupService.findById(id);
    }

    //获取检查组
    @RequestMapping("/findCheckGroupById.do")
    public CheckGroup findCheckGroupById(Integer id){
        return checkGroupService.findCheckGroupById(id);
    }


    //添加检查组
    @RequestMapping("/addCheckGroup.do")
    public Result addCheckGroup(@RequestBody CheckGroup checkGroup, Integer[] ids){
        checkGroupService.addCheckGroup(checkGroup,ids);
        return new Result();
    }

    //更新检查组
    @RequestMapping("/updateCheckGroup.do")
    public Result updateCheckGroup(@RequestBody CheckGroup checkGroup, Integer[] ids){
        checkGroupService.updateCheckGroup(checkGroup,ids);
        return new Result();
    }

    //删除检查组
    @RequestMapping("/deleteCheckGroup.do")
    public Result deleteCheckGroup(Integer id){
        try {
            checkGroupService.deleteCheckGroup(id);
            return new Result();
        }catch (Exception e){
            return new Result(false,e.getMessage());
        }
    }

    //查找所有
    @RequestMapping("/findAll.do")
    public List<CheckGroup> findAll(){
        return checkGroupService.findAll();
    }
}
