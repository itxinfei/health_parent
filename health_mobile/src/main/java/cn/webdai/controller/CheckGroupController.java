package cn.webdai.controller;

import cn.webdai.entity.PageResult;
import cn.webdai.entity.QueryPageBean;
import cn.webdai.entity.Result;
import cn.webdai.pojo.CheckGroup;
import cn.webdai.pojo.CheckItem;
import cn.webdai.service.CheckGroupService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 戴金华
 * @date 2019-11-08 11:47
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
        checkGroupService.deleteCheckGroup(id);
        return new Result();
    }
}
