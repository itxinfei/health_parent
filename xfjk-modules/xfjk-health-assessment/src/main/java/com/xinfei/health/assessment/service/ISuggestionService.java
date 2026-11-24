package com.xinfei.health.assessment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.Suggestion;

import java.util.List;

/**
 * 评估建议 服务层（09 §2.10）
 *
 * @author xinfei
 */
public interface ISuggestionService {

    TableDataInfo<Suggestion> queryPageList(Suggestion bo, PageQuery pageQuery);

    List<Suggestion> queryList(Suggestion bo);

    Suggestion queryById(Long id);

    Long insert(Suggestion entity);

    Boolean update(Suggestion entity);

    Boolean deleteById(Long id);

}
