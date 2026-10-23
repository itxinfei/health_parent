package com.xinfei.health.appointment.mapper;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.HealthCheckitemForbid;

import java.util.Collection;
import java.util.List;

/**
 * 禁忌规则 数据层（08 §2.7）
 *
 * @author xinfei
 */
public interface HealthCheckitemForbidMapper extends BaseMapperPlus<HealthCheckitemForbid, HealthCheckitemForbid> {

    /**
     * 查询某套餐触点集合内的启用规则（scope=SETMEAL 指向套餐；CHECKGROUP 指向组集合；CHECKITEM 指向项集合）
     *
     * @param setmealId 套餐 id
     * @param groupIds  组 id 集合
     * @param itemIds   项 id 集合
     */
    default List<HealthCheckitemForbid> selectEnabledByTargets(Integer setmealId, Collection<Integer> groupIds,
                                                               Collection<Integer> itemIds) {
        return this.selectList(Wrappers.<HealthCheckitemForbid>lambdaQuery()
            .eq(HealthCheckitemForbid::getEnabled, "1")
            .and(w -> w
                .and(s -> s.eq(HealthCheckitemForbid::getScope, HealthCheckitemForbid.SCOPE_SETMEAL)
                    .eq(HealthCheckitemForbid::getTargetId, setmealId))
                .or(ObjectUtil.isNotEmpty(groupIds), s -> s.eq(HealthCheckitemForbid::getScope,
                        HealthCheckitemForbid.SCOPE_CHECKGROUP).in(HealthCheckitemForbid::getTargetId, groupIds))
                .or(ObjectUtil.isNotEmpty(itemIds), s -> s.eq(HealthCheckitemForbid::getScope,
                        HealthCheckitemForbid.SCOPE_CHECKITEM).in(HealthCheckitemForbid::getTargetId, itemIds)))
            .orderByAsc(HealthCheckitemForbid::getPriority));
    }

    /**
     * 规则编码唯一校验（编辑时排除自身）
     */
    default boolean existsByRuleCode(String ruleCode, Long excludeId) {
        return this.exists(Wrappers.<HealthCheckitemForbid>lambdaQuery()
            .eq(HealthCheckitemForbid::getRuleCode, ruleCode)
            .ne(excludeId != null, HealthCheckitemForbid::getId, excludeId));
    }

}
