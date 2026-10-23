package com.xinfei.health.appointment.service;

import com.xinfei.health.appointment.domain.bo.ForbidBo;
import com.xinfei.health.appointment.domain.bo.ForbidCheckBo;
import com.xinfei.health.appointment.domain.vo.ForbidCheckVo;

import java.util.List;

/**
 * 禁忌规则 服务层（08 §4.5、§5.3 步骤 5）
 *
 * @author xinfei
 */
public interface IForbidService {

    /**
     * 新增规则（ruleCode 唯一、conditionJson 键白名单校验，违反 5013）
     */
    Long insertByBo(ForbidBo bo);

    /**
     * 编辑规则
     */
    Boolean updateByBo(ForbidBo bo);

    /**
     * 删除规则
     */
    Boolean deleteById(Long id);

    /**
     * 规则列表（运营自检用，全量）
     */
    List<com.xinfei.health.appointment.domain.HealthCheckitemForbid> queryList();

    /**
     * 禁忌试算（08 §6.2）：解析套餐触点 → 匹配启用规则 → 按处置档分流
     */
    ForbidCheckVo trialCalc(ForbidCheckBo bo);

}
