package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.bo.CriticalHandleBo;
import com.xinfei.health.appointment.domain.bo.CriticalQueryBo;
import com.xinfei.health.appointment.domain.bo.CriticalRegisterBo;
import com.xinfei.health.appointment.domain.vo.CriticalLogVo;

/**
 * 危急值服务（07 §3.2 / §3.2.1 / §5.5）
 *
 * @author xinfei
 */
public interface IHealthCriticalService {

    TableDataInfo<CriticalLogVo> queryPageList(CriticalQueryBo bo, PageQuery pageQuery);

    void register(Long reportId, CriticalRegisterBo bo);

    void handle(Long id, CriticalHandleBo bo);

}
