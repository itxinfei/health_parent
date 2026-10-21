package com.pangzhao.controller;

import com.pangzhao.entity.PageResult;
import com.pangzhao.entity.QueryPageBean;
import com.pangzhao.entity.Result;
import com.pangzhao.pojo.CheckItem;
import com.pangzhao.service.CheckItemService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 戴金华
 * @date 2019-11-07 18:56
 */
@RestController
@RequestMapping("/checkItem")
public class CheckItemController {

    @Reference
    private CheckItemService checkItemService;


    /**
     * 分页查询数据
     * @param queryPageBean
     * @return
     */
    @RequestMapping("/findByPage.do")
    public PageResult findByPage(@RequestBody QueryPageBean queryPageBean){
        return checkItemService.findByPage(queryPageBean.getCurrentPage(),queryPageBean.getPageSize(),queryPageBean.getQueryString());
    }

    /**
     * 添加数据
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody CheckItem checkItem){
        checkItemService.add(checkItem);
        return new Result();
    }

    /**
     * 根据id查询对象
     * @param id
     * @return
     */
    @RequestMapping("/findById.do")
    public CheckItem findById(Integer id){
        return checkItemService.findById(id);
    }

    /**
     * 修改CheckItem
     * @param checkItem
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody CheckItem checkItem){
        checkItemService.update(checkItem);
        return new Result();
    }

    /**
     * 删除检查项
     */
    @RequestMapping("/deleteCheckItem.do")
    public Result deleteCheckItem(Integer id){
        checkItemService.deleteCheckItem(id);
        return new Result();
    }

    //查询所有
    @RequestMapping("/findAll.do")
    public List<CheckItem> findAll(){
        return checkItemService.findAll();
    }
}
