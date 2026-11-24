package com.xinfei.health.assessment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.Indicator;

import java.util.List;

/**
 * 评估指标库 服务层（09 §2.11）
 *
 * @author xinfei
 */
public interface IIndicatorService {

    TableDataInfo<Indicator> queryPageList(Indicator bo, PageQuery pageQuery);

    List<Indicator> queryList(Indicator bo);

    Indicator queryById(Long id);

    /** code 全局唯一 */
    Long insert(Indicator entity);

    Boolean update(Indicator entity);

    Boolean deleteById(Long id);

}
