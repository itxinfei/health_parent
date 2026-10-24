package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.satoken.utils.LoginHelper;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.HealthMemberArchiveNo;
import com.xinfei.health.appointment.domain.HealthMemberHistory;
import com.xinfei.health.appointment.domain.HealthMemberMissing;
import com.xinfei.health.appointment.domain.HealthMemberProfile;
import com.xinfei.health.appointment.domain.HealthMemberTag;
import com.xinfei.health.appointment.domain.HealthMemberTagRel;
import com.xinfei.health.appointment.domain.Member;
import com.xinfei.health.appointment.domain.bo.MemberHistoryBo;
import com.xinfei.health.appointment.domain.bo.MemberMissingBo;
import com.xinfei.health.appointment.domain.bo.MemberProfileBo;
import com.xinfei.health.appointment.domain.bo.MemberQueryBo;
import com.xinfei.health.appointment.domain.bo.MemberSaveBo;
import com.xinfei.health.appointment.domain.bo.MemberStatusBo;
import com.xinfei.health.appointment.domain.vo.MemberCompletenessVo;
import com.xinfei.health.appointment.domain.vo.MemberDetailVo;
import com.xinfei.health.appointment.domain.vo.MemberExportVo;
import com.xinfei.health.appointment.domain.vo.MemberVo;
import com.xinfei.health.appointment.mapper.HealthMemberArchiveNoMapper;
import com.xinfei.health.appointment.mapper.HealthMemberHistoryMapper;
import com.xinfei.health.appointment.mapper.HealthMemberMissingMapper;
import com.xinfei.health.appointment.mapper.HealthMemberProfileMapper;
import com.xinfei.health.appointment.mapper.HealthMemberTagMapper;
import com.xinfei.health.appointment.mapper.HealthMemberTagRelMapper;
import com.xinfei.health.appointment.mapper.MemberMapper;
import com.xinfei.health.appointment.service.IMemberService;
import com.xinfei.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 会员与档案服务实现（07 §3.1 会员/档案侧，B6）
 *
 * <p>一号制：唯一性只认 t_health_member_archive_no（07 §2.2），t_member.fileNumber 仅冗余展示；
 * 停用标记唯一落点 profile.member_status（07 §5.8）；敏感字段输出一律脱敏（07 §9）。
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements IMemberService {

    private final MemberMapper memberMapper;
    private final HealthMemberProfileMapper profileMapper;
    private final HealthMemberArchiveNoMapper archiveNoMapper;
    private final HealthMemberMissingMapper missingMapper;
    private final HealthMemberHistoryMapper historyMapper;
    private final HealthMemberTagMapper tagMapper;
    private final HealthMemberTagRelMapper tagRelMapper;
    private final ISysConfigService configService;

    /** 必填清单配置键（07 §5.2） */
    private static final String CFG_REQUIRED = "health:profile:required";
    /** 档案号规则三键（04-config：前缀 + 区划占位） */
    private static final String CFG_ARCHIVE_PREFIX = "health:number:archive_prefix";
    private static final String CFG_ARCHIVE_REGION = "health:number:archive_region_code";
    /** 导出脱敏级别（本期约定=部分脱敏） */
    private static final String CFG_EXPORT_MASK = "health:export:mask_level";

    /** 手机号规则（18 H15：11 位 1 开头） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    /** 档案号序号位数 */
    private static final int ARCHIVE_SEQ_LEN = 5;
    /** 档案号冲突重试次数（07 §2.2） */
    private static final int ARCHIVE_RETRY = 3;
    /** 导出行数上限（06 §9 6104，同订单导出遗留口径） */
    private static final int EXPORT_MAX_ROWS = 6104;

    @Override
    public TableDataInfo<MemberVo> queryPageList(MemberQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Member> lqw = buildQueryWrapper(bo);
        if (lqw == null) {
            return TableDataInfo.build(new Page<>());
        }
        Page<Member> page = memberMapper.selectPage(pageQuery.build(), lqw.orderByDesc(Member::getId));
        List<MemberVo> rows = assembleVoList(page.getRecords(), true);
        Page<MemberVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(rows);
        return TableDataInfo.build(voPage);
    }

    /**
     * 列表/导出共用筛选（07 §3.1）；返回 null 表示状态/档案号/标签筛选无命中
     */
    private LambdaQueryWrapper<Member> buildQueryWrapper(MemberQueryBo bo) {
        LambdaQueryWrapper<Member> lqw = Wrappers.<Member>lambdaQuery()
            .like(StrUtil.isNotBlank(bo.getName()), Member::getName, bo.getName())
            .like(StrUtil.isNotBlank(bo.getPhoneNumber()), Member::getPhoneNumber, bo.getPhoneNumber())
            .like(StrUtil.isNotBlank(bo.getIdCard()), Member::getIdCard, bo.getIdCard())
            .eq(StrUtil.isNotBlank(bo.getSex()), Member::getSex, bo.getSex());
        // 状态默认只看正常（07 §5.8 四处一致第 1 处）：无 profile 行的存量会员按正常处理
        String status = StrUtil.blankToDefault(bo.getMemberStatus(), HealthMemberProfile.STATUS_NORMAL);
        if (HealthMemberProfile.STATUS_DISABLED.equals(status)) {
            List<Integer> disabledIds = profileMapper.selectList(Wrappers.<HealthMemberProfile>lambdaQuery()
                    .eq(HealthMemberProfile::getMemberStatus, HealthMemberProfile.STATUS_DISABLED)
                    .select(HealthMemberProfile::getMemberId))
                .stream().map(HealthMemberProfile::getMemberId).toList();
            if (disabledIds.isEmpty()) {
                return null;
            }
            lqw.in(Member::getId, disabledIds);
        } else {
            List<Integer> disabledIds = profileMapper.selectList(Wrappers.<HealthMemberProfile>lambdaQuery()
                    .eq(HealthMemberProfile::getMemberStatus, HealthMemberProfile.STATUS_DISABLED)
                    .select(HealthMemberProfile::getMemberId))
                .stream().map(HealthMemberProfile::getMemberId).toList();
            if (!disabledIds.isEmpty()) {
                lqw.notIn(Member::getId, disabledIds);
            }
        }
        // 档案号精确匹配走一号制表（07 §10 禁止用 t_member.fileNumber 判唯一）
        if (StrUtil.isNotBlank(bo.getFileNumber())) {
            List<Integer> ids = archiveNoMapper.selectList(Wrappers.<HealthMemberArchiveNo>lambdaQuery()
                    .eq(HealthMemberArchiveNo::getFileNumber, bo.getFileNumber())
                    .select(HealthMemberArchiveNo::getMemberId))
                .stream().map(HealthMemberArchiveNo::getMemberId).toList();
            if (ids.isEmpty()) {
                return null;
            }
            lqw.in(Member::getId, ids);
        }
        // 标签筛选
        if (ObjectUtil.isNotNull(bo.getTagId())) {
            List<Integer> ids = tagRelMapper.selectList(Wrappers.<HealthMemberTagRel>lambdaQuery()
                    .eq(HealthMemberTagRel::getTagId, bo.getTagId())
                    .select(HealthMemberTagRel::getMemberId))
                .stream().map(HealthMemberTagRel::getMemberId).toList();
            if (ids.isEmpty()) {
                return null;
            }
            lqw.in(Member::getId, ids);
        }
        return lqw;
    }

    /**
     * 批量装配 Vo（状态/档案号/标签/完整度各一次批量查询）
     */
    private List<MemberVo> assembleVoList(List<Member> members, boolean withCompleteness) {
        if (members.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = members.stream().map(Member::getId).toList();
        Map<Integer, HealthMemberProfile> profileMap = profileMapper.selectList(
                Wrappers.<HealthMemberProfile>lambdaQuery().in(HealthMemberProfile::getMemberId, ids))
            .stream().collect(Collectors.toMap(HealthMemberProfile::getMemberId, Function.identity(), (a, b) -> a));
        Map<Integer, String> archiveMap = archiveNoMapper.selectList(
                Wrappers.<HealthMemberArchiveNo>lambdaQuery().in(HealthMemberArchiveNo::getMemberId, ids))
            .stream().collect(Collectors.toMap(HealthMemberArchiveNo::getMemberId, HealthMemberArchiveNo::getFileNumber, (a, b) -> a));
        Map<Integer, List<HealthMemberTag>> tagMap = loadTagMap(ids);
        List<String> required = requiredFields();
        Map<String, Set<String>> requiredProps = requiredPropertySets(required);
        List<MemberVo> result = new ArrayList<>(members.size());
        for (Member m : members) {
            MemberVo vo = toMaskedVo(m);
            vo.setFileNumber(archiveMap.getOrDefault(m.getId(), m.getFileNumber()));
            HealthMemberProfile profile = profileMap.get(m.getId());
            vo.setMemberStatus(profile == null ? HealthMemberProfile.STATUS_NORMAL : profile.getMemberStatus());
            vo.setTags(tagMap.getOrDefault(m.getId(), List.of()));
            if (withCompleteness) {
                Set<String> missingKeys = loadMissingKeys(m.getId());
                vo.setCompleteness(completenessText(required, props(m), props(profile), missingKeys));
            }
            result.add(vo);
        }
        return result;
    }

    private Map<Integer, List<HealthMemberTag>> loadTagMap(List<Integer> memberIds) {
        List<HealthMemberTagRel> rels = tagRelMapper.selectList(Wrappers.<HealthMemberTagRel>lambdaQuery()
            .in(HealthMemberTagRel::getMemberId, memberIds));
        if (rels.isEmpty()) {
            return Map.of();
        }
        Set<Long> tagIds = rels.stream().map(HealthMemberTagRel::getTagId).collect(Collectors.toSet());
        Map<Long, HealthMemberTag> tags = tagMapper.selectByIds(tagIds).stream()
            .collect(Collectors.toMap(HealthMemberTag::getId, Function.identity()));
        Map<Integer, List<HealthMemberTag>> result = new HashMap<>();
        for (HealthMemberTagRel rel : rels) {
            HealthMemberTag tag = tags.get(rel.getTagId());
            if (tag != null) {
                result.computeIfAbsent(rel.getMemberId(), k -> new ArrayList<>()).add(tag);
            }
        }
        return result;
    }

    @Override
    public MemberDetailVo queryDetail(Integer memberId) {
        Member member = requireMember(memberId);
        HealthMemberProfile profile = findProfile(memberId);
        MemberVo vo = toMaskedVo(member);
        Map<Integer, String> archiveMap = archiveNoMapper.selectList(
                Wrappers.<HealthMemberArchiveNo>lambdaQuery().in(HealthMemberArchiveNo::getMemberId, List.of(memberId)))
            .stream().collect(Collectors.toMap(HealthMemberArchiveNo::getMemberId, HealthMemberArchiveNo::getFileNumber));
        vo.setFileNumber(archiveMap.get(memberId));
        vo.setMemberStatus(profile == null ? HealthMemberProfile.STATUS_NORMAL : profile.getMemberStatus());
        vo.setTags(loadTagMap(List.of(memberId)).getOrDefault(memberId, List.of()));

        MemberDetailVo detail = new MemberDetailVo();
        detail.setMember(vo);
        detail.setProfile(profile);
        detail.setHistories(historyMapper.selectList(Wrappers.<HealthMemberHistory>lambdaQuery()
            .eq(HealthMemberHistory::getMemberId, memberId)
            .orderByAsc(HealthMemberHistory::getId)));
        List<String> required = requiredFields();
        detail.setCompleteness(completeness(required, props(member), props(profile),
            missingMapper.selectList(Wrappers.<HealthMemberMissing>lambdaQuery()
                .eq(HealthMemberMissing::getMemberId, memberId))));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberVo insertMember(MemberSaveBo bo) {
        if (StrUtil.isBlank(bo.getIdCard())) {
            throw new ServiceException("身份证号不能为空", HealthErrorCode.IDCARD_INVALID);
        }
        if (!IdcardUtil.isValidCard(bo.getIdCard())) {
            throw new ServiceException("身份证号不合法：校验位错误", HealthErrorCode.IDCARD_INVALID);
        }
        checkPhone(bo.getPhoneNumber());
        Long dup = memberMapper.selectCount(Wrappers.<Member>lambdaQuery().eq(Member::getIdCard, bo.getIdCard()));
        if (dup > 0) {
            throw new ServiceException("该身份证已建档（会员不允许重复建档，07 §5.1）", HealthErrorCode.ARCHIVE_NO_CONFLICT);
        }
        Member member = new Member();
        member.setName(bo.getName());
        member.setSex(bo.getSex());
        member.setIdCard(bo.getIdCard());
        member.setPhoneNumber(bo.getPhoneNumber());
        member.setBirthday(bo.getBirthday());
        member.setEmail(bo.getEmail());
        member.setRemark(bo.getRemark());
        member.setRegTime(LocalDate.now());
        memberMapper.insert(member);

        HealthMemberArchiveNo archive = generateArchiveNo(member.getId(), HealthMemberArchiveNo.SOURCE_MANUAL);
        Member update = new Member();
        update.setId(member.getId());
        update.setFileNumber(archive.getFileNumber());
        memberMapper.updateById(update);

        HealthMemberProfile profile = new HealthMemberProfile();
        profile.setMemberId(member.getId());
        profile.setMemberStatus(HealthMemberProfile.STATUS_NORMAL);
        profile.setDataTime(LocalDateTime.now());
        profileMapper.insert(profile);

        MemberVo vo = toMaskedVo(member);
        vo.setFileNumber(archive.getFileNumber());
        vo.setMemberStatus(HealthMemberProfile.STATUS_NORMAL);
        List<String> required = requiredFields();
        vo.setCompleteness(completenessText(required, props(member), props(profile), Set.of()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMember(MemberSaveBo bo) {
        Member exist = requireMember(bo.getId());
        // idCard 建后不可改（07 §5.1）；历史种子 NULL 允许补录一次
        if (StrUtil.isNotBlank(bo.getIdCard()) && StrUtil.isNotBlank(exist.getIdCard())
            && !bo.getIdCard().equals(exist.getIdCard())) {
            throw new ServiceException("身份证号建档后不可修改", HealthErrorCode.IDCARD_INVALID);
        }
        // 列表值是脱敏串无法回填：编辑留空 = 不改原手机号
        if (StrUtil.isNotBlank(bo.getPhoneNumber())) {
            checkPhone(bo.getPhoneNumber());
        }
        Member member = new Member();
        member.setId(exist.getId());
        member.setName(bo.getName());
        member.setSex(bo.getSex());
        // updateById 只忽略 null 不忽略空串：留空必须不写列，否则会把原号抹掉（07 §5.1）
        if (StrUtil.isNotBlank(bo.getPhoneNumber())) {
            member.setPhoneNumber(bo.getPhoneNumber());
        }
        member.setBirthday(bo.getBirthday());
        member.setEmail(bo.getEmail());
        member.setRemark(bo.getRemark());
        if (StrUtil.isBlank(exist.getIdCard()) && StrUtil.isNotBlank(bo.getIdCard())) {
            if (!IdcardUtil.isValidCard(bo.getIdCard())) {
                throw new ServiceException("身份证号不合法：校验位错误", HealthErrorCode.IDCARD_INVALID);
            }
            Long dup = memberMapper.selectCount(Wrappers.<Member>lambdaQuery()
                .eq(Member::getIdCard, bo.getIdCard()).ne(Member::getId, exist.getId()));
            if (dup > 0) {
                throw new ServiceException("该身份证已被其他会员使用", HealthErrorCode.IDCARD_INVALID);
            }
            member.setIdCard(bo.getIdCard());
        }
        return memberMapper.updateById(member) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Integer memberId, MemberStatusBo bo) {
        requireMember(memberId);
        String status = bo.getMemberStatus();
        if (!HealthMemberProfile.STATUS_NORMAL.equals(status) && !HealthMemberProfile.STATUS_DISABLED.equals(status)) {
            throw new ServiceException("非法会员状态值：" + status);
        }
        HealthMemberProfile profile = findProfile(memberId);
        if (profile == null) {
            // 无 profile 行的存量会员按 upsert 先建行（07 §5.8）
            profile = new HealthMemberProfile();
            profile.setMemberId(memberId);
            profile.setDataTime(LocalDateTime.now());
            profileMapper.insert(profile);
        }
        return profileMapper.update(null, Wrappers.<HealthMemberProfile>lambdaUpdate()
            .eq(HealthMemberProfile::getId, profile.getId())
            .set(HealthMemberProfile::getMemberStatus, status)) > 0;
    }

    @Override
    public MemberVo queryArchiveNo(Integer memberId) {
        Member member = requireMember(memberId);
        MemberVo vo = new MemberVo();
        vo.setId(member.getId());
        vo.setName(member.getName());
        archiveNoMapper.selectList(Wrappers.<HealthMemberArchiveNo>lambdaQuery()
                .eq(HealthMemberArchiveNo::getMemberId, memberId))
            .stream().findFirst()
            .ifPresent(archive -> vo.setFileNumber(archive.getFileNumber()));
        return vo;
    }

    @Override
    public HealthMemberProfile queryProfile(Integer memberId) {
        requireMember(memberId);
        return findProfile(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveProfile(Integer memberId, MemberProfileBo bo) {
        requireMember(memberId);
        checkBloodPressure(bo.getBloodPressureHigh());
        checkBloodPressure(bo.getBloodPressureLow());
        HealthMemberProfile profile = findProfile(memberId);
        boolean isNew = profile == null;
        if (isNew) {
            profile = new HealthMemberProfile();
            profile.setMemberId(memberId);
        }
        BeanUtil.copyProperties(bo, profile, "memberId");
        // bmi / 腰臀比服务端计算，前端值忽略（07 §7）
        profile.setBmi(calcBmi(bo.getHeight(), bo.getWeight()));
        profile.setWhtr(calcWhtr(bo.getWaist(), bo.getHip()));
        if (profile.getDataTime() == null) {
            profile.setDataTime(LocalDateTime.now());
        }
        if (isNew) {
            return profileMapper.insert(profile) > 0;
        }
        return profileMapper.updateById(profile) > 0;
    }

    @Override
    public MemberCompletenessVo queryCompleteness(Integer memberId) {
        Member member = requireMember(memberId);
        List<HealthMemberMissing> missingRows = missingMapper.selectList(Wrappers.<HealthMemberMissing>lambdaQuery()
            .eq(HealthMemberMissing::getMemberId, memberId));
        return completeness(requiredFields(), props(member), props(findProfile(memberId)), missingRows);
    }

    @Override
    public Boolean markMissing(Integer memberId, MemberMissingBo bo) {
        requireMember(memberId);
        HealthMemberMissing exist = missingMapper.selectOne(Wrappers.<HealthMemberMissing>lambdaQuery()
            .eq(HealthMemberMissing::getMemberId, memberId)
            .eq(HealthMemberMissing::getFieldKey, bo.getFieldKey()));
        HealthMemberMissing missing = exist == null ? new HealthMemberMissing() : exist;
        missing.setMemberId(memberId);
        missing.setFieldKey(bo.getFieldKey());
        missing.setReason(bo.getReason());
        missing.setMarkedBy(LoginHelper.getUsername());
        missing.setMarkTime(LocalDateTime.now());
        return exist == null
            ? missingMapper.insert(missing) > 0
            : missingMapper.updateById(missing) > 0;
    }

    @Override
    public Boolean unmarkMissing(Integer memberId, String fieldKey) {
        requireMember(memberId);
        return missingMapper.delete(Wrappers.<HealthMemberMissing>lambdaQuery()
            .eq(HealthMemberMissing::getMemberId, memberId)
            .eq(HealthMemberMissing::getFieldKey, fieldKey)) > 0;
    }

    @Override
    public List<MemberHistoryBo> queryHistories(Integer memberId, String type) {
        requireMember(memberId);
        return historyMapper.selectList(Wrappers.<HealthMemberHistory>lambdaQuery()
                .eq(HealthMemberHistory::getMemberId, memberId)
                .eq(StrUtil.isNotBlank(type), HealthMemberHistory::getType, type)
                .orderByAsc(HealthMemberHistory::getId))
            .stream().map(e -> BeanUtil.copyProperties(e, MemberHistoryBo.class)).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveHistories(Integer memberId, List<MemberHistoryBo> list) {
        requireMember(memberId);
        for (MemberHistoryBo bo : list) {
            if (!HealthMemberHistory.TYPES.contains(bo.getType())) {
                throw new ServiceException("非法病史类型：" + bo.getType());
            }
            if (StrUtil.isBlank(bo.getContent()) || bo.getContent().length() > 500) {
                throw new ServiceException("病史描述必填且不超过500字：" + bo.getType());
            }
        }
        historyMapper.delete(Wrappers.<HealthMemberHistory>lambdaQuery()
            .eq(HealthMemberHistory::getMemberId, memberId));
        for (MemberHistoryBo bo : list) {
            HealthMemberHistory entity = BeanUtil.copyProperties(bo, HealthMemberHistory.class);
            entity.setMemberId(memberId);
            if (StrUtil.isBlank(entity.getConfirmFlag())) {
                entity.setConfirmFlag("1");
            }
            historyMapper.insert(entity);
        }
        return true;
    }

    @Override
    public List<HealthMemberTag> queryEnabledTags() {
        return tagMapper.selectList(Wrappers.<HealthMemberTag>lambdaQuery()
            .eq(HealthMemberTag::getEnabled, "1")
            .orderByAsc(HealthMemberTag::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveTags(Integer memberId, List<Long> tagIds) {
        requireMember(memberId);
        if (!tagIds.isEmpty()) {
            List<HealthMemberTag> tags = tagMapper.selectByIds(tagIds);
            if (tags.size() != new HashSet<>(tagIds).size()) {
                throw new ServiceException("存在无效标签id");
            }
            if (tags.stream().anyMatch(t -> !"1".equals(t.getEnabled()))) {
                throw new ServiceException("停用标签不可指派");
            }
        }
        tagRelMapper.delete(Wrappers.<HealthMemberTagRel>lambdaQuery()
            .eq(HealthMemberTagRel::getMemberId, memberId));
        for (Long tagId : tagIds) {
            HealthMemberTagRel rel = new HealthMemberTagRel();
            rel.setTagId(tagId);
            rel.setMemberId(memberId);
            rel.setSource(HealthMemberTagRel.SOURCE_MANUAL);
            tagRelMapper.insert(rel);
        }
        return true;
    }

    @Override
    public List<MemberExportVo> queryExportList(MemberQueryBo bo) {
        LambdaQueryWrapper<Member> lqw = buildQueryWrapper(bo);
        if (lqw == null) {
            return List.of();
        }
        List<Member> members = memberMapper.selectList(lqw.orderByDesc(Member::getId));
        if (members.isEmpty()) {
            return List.of();
        }
        if (members.size() > EXPORT_MAX_ROWS) {
            throw new ServiceException("导出行数超过 " + EXPORT_MAX_ROWS + " 上限，请缩小筛选范围",
                HealthErrorCode.EXPORT_ROW_LIMIT);
        }
        // 脱敏级别（health_export_mask）：本期约定=部分脱敏；明文分支在 07 §9 清单页禁用，仅留接口
        boolean plain = "明文".equals(configService.selectConfigByKey(CFG_EXPORT_MASK));
        List<Integer> ids = members.stream().map(Member::getId).toList();
        Map<Integer, HealthMemberProfile> profileMap = profileMapper.selectList(
                Wrappers.<HealthMemberProfile>lambdaQuery().in(HealthMemberProfile::getMemberId, ids))
            .stream().collect(Collectors.toMap(HealthMemberProfile::getMemberId, Function.identity(), (a, b) -> a));
        Map<Integer, String> archiveMap = archiveNoMapper.selectList(
                Wrappers.<HealthMemberArchiveNo>lambdaQuery().in(HealthMemberArchiveNo::getMemberId, ids))
            .stream().collect(Collectors.toMap(HealthMemberArchiveNo::getMemberId, HealthMemberArchiveNo::getFileNumber, (a, b) -> a));
        List<String> required = requiredFields();
        List<MemberExportVo> result = new ArrayList<>(members.size());
        for (Member m : members) {
            MemberExportVo vo = new MemberExportVo();
            vo.setId(m.getId());
            vo.setFileNumber(archiveMap.getOrDefault(m.getId(), m.getFileNumber()));
            vo.setName(m.getName());
            vo.setSex(m.getSex());
            vo.setIdCard(plain ? m.getIdCard() : DesensitizedUtil.idCardNum(m.getIdCard(), 4, 4));
            vo.setPhoneNumber(plain ? m.getPhoneNumber() : DesensitizedUtil.mobilePhone(m.getPhoneNumber()));
            vo.setBirthday(m.getBirthday());
            vo.setRegTime(m.getRegTime());
            HealthMemberProfile profile = profileMap.get(m.getId());
            vo.setMemberStatus(profile == null ? HealthMemberProfile.STATUS_NORMAL : profile.getMemberStatus());
            vo.setCompleteness(completenessText(required, props(m), props(profile), loadMissingKeys(m.getId())));
            result.add(vo);
        }
        return result;
    }

    // ---------- 私有工具 ----------

    private Member requireMember(Integer memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在：" + memberId, HealthErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    private HealthMemberProfile findProfile(Integer memberId) {
        return profileMapper.selectOne(Wrappers.<HealthMemberProfile>lambdaQuery()
            .eq(HealthMemberProfile::getMemberId, memberId));
    }

    /**
     * 列表/详情统一脱敏输出（07 §9：列表、详情、导出均脱敏）
     */
    private MemberVo toMaskedVo(Member member) {
        MemberVo vo = BeanUtil.copyProperties(member, MemberVo.class);
        vo.setIdCard(DesensitizedUtil.idCardNum(member.getIdCard(), 4, 4));
        vo.setPhoneNumber(DesensitizedUtil.mobilePhone(member.getPhoneNumber()));
        return vo;
    }

    private void checkPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            throw new ServiceException("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ServiceException("手机号格式不合法（18 H15）：" + phone);
        }
    }

    /**
     * 血压尾数须为偶（07 §7，18 B5 同规则）→ 6007
     */
    private void checkBloodPressure(Integer value) {
        if (value != null && value % 2 != 0) {
            throw new ServiceException("血压尾数须为偶数：" + value, HealthErrorCode.BP_INVALID);
        }
    }

    private BigDecimal calcBmi(BigDecimal heightCm, BigDecimal weightKg) {
        if (heightCm == null || weightKg == null
            || heightCm.compareTo(BigDecimal.ZERO) <= 0 || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal meters = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weightKg.divide(meters.multiply(meters), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcWhtr(BigDecimal waist, BigDecimal hip) {
        if (waist == null || hip == null || hip.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return waist.divide(hip, 4, RoundingMode.HALF_UP);
    }

    /**
     * 档案号生成（07 §2.2）：前缀+区划+yyyyMMdd+5位序号，取当日最大值+1，撞唯一索引重试 3 次 → 6002
     */
    private HealthMemberArchiveNo generateArchiveNo(Integer memberId, String bindSource) {
        String prefix = configService.selectConfigByKey(CFG_ARCHIVE_PREFIX)
            + configService.selectConfigByKey(CFG_ARCHIVE_REGION)
            + LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        int seq = nextSeq(prefix);
        for (int attempt = 0; attempt < ARCHIVE_RETRY; attempt++) {
            HealthMemberArchiveNo archive = new HealthMemberArchiveNo();
            archive.setMemberId(memberId);
            archive.setBindSource(bindSource);
            archive.setFileNumber(prefix + StrUtil.fillBefore(String.valueOf(seq), '0', ARCHIVE_SEQ_LEN));
            try {
                archiveNoMapper.insert(archive);
                return archive;
            } catch (Exception e) {
                log.warn("档案号生成冲突（第 {} 次）：{}", attempt + 1, archive.getFileNumber(), e);
                seq = nextSeq(prefix);
            }
        }
        throw new ServiceException("档案号生成冲突，请重试", HealthErrorCode.ARCHIVE_NO_CONFLICT);
    }

    private int nextSeq(String prefix) {
        String max = archiveNoMapper.selectList(Wrappers.<HealthMemberArchiveNo>lambdaQuery()
                .likeRight(HealthMemberArchiveNo::getFileNumber, prefix)
                .select(HealthMemberArchiveNo::getFileNumber))
            .stream().map(HealthMemberArchiveNo::getFileNumber)
            .max(String::compareTo).orElse(null);
        if (max == null || max.length() <= prefix.length()) {
            return 1;
        }
        try {
            return Integer.parseInt(max.substring(prefix.length())) + 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private List<String> requiredFields() {
        String raw = configService.selectConfigByKey(CFG_REQUIRED);
        if (StrUtil.isBlank(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split(",")).map(String::trim).filter(StrUtil::isNotBlank).toList();
    }

    /** 实体驼峰属性名缓存（完整度反射取值用） */
    private static final Map<Class<?>, Set<String>> CAMEL_PROPS = new HashMap<>();

    private Map<String, Set<String>> requiredPropertySets(List<String> required) {
        Set<String> memberCamel = camelProps(Member.class);
        Set<String> snake = new HashSet<>();
        for (String key : required) {
            if (!memberCamel.contains(toCamel(key))) {
                snake.add(key);
            }
        }
        Set<String> memberKeys = required.stream().map(this::toCamel).filter(memberCamel::contains).collect(Collectors.toSet());
        return Map.of("member", memberKeys, "profile", snake);
    }

    private Set<String> camelProps(Class<?> clazz) {
        return CAMEL_PROPS.computeIfAbsent(clazz, c -> {
            Set<String> names = new HashSet<>();
            for (var m : c.getMethods()) {
                if (m.getName().startsWith("get") && m.getName().length() > 3 && m.getParameterCount() == 0) {
                    names.add(StrUtil.lowerFirst(m.getName().substring(3)));
                }
            }
            return names;
        });
    }

    private Map<String, Object> props(Object bean) {
        return bean == null ? Map.of() : BeanUtil.beanToMap(bean, false, true);
    }

    private String toCamel(String key) {
        if (!key.contains("_")) {
            return key;
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : key.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

    private Set<String> loadMissingKeys(Integer memberId) {
        return missingMapper.selectList(Wrappers.<HealthMemberMissing>lambdaQuery()
                .eq(HealthMemberMissing::getMemberId, memberId)
                .select(HealthMemberMissing::getFieldKey))
            .stream().map(HealthMemberMissing::getFieldKey).collect(Collectors.toSet());
    }

    private String completenessText(List<String> required, Map<String, Object> memberProps,
                                    Map<String, Object> profileProps, Set<String> missingKeys) {
        Map<String, Set<String>> sets = requiredPropertySets(required);
        int filled = 0;
        for (String key : required) {
            String camel = toCamel(key);
            Object value = sets.get("member").contains(camel) ? memberProps.get(camel) : profileProps.get(camel);
            if (isFilled(value) || missingKeys.contains(key)) {
                filled++;
            }
        }
        return filled + "/" + required.size();
    }

    private MemberCompletenessVo completeness(List<String> required, Map<String, Object> memberProps,
                                              Map<String, Object> profileProps, List<HealthMemberMissing> missingRows) {
        Map<String, Set<String>> sets = requiredPropertySets(required);
        Set<String> markedKeys = missingRows.stream().map(HealthMemberMissing::getFieldKey).collect(Collectors.toSet());
        MemberCompletenessVo vo = new MemberCompletenessVo();
        vo.setRequiredKey(CFG_REQUIRED);
        vo.setTotal(required.size());
        List<String> unmarked = new ArrayList<>();
        int filled = 0;
        for (String key : required) {
            String camel = toCamel(key);
            Object value = sets.get("member").contains(camel) ? memberProps.get(camel) : profileProps.get(camel);
            boolean hasValue = isFilled(value);
            boolean marked = markedKeys.contains(key);
            if (hasValue || marked) {
                filled++;
            }
            if (!hasValue && !marked) {
                unmarked.add(key);
            }
        }
        vo.setFilled(filled);
        vo.setText(filled + "/" + required.size());
        vo.setUnmarkedFields(unmarked);
        List<MemberCompletenessVo.MissingItem> items = new ArrayList<>(missingRows.size());
        for (HealthMemberMissing row : missingRows) {
            MemberCompletenessVo.MissingItem item = new MemberCompletenessVo.MissingItem();
            item.setFieldKey(row.getFieldKey());
            item.setReason(row.getReason());
            item.setMarkedBy(row.getMarkedBy());
            items.add(item);
        }
        vo.setMissingFields(items);
        return vo;
    }

    private boolean isFilled(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String s) {
            return StrUtil.isNotBlank(s);
        }
        return true;
    }

}
