package com.xinfei.health.appointment.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.CheckgroupCheckitem;

import java.util.Collection;
import java.util.List;

/**
 * 检查组-检查项关联 数据层
 *
 * <p>关联表无视图对象，泛型 V 与 T 同型（与框架 sys_role_menu 关联表一致的做法），
 * 只使用插入与条件删除，不使用任何 selectVo 方法。
 *
 * @author xinfei
 */
public interface CheckgroupCheckitemMapper extends BaseMapperPlus<CheckgroupCheckitem, CheckgroupCheckitem> {

    /**
     * 查询组内检查项 id
     *
     * @param checkgroupId 检查组 id
     * @return 检查项 id 列表
     */
    default List<CheckgroupCheckitem> selectByCheckgroupId(Integer checkgroupId) {
        return this.selectList(Wrappers.<CheckgroupCheckitem>lambdaQuery()
            .eq(CheckgroupCheckitem::getCheckgroupId, checkgroupId));
    }

    /**
     * 批量查询多个检查组的关联记录（套餐三级详情用，避免逐组查询）
     *
     * @param checkgroupIds 检查组 id 集合
     * @return 关联记录列表
     */
    default List<CheckgroupCheckitem> selectByCheckgroupIds(Collection<Integer> checkgroupIds) {
        return this.selectList(Wrappers.<CheckgroupCheckitem>lambdaQuery()
            .in(CheckgroupCheckitem::getCheckgroupId, checkgroupIds));
    }

    /**
     * 按检查组 id 删除关联
     *
     * @param checkgroupId 检查组 id
     * @return 结果
     */
    default int deleteByCheckgroupId(Integer checkgroupId) {
        return this.delete(Wrappers.<CheckgroupCheckitem>lambdaQuery()
            .eq(CheckgroupCheckitem::getCheckgroupId, checkgroupId));
    }

    /**
     * 统计检查项被多少检查组引用
     *
     * @param checkitemId 检查项 id
     * @return 引用数
     */
    default long countByCheckitemId(Integer checkitemId) {
        return this.selectCount(Wrappers.<CheckgroupCheckitem>lambdaQuery()
            .eq(CheckgroupCheckitem::getCheckitemId, checkitemId));
    }

}
