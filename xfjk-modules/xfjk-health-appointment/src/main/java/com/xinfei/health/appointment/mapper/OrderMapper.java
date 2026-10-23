package com.xinfei.health.appointment.mapper;

import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.Order;

/**
 * 预约订单 数据层
 *
 * <p>本切片只用于套餐删除前的引用校验（08 §4.1 错误码 4001）。
 *
 * @author xinfei
 */
public interface OrderMapper extends BaseMapperPlus<Order, Order> {

}
