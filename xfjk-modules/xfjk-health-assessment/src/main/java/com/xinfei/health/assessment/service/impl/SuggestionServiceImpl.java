package com.xinfei.health.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.Suggestion;
import com.xinfei.health.assessment.mapper.SuggestionMapper;
import com.xinfei.health.assessment.service.ISuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估建议 服务层处理（09 §2.10）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class SuggestionServiceImpl implements ISuggestionService {

    private final SuggestionMapper baseMapper;

    @Override
    public TableDataInfo<Suggestion> queryPageList(Suggestion bo, PageQuery pageQuery) {
        Page<Suggestion> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<Suggestion> queryList(Suggestion bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    @Override
    public Suggestion queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Long insert(Suggestion entity) {
        if (StringUtils.isBlank(entity.getContent())) {
            throw new ServiceException("建议文案不能为空");
        }
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean update(Suggestion entity) {
        if (entity.getId() == null) {
            throw new ServiceException("建议主键不能为空");
        }
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    private LambdaQueryWrapper<Suggestion> buildQueryWrapper(Suggestion bo) {
        return Wrappers.<Suggestion>lambdaQuery()
            .eq(bo.getQuestionnaireId() != null, Suggestion::getQuestionnaireId, bo.getQuestionnaireId())
            .eq(StringUtils.isNotBlank(bo.getFactorCode()), Suggestion::getFactorCode, bo.getFactorCode())
            .eq(StringUtils.isNotBlank(bo.getLevelCode()), Suggestion::getLevelCode, bo.getLevelCode())
            .eq(StringUtils.isNotBlank(bo.getType()), Suggestion::getType, bo.getType())
            .orderByAsc(Suggestion::getSort);
    }

}
