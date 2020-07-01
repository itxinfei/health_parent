package cn.webdai.mapper;

import cn.webdai.pojo.Member;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author 戴金华
 * @date 2019-11-15 15:01
 */
public interface MemberMapper extends Mapper<Member> {


    @Select("SELECT MONTH(regTime) AS time,COUNT(*) total  FROM t_member WHERE YEAR(regTime) = YEAR(NOW()) GROUP BY MONTH(regTime)")
    List<Map> findByDate();
}
