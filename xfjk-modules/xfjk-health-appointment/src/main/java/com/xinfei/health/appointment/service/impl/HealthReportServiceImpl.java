package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.excel.utils.ExcelUtil;
import com.xinfei.common.json.utils.JsonUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.satoken.utils.LoginHelper;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.Checkitem;
import com.xinfei.health.appointment.domain.HealthCheckitemExt;
import com.xinfei.health.appointment.domain.HealthConclusionDict;
import com.xinfei.health.appointment.domain.HealthCriticalLog;
import com.xinfei.health.appointment.domain.HealthDeptSummary;
import com.xinfei.health.appointment.domain.HealthMemberArchiveNo;
import com.xinfei.health.appointment.domain.HealthMemberProfile;
import com.xinfei.health.appointment.domain.HealthOrderExt;
import com.xinfei.health.appointment.domain.HealthReport;
import com.xinfei.health.appointment.domain.HealthReportArchive;
import com.xinfei.health.appointment.domain.HealthReportConclusion;
import com.xinfei.health.appointment.domain.HealthReportItem;
import com.xinfei.health.appointment.domain.Member;
import com.xinfei.health.appointment.domain.bo.ArchiveBo;
import com.xinfei.health.appointment.domain.bo.DeptSummaryBo;
import com.xinfei.health.appointment.domain.bo.ReportConclusionBo;
import com.xinfei.health.appointment.domain.bo.ReportItemBo;
import com.xinfei.health.appointment.domain.bo.ReportQueryBo;
import com.xinfei.health.appointment.domain.bo.ReportResultRowBo;
import com.xinfei.health.appointment.domain.bo.ReportRevokeBo;
import com.xinfei.health.appointment.domain.bo.ReportSaveBo;
import com.xinfei.health.appointment.domain.bo.ReportSummaryBo;
import com.xinfei.health.appointment.domain.bo.SendBackBo;
import com.xinfei.health.appointment.domain.vo.ReportCompareVo;
import com.xinfei.health.appointment.domain.vo.ReportDetailVo;
import com.xinfei.health.appointment.domain.vo.ReportItemResultVo;
import com.xinfei.health.appointment.domain.vo.ReportParseVo;
import com.xinfei.health.appointment.domain.vo.ReportVo;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthCheckitemExtMapper;
import com.xinfei.health.appointment.mapper.HealthConclusionDictMapper;
import com.xinfei.health.appointment.mapper.HealthCriticalLogMapper;
import com.xinfei.health.appointment.mapper.HealthDeptSummaryMapper;
import com.xinfei.health.appointment.mapper.HealthMemberArchiveNoMapper;
import com.xinfei.health.appointment.mapper.HealthMemberProfileMapper;
import com.xinfei.health.appointment.mapper.HealthOrderExtMapper;
import com.xinfei.health.appointment.mapper.HealthReportArchiveMapper;
import com.xinfei.health.appointment.mapper.HealthReportConclusionMapper;
import com.xinfei.health.appointment.mapper.HealthReportItemMapper;
import com.xinfei.health.appointment.mapper.HealthReportMapper;
import com.xinfei.health.appointment.mapper.MemberMapper;
import com.xinfei.health.appointment.service.IHealthReportService;
import com.xinfei.system.service.ISysConfigService;
import com.xinfei.system.service.ISysOssService;
import com.xinfei.system.domain.vo.SysOssVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 体检报告服务实现（07 §3.2 / §4 / §5.3~5.6，B7）
 *
 * <p>判定列只由 {@link ReportJudgeEngine} 产出；结论条目末次语义按 18 F7；
 * 质控链三环节与撤销/打回留痕按 18 C8/C9。
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HealthReportServiceImpl implements IHealthReportService {

    private static final String CFG_REPORT_PREFIX = "health:number:report_prefix";
    private static final String CFG_WATERMARK = "health:report:watermark_flag";

    private static final int REPORT_SEQ_LEN = 5;
    private static final int REPORT_RETRY = 3;

    /** 总检允许的明细状态（07 §6.3：未检/待查项阻止提交） */
    private static final Set<String> SUMMARY_READY_ITEM_STATUS = Set.of("已检", "弃检", "延期");

    private final HealthReportMapper reportMapper;
    private final HealthReportItemMapper itemMapper;
    private final HealthDeptSummaryMapper deptSummaryMapper;
    private final HealthReportConclusionMapper conclusionMapper;
    private final HealthReportArchiveMapper archiveMapper;
    private final HealthCriticalLogMapper criticalMapper;
    private final HealthConclusionDictMapper conclusionDictMapper;
    private final MemberMapper memberMapper;
    private final HealthMemberProfileMapper profileMapper;
    private final HealthMemberArchiveNoMapper archiveNoMapper;
    private final HealthOrderExtMapper orderExtMapper;
    private final CheckitemMapper checkitemMapper;
    private final HealthCheckitemExtMapper checkitemExtMapper;
    private final ReportJudgeEngine judgeEngine;
    private final ISysConfigService configService;
    private final ISysOssService ossService;

    // ------------------------------------------------------------------ 查询

    @Override
    public TableDataInfo<ReportVo> queryPageList(ReportQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthReport> lqw = Wrappers.<HealthReport>lambdaQuery()
            .eq(StrUtil.isNotBlank(bo.getArchiveNo()), HealthReport::getArchiveNo, bo.getArchiveNo())
            .eq(bo.getMemberId() != null, HealthReport::getMemberId, bo.getMemberId())
            .eq(bo.getBatchId() != null, HealthReport::getBatchId, bo.getBatchId())
            .eq(StrUtil.isNotBlank(bo.getExamNo()), HealthReport::getExamNo, bo.getExamNo())
            .eq(StrUtil.isNotBlank(bo.getStatus()), HealthReport::getStatus, bo.getStatus())
            .eq(StrUtil.isNotBlank(bo.getSource()), HealthReport::getSource, bo.getSource())
            .ge(bo.getBeginDate() != null, HealthReport::getCheckDate, bo.getBeginDate())
            .le(bo.getEndDate() != null, HealthReport::getCheckDate, bo.getEndDate())
            .orderByDesc(HealthReport::getCheckDate)
            .orderByDesc(HealthReport::getId);
        Page<HealthReport> page = reportMapper.selectPage(pageQuery.build(), lqw);
        List<ReportVo> rows = assembleReportVo(page.getRecords());
        Page<ReportVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(rows);
        return TableDataInfo.build(voPage);
    }

    private List<ReportVo> assembleReportVo(List<HealthReport> reports) {
        if (reports.isEmpty()) {
            return List.of();
        }
        Map<Integer, Member> memberMap = memberMapper.selectBatchIds(
                reports.stream().map(HealthReport::getMemberId).distinct().toList())
            .stream().collect(Collectors.toMap(Member::getId, Function.identity()));
        Map<Long, long[]> counts = new HashMap<>();
        List<Long> ids = reports.stream().map(HealthReport::getId).toList();
        itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
                .in(HealthReportItem::getReportId, ids)
                .select(HealthReportItem::getReportId, HealthReportItem::getAbnormalFlag,
                    HealthReportItem::getCriticalFlag))
            .forEach(item -> {
                long[] c = counts.computeIfAbsent(item.getReportId(), k -> new long[2]);
                if (!HealthReportItem.FLAG_NORMAL.equals(item.getAbnormalFlag())) {
                    c[0]++;
                }
                if (HealthReportItem.YES.equals(item.getCriticalFlag())) {
                    c[1]++;
                }
            });
        return reports.stream().map(r -> {
            ReportVo vo = new ReportVo();
            vo.setId(r.getId());
            vo.setReportNo(r.getReportNo());
            vo.setMemberId(r.getMemberId());
            Member member = memberMap.get(r.getMemberId());
            vo.setMemberName(member == null ? null : member.getName());
            vo.setMemberSex(member == null ? null : member.getSex());
            vo.setArchiveNo(r.getArchiveNo());
            vo.setOrderId(r.getOrderId());
            vo.setExamNo(r.getExamNo());
            vo.setBatchId(r.getBatchId());
            vo.setCheckDate(r.getCheckDate());
            vo.setDoctor(r.getDoctor());
            vo.setSource(r.getSource());
            vo.setStatus(r.getStatus());
            long[] c = counts.getOrDefault(r.getId(), new long[2]);
            vo.setAbnormalCount((int) c[0]);
            vo.setCriticalCount((int) c[1]);
            vo.setConclusionType(r.getConclusionType());
            vo.setConclusionFinal(r.getConclusionFinal());
            vo.setFileUrl(r.getFileUrl());
            vo.setRevokeFlag(r.getRevokeFlag());
            vo.setSummaryBy(r.getSummaryBy());
            vo.setSummaryTime(r.getSummaryTime());
            vo.setFinalCheckBy(r.getFinalCheckBy());
            vo.setFinalCheckTime(r.getFinalCheckTime());
            vo.setSourceReportId(r.getSourceReportId());
            vo.setRemark(r.getRemark());
            vo.setCreateTime(r.getCreateTime() == null ? null
                : new java.sql.Timestamp(r.getCreateTime().getTime()).toLocalDateTime());
            return vo;
        }).toList();
    }

    @Override
    public ReportDetailVo queryDetail(Long id) {
        HealthReport report = requireReport(id);
        ReportDetailVo vo = new ReportDetailVo();
        vo.setReport(assembleReportVo(List.of(report)).get(0));
        vo.setDeptSummaryText(report.getDeptSummaryText());
        vo.setAbnormalSummary(report.getAbnormalSummary());
        vo.setConclusion(report.getConclusion());
        vo.setBasis(report.getBasis());
        vo.setAdvice(report.getAdvice());
        vo.setSummary(report.getSummary());
        vo.setConclusionJson(report.getConclusionJson());
        vo.setFirstCheckBy(report.getFirstCheckBy());
        vo.setFirstCheckTime(report.getFirstCheckTime());
        vo.setSummaryBy(report.getSummaryBy());
        vo.setSummaryTime(report.getSummaryTime());
        vo.setRevokeReason(report.getRevokeReason());
        vo.setRevokeTime(report.getRevokeTime());
        vo.setItems(itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, id)
            .orderByAsc(HealthReportItem::getSortNo)
            .orderByAsc(HealthReportItem::getId)));
        vo.setDeptSummaries(deptSummaryMapper.selectList(Wrappers.<HealthDeptSummary>lambdaQuery()
            .eq(HealthDeptSummary::getReportId, id)
            .orderByAsc(HealthDeptSummary::getDeptId)));
        vo.setConclusions(conclusionMapper.selectList(Wrappers.<HealthReportConclusion>lambdaQuery()
            .eq(HealthReportConclusion::getReportId, id)
            .eq(HealthReportConclusion::getFinalFlag, HealthReportItem.YES)
            .orderByAsc(HealthReportConclusion::getSortNo)));
        vo.setCriticals(criticalMapper.selectList(Wrappers.<HealthCriticalLog>lambdaQuery()
            .eq(HealthCriticalLog::getReportId, id)
            .orderByAsc(HealthCriticalLog::getReportTime)));
        vo.setArchives(archiveMapper.selectList(Wrappers.<HealthReportArchive>lambdaQuery()
            .eq(HealthReportArchive::getReportId, id)
            .orderByAsc(HealthReportArchive::getUploadTime)));
        if (report.getSourceReportId() != null) {
            HealthReport prev = reportMapper.selectById(report.getSourceReportId());
            if (prev != null) {
                vo.setPrevReport(assembleReportVo(List.of(prev)).get(0));
            }
        }
        return vo;
    }

    @Override
    public ReportVo queryPrev(String archiveNo) {
        if (StrUtil.isBlank(archiveNo)) {
            return null;
        }
        List<HealthReport> list = reportMapper.selectList(Wrappers.<HealthReport>lambdaQuery()
            .eq(HealthReport::getArchiveNo, archiveNo)
            .orderByDesc(HealthReport::getCheckDate)
            .orderByDesc(HealthReport::getId));
        return list.isEmpty() ? null : assembleReportVo(List.of(list.get(0))).get(0);
    }

    @Override
    public List<ReportCompareVo> queryCompare(Integer memberId, Long checkitemId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在：" + memberId, HealthErrorCode.MEMBER_NOT_FOUND);
        }
        String archiveNo = resolveArchiveNo(memberId);
        if (StrUtil.isBlank(archiveNo)) {
            return List.of();
        }
        List<HealthReport> reports = reportMapper.selectList(Wrappers.<HealthReport>lambdaQuery()
            .eq(HealthReport::getArchiveNo, archiveNo)
            .orderByAsc(HealthReport::getCheckDate)
            .orderByAsc(HealthReport::getId));
        if (reports.isEmpty()) {
            return List.of();
        }
        Map<Long, HealthReport> reportMap = reports.stream()
            .collect(Collectors.toMap(HealthReport::getId, Function.identity()));
        List<ReportCompareVo> rows = new ArrayList<>();
        itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
                .in(HealthReportItem::getReportId, reportMap.keySet())
                .eq(checkitemId != null, HealthReportItem::getCheckitemId, checkitemId)
                .eq(HealthReportItem::getItemStatus, HealthReportItem.ITEM_CHECKED)
                .orderByAsc(HealthReportItem::getCheckTime))
            .forEach(item -> {
                HealthReport report = reportMap.get(item.getReportId());
                if (report == null) {
                    return;
                }
                ReportCompareVo vo = new ReportCompareVo();
                vo.setReportId(report.getId());
                vo.setReportNo(report.getReportNo());
                vo.setCheckDate(report.getCheckDate());
                vo.setCheckitemId(item.getCheckitemId());
                vo.setItemName(item.getItemName());
                vo.setResult(item.getResult());
                vo.setUnit(item.getUnit());
                vo.setRefRange(item.getRefRange());
                vo.setAbnormalFlag(item.getAbnormalFlag());
                rows.add(vo);
            });
        rows.sort(Comparator.comparing(ReportCompareVo::getCheckDate,
            Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
    }

    @Override
    public List<HealthConclusionDict> queryConclusionOptions(String keyword) {
        return conclusionDictMapper.selectList(Wrappers.<HealthConclusionDict>lambdaQuery()
            .eq(HealthConclusionDict::getStatus, HealthConclusionDict.STATUS_PUBLISHED)
            .and(StrUtil.isNotBlank(keyword), w -> w
                .like(HealthConclusionDict::getTerm, keyword)
                .or().like(HealthConclusionDict::getSynonyms, keyword)
                .or().like(HealthConclusionDict::getCode, keyword))
            .orderByAsc(HealthConclusionDict::getSort));
    }

    // ------------------------------------------------------------------ 建档

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVo insertReport(ReportSaveBo bo) {
        Member member = memberMapper.selectById(bo.getMemberId());
        if (member == null) {
            throw new ServiceException("会员不存在：" + bo.getMemberId(), HealthErrorCode.MEMBER_NOT_FOUND);
        }
        if (!HealthReport.SOURCES.contains(bo.getSource())) {
            throw new ServiceException("报告来源取值非法：" + bo.getSource(), 400);
        }
        String archiveNo = resolveArchiveNo(bo.getMemberId());
        if (StrUtil.isBlank(archiveNo)) {
            throw new ServiceException("会员无档案号，请先完成建档（07 §5.1）", HealthErrorCode.MEMBER_NOT_FOUND);
        }

        HealthReport report = new HealthReport();
        report.setReportNo(generateReportNo());
        report.setMemberId(bo.getMemberId());
        report.setArchiveNo(archiveNo);
        report.setOrderId(bo.getOrderId());
        report.setExamNo(bo.getExamNo());
        report.setBatchId(bo.getBatchId());
        report.setCheckDate(bo.getCheckDate());
        report.setSource(bo.getSource());
        report.setDoctor(bo.getDoctor());
        report.setRemark(bo.getRemark());
        report.setRevokeFlag(HealthReportItem.NO);
        report.setWatermarkFlag(HealthReportItem.NO);
        report.setStatus(resolveInitialStatus(bo.getOrderId()));
        // 按档案号自动关联上一次报告（18 D11/F5）
        List<HealthReport> prev = reportMapper.selectList(Wrappers.<HealthReport>lambdaQuery()
            .eq(HealthReport::getArchiveNo, archiveNo)
            .orderByDesc(HealthReport::getCheckDate)
            .orderByDesc(HealthReport::getId));
        if (!prev.isEmpty()) {
            report.setSourceReportId(prev.get(0).getId());
        }
        reportMapper.insert(report);

        if (bo.getOrderId() != null) {
            HealthOrderExt ext = findOrderExt(bo.getOrderId());
            if (ext != null) {
                ext.setReportId(report.getId());
                orderExtMapper.updateById(ext);
            }
        }
        return assembleReportVo(List.of(report)).get(0);
    }

    private String resolveInitialStatus(Integer orderId) {
        if (orderId == null) {
            return HealthReport.STATUS_WAIT_CREATE;
        }
        HealthOrderExt ext = findOrderExt(orderId);
        return ext == null ? HealthReport.STATUS_WAIT_CREATE : mapFlowToStatus(ext.getFlowStatus());
    }

    private String resolveArchiveNo(Integer memberId) {
        HealthMemberArchiveNo archive = archiveNoMapper.selectOne(Wrappers.<HealthMemberArchiveNo>lambdaQuery()
            .eq(HealthMemberArchiveNo::getMemberId, memberId));
        if (archive != null && StrUtil.isNotBlank(archive.getFileNumber())) {
            return archive.getFileNumber();
        }
        Member member = memberMapper.selectById(memberId);
        return member == null ? null : member.getFileNumber();
    }

    /**
     * 报告号生成（07 §2.6）：前缀 + yyyyMMdd + 5 位序号，撞唯一索引重试 3 次
     */
    private String generateReportNo() {
        String prefix = configService.selectConfigByKey(CFG_REPORT_PREFIX)
            + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int seq = nextReportSeq(prefix);
        for (int attempt = 0; attempt < REPORT_RETRY; attempt++) {
            String no = prefix + StrUtil.fillBefore(String.valueOf(seq), '0', REPORT_SEQ_LEN);
            Long exists = reportMapper.selectCount(Wrappers.<HealthReport>lambdaQuery()
                .eq(HealthReport::getReportNo, no));
            if (exists == null || exists == 0) {
                return no;
            }
            seq++;
        }
        throw new ServiceException("报告号生成冲突，请重试", HealthErrorCode.ARCHIVE_NO_CONFLICT);
    }

    private int nextReportSeq(String prefix) {
        String max = reportMapper.selectList(Wrappers.<HealthReport>lambdaQuery()
                .likeRight(HealthReport::getReportNo, prefix)
                .select(HealthReport::getReportNo))
            .stream().map(HealthReport::getReportNo)
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

    @Override
    public ReportVo createRegisterDraft(ReportSaveBo bo) {
        if (StrUtil.isBlank(resolveArchiveNo(bo.getMemberId()))) {
            return null;
        }
        return insertReport(bo);
    }

    @Override
    public int restoreDelayedItems(Long reportId) {
        List<HealthReportItem> items = itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, reportId)
            .eq(HealthReportItem::getItemStatus, HealthReportItem.ITEM_DELAYED));
        if (items.isEmpty()) {
            return 0;
        }
        itemMapper.update(null, Wrappers.<HealthReportItem>lambdaUpdate()
            .set(HealthReportItem::getItemStatus, HealthReportItem.ITEM_NOT_CHECKED)
            .eq(HealthReportItem::getReportId, reportId)
            .eq(HealthReportItem::getItemStatus, HealthReportItem.ITEM_DELAYED));
        return items.size();
    }

    @Override
    public List<HealthReportItem> resetReportItems(Long reportId, List<Long> itemIds) {
        HealthReport report = requireReport(reportId);
        requireEditable(report);
        boolean partial = itemIds != null && !itemIds.isEmpty();
        LambdaQueryWrapper<HealthReportItem> qw = Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, reportId);
        if (partial) {
            qw.in(HealthReportItem::getId, itemIds);
        }
        List<HealthReportItem> items = itemMapper.selectList(qw);
        if (items.isEmpty()) {
            return List.of();
        }
        // NOT NULL 列回默认：result 空串、abnormal 正常、标记位否（08 §5.7 清空回未检）
        LambdaUpdateWrapper<HealthReportItem> uw = Wrappers.<HealthReportItem>lambdaUpdate()
            .set(HealthReportItem::getItemStatus, HealthReportItem.ITEM_NOT_CHECKED)
            .set(HealthReportItem::getResult, "")
            .set(HealthReportItem::getAbnormalFlag, HealthReportItem.FLAG_NORMAL)
            .set(HealthReportItem::getQualitative, null)
            .set(HealthReportItem::getAbnormalGrade, null)
            .set(HealthReportItem::getCriticalFlag, HealthReportItem.NO)
            .set(HealthReportItem::getEdgeFlag, HealthReportItem.NO)
            .set(HealthReportItem::getMajorPositive, HealthReportItem.NO)
            .set(HealthReportItem::getCheckTime, null)
            .set(HealthReportItem::getCheckDevice, null)
            .set(HealthReportItem::getDiscardReason, null)
            .eq(HealthReportItem::getReportId, reportId);
        if (partial) {
            uw.in(HealthReportItem::getId, itemIds);
        }
        itemMapper.update(null, uw);
        return items;
    }

    // ------------------------------------------------------------------ 上传与解析

    @Override
    public Long uploadFile(MultipartFile file, Long reportId) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件为空", 400);
        }
        SysOssVo oss = ossService.upload(file);
        if (reportId != null) {
            HealthReport report = requireReport(reportId);
            report.setFileUrl(String.valueOf(oss.getOssId()));
            reportMapper.updateById(report);
        }
        return oss.getOssId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportParseVo parseResult(Long reportId, MultipartFile file) {
        HealthReport report = requireReport(reportId);
        requireEditable(report);
        List<ReportResultRowBo> rows = readResultFile(file);
        ReportParseVo result = new ReportParseVo();
        result.setTotal(rows.size());
        int rowNo = 1;
        for (ReportResultRowBo row : rows) {
            rowNo++;
            if (row == null || StrUtil.isBlank(row.getItemName())) {
                result.getErrors().add(new ReportParseVo.RowError(rowNo, "检查项为空"));
                continue;
            }
            boolean unmatched = false;
            ReportItemBo bo = new ReportItemBo();
            bo.setItemName(row.getItemName().trim());
            bo.setResult(row.getResult() == null ? "" : row.getResult().trim());
            bo.setUnit(row.getUnit());
            Checkitem checkitem = checkitemMapper.selectOne(Wrappers.<Checkitem>lambdaQuery()
                .eq(Checkitem::getName, bo.getItemName())
                .last("LIMIT 1"));
            if (checkitem == null) {
                unmatched = true;
                bo.setResultType(isNumeric(bo.getResult())
                    ? HealthReportItem.TYPE_NUMERIC : HealthReportItem.TYPE_TEXT);
                bo.setItemStatus(HealthReportItem.ITEM_CHECKED);
                bo.setDeptId(0L);
            } else {
                HealthCheckitemExt ext = checkitemExtMapper.selectOne(Wrappers.<HealthCheckitemExt>lambdaQuery()
                    .eq(HealthCheckitemExt::getCheckitemId, checkitem.getId())
                    .last("LIMIT 1"));
                bo.setCheckitemId(checkitem.getId());
                bo.setResultType(ext == null ? HealthReportItem.TYPE_TEXT : ext.getResultType());
                bo.setUnit(StrUtil.blankToDefault(bo.getUnit(), ext == null ? null : ext.getUnit()));
                bo.setDeptId(ext == null ? 0L : ext.getDeptId());
                bo.setItemStatus(HealthReportItem.ITEM_CHECKED);
                if (HealthReportItem.TYPE_QUALITATIVE.equals(bo.getResultType())) {
                    bo.setQualitative(mapQualitative(bo.getResult()));
                }
            }
            try {
                doSaveItem(report, bo, true);
                result.setSuccess(result.getSuccess() + 1);
                if (unmatched) {
                    result.setUnmatched(result.getUnmatched() + 1);
                }
            } catch (ServiceException e) {
                // 逐行容错：跳过异常行，不整体回滚已成功行（07 §4）
                result.getErrors().add(new ReportParseVo.RowError(rowNo, e.getMessage()));
            }
        }
        refreshReportStatus(report);
        return result;
    }

    private List<ReportResultRowBo> readResultFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("结果文件为空", 400);
        }
        String name = StrUtil.blankToDefault(file.getOriginalFilename(), "").toLowerCase();
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".csv") || name.endsWith(".txt")) {
                return readCsv(in);
            }
            return ExcelUtil.importExcel(in, ReportResultRowBo.class);
        } catch (IOException e) {
            throw new ServiceException("结果文件读取失败：" + e.getMessage(), 400);
        }
    }

    private List<ReportResultRowBo> readCsv(InputStream in) throws IOException {
        List<ReportResultRowBo> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (StrUtil.isBlank(line)) {
                    continue;
                }
                if (header) {
                    header = false;
                    continue;
                }
                String[] cols = line.split(",", -1);
                ReportResultRowBo row = new ReportResultRowBo();
                row.setItemName(cols.length > 0 ? cols[0].trim() : null);
                row.setResult(cols.length > 1 ? cols[1].trim() : null);
                row.setUnit(cols.length > 2 ? cols[2].trim() : null);
                row.setRefRange(cols.length > 3 ? cols[3].trim() : null);
                rows.add(row);
            }
        }
        return rows;
    }

    private String mapQualitative(String result) {
        if (StrUtil.isBlank(result)) {
            return null;
        }
        String text = result.trim();
        if (text.contains("弱阳") || "±".equals(text)) {
            return "3";
        }
        if (text.contains("阳性") || text.contains("+") || "阳".equals(text)) {
            return "1";
        }
        if (text.contains("阴性") || text.contains("-") || "阴".equals(text)) {
            return "2";
        }
        return null;
    }

    private boolean isNumeric(String value) {
        if (StrUtil.isBlank(value)) {
            return false;
        }
        return value.trim().matches("^-?\\d+(\\.\\d+)?\\s*\\S*$");
    }

    // ------------------------------------------------------------------ 明细录入

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportItemResultVo saveItem(Long reportId, ReportItemBo bo) {
        HealthReport report = requireReport(reportId);
        requireEditable(report);
        ItemSaveResult saved = doSaveItem(report, bo, false);
        refreshReportStatus(report);
        HealthReportItem item = saved.item();
        ReportItemResultVo vo = new ReportItemResultVo();
        vo.setId(item.getId());
        vo.setAbnormalFlag(item.getAbnormalFlag());
        vo.setRefRange(item.getRefRange());
        vo.setRefConfigured(item.getRefConfigured());
        vo.setCriticalFlag(item.getCriticalFlag());
        vo.setAbnormalGrade(item.getAbnormalGrade());
        vo.setMajorPositive(item.getMajorPositive());
        vo.setEdgeFlag(item.getEdgeFlag());
        vo.setTip(saved.tip());
        return vo;
    }

    private ItemSaveResult doSaveItem(HealthReport report, ReportItemBo bo, boolean fromParse) {
        if (HealthReportItem.ITEM_DISCARDED.equals(bo.getItemStatus()) && StrUtil.isBlank(bo.getDiscardReason())) {
            throw new ServiceException("弃检须填写原因（18 C2）", HealthErrorCode.DISCARD_REASON_REQUIRED);
        }
        Member member = memberMapper.selectById(report.getMemberId());
        if (member == null) {
            throw new ServiceException("会员不存在：" + report.getMemberId(), HealthErrorCode.MEMBER_NOT_FOUND);
        }
        HealthMemberProfile profile = profileMapper.selectOne(Wrappers.<HealthMemberProfile>lambdaQuery()
            .eq(HealthMemberProfile::getMemberId, report.getMemberId()).last("LIMIT 1"));
        Integer age = member.getBirthday() == null || report.getCheckDate() == null ? null
            : Period.between(member.getBirthday(), report.getCheckDate()).getYears();
        String physio = profile == null ? null : profile.getPregnancyStatus();

        ReportJudgeEngine.Outcome outcome = judgeEngine.judge(bo.getResultType(), bo.getResult(),
            bo.getQualitative(), bo.getAbnormalFlag(), bo.getEdgeFlag(), bo.getCheckitemId(),
            bo.getItemName(), member.getSex(), age, physio);

        HealthReportItem item = findExistingItem(report.getId(), bo);
        boolean create = item == null;
        if (create) {
            item = new HealthReportItem();
            item.setReportId(report.getId());
        }
        item.setCheckitemId(bo.getCheckitemId());
        item.setDeptId(bo.getDeptId());
        item.setItemName(bo.getItemName());
        item.setResultType(bo.getResultType());
        item.setResult(bo.getResult());
        item.setUnit(bo.getUnit());
        item.setRefRange(StrUtil.blankToDefault(outcome.getRefRange(), bo.getUnit() == null ? null : item.getRefRange()));
        item.setRefMin(outcome.getRefMin());
        item.setRefMax(outcome.getRefMax());
        item.setRefConfigured(outcome.getRefConfigured());
        item.setAbnormalFlag(outcome.getAbnormalFlag());
        item.setQualitative(outcome.getQualitative());
        item.setAbnormalGrade(outcome.getAbnormalGrade());
        item.setCriticalFlag(outcome.getCriticalFlag());
        item.setEdgeFlag(HealthReportItem.YES.equals(bo.getEdgeFlag()) ? HealthReportItem.YES : HealthReportItem.NO);
        item.setMajorPositive(HealthReportItem.YES.equals(bo.getMajorPositive())
            ? HealthReportItem.YES : HealthReportItem.NO);
        item.setItemStatus(resolveItemStatus(bo, item));
        item.setDiscardReason(bo.getDiscardReason());
        item.setMergeGroupId(bo.getMergeGroupId());
        item.setSortNo(bo.getSortNo() == null ? 0 : bo.getSortNo());
        item.setDoctor(StrUtil.blankToDefault(bo.getDoctor(), safeUsername()));
        item.setCheckTime(LocalDateTime.now());
        item.setCheckDevice(bo.getCheckDevice());
        item.setRemark(bo.getRemark());
        applyDualDept(item, bo);
        applyPrevSnapshot(report, item);
        if (create) {
            itemMapper.insert(item);
        } else {
            itemMapper.updateById(item);
        }
        if (fromParse) {
            item.setRemark(StrUtil.isBlank(item.getRemark()) ? "解析导入" : item.getRemark());
        }
        return new ItemSaveResult(item, outcome.getTip());
    }

    private record ItemSaveResult(HealthReportItem item, String tip) {
    }

    private HealthReportItem findExistingItem(Long reportId, ReportItemBo bo) {
        LambdaQueryWrapper<HealthReportItem> lqw = Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, reportId)
            .last("LIMIT 1");
        if (bo.getCheckitemId() != null) {
            lqw.eq(HealthReportItem::getCheckitemId, bo.getCheckitemId());
        } else {
            lqw.eq(HealthReportItem::getItemName, bo.getItemName());
        }
        return itemMapper.selectOne(lqw);
    }

    /**
     * 18 C3：双科室分检项目对侧未完成时，本侧不得置为已检
     */
    private String resolveItemStatus(ReportItemBo bo, HealthReportItem item) {
        if (HealthReportItem.ITEM_CHECKED.equals(bo.getItemStatus())
            && HealthReportItem.YES.equals(item.getDualDeptFlag())
            && !HealthReportItem.ITEM_CHECKED.equals(item.getPeerDeptStatus())) {
            return HealthReportItem.ITEM_PENDING;
        }
        return bo.getItemStatus();
    }

    private void applyDualDept(HealthReportItem item, ReportItemBo bo) {
        if (bo.getCheckitemId() == null || item.getDualDeptFlag() != null) {
            return;
        }
        HealthCheckitemExt ext = checkitemExtMapper.selectOne(Wrappers.<HealthCheckitemExt>lambdaQuery()
            .eq(HealthCheckitemExt::getCheckitemId, bo.getCheckitemId()).last("LIMIT 1"));
        if (ext != null) {
            item.setDualDeptFlag(StrUtil.blankToDefault(ext.getDualDeptFlag(), HealthReportItem.NO));
        } else if (item.getDualDeptFlag() == null) {
            item.setDualDeptFlag(HealthReportItem.NO);
        }
    }

    /**
     * 上次结果快照（18 D11/F5）：同档案上一报告的同项结果
     */
    private void applyPrevSnapshot(HealthReport report, HealthReportItem item) {
        if (report.getSourceReportId() == null || item.getCheckitemId() == null) {
            return;
        }
        HealthReportItem prev = itemMapper.selectOne(Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, report.getSourceReportId())
            .eq(HealthReportItem::getCheckitemId, item.getCheckitemId())
            .last("LIMIT 1"));
        if (prev != null) {
            item.setPrevResult(prev.getResult());
            item.setPrevAbnormalFlag(prev.getAbnormalFlag());
            item.setPrevRefRange(prev.getRefRange());
        }
    }

    private void refreshReportStatus(HealthReport report) {
        List<HealthReportItem> items = itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, report.getId()));
        if (items.isEmpty() || !HealthReport.EDITABLE_STATUS.contains(report.getStatus())) {
            return;
        }
        boolean allDone = items.stream().allMatch(i -> SUMMARY_READY_ITEM_STATUS.contains(i.getItemStatus()));
        String target = allDone ? HealthReport.STATUS_PENDING_FINAL : HealthReport.STATUS_IN_EXAM;
        if (!target.equals(report.getStatus())) {
            report.setStatus(target);
            reportMapper.updateById(report);
        }
    }

    // ------------------------------------------------------------------ 科室小结

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDeptSummary(Long reportId, DeptSummaryBo bo) {
        HealthReport report = requireReport(reportId);
        requireEditable(report);
        boolean submit = Boolean.TRUE.equals(bo.getSubmit());
        if (submit && StrUtil.isBlank(bo.getSummaryText())) {
            throw new ServiceException("科室小结提交时正文不能为空", HealthErrorCode.DEPT_SUMMARY_REQUIRED);
        }
        HealthDeptSummary summary = deptSummaryMapper.selectOne(Wrappers.<HealthDeptSummary>lambdaQuery()
            .eq(HealthDeptSummary::getReportId, reportId)
            .eq(HealthDeptSummary::getDeptId, bo.getDeptId()));
        if (summary == null) {
            summary = new HealthDeptSummary();
            summary.setReportId(reportId);
            summary.setDeptId(bo.getDeptId());
        } else if (HealthDeptSummary.YES.equals(summary.getSubmitFlag())
            && !HealthDeptSummary.YES.equals(summary.getSendBackFlag())
            && !HealthReportItem.YES.equals(report.getRevokeFlag())) {
            // 18 C9：已提交小结在总检撤销/打回前不可改
            throw new ServiceException("该科室小结已提交，总检撤销或打回后方可修改", HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
        summary.setSummaryText(bo.getSummaryText());
        summary.setPrintLabel(bo.getPrintLabel());
        summary.setLastModifyBy(safeUsername());
        summary.setLastModifyTime(LocalDateTime.now());
        summary.setSubmitFlag(submit ? HealthDeptSummary.YES : HealthDeptSummary.NO);
        if (submit) {
            summary.setSendBackFlag(HealthDeptSummary.NO);
            summary.setSendBackReason(null);
        }
        if (summary.getId() == null) {
            deptSummaryMapper.insert(summary);
        } else {
            deptSummaryMapper.updateById(summary);
        }
        if (submit && report.getFirstCheckBy() == null) {
            report.setFirstCheckBy(safeUsername());
            report.setFirstCheckTime(LocalDateTime.now());
            reportMapper.updateById(report);
        }
    }

    // ------------------------------------------------------------------ 总检

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitSummary(Long reportId, ReportSummaryBo bo) {
        HealthReport report = requireReport(reportId);
        if (!HealthReport.STATUS_PENDING_FINAL.equals(report.getStatus())
            && !(HealthReport.STATUS_IN_EXAM.equals(report.getStatus()) && HealthReportItem.YES.equals(report.getRevokeFlag()))) {
            throw new ServiceException("当前报告状态不允许总检提交：" + report.getStatus(), HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
        List<HealthReportItem> items = itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
            .eq(HealthReportItem::getReportId, reportId));
        if (items.isEmpty()) {
            throw new ServiceException("报告无明细，不满足总检提交条件", HealthErrorCode.SUMMARY_PRECONDITION);
        }
        List<String> blocking = items.stream()
            .filter(i -> !SUMMARY_READY_ITEM_STATUS.contains(i.getItemStatus()))
            .map(HealthReportItem::getItemName)
            .toList();
        if (!blocking.isEmpty()) {
            throw new ServiceException("存在未检/待查项目，不满足总检提交条件：" + String.join("、", blocking),
                HealthErrorCode.SUMMARY_PRECONDITION);
        }

        String conclusionText = saveConclusions(reportId, bo.getConclusions());
        String deptSummaryText = StrUtil.isNotBlank(bo.getDeptSummaryText())
            ? bo.getDeptSummaryText() : composeDeptSummary(reportId);
        String abnormalSummary = StrUtil.isNotBlank(bo.getAbnormalSummary())
            ? bo.getAbnormalSummary() : composeAbnormalSummary(items);

        report.setDeptSummaryText(deptSummaryText);
        report.setAbnormalSummary(abnormalSummary);
        report.setConclusion(conclusionText);
        report.setConclusionFinal(conclusionText);
        report.setConclusionType(bo.getConclusionType());
        report.setBasis(bo.getBasis());
        report.setAdvice(bo.getAdvice());
        report.setDoctor(StrUtil.blankToDefault(bo.getDoctor(), report.getDoctor()));
        report.setSummary(composePrintSnapshot(deptSummaryText, abnormalSummary, conclusionText,
            bo.getBasis(), bo.getAdvice()));
        report.setStatus(HealthReport.STATUS_FINALIZED);
        report.setRevokeFlag(HealthReportItem.NO);
        report.setSummaryBy(safeUsername());
        report.setSummaryTime(LocalDateTime.now());
        reportMapper.updateById(report);
        syncOrderFlow(report, HealthOrderExt.FLOW_FINALIZED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveConclusions(Long reportId, List<ReportConclusionBo> list) {
        HealthReport report = requireReport(reportId);
        if (list == null || list.isEmpty()) {
            throw new ServiceException("总检结论条目不能为空（07 §5.4）", HealthErrorCode.CONCLUSION_DICT_INVALID);
        }
        List<Long> dictIds = list.stream().map(ReportConclusionBo::getConclusionDictId).filter(Objects::nonNull).distinct().toList();
        Map<Long, HealthConclusionDict> dictMap = dictIds.isEmpty() ? Map.of()
            : conclusionDictMapper.selectBatchIds(dictIds).stream()
                .collect(Collectors.toMap(HealthConclusionDict::getId, Function.identity()));
        for (Long dictId : dictIds) {
            HealthConclusionDict dict = dictMap.get(dictId);
            if (dict == null || !HealthConclusionDict.STATUS_PUBLISHED.equals(dict.getStatus())) {
                throw new ServiceException("结论词不存在或已停用：" + dictId, HealthErrorCode.CONCLUSION_DICT_INVALID);
            }
        }
        // 末次语义：旧组 final_flag 置 0，新组置 1（18 F7）
        conclusionMapper.update(null, Wrappers.<HealthReportConclusion>lambdaUpdate()
            .eq(HealthReportConclusion::getReportId, reportId)
            .eq(HealthReportConclusion::getFinalFlag, HealthReportItem.YES)
            .set(HealthReportConclusion::getFinalFlag, HealthReportItem.NO));

        List<Map<String, Object>> json = new ArrayList<>();
        List<String> terms = new ArrayList<>();
        int sort = 1;
        for (ReportConclusionBo bo : list) {
            HealthConclusionDict dict = dictMap.get(bo.getConclusionDictId());
            HealthReportConclusion row = new HealthReportConclusion();
            row.setReportId(reportId);
            row.setConclusionDictId(bo.getConclusionDictId());
            row.setConclusionType(StrUtil.blankToDefault(bo.getConclusionType(), dict.getConclusionType()));
            row.setItemIds(joinIds(bo.getItemIds()));
            row.setAdviceIds(joinIds(bo.getAdviceIds()));
            row.setGrade(StrUtil.blankToDefault(bo.getGrade(), dict.getAbnormalGrade()));
            row.setMergeGroupId(StrUtil.blankToDefault(bo.getMergeGroupId(), dict.getMergeGroupCode()));
            row.setFinalFlag(HealthReportItem.YES);
            row.setSortNo(bo.getSortNo() == null ? sort : bo.getSortNo());
            conclusionMapper.insert(row);
            terms.add(row.getSortNo() + "、" + dict.getTerm());

            Map<String, Object> node = new LinkedHashMap<>();
            node.put("dictId", row.getConclusionDictId());
            node.put("type", row.getConclusionType());
            node.put("grade", row.getGrade());
            node.put("adviceIds", bo.getAdviceIds());
            node.put("itemIds", bo.getItemIds());
            json.add(node);
            sort++;
        }
        // 条目化内容随报告保存（07 §2.6 conclusion_json）
        report.setConclusionJson(JsonUtils.toJsonString(json));
        reportMapper.updateById(report);
        return String.join("；", terms);
    }

    private String joinIds(List<Long> ids) {
        return ids == null || ids.isEmpty() ? null
            : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String composeDeptSummary(Long reportId) {
        List<HealthDeptSummary> summaries = deptSummaryMapper.selectList(Wrappers.<HealthDeptSummary>lambdaQuery()
            .eq(HealthDeptSummary::getReportId, reportId)
            .orderByAsc(HealthDeptSummary::getDeptId));
        return summaries.stream()
            .filter(s -> StrUtil.isNotBlank(s.getSummaryText()))
            .map(s -> StrUtil.blankToDefault(s.getPrintLabel(), "科室" + s.getDeptId()) + "：" + s.getSummaryText())
            .collect(Collectors.joining("\n"));
    }

    private String composeAbnormalSummary(List<HealthReportItem> items) {
        return items.stream()
            .filter(i -> !HealthReportItem.FLAG_NORMAL.equals(i.getAbnormalFlag()))
            .map(i -> i.getItemName() + " " + i.getResult()
                + (StrUtil.isBlank(i.getUnit()) ? "" : " " + i.getUnit())
                + (StrUtil.isBlank(i.getRefRange()) ? "" : "（参考 " + i.getRefRange() + "）"))
            .collect(Collectors.joining("；"));
    }

    private String composePrintSnapshot(String deptSummary, String abnormalSummary, String conclusion,
                                        String basis, String advice) {
        StringBuilder sb = new StringBuilder();
        appendSegment(sb, "科室小结", deptSummary);
        appendSegment(sb, "异常汇总", abnormalSummary);
        appendSegment(sb, "总检结论", conclusion);
        appendSegment(sb, "依据", basis);
        appendSegment(sb, "健康建议", advice);
        String text = sb.toString();
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }

    private void appendSegment(StringBuilder sb, String label, String text) {
        if (StrUtil.isBlank(text)) {
            return;
        }
        sb.append("【").append(label).append("】").append(text).append("\n");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long reportId, ReportRevokeBo bo) {
        HealthReport report = requireReport(reportId);
        if (!HealthReport.STATUS_FINALIZED.equals(report.getStatus())
            && !HealthReport.STATUS_PRINTED.equals(report.getStatus())) {
            throw new ServiceException("仅已总检/已打印报告可撤销：" + report.getStatus(), HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
        report.setRevokeFlag(HealthReportItem.YES);
        report.setRevokeReason(bo.getReason());
        report.setRevokeBy(safeUsername());
        report.setRevokeTime(LocalDateTime.now());
        report.setStatus(HealthReport.STATUS_PENDING_FINAL);
        reportMapper.updateById(report);
        syncOrderFlow(report, HealthOrderExt.FLOW_PENDING_FINAL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendBack(Long reportId, SendBackBo bo) {
        HealthReport report = requireReport(reportId);
        if (report.getFinalCheckTime() != null) {
            throw new ServiceException("报告已终审签发，不可打回", HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
        HealthDeptSummary summary = deptSummaryMapper.selectOne(Wrappers.<HealthDeptSummary>lambdaQuery()
            .eq(HealthDeptSummary::getReportId, reportId)
            .eq(HealthDeptSummary::getDeptId, bo.getDeptId()));
        if (summary == null) {
            summary = new HealthDeptSummary();
            summary.setReportId(reportId);
            summary.setDeptId(bo.getDeptId());
            summary.setSubmitFlag(HealthDeptSummary.NO);
            summary.setSendBackFlag(HealthDeptSummary.YES);
            summary.setSendBackReason(bo.getReason());
            deptSummaryMapper.insert(summary);
        } else {
            summary.setSubmitFlag(HealthDeptSummary.NO);
            summary.setSendBackFlag(HealthDeptSummary.YES);
            summary.setSendBackReason(bo.getReason());
            deptSummaryMapper.updateById(summary);
        }
        report.setRevokeFlag(HealthReportItem.YES);
        report.setRevokeReason("打回科室 " + bo.getDeptId() + "：" + bo.getReason());
        report.setRevokeBy(safeUsername());
        report.setRevokeTime(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finalCheck(Long reportId) {
        HealthReport report = requireReport(reportId);
        if (!HealthReport.STATUS_FINALIZED.equals(report.getStatus())
            || HealthReportItem.YES.equals(report.getRevokeFlag())) {
            throw new ServiceException("当前状态不允许终审签发：" + report.getStatus(), HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
        report.setFinalCheckBy(safeUsername());
        report.setFinalCheckTime(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void printOrIssue(Long reportId, boolean issue) {
        HealthReport report = requireReport(reportId);
        boolean watermark = "1".equals(configService.selectConfigByKey(CFG_WATERMARK));
        if (issue) {
            if (!HealthReport.STATUS_PRINTED.equals(report.getStatus())) {
                throw new ServiceException("仅已打印报告可发放：" + report.getStatus(), HealthErrorCode.REPORT_STATUS_ILLEGAL);
            }
            // 18 C8：未终审签发的报告不可发放
            if (report.getFinalCheckTime() == null) {
                throw new ServiceException("报告未终审签发，不可发放", HealthErrorCode.REPORT_STATUS_ILLEGAL);
            }
            report.setStatus(HealthReport.STATUS_ISSUED);
            report.setWatermarkFlag(watermark ? HealthReportItem.YES : HealthReportItem.NO);
            syncOrderFlow(report, HealthOrderExt.FLOW_ISSUED);
        } else {
            if (!HealthReport.STATUS_FINALIZED.equals(report.getStatus())
                || HealthReportItem.YES.equals(report.getRevokeFlag())) {
                throw new ServiceException("仅已总检报告可打印：" + report.getStatus(), HealthErrorCode.REPORT_STATUS_ILLEGAL);
            }
            report.setStatus(HealthReport.STATUS_PRINTED);
            syncOrderFlow(report, HealthOrderExt.FLOW_PRINTED);
        }
        reportMapper.updateById(report);
        HealthOrderExt ext = report.getOrderId() == null ? null : findOrderExt(report.getOrderId());
        if (ext != null) {
            if (issue) {
                ext.setIssueTime(LocalDateTime.now());
            } else {
                ext.setPrintTime(LocalDateTime.now());
            }
            orderExtMapper.updateById(ext);
        }
    }

    // ------------------------------------------------------------------ 归档与下载

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArchive(Long reportId, ArchiveBo bo) {
        requireReport(reportId);
        HealthReportArchive archive = new HealthReportArchive();
        archive.setReportId(reportId);
        archive.setType(bo.getType());
        archive.setOssId(bo.getOssId());
        archive.setPageCount(bo.getPageCount());
        archive.setUploadBy(safeUsername());
        archive.setUploadTime(LocalDateTime.now());
        archive.setRemark(bo.getRemark());
        archiveMapper.insert(archive);
    }

    @Override
    public void downloadFile(Long reportId, HttpServletResponse response) {
        HealthReport report = requireReport(reportId);
        if (StrUtil.isBlank(report.getFileUrl())) {
            throw new ServiceException("报告无附件", 400);
        }
        try {
            ossService.download(Long.valueOf(report.getFileUrl()), response);
        } catch (NumberFormatException e) {
            throw new ServiceException("附件标识非法：" + report.getFileUrl(), 400);
        } catch (IOException e) {
            throw new ServiceException("附件下载失败：" + e.getMessage(), 400);
        }
    }

    // ------------------------------------------------------------------ 内部工具

    private HealthReport requireReport(Long id) {
        HealthReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new ServiceException("报告不存在：" + id, HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
        return report;
    }

    /**
     * 18 C9：已总检且未撤销时，科室不得改结果
     */
    private void requireEditable(HealthReport report) {
        boolean editable = HealthReport.EDITABLE_STATUS.contains(report.getStatus())
            || HealthReportItem.YES.equals(report.getRevokeFlag());
        if (!editable) {
            throw new ServiceException("报告已总检且未撤销，禁止修改结果：" + report.getStatus(),
                HealthErrorCode.REPORT_STATUS_ILLEGAL);
        }
    }

    private HealthOrderExt findOrderExt(Integer orderId) {
        return orderExtMapper.selectOne(Wrappers.<HealthOrderExt>lambdaQuery()
            .eq(HealthOrderExt::getOrderId, orderId).last("LIMIT 1"));
    }

    private String mapFlowToStatus(String flow) {
        if (flow == null) {
            return HealthReport.STATUS_WAIT_CREATE;
        }
        return switch (flow) {
            case HealthOrderExt.FLOW_IN_EXAM -> HealthReport.STATUS_IN_EXAM;
            case HealthOrderExt.FLOW_PENDING_FINAL -> HealthReport.STATUS_PENDING_FINAL;
            case HealthOrderExt.FLOW_FINALIZED -> HealthReport.STATUS_FINALIZED;
            case HealthOrderExt.FLOW_PRINTED -> HealthReport.STATUS_PRINTED;
            case HealthOrderExt.FLOW_ISSUED -> HealthReport.STATUS_ISSUED;
            default -> HealthReport.STATUS_WAIT_CREATE;
        };
    }

    /**
     * 报告状态回写订单流程状态（08 §3.2）；非法迁移不阻断报告操作，仅告警
     */
    private void syncOrderFlow(HealthReport report, String target) {
        if (report.getOrderId() == null) {
            return;
        }
        HealthOrderExt ext = findOrderExt(report.getOrderId());
        if (ext == null) {
            return;
        }
        if (!OrderFlowStateMachine.canTransit(ext.getFlowStatus(), target)) {
            log.warn("订单流程状态非法迁移，已跳过：orderId={}, {} => {}",
                report.getOrderId(), ext.getFlowStatus(), target);
            return;
        }
        ext.setFlowStatus(target);
        orderExtMapper.updateById(ext);
    }

    private String safeUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

}
