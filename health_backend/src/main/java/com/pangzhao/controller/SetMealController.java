package com.pangzhao.controller;

import com.pangzhao.constant.MessageConstant;
import com.pangzhao.entity.PageResult;
import com.pangzhao.entity.QueryPageBean;
import com.pangzhao.entity.Result;
import com.pangzhao.pojo.CheckGroup;
import com.pangzhao.pojo.Setmeal;
import com.pangzhao.service.SetMealService;
import com.pangzhao.util.QiniuUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@RestController
@RequestMapping("/setMeal")
public class SetMealController {


    @Reference
    private SetMealService setMealService;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping("/findByPage.do")
    public PageResult findByPage(@RequestBody QueryPageBean queryPageBean){
        return  setMealService.findByPage(queryPageBean.getCurrentPage(),queryPageBean.getPageSize(),queryPageBean.getQueryString());
    }

    @RequestMapping("/add.do")
    public Result add(@RequestBody Setmeal setmeal, Integer[] ids){
        try {
            setMealService.add(setmeal,ids);
            redisTemplate.boundSetOps("ADD_IMG").add(setmeal.getImg());
            return new Result(true, MessageConstant.ADD_CHECKGROUP_SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, MessageConstant.ADD_CHECKGROUP_FAIL);
        }
    }


    @RequestMapping("/upload.do")
    public Result upload(@RequestParam MultipartFile imgFile){
        String originalFilename = imgFile.getOriginalFilename();
        int lastIndexOf  = originalFilename.lastIndexOf(".");
        //获得后缀名
        String suffixName = originalFilename.substring( lastIndexOf);
        //获得新的文件名
        String fileName = UUID.randomUUID().toString()+suffixName;
        try {
            QiniuUtils.upload2Qiniu(imgFile.getBytes(),fileName);
            redisTemplate.boundSetOps("UPLOAD_IMG").add(fileName);
             return new Result(true, MessageConstant.PIC_UPLOAD_SUCCESS,fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.PIC_UPLOAD_FAIL);

        }
    }

    @RequestMapping("/findById.do")
    public Setmeal findById(Integer id){
        return setMealService.findById(id);
    }

    @RequestMapping("/findCheckGroupById.do")
    public List<CheckGroup> findCheckGroupById(Integer id){
        return setMealService.findCheckGroupById(id);
    }

    @RequestMapping("/update.do")
    public Result update(@RequestBody Setmeal setmeal, Integer[] ids){
        try {
            setMealService.update(setmeal,ids);
            return new Result(true,"套餐更新成功！");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"套餐更新失败！");
        }
    }

    //删除套餐
    @RequestMapping("/delete.do")
    public Result delete(Integer id){
        try {
            setMealService.delete(id);
            return new Result(true,"删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"服务器异常");
        }
    }
}
