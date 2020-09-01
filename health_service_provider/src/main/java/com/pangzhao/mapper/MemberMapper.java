package com.pangzhao.mapper;

import com.pangzhao.pojo.Member;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;
/**
 *
 */
public interface MemberMapper extends Mapper<Member> {


    @Select("SELECT MONTH(regTime) AS time,COUNT(*) total  FROM t_member WHERE YEAR(regTime) = YEAR(NOW()) GROUP BY MONTH(regTime)")
    List<Map> findByDate();
}
