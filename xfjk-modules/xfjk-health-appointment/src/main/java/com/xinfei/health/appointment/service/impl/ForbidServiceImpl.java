package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.Checkgroup;
import com.xinfei.health.appointment.domain.Checkitem;
import com.xinfei.health.appointment.domain.HealthCheckitemForbid;
import com.xinfei.health.appointment.domain.Setmeal;
import com.xinfei.health.appointment.domain.bo.ForbidBo;
import com.xinfei.health.appointment.domain.bo.ForbidCheckBo;
import com.xinfei.health.appointment.domain.vo.ForbidCheckVo;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckgroupMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthCheckitemForbidMapper;
import com.xinfei.health.appointment.mapper.SetmealCheckgroupMapper;
import com.xinfei.health.appointment.mapper.SetmealMapper;
import com.xinfei.health.appointment.service.IForbidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 禁忌规则 服务层处理（08 §4.5、§5.3 步骤 5）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class ForbidServiceImpl implements IForbidService {

    /**
     * 处置档合法值（08 §12：只有 0/1/2，禁止新增）
     */
    private static final Set<String> ACTIONS = Set.of("0", "1", "2");

    /**
     * scope 合法值（08 §2.7）
     */
    private static final Set<String> SCOPES = Set.of(
        HealthCheckitemForbid.SCOPE_CHECKITEM,
        HealthCheckitemForbid.SCOPE_CHECKGROUP,
        HealthCheckitemForbid.SCOPE_SETMEAL
    );

    private final HealthCheckitemForbidMapper forbidMapper;
    private final SetmealMapper setmealMapper;
    private final CheckgroupMapper checkgroupMapper;
    private final CheckitemMapper checkitemMapper;
    private final SetmealCheckgroupMapper setmealCheckgroupMapper;
    private final CheckgroupCheckitemMapper checkgroupCheckitemMapper;

    @Override
    public Long insertByBo(ForbidBo bo) {
        validateRule(bo, null);
        HealthCheckitemForbid entity = new HealthCheckitemForbid();
        copyBo(bo, entity);
        entity.setPriority(ObjectUtil.defaultIfNull(bo.getPriority(), 100));
        entity.setEnabled(ObjectUtil.defaultIfNull(bo.getEnabled(), "1"));
        forbidMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean updateByBo(ForbidBo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("禁忌规则主键不能为空", HealthErrorCode.FORBID_INVALID);
        }
        validateRule(bo, bo.getId());
        HealthCheckitemForbid entity = forbidMapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("禁忌规则不存在", HealthErrorCode.FORBID_INVALID);
        }
        copyBo(bo, entity);
        return forbidMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteById(Long id) {
        return forbidMapper.deleteById(id) > 0;
    }

    @Override
    public List<HealthCheckitemForbid> queryList() {
        return forbidMapper.selectList(Wrappers.<HealthCheckitemForbid>lambdaQuery()
            .orderByAsc(HealthCheckitemForbid::getPriority));
    }

    @Override
    public ForbidCheckVo trialCalc(ForbidCheckBo bo) {
        ForbidRuleMatcher.PersonTraits person = toTraits(bo);
        List<HealthCheckitemForbid> rules = selectRulesForSetmeal(bo.getSetmealId());

        List<ForbidCheckVo.ForbidHitVo> hits = new java.util.ArrayList<>();
        List<ForbidCheckVo.ForbidHitVo> warns = new java.util.ArrayList<>();
        List<ForbidCheckVo.ForbidHitVo> delays = new java.util.ArrayList<>();
        for (HealthCheckitemForbid rule : rules) {
            if (!ForbidRuleMatcher.matches(rule.getConditionJson(), person)) {
                continue;
            }
            ForbidCheckVo.ForbidHitVo hit = new ForbidCheckVo.ForbidHitVo();
            hit.setRuleCode(rule.getRuleCode());
            hit.setAction(rule.getAction());
            hit.setTargetId(rule.getTargetId());
            hit.setItemName(resolveTargetName(rule.getScope(), rule.getTargetId()));
            hit.setTipText(rule.getTipText());
            switch (rule.getAction()) {
                case "0" -> hits.add(hit);
                case "1" -> warns.add(hit);
                case "2" -> delays.add(hit);
                default -> throw new ServiceException("禁忌规则处置档非法：" + rule.getAction(),
                    HealthErrorCode.FORBID_INVALID);
            }
        }
        ForbidCheckVo vo = new ForbidCheckVo();
        vo.setPass(hits.isEmpty());
        vo.setHitCount(hits.size());
        vo.setHits(hits);
        vo.setWarns(warns);
        vo.setDelays(delays);
        return vo;
    }

    /**
     * 新增/编辑校验（08 §7：scope/action 值域、conditionJson 键白名单、ruleCode 唯一，违反 5013）
     */
    private void validateRule(ForbidBo bo, Long excludeId) {
        if (!SCOPES.contains(bo.getScope())) {
            throw new ServiceException("作用范围非法：" + bo.getScope(), HealthErrorCode.FORBID_INVALID);
        }
        if (!ACTIONS.contains(bo.getAction())) {
            throw new ServiceException("处置档非法（仅 0/1/2）：" + bo.getAction(), HealthErrorCode.FORBID_INVALID);
        }
        if (!ForbidRuleMatcher.isValidConditionJson(bo.getConditionJson())) {
            throw new ServiceException("触发条件 JSON 非法：仅允许 sex/pregnancy/married/hasSexHistory/"
                + "ageMin/ageMax/inMenstruation/metalImplant", HealthErrorCode.FORBID_INVALID);
        }
        if (forbidMapper.existsByRuleCode(bo.getRuleCode(), excludeId)) {
            throw new ServiceException("规则编码已存在：" + bo.getRuleCode(), HealthErrorCode.FORBID_INVALID);
        }
        if (resolveTargetName(bo.getScope(), bo.getTargetId()) == null) {
            throw new ServiceException("目标不存在：scope=" + bo.getScope() + " targetId=" + bo.getTargetId(),
                HealthErrorCode.FORBID_INVALID);
        }
    }

    private void copyBo(ForbidBo bo, HealthCheckitemForbid entity) {
        entity.setRuleCode(bo.getRuleCode());
        entity.setScope(bo.getScope());
        entity.setTargetId(bo.getTargetId());
        entity.setConditionJson(bo.getConditionJson());
        entity.setAction(bo.getAction());
        entity.setTipText(bo.getTipText());
        if (bo.getPriority() != null) {
            entity.setPriority(bo.getPriority());
        }
        if (bo.getEnabled() != null) {
            entity.setEnabled(bo.getEnabled());
        }
    }

    /**
     * 套餐触点规则：scope=SETMEAL 指向套餐、CHECKGROUP 指向套餐内检查组、CHECKITEM 指向组内检查项
     */
    private List<HealthCheckitemForbid> selectRulesForSetmeal(Integer setmealId) {
        List<Integer> groupIds = setmealCheckgroupMapper.selectBySetmealId(setmealId).stream()
            .map(com.xinfei.health.appointment.domain.SetmealCheckgroup::getCheckgroupId)
            .distinct().toList();
        List<Integer> itemIds = groupIds.isEmpty() ? List.of()
            : checkgroupCheckitemMapper.selectByCheckgroupIds(groupIds).stream()
                .map(com.xinfei.health.appointment.domain.CheckgroupCheckitem::getCheckitemId)
                .distinct().toList();
        return forbidMapper.selectEnabledByTargets(setmealId, groupIds, itemIds);
    }

    /**
     * 按 scope 解析目标名称（列表与试算的 itemName）
     */
    private String resolveTargetName(String scope, Integer targetId) {
        return switch (scope) {
            case HealthCheckitemForbid.SCOPE_SETMEAL -> {
                Setmeal setmeal = setmealMapper.selectById(targetId);
                yield setmeal == null ? null : setmeal.getName();
            }
            case HealthCheckitemForbid.SCOPE_CHECKGROUP -> {
                Checkgroup group = checkgroupMapper.selectById(targetId);
                yield group == null ? null : group.getName();
            }
            case HealthCheckitemForbid.SCOPE_CHECKITEM -> {
                Checkitem item = checkitemMapper.selectById(targetId);
                yield item == null ? null : item.getName();
            }
            default -> null;
        };
    }

    private ForbidRuleMatcher.PersonTraits toTraits(ForbidCheckBo bo) {
        ForbidRuleMatcher.PersonTraits p = new ForbidRuleMatcher.PersonTraits();
        p.setSex(bo.getSex());
        p.setMarried(bo.getMarried());
        p.setHasSexHistory(bo.getHasSexHistory());
        p.setPregnancy(bo.getPregnancy());
        p.setAge(bo.getAge());
        p.setInMenstruation(bo.getInMenstruation());
        p.setMetalImplant(bo.getMetalImplant());
        return p;
    }

}
