package com.xinfei.health.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.Indicator;
import com.xinfei.health.assessment.mapper.IndicatorMapper;
import com.xinfei.health.assessment.service.IIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估指标库 服务层处理（09 §2.11）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class IndicatorServiceImpl implements IIndicatorService {

    private final IndicatorMapper baseMapper;

    @Override
    public TableDataInfo<Indicator> queryPageList(Indicator bo, PageQuery pageQuery) {
        Page<Indicator> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<Indicator> queryList(Indicator bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    @Override
    public Indicator queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Long insert(Indicator entity) {
        checkCodeUnique(entity.getCode(), null);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean update(Indicator entity) {
        if (entity.getId() == null) {
            throw new ServiceException("指标主键不能为空");
        }
        checkCodeUnique(entity.getCode(), entity.getId());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    private void checkCodeUnique(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            throw new ServiceException("指标编码不能为空");
        }
        boolean exists = baseMapper.exists(Wrappers.<Indicator>lambdaQuery()
            .eq(Indicator::getCode, code)
            .ne(excludeId != null, Indicator::getId, excludeId));
        if (exists) {
            throw new ServiceException("指标编码已存在：" + code);
        }
    }

    private LambdaQueryWrapper<Indicator> buildQueryWrapper(Indicator bo) {
        return Wrappers.<Indicator>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getCode()), Indicator::getCode, bo.getCode())
            .like(StringUtils.isNotBlank(bo.getName()), Indicator::getName, bo.getName())
            .eq(StringUtils.isNotBlank(bo.getCategory()), Indicator::getCategory, bo.getCategory())
            .orderByAsc(Indicator::getSort)
            .orderByAsc(Indicator::getCode);
    }

}
