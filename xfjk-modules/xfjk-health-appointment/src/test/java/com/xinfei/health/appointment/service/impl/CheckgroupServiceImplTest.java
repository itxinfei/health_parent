package com.xinfei.health.appointment.service.impl;

import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.SetmealCheckgroup;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckgroupMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthCheckgroupExtMapper;
import com.xinfei.health.appointment.mapper.SetmealCheckgroupMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 检查组删除引用校验（08 §4.1 错误码 3002）
 *
 * @author xinfei
 */
@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("检查组服务-删除引用校验")
class CheckgroupServiceImplTest {

    @Mock
    private CheckgroupMapper baseMapper;
    @Mock
    private CheckitemMapper checkitemMapper;
    @Mock
    private CheckgroupCheckitemMapper checkgroupCheckitemMapper;
    @Mock
    private SetmealCheckgroupMapper setmealCheckgroupMapper;
    @Mock
    private HealthCheckgroupExtMapper checkgroupExtMapper;

    @InjectMocks
    private CheckgroupServiceImpl service;

    @Test
    @DisplayName("被套餐引用的检查组删除时抛 3002 且不删关联不删组")
    void deleteReferencedCheckgroupThrows3002() {
        when(setmealCheckgroupMapper.selectByCheckgroupId(5)).thenReturn(List.of(new SetmealCheckgroup()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteById(5));

        assertEquals(HealthErrorCode.CHECKGROUP_REFERENCED, ex.getCode().intValue());
        verify(checkgroupCheckitemMapper, never()).deleteByCheckgroupId(any());
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("未被套餐引用的检查组删除时先清关联再删组")
    void deleteUnreferencedCheckgroupClearsRelationsFirst() {
        when(setmealCheckgroupMapper.selectByCheckgroupId(5)).thenReturn(List.of());
        when(checkgroupCheckitemMapper.deleteByCheckgroupId(5)).thenReturn(3);
        when(baseMapper.deleteById(5)).thenReturn(1);

        assertEquals(Boolean.TRUE, service.deleteById(5));

        verify(checkgroupCheckitemMapper).deleteByCheckgroupId(5);
        verify(baseMapper).deleteById(5);
    }

}
