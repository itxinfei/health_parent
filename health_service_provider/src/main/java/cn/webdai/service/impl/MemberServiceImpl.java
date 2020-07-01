package cn.webdai.service.impl;

import cn.webdai.mapper.MemberMapper;
import cn.webdai.pojo.Member;
import cn.webdai.service.MemberService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.*;


/**
 * @author 戴金华
 * @date 2019-11-15 15:03
 */
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public Member findById(Integer id) {
        return memberMapper.selectByPrimaryKey(id);
    }

    @Override
    public Map<String, List> findByDate() {
        List<Map> members = memberMapper.findByDate();
        Map map = new HashMap();
        ArrayList<String> timeList = new ArrayList<>();
        ArrayList<Long> totalList = new ArrayList<>();
        for (Map member : members) {
            timeList.add(Calendar.getInstance().get(Calendar.YEAR) + "." + member.get("time"));
            Long total = (Long) member.get("total");
            totalList.add(total);
        }

        map.put("months", timeList);
        map.put("memberCount", totalList);
        return map;
    }

    /**
     * 验证是否存在该会员 即手机号是否存在于数据库表中
     */
    @Override
    public void checkLogin(String telephone) {
        //判断表中是否存在该手机号
        Example example = new Example(Member.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("telephone", telephone);
        Member member = memberMapper.selectOneByExample(example);

        Member newMember = new Member();
        newMember.setName(telephone);
        newMember.setPhoneNumber(telephone);
        if (member == null) {
            //如果数据库中 无该用户的数据 插入数据库
            memberMapper.insert(newMember);
        }

    }
}
