package com.xinfei.health.appointment.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.HealthCheckitemExt;

/**
 * 检查项元数据扩展 数据层（08 §2.2）
 *
 * @author xinfei
 */
public interface HealthCheckitemExtMapper extends BaseMapperPlus<HealthCheckitemExt, HealthCheckitemExt> {

    /**
     * 按检查项查扩展（1:1）
     */
    default HealthCheckitemExt selectByCheckitemId(Integer checkitemId) {
        return this.selectOne(Wrappers.<HealthCheckitemExt>lambdaQuery()
            .eq(HealthCheckitemExt::getCheckitemId, checkitemId)
            .last("LIMIT 1"));
    }

    /**
     * 按检查项删除扩展（删除检查项时级联）
     */
    default int deleteByCheckitemId(Integer checkitemId) {
        return this.delete(Wrappers.<HealthCheckitemExt>lambdaQuery()
            .eq(HealthCheckitemExt::getCheckitemId, checkitemId));
    }

}
