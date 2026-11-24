package com.xinfei.health.assessment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.Crowd;
import com.xinfei.health.assessment.domain.CrowdRule;

import java.util.List;

/**
 * 人群分类 服务层（09 §2.12）
 *
 * @author xinfei
 */
public interface ICrowdService {

    TableDataInfo<Crowd> queryPageList(Crowd bo, PageQuery pageQuery);

    List<Crowd> queryList(Crowd bo);

    Crowd queryById(Long id);

    /** code 全局唯一 */
    Long insert(Crowd entity);

    Boolean update(Crowd entity);

    Boolean deleteById(Long id);

    /** 人群下的规则条件 */
    List<CrowdRule> listRules(Long crowdId);

    /** 新增规则（source_type/operator/between 双值校验，违规 7009） */
    Long insertRule(CrowdRule entity);

    Boolean updateRule(CrowdRule entity);

    Boolean deleteRule(Long id);

}
