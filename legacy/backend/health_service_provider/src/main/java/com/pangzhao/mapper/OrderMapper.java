package com.pangzhao.mapper;

import com.pangzhao.pojo.Order;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface OrderMapper extends Mapper<Order> {


    @Select("SELECT COUNT(*) total,setmeal_id setmealId FROM t_order GROUP BY setmeal_id")
    List<Map> findBySetmeal();

    @Select("SELECT COUNT(o.id) total,s.name,COUNT(o.id)/(SELECT COUNT(id) FROM t_order) proportion,s.attention attention FROM t_order o ,t_setmeal s WHERE o.setmeal_id=s.id GROUP BY setmeal_id " +
            "ORDER BY total DESC LIMIT 0,4")
    List<Map> findHotSetMeal();
}
