package com.pangzhao.controller;

import com.pangzhao.entity.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 公共异常处理类
 * @author 戴金华
 * @date 2019-11-07 21:01
 */
@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return new Result(false,e.getMessage(),null);
    }
}
