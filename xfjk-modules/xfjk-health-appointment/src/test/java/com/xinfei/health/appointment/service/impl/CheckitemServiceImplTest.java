package com.xinfei.health.appointment.service.impl;

import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthCheckitemExtMapper;
import com.xinfei.health.appointment.mapper.HealthCheckitemRefMapper;
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
 * 检查项删除引用校验（08 §4.1 错误码 3001）
 *
 * @author xinfei
 */
@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("检查项服务-删除引用校验")
class CheckitemServiceImplTest {

    @Mock
    private CheckitemMapper baseMapper;
    @Mock
    private CheckgroupCheckitemMapper checkgroupCheckitemMapper;
    @Mock
    private HealthCheckitemExtMapper extMapper;
    @Mock
    private HealthCheckitemRefMapper refMapper;

    @InjectMocks
    private CheckitemServiceImpl service;

    @Test
    @DisplayName("被检查组引用的检查项删除时抛 3001 且不落库")
    void deleteReferencedCheckitemThrows3001() {
        when(checkgroupCheckitemMapper.countByCheckitemId(28)).thenReturn(3L);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteById(28));

        assertEquals(HealthErrorCode.CHECKITEM_REFERENCED, ex.getCode().intValue());
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("未被引用的检查项删除正常落库")
    void deleteUnreferencedCheckitemSucceeds() {
        when(checkgroupCheckitemMapper.countByCheckitemId(28)).thenReturn(0L);
        when(baseMapper.deleteById(28)).thenReturn(1);

        assertEquals(Boolean.TRUE, service.deleteById(28));

        verify(baseMapper).deleteById(28);
    }

}
