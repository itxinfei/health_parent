package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.MapstructUtils;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.Checkitem;
import com.xinfei.health.appointment.domain.HealthCheckitemExt;
import com.xinfei.health.appointment.domain.HealthCheckitemRef;
import com.xinfei.health.appointment.domain.bo.CheckitemBo;
import com.xinfei.health.appointment.domain.bo.CheckitemExtBo;
import com.xinfei.health.appointment.domain.bo.CheckitemRefBo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthCheckitemExtMapper;
import com.xinfei.health.appointment.mapper.HealthCheckitemRefMapper;
import com.xinfei.health.appointment.service.ICheckitemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 检查项 服务层处理（08 §3.1、§4.1）
 *
 * <p>ext 并入主请求体：新增须同时落 ext 与至少一条参考区间（缺失 5007）；
 * 编辑 ext 走 upsert、refs 走差异更新；删除级联清理 ext 与 refs。
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class CheckitemServiceImpl implements ICheckitemService {

    /**
     * 结果类型值域（08 §7：0/1/2/3）
     */
    private static final Set<String> RESULT_TYPES = Set.of("0", "1", "2", "3");

    /**
     * 参考区间小数位上限（08 §7）
     */
    private static final int REF_SCALE_LIMIT = 4;

    private final CheckitemMapper baseMapper;
    private final CheckgroupCheckitemMapper checkgroupCheckitemMapper;
    private final HealthCheckitemExtMapper extMapper;
    private final HealthCheckitemRefMapper refMapper;

    @Override
    public TableDataInfo<CheckitemVo> queryPageList(CheckitemBo bo, PageQuery pageQuery) {
        Page<CheckitemVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        attachExt(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public List<CheckitemVo> queryList(CheckitemBo bo) {
        List<CheckitemVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        attachExt(list);
        return list;
    }

    @Override
    public CheckitemVo queryById(Integer id) {
        CheckitemVo vo = baseMapper.selectVoById(id);
        if (vo != null) {
            vo.setExt(extMapper.selectByCheckitemId(id));
            vo.setRefs(refMapper.selectByCheckitemId(id));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insertByBo(CheckitemBo bo) {
        checkCodeUnique(bo.getCode(), null);
        // 08 §4.1：ext 缺失返回 5007，禁止「只建主表不建扩展表」的半条数据
        validateExt(bo.getExt());
        // 08 §11 验收：新增必须同时落至少一条参考区间
        if (CollUtil.isEmpty(bo.getRefs())) {
            throw new ServiceException("新增检查项必须至少配置一条参考区间", HealthErrorCode.EXT_REQUIRED);
        }
        validateRefs(bo.getRefs(), null);
        Checkitem entity = MapstructUtils.convert(bo, Checkitem.class);
        baseMapper.insert(entity);
        insertExt(entity.getId(), bo.getExt());
        insertRefs(entity.getId(), bo.getRefs());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(CheckitemBo bo) {
        if (ObjectUtil.isNull(bo.getId())) {
            throw new ServiceException("检查项主键不能为空");
        }
        checkCodeUnique(bo.getCode(), bo.getId());
        boolean updated = baseMapper.updateById(MapstructUtils.convert(bo, Checkitem.class)) > 0;
        // ext：传了就校验并 upsert，不传保持原值（08 §4.1 编辑走 upsert）
        if (bo.getExt() != null) {
            validateExt(bo.getExt());
            upsertExt(bo.getId(), bo.getExt());
        }
        // refs：传了就差异更新（删旧 + 插新），不传保持原值
        if (bo.getRefs() != null) {
            if (CollUtil.isEmpty(bo.getRefs())) {
                throw new ServiceException("参考区间不能为空列表，如需清空请删除检查项", HealthErrorCode.REF_INVALID);
            }
            validateRefs(bo.getRefs(), bo.getId());
            refMapper.deleteByCheckitemId(bo.getId());
            insertRefs(bo.getId(), bo.getRefs());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        if (checkgroupCheckitemMapper.countByCheckitemId(id) > 0) {
            throw new ServiceException("该检查项已被检查组引用，不能删除", HealthErrorCode.CHECKITEM_REFERENCED);
        }
        // 级联清理扩展与参考区间（08 §4.1）
        refMapper.deleteByCheckitemId(id);
        extMapper.deleteByCheckitemId(id);
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public List<HealthCheckitemRef> queryRefs(Integer checkitemId) {
        return refMapper.selectByCheckitemId(checkitemId);
    }

    @Override
    public Long insertRef(Integer checkitemId, CheckitemRefBo bo) {
        validateRef(bo);
        checkRefUnique(checkitemId, bo, null);
        HealthCheckitemRef entity = toRefEntity(checkitemId, bo);
        refMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean updateRef(Long refId, CheckitemRefBo bo) {
        HealthCheckitemRef entity = refMapper.selectById(refId);
        if (entity == null) {
            throw new ServiceException("参考区间不存在：" + refId, HealthErrorCode.REF_INVALID);
        }
        validateRef(bo);
        checkRefUnique(entity.getCheckitemId(), bo, refId);
        HealthCheckitemRef update = toRefEntity(entity.getCheckitemId(), bo);
        update.setId(refId);
        return refMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteRef(Long refId) {
        return refMapper.deleteById(refId) > 0;
    }

    /**
     * 编码唯一校验（08 §4.1）
     *
     * @param code      待校验编码
     * @param excludeId 编辑时排除自身主键
     */
    private void checkCodeUnique(String code, Integer excludeId) {
        boolean exists = baseMapper.exists(Wrappers.<Checkitem>lambdaQuery()
            .eq(Checkitem::getCode, code)
            .ne(ObjectUtil.isNotNull(excludeId), Checkitem::getId, excludeId));
        if (exists) {
            throw new ServiceException("检查项编码已存在：" + code, HealthErrorCode.CODE_DUPLICATE);
        }
    }

    /**
     * ext 校验（08 §7：resultType 值域 0/1/2/3；0 时 unit 必填；deptId 必填）
     */
    private void validateExt(CheckitemExtBo ext) {
        if (ext == null) {
            throw new ServiceException("检查项扩展信息必填：结果类型、所属科室", HealthErrorCode.EXT_REQUIRED);
        }
        if (!RESULT_TYPES.contains(ext.getResultType())) {
            throw new ServiceException("结果类型非法（仅 0/1/2/3）：" + ext.getResultType(), HealthErrorCode.EXT_REQUIRED);
        }
        if ("0".equals(ext.getResultType()) && StringUtils.isBlank(ext.getUnit())) {
            throw new ServiceException("结果类型为数值时必须填写单位", HealthErrorCode.EXT_REQUIRED);
        }
        if (ext.getDeptId() == null) {
            throw new ServiceException("所属科室不能为空", HealthErrorCode.EXT_REQUIRED);
        }
    }

    /**
     * 参考区间集合校验：逐条 5012 + 同组唯一性
     */
    private void validateRefs(List<CheckitemRefBo> refs, Integer checkitemId) {
        Set<String> keys = new java.util.HashSet<>();
        for (CheckitemRefBo ref : refs) {
            validateRef(ref);
            String key = refKey(ref.getSex(), ref.getAgeMin(), ref.getAgeMax(), ref.getPhysioState());
            if (!keys.add(key)) {
                throw new ServiceException("参考区间组合重复（性别/年龄/生理状态）：" + key, HealthErrorCode.REF_INVALID);
            }
        }
        // 与库中已有区间（编辑时排除将被删除的旧记录，由调用方先删后插）比对
        if (checkitemId != null) {
            List<HealthCheckitemRef> existing = refMapper.selectByCheckitemId(checkitemId);
            Set<String> existingKeys = existing.stream()
                .map(r -> refKey(r.getSex(), r.getAgeMin(), r.getAgeMax(), r.getPhysioState()))
                .collect(Collectors.toSet());
            for (String key : keys) {
                if (existingKeys.contains(key)) {
                    throw new ServiceException("参考区间组合与已有记录重复：" + key, HealthErrorCode.REF_INVALID);
                }
            }
        }
    }

    /**
     * 单条参考区间校验（08 §7：同时存在时 refMin < refMax；小数位 ≤4）
     */
    private void validateRef(CheckitemRefBo ref) {
        if (StringUtils.isBlank(ref.getRefText())) {
            throw new ServiceException("参考区间原文不能为空", HealthErrorCode.REF_INVALID);
        }
        if (ref.getRefMin() != null && ref.getRefMax() != null
            && ref.getRefMin().compareTo(ref.getRefMax()) >= 0) {
            throw new ServiceException("参考区间下限必须小于上限", HealthErrorCode.REF_INVALID);
        }
        if (scaleExceeded(ref.getRefMin()) || scaleExceeded(ref.getRefMax())) {
            throw new ServiceException("参考区间数值小数位不能超过 4 位", HealthErrorCode.REF_INVALID);
        }
    }

    private boolean scaleExceeded(BigDecimal value) {
        return value != null && value.scale() > REF_SCALE_LIMIT;
    }

    private String refKey(String sex, Integer ageMin, Integer ageMax, String physioState) {
        return (StringUtils.isBlank(sex) ? "0" : sex) + "/"
            + (ageMin == null ? "*" : ageMin) + "-" + (ageMax == null ? "*" : ageMax) + "/"
            + (StringUtils.isBlank(physioState) ? "0" : physioState);
    }

    /**
     * 同检查项下区间组合唯一校验（编辑时排除自身）
     */
    private void checkRefUnique(Integer checkitemId, CheckitemRefBo bo, Long excludeId) {
        List<HealthCheckitemRef> existing = refMapper.selectByCheckitemId(checkitemId);
        String key = refKey(bo.getSex(), bo.getAgeMin(), bo.getAgeMax(), bo.getPhysioState());
        boolean duplicated = existing.stream()
            .filter(r -> excludeId == null || !excludeId.equals(r.getId()))
            .anyMatch(r -> refKey(r.getSex(), r.getAgeMin(), r.getAgeMax(), r.getPhysioState()).equals(key));
        if (duplicated) {
            throw new ServiceException("参考区间组合已存在：" + key, HealthErrorCode.REF_INVALID);
        }
    }

    /**
     * 插入扩展（char 标记缺省补 "0"，避免 NOT NULL 列插空）
     */
    private void insertExt(Integer checkitemId, CheckitemExtBo ext) {
        HealthCheckitemExt entity = new HealthCheckitemExt();
        entity.setCheckitemId(checkitemId);
        copyExt(ext, entity);
        extMapper.insert(entity);
    }

    /**
     * ext upsert：存在则整行更新，不存在则插入
     */
    private void upsertExt(Integer checkitemId, CheckitemExtBo ext) {
        HealthCheckitemExt entity = extMapper.selectByCheckitemId(checkitemId);
        if (entity == null) {
            insertExt(checkitemId, ext);
            return;
        }
        copyExt(ext, entity);
        extMapper.updateById(entity);
    }

    private void copyExt(CheckitemExtBo ext, HealthCheckitemExt entity) {
        entity.setResultType(ext.getResultType());
        entity.setUnit(ext.getUnit());
        entity.setDeptId(ext.getDeptId());
        entity.setSpecimen(ext.getSpecimen());
        entity.setIsFasting(ObjectUtil.defaultIfNull(ext.getIsFasting(), "0"));
        entity.setIsRadiation(ObjectUtil.defaultIfNull(ext.getIsRadiation(), "0"));
        entity.setIsRequired(ObjectUtil.defaultIfNull(ext.getIsRequired(), "0"));
        entity.setDualDeptFlag(ObjectUtil.defaultIfNull(ext.getDualDeptFlag(), "0"));
        entity.setSortNo(ObjectUtil.defaultIfNull(ext.getSortNo(), 0));
        entity.setDefaultConclusionId(ext.getDefaultConclusionId());
        entity.setNoticeText(ext.getNoticeText());
    }

    private void insertRefs(Integer checkitemId, List<CheckitemRefBo> refs) {
        for (CheckitemRefBo ref : refs) {
            refMapper.insert(toRefEntity(checkitemId, ref));
        }
    }

    private HealthCheckitemRef toRefEntity(Integer checkitemId, CheckitemRefBo bo) {
        HealthCheckitemRef entity = new HealthCheckitemRef();
        entity.setCheckitemId(checkitemId);
        entity.setSex(StringUtils.isBlank(bo.getSex()) ? "0" : bo.getSex());
        entity.setAgeMin(bo.getAgeMin());
        entity.setAgeMax(bo.getAgeMax());
        entity.setPhysioState(StringUtils.isBlank(bo.getPhysioState()) ? "0" : bo.getPhysioState());
        entity.setRefText(bo.getRefText());
        entity.setRefMin(bo.getRefMin());
        entity.setRefMax(bo.getRefMax());
        entity.setPriority(ObjectUtil.defaultIfNull(bo.getPriority(), 100));
        return entity;
    }

    /**
     * 列表批量附加 ext（避免逐行查询）
     */
    private void attachExt(List<CheckitemVo> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        List<Integer> ids = records.stream().map(CheckitemVo::getId).toList();
        Map<Integer, HealthCheckitemExt> extMap = extMapper.selectList(
                Wrappers.<HealthCheckitemExt>lambdaQuery().in(HealthCheckitemExt::getCheckitemId, ids))
            .stream().collect(Collectors.toMap(HealthCheckitemExt::getCheckitemId, Function.identity()));
        records.forEach(vo -> vo.setExt(extMap.get(vo.getId())));
    }

    private LambdaQueryWrapper<Checkitem> buildQueryWrapper(CheckitemBo bo) {
        return Wrappers.<Checkitem>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getCode()), Checkitem::getCode, bo.getCode())
            .like(StringUtils.isNotBlank(bo.getName()), Checkitem::getName, bo.getName())
            .eq(StringUtils.isNotBlank(bo.getSex()), Checkitem::getSex, bo.getSex())
            .eq(StringUtils.isNotBlank(bo.getType()), Checkitem::getType, bo.getType())
            .orderByAsc(Checkitem::getCode);
    }

}
