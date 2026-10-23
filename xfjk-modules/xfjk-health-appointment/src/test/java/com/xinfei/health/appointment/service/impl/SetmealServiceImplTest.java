package com.xinfei.health.appointment.service.impl;

import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckgroupMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthSetmealExtMapper;
import com.xinfei.health.appointment.mapper.OrderMapper;
import com.xinfei.health.appointment.mapper.SetmealCheckgroupMapper;
import com.xinfei.health.appointment.mapper.SetmealMapper;
import com.xinfei.system.service.ISysOssService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 套餐删除引用校验（08 §4.1 错误码 4001）
 *
 * @author xinfei
 */
@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("套餐服务-删除引用校验")
class SetmealServiceImplTest {

    @Mock
    private SetmealMapper baseMapper;
    @Mock
    private CheckgroupMapper checkgroupMapper;
    @Mock
    private CheckitemMapper checkitemMapper;
    @Mock
    private SetmealCheckgroupMapper setmealCheckgroupMapper;
    @Mock
    private CheckgroupCheckitemMapper checkgroupCheckitemMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ISysOssService sysOssService;
    @Mock
    private HealthSetmealExtMapper setmealExtMapper;

    @InjectMocks
    private SetmealServiceImpl service;

    @Test
    @DisplayName("套餐下存在预约订单时删除抛 4001 且不删关联不删套餐")
    void deleteSetmealWithOrdersThrows4001() {
        when(orderMapper.selectCount(any())).thenReturn(2L);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteById(7));

        assertEquals(HealthErrorCode.SETMEAL_ORDER_EXISTS, ex.getCode().intValue());
        verify(setmealCheckgroupMapper, never()).deleteBySetmealId(any());
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("无预约订单的套餐删除时先清套餐-检查组关联再删套餐")
    void deleteSetmealWithoutOrdersClearsRelationsFirst() {
        when(orderMapper.selectCount(any())).thenReturn(0L);
        when(setmealCheckgroupMapper.deleteBySetmealId(7)).thenReturn(4);
        when(baseMapper.deleteById(7)).thenReturn(1);

        assertEquals(Boolean.TRUE, service.deleteById(7));

        verify(setmealCheckgroupMapper).deleteBySetmealId(7);
        verify(baseMapper).deleteById(7);
    }

}
