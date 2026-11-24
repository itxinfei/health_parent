package com.xinfei.health.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.constant.AssessmentErrorCode;
import com.xinfei.health.assessment.domain.Crowd;
import com.xinfei.health.assessment.domain.CrowdRule;
import com.xinfei.health.assessment.mapper.CrowdMapper;
import com.xinfei.health.assessment.mapper.CrowdRuleMapper;
import com.xinfei.health.assessment.mapper.MemberCrowdMapper;
import com.xinfei.health.assessment.service.ICrowdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 人群分类 服务层处理（09 §2.12；规则条件 7009）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class CrowdServiceImpl implements ICrowdService {

    private static final Set<String> SOURCE_TYPES = Set.of(
        CrowdRule.SOURCE_FACTOR, CrowdRule.SOURCE_INDICATOR, CrowdRule.SOURCE_REPORT,
        CrowdRule.SOURCE_PROFILE, CrowdRule.SOURCE_ASSESSMENT);

    private static final Set<String> OPERATORS = Set.of(
        CrowdRule.OP_GT, CrowdRule.OP_GE, CrowdRule.OP_LT, CrowdRule.OP_LE,
        CrowdRule.OP_EQ, CrowdRule.OP_NE, CrowdRule.OP_IN, CrowdRule.OP_BETWEEN);

    private final CrowdMapper baseMapper;
    private final CrowdRuleMapper ruleMapper;
    private final MemberCrowdMapper memberCrowdMapper;

    @Override
    public TableDataInfo<Crowd> queryPageList(Crowd bo, PageQuery pageQuery) {
        Page<Crowd> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<Crowd> queryList(Crowd bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    @Override
    public Crowd queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Long insert(Crowd entity) {
        checkCodeUnique(entity.getCode(), null);
        entity.setMemberCount(0);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean update(Crowd entity) {
        if (entity.getId() == null) {
            throw new ServiceException("人群主键不能为空");
        }
        checkCodeUnique(entity.getCode(), entity.getId());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteById(Long id) {
        ruleMapper.delete(Wrappers.<CrowdRule>lambdaQuery().eq(CrowdRule::getCrowdId, id));
        memberCrowdMapper.delete(Wrappers.<com.xinfei.health.assessment.domain.MemberCrowd>lambdaQuery()
            .eq(com.xinfei.health.assessment.domain.MemberCrowd::getCrowdId, id));
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public List<CrowdRule> listRules(Long crowdId) {
        return ruleMapper.selectList(Wrappers.<CrowdRule>lambdaQuery()
            .eq(CrowdRule::getCrowdId, crowdId)
            .orderByAsc(CrowdRule::getGroupNo)
            .orderByAsc(CrowdRule::getSort));
    }

    @Override
    public Long insertRule(CrowdRule entity) {
        validateRule(entity);
        ruleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean updateRule(CrowdRule entity) {
        if (entity.getId() == null) {
            throw new ServiceException("规则主键不能为空");
        }
        validateRule(entity);
        return ruleMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteRule(Long id) {
        return ruleMapper.deleteById(id) > 0;
    }

    /**
     * 规则条件校验（09 §7：source_type 合法、operator 属枚举、between 须给双值 → 7009）
     */
    private void validateRule(CrowdRule rule) {
        if (rule.getCrowdId() == null) {
            throw new ServiceException("人群规则必须归属人群", AssessmentErrorCode.CROWD_RULE_INVALID);
        }
        if (!SOURCE_TYPES.contains(rule.getSourceType())) {
            throw new ServiceException("规则条件来源非法：" + rule.getSourceType(), AssessmentErrorCode.CROWD_RULE_INVALID);
        }
        if (StringUtils.isBlank(rule.getSourceCode())) {
            throw new ServiceException("规则条件编码不能为空", AssessmentErrorCode.CROWD_RULE_INVALID);
        }
        if (!OPERATORS.contains(rule.getOperator())) {
            throw new ServiceException("规则运算符非法：" + rule.getOperator(), AssessmentErrorCode.CROWD_RULE_INVALID);
        }
        if (CrowdRule.OP_BETWEEN.equals(rule.getOperator())
            && (rule.getValueMin() == null || rule.getValueMax() == null)) {
            throw new ServiceException("between 运算符必须同时给出上下界", AssessmentErrorCode.CROWD_RULE_INVALID);
        }
    }

    private void checkCodeUnique(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            throw new ServiceException("人群编码不能为空");
        }
        boolean exists = baseMapper.exists(Wrappers.<Crowd>lambdaQuery()
            .eq(Crowd::getCode, code)
            .ne(excludeId != null, Crowd::getId, excludeId));
        if (exists) {
            throw new ServiceException("人群编码已存在：" + code);
        }
    }

    private LambdaQueryWrapper<Crowd> buildQueryWrapper(Crowd bo) {
        return Wrappers.<Crowd>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getCode()), Crowd::getCode, bo.getCode())
            .like(StringUtils.isNotBlank(bo.getName()), Crowd::getName, bo.getName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), Crowd::getStatus, bo.getStatus())
            .orderByAsc(Crowd::getCode);
    }

}
