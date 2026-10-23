package com.xinfei.health.appointment.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.HealthCheckitemRef;

import java.util.List;

/**
 * 检查项参考区间 数据层（08 §2.3）
 *
 * @author xinfei
 */
public interface HealthCheckitemRefMapper extends BaseMapperPlus<HealthCheckitemRef, HealthCheckitemRef> {

    /**
     * 按检查项查区间（按优先级排序）
     */
    default List<HealthCheckitemRef> selectByCheckitemId(Integer checkitemId) {
        return this.selectList(Wrappers.<HealthCheckitemRef>lambdaQuery()
            .eq(HealthCheckitemRef::getCheckitemId, checkitemId)
            .orderByAsc(HealthCheckitemRef::getPriority));
    }

    /**
     * 按检查项删除全部区间（编辑时差异更新、删除时级联）
     */
    default int deleteByCheckitemId(Integer checkitemId) {
        return this.delete(Wrappers.<HealthCheckitemRef>lambdaQuery()
            .eq(HealthCheckitemRef::getCheckitemId, checkitemId));
    }

}
