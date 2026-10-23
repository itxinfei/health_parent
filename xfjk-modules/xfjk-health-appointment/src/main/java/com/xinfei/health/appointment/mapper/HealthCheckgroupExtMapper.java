package com.xinfei.health.appointment.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.HealthCheckgroupExt;

/**
 * 检查组元数据扩展 数据层（08 §2.4）
 *
 * @author xinfei
 */
public interface HealthCheckgroupExtMapper extends BaseMapperPlus<HealthCheckgroupExt, HealthCheckgroupExt> {

    /**
     * 按检查组查扩展（1:1）
     */
    default HealthCheckgroupExt selectByCheckgroupId(Integer checkgroupId) {
        return this.selectOne(Wrappers.<HealthCheckgroupExt>lambdaQuery()
            .eq(HealthCheckgroupExt::getCheckgroupId, checkgroupId)
            .last("LIMIT 1"));
    }

    /**
     * 按检查组删除扩展（删除检查组时级联）
     */
    default int deleteByCheckgroupId(Integer checkgroupId) {
        return this.delete(Wrappers.<HealthCheckgroupExt>lambdaQuery()
            .eq(HealthCheckgroupExt::getCheckgroupId, checkgroupId));
    }

}
