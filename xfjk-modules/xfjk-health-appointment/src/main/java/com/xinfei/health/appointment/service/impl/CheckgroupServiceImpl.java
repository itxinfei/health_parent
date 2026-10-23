package com.xinfei.health.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.MapstructUtils;
import com.xinfei.common.core.utils.StreamUtils;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.Checkgroup;
import com.xinfei.health.appointment.domain.CheckgroupCheckitem;
import com.xinfei.health.appointment.domain.HealthCheckgroupExt;
import com.xinfei.health.appointment.domain.bo.CheckgroupBo;
import com.xinfei.health.appointment.domain.bo.CheckgroupExtBo;
import com.xinfei.health.appointment.domain.vo.CheckgroupVo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckgroupMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthCheckgroupExtMapper;
import com.xinfei.health.appointment.mapper.SetmealCheckgroupMapper;
import com.xinfei.health.appointment.service.ICheckgroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检查组 服务层处理
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CheckgroupServiceImpl implements ICheckgroupService {

    private final CheckgroupMapper baseMapper;
    private final CheckitemMapper checkitemMapper;
    private final CheckgroupCheckitemMapper checkgroupCheckitemMapper;
    private final SetmealCheckgroupMapper setmealCheckgroupMapper;
    private final HealthCheckgroupExtMapper checkgroupExtMapper;

    @Override
    public TableDataInfo<CheckgroupVo> queryPageList(CheckgroupBo bo, PageQuery pageQuery) {
        Page<CheckgroupVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<CheckgroupVo> queryList(CheckgroupBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public CheckgroupVo queryById(Integer id) {
        CheckgroupVo vo = baseMapper.selectVoById(id);
        if (vo != null) {
            vo.setExt(checkgroupExtMapper.selectByCheckgroupId(id));
        }
        return vo;
    }

    @Override
    public List<CheckitemVo> queryCheckItems(Integer id) {
        List<Integer> itemIds = StreamUtils.toList(
            checkgroupCheckitemMapper.selectByCheckgroupId(id), CheckgroupCheckitem::getCheckitemId);
        if (CollUtil.isEmpty(itemIds)) {
            return List.of();
        }
        return checkitemMapper.selectVoByIds(itemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insertByBo(CheckgroupBo bo) {
        checkCodeUnique(bo.getCode(), null);
        Checkgroup entity = MapstructUtils.convert(bo, Checkgroup.class);
        baseMapper.insert(entity);
        saveRelations(entity.getId(), bo.getCheckItemIds());
        // 08 §4.2：新增同事务写扩展表
        if (bo.getExt() != null) {
            insertExt(entity.getId(), bo.getExt());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(CheckgroupBo bo) {
        if (ObjectUtil.isNull(bo.getId())) {
            throw new ServiceException("检查组主键不能为空");
        }
        checkCodeUnique(bo.getCode(), bo.getId());
        boolean updated = baseMapper.updateById(MapstructUtils.convert(bo, Checkgroup.class)) > 0;
        if (ObjectUtil.isNotNull(bo.getCheckItemIds())) {
            diffUpdateRelations(bo.getId(), bo.getCheckItemIds());
        }
        // ext upsert（08 §4.2：编辑走 upsert）
        if (bo.getExt() != null) {
            upsertExt(bo.getId(), bo.getExt());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        if (CollUtil.isNotEmpty(setmealCheckgroupMapper.selectByCheckgroupId(id))) {
            throw new ServiceException("该检查组已被套餐引用，不能删除", HealthErrorCode.CHECKGROUP_REFERENCED);
        }
        checkgroupCheckitemMapper.deleteByCheckgroupId(id);
        // 级联清理扩展（08 §4.2）
        checkgroupExtMapper.deleteByCheckgroupId(id);
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 检查组 ext upsert：存在则更新，不存在则插入
     */
    private void upsertExt(Integer checkgroupId, CheckgroupExtBo ext) {
        HealthCheckgroupExt entity = checkgroupExtMapper.selectByCheckgroupId(checkgroupId);
        if (entity == null) {
            insertExt(checkgroupId, ext);
            return;
        }
        copyExt(ext, entity);
        checkgroupExtMapper.updateById(entity);
    }

    private void insertExt(Integer checkgroupId, CheckgroupExtBo ext) {
        HealthCheckgroupExt entity = new HealthCheckgroupExt();
        entity.setCheckgroupId(checkgroupId);
        copyExt(ext, entity);
        checkgroupExtMapper.insert(entity);
    }

    private void copyExt(CheckgroupExtBo ext, HealthCheckgroupExt entity) {
        entity.setDeptId(ext.getDeptId());
        entity.setSpecimen(ext.getSpecimen());
        entity.setDualDeptFlag(ObjectUtil.defaultIfNull(ext.getDualDeptFlag(), "0"));
        entity.setIsFasting(ObjectUtil.defaultIfNull(ext.getIsFasting(), "0"));
        entity.setDefaultConclusionId(ext.getDefaultConclusionId());
        entity.setSortNo(ObjectUtil.defaultIfNull(ext.getSortNo(), 0));
    }

    /**
     * 新增时直接建立关联
     */
    private void saveRelations(Integer checkgroupId, List<Integer> checkItemIds) {
        if (CollUtil.isEmpty(checkItemIds)) {
            return;
        }
        Set<Integer> distinctIds = new HashSet<>(checkItemIds);
        checkgroupCheckitemMapper.insertBatch(distinctIds.stream()
            .map(itemId -> newRelation(checkgroupId, itemId))
            .toList());
    }

    /**
     * 关联差异更新：删除不在新集合的旧关联、补插新增（修正原系统「只删不增」缺陷，08 §4.1）
     */
    private void diffUpdateRelations(Integer checkgroupId, List<Integer> checkItemIds) {
        List<Integer> oldIds = StreamUtils.toList(
            checkgroupCheckitemMapper.selectByCheckgroupId(checkgroupId), CheckgroupCheckitem::getCheckitemId);
        RelationDiff diff = RelationDiff.compute(oldIds, checkItemIds);

        if (CollUtil.isNotEmpty(diff.toRemove())) {
            checkgroupCheckitemMapper.delete(Wrappers.<CheckgroupCheckitem>lambdaQuery()
                .eq(CheckgroupCheckitem::getCheckgroupId, checkgroupId)
                .in(CheckgroupCheckitem::getCheckitemId, diff.toRemove()));
        }
        if (CollUtil.isNotEmpty(diff.toAdd())) {
            checkgroupCheckitemMapper.insertBatch(diff.toAdd().stream()
                .map(itemId -> newRelation(checkgroupId, itemId))
                .toList());
        }
        log.info("检查组[{}]关联差异更新：删除 {} 条，新增 {} 条", checkgroupId,
            diff.toRemove().size(), diff.toAdd().size());
    }

    private CheckgroupCheckitem newRelation(Integer checkgroupId, Integer checkitemId) {
        CheckgroupCheckitem relation = new CheckgroupCheckitem();
        relation.setCheckgroupId(checkgroupId);
        relation.setCheckitemId(checkitemId);
        return relation;
    }

    /**
     * 编码唯一校验（08 §4.1）
     */
    private void checkCodeUnique(String code, Integer excludeId) {
        boolean exists = baseMapper.exists(Wrappers.<Checkgroup>lambdaQuery()
            .eq(Checkgroup::getCode, code)
            .ne(ObjectUtil.isNotNull(excludeId), Checkgroup::getId, excludeId));
        if (exists) {
            throw new ServiceException("检查组编码已存在：" + code);
        }
    }

    private LambdaQueryWrapper<Checkgroup> buildQueryWrapper(CheckgroupBo bo) {
        return Wrappers.<Checkgroup>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getCode()), Checkgroup::getCode, bo.getCode())
            .like(StringUtils.isNotBlank(bo.getName()), Checkgroup::getName, bo.getName())
            .like(StringUtils.isNotBlank(bo.getHelpCode()), Checkgroup::getHelpCode, bo.getHelpCode())
            .eq(StringUtils.isNotBlank(bo.getSex()), Checkgroup::getSex, bo.getSex())
            .orderByAsc(Checkgroup::getCode);
    }

}
