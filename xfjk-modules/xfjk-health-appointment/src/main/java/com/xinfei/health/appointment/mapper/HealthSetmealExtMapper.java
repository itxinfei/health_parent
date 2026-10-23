package com.xinfei.health.appointment.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.HealthSetmealExt;

/**
 * 套餐扩展 数据层（08 §2.5）
 *
 * @author xinfei
 */
public interface HealthSetmealExtMapper extends BaseMapperPlus<HealthSetmealExt, HealthSetmealExt> {

    /**
     * 按套餐查扩展（1:1）
     */
    default HealthSetmealExt selectBySetmealId(Integer setmealId) {
        return this.selectOne(Wrappers.<HealthSetmealExt>lambdaQuery()
            .eq(HealthSetmealExt::getSetmealId, setmealId)
            .last("LIMIT 1"));
    }

    /**
     * 按套餐删除扩展（删除套餐时级联）
     */
    default int deleteBySetmealId(Integer setmealId) {
        return this.delete(Wrappers.<HealthSetmealExt>lambdaQuery()
            .eq(HealthSetmealExt::getSetmealId, setmealId));
    }

}
