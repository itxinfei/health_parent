package cn.webdai.controller;

import cn.webdai.entity.PageResult;
import cn.webdai.entity.QueryPageBean;
import cn.webdai.pojo.Setmeal;
import cn.webdai.service.SetMealService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 戴金华
 * @date 2019-11-09 11:17
 */
@RestController
@RequestMapping("/setMeal")
public class SetMealController {


    @Reference
    private SetMealService setMealService;


    @RequestMapping("/findByPage.do")
    public PageResult findByPage(@RequestBody QueryPageBean queryPageBean){
        return  setMealService.findByPage(queryPageBean.getCurrentPage(),queryPageBean.getPageSize(),queryPageBean.getQueryString());
    }

    @RequestMapping("/findAll.do")
    public List<Setmeal> findAll(){
        return setMealService.findAll();
    }


    @RequestMapping("/findById")
    public Setmeal findById(Integer id){
        return setMealService.findById(id);
    }
}
