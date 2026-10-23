package com.xinfei.health.appointment.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.SetmealCheckgroup;

import java.util.List;

/**
 * 套餐-检查组关联 数据层
 *
 * @author xinfei
 */
public interface SetmealCheckgroupMapper extends BaseMapperPlus<SetmealCheckgroup, SetmealCheckgroup> {

    /**
     * 查询套餐关联记录
     *
     * @param setmealId 套餐 id
     * @return 关联记录列表
     */
    default List<SetmealCheckgroup> selectBySetmealId(Integer setmealId) {
        return this.selectList(Wrappers.<SetmealCheckgroup>lambdaQuery()
            .eq(SetmealCheckgroup::getSetmealId, setmealId));
    }

    /**
     * 查询引用某检查组的套餐关联记录
     *
     * @param checkgroupId 检查组 id
     * @return 关联记录列表
     */
    default List<SetmealCheckgroup> selectByCheckgroupId(Integer checkgroupId) {
        return this.selectList(Wrappers.<SetmealCheckgroup>lambdaQuery()
            .eq(SetmealCheckgroup::getCheckgroupId, checkgroupId));
    }

    /**
     * 按套餐 id 删除关联
     *
     * @param setmealId 套餐 id
     * @return 结果
     */
    default int deleteBySetmealId(Integer setmealId) {
        return this.delete(Wrappers.<SetmealCheckgroup>lambdaQuery()
            .eq(SetmealCheckgroup::getSetmealId, setmealId));
    }

}
