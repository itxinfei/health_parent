package com.xinfei.health.assessment.service;

import com.xinfei.health.assessment.domain.ScaleConfig;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;

import java.util.List;

/**
 * 量表因子/规则/阈值配置 服务层（09 §2.4~§2.6）
 *
 * @author xinfei
 */
public interface IScaleService {

    /** 问卷下的因子列表 */
    List<ScaleFactor> listFactors(Long questionnaireId);

    /** 新增因子（问卷已发布 7007） */
    Long insertFactor(ScaleFactor entity);

    /** 编辑因子（已发布 7007） */
    Boolean updateFactor(ScaleFactor entity);

    /** 删除因子（已发布 7007） */
    Boolean deleteFactor(Long id);

    /** 问卷下的规则列表 */
    List<ScaleRule> listRules(Long questionnaireId);

    /** 新增规则（区间 min<max、level_code 属值域，违规 7004；已发布 7007） */
    Long insertRule(ScaleRule entity);

    /** 编辑规则 */
    Boolean updateRule(ScaleRule entity);

    /** 删除规则 */
    Boolean deleteRule(Long id);

    /** 问卷下的阈值配置列表（09 §2.6，A13） */
    List<ScaleConfig> listConfigs(Long questionnaireId);

    /** 全量保存配置（upsert；已发布 7007） */
    Boolean saveConfigs(Long questionnaireId, List<ScaleConfig> configs);

}
