package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.HealthCriticalLog;
import com.xinfei.health.appointment.domain.HealthReport;
import com.xinfei.health.appointment.domain.HealthReportItem;
import com.xinfei.health.appointment.domain.Member;
import com.xinfei.health.appointment.domain.bo.CriticalHandleBo;
import com.xinfei.health.appointment.domain.bo.CriticalQueryBo;
import com.xinfei.health.appointment.domain.bo.CriticalRegisterBo;
import com.xinfei.health.appointment.domain.vo.CriticalLogVo;
import com.xinfei.health.appointment.mapper.HealthCriticalLogMapper;
import com.xinfei.health.appointment.mapper.HealthReportItemMapper;
import com.xinfei.health.appointment.mapper.HealthReportMapper;
import com.xinfei.health.appointment.mapper.MemberMapper;
import com.xinfei.health.appointment.service.IHealthCriticalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 危急值服务实现（07 §3.2.1 派生筛选 + §5.5 登记/处置）
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HealthCriticalServiceImpl implements IHealthCriticalService {

    private final HealthCriticalLogMapper criticalMapper;
    private final HealthReportMapper reportMapper;
    private final HealthReportItemMapper itemMapper;
    private final MemberMapper memberMapper;
    private final ReportJudgeEngine judgeEngine;

    @Override
    public TableDataInfo<CriticalLogVo> queryPageList(CriticalQueryBo bo, PageQuery pageQuery) {
        if (bo.getBeginTime() != null && bo.getEndTime() != null && bo.getBeginTime().isAfter(bo.getEndTime())) {
            throw new ServiceException("时间区间倒置：beginTime 晚于 endTime", 400);
        }
        TableDataInfo<CriticalLogVo> empty = TableDataInfo.build(new Page<>());
        if (StrUtil.isNotBlank(bo.getRuleCode()) && !HealthCriticalLog.RULE_CODES.contains(bo.getRuleCode())) {
            empty.setMsg("未登记的危急值规则码：" + bo.getRuleCode() + "（仅支持 D4-WBC/HB/PLT 六码）");
            return empty;
        }
        if (StrUtil.isNotBlank(bo.getNotifyWay()) && !HealthCriticalLog.NOTIFY_WAYS.contains(bo.getNotifyWay())) {
            throw new ServiceException("通知方式取值非法：" + bo.getNotifyWay(), 400);
        }

        LambdaQueryWrapper<HealthCriticalLog> lqw = Wrappers.<HealthCriticalLog>lambdaQuery()
            .eq(StrUtil.isNotBlank(bo.getRuleCode()), HealthCriticalLog::getRuleCode, bo.getRuleCode())
            .eq(StrUtil.isNotBlank(bo.getArchiveNo()), HealthCriticalLog::getArchiveNo, bo.getArchiveNo())
            .eq(bo.getMemberId() != null, HealthCriticalLog::getMemberId, bo.getMemberId())
            .like(StrUtil.isNotBlank(bo.getReceiver()), HealthCriticalLog::getReceiver, bo.getReceiver())
            .eq(StrUtil.isNotBlank(bo.getNotifyWay()), HealthCriticalLog::getNotifyWay, bo.getNotifyWay())
            .ge(bo.getBeginTime() != null, HealthCriticalLog::getReportTime, bo.getBeginTime())
            .le(bo.getEndTime() != null, HealthCriticalLog::getReportTime, bo.getEndTime())
            .orderByDesc(HealthCriticalLog::getReportTime);

        // 派生筛选（07 §3.2.1：非库列）
        if (Boolean.FALSE.equals(bo.getHandled())) {
            lqw.and(w -> w.isNull(HealthCriticalLog::getHandleResult).or().eq(HealthCriticalLog::getHandleResult, ""));
        } else if (Boolean.TRUE.equals(bo.getHandled())) {
            lqw.isNotNull(HealthCriticalLog::getHandleResult).ne(HealthCriticalLog::getHandleResult, "");
        }
        if (Boolean.FALSE.equals(bo.getConfirmed())) {
            lqw.isNull(HealthCriticalLog::getReceiverConfirmTime);
        } else if (Boolean.TRUE.equals(bo.getConfirmed())) {
            lqw.isNotNull(HealthCriticalLog::getReceiverConfirmTime);
        }

        // 科室过滤：取明细所属科室（06 §5.6 统计同源）
        if (bo.getDeptId() != null) {
            List<Long> itemIds = itemMapper.selectList(Wrappers.<HealthReportItem>lambdaQuery()
                    .eq(HealthReportItem::getDeptId, bo.getDeptId())
                    .select(HealthReportItem::getId))
                .stream().map(HealthReportItem::getId).toList();
            if (itemIds.isEmpty()) {
                return empty;
            }
            lqw.in(HealthCriticalLog::getReportItemId, itemIds);
        }

        Page<HealthCriticalLog> page = criticalMapper.selectPage(pageQuery.build(), lqw);
        List<CriticalLogVo> rows = assemble(page.getRecords());
        Page<CriticalLogVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(rows);
        return TableDataInfo.build(voPage);
    }

    private List<CriticalLogVo> assemble(List<HealthCriticalLog> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Map<Integer, Member> memberMap = memberMapper.selectBatchIds(
                list.stream().map(HealthCriticalLog::getMemberId).distinct().toList())
            .stream().collect(Collectors.toMap(Member::getId, Function.identity()));
        Map<Long, HealthReportItem> itemMap = itemMapper.selectBatchIds(
                list.stream().map(HealthCriticalLog::getReportItemId).distinct().toList())
            .stream().collect(Collectors.toMap(HealthReportItem::getId, Function.identity()));
        return list.stream().map(log -> {
            CriticalLogVo vo = new CriticalLogVo();
            vo.setId(log.getId());
            vo.setReportId(log.getReportId());
            vo.setReportItemId(log.getReportItemId());
            vo.setMemberId(log.getMemberId());
            Member member = memberMap.get(log.getMemberId());
            vo.setMemberName(member == null ? null : member.getName());
            vo.setArchiveNo(log.getArchiveNo());
            vo.setItemName(log.getItemName());
            vo.setCriticalValue(log.getCriticalValue());
            vo.setRuleCode(log.getRuleCode());
            HealthReportItem item = itemMap.get(log.getReportItemId());
            vo.setDeptId(item == null ? null : item.getDeptId());
            vo.setReportTime(log.getReportTime());
            vo.setReceiver(log.getReceiver());
            vo.setReceiverConfirmTime(log.getReceiverConfirmTime());
            vo.setNotifyWay(log.getNotifyWay());
            vo.setHandleResult(log.getHandleResult());
            vo.setHandled(StrUtil.isNotBlank(log.getHandleResult()));
            vo.setConfirmed(log.getReceiverConfirmTime() != null);
            vo.setTrackBy(log.getTrackBy());
            vo.setTrackTime(log.getTrackTime());
            vo.setTrackRemark(log.getTrackRemark());
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(Long reportId, CriticalRegisterBo bo) {
        HealthReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new ServiceException("报告不存在：" + reportId, 6013);
        }
        HealthReportItem item = itemMapper.selectById(bo.getReportItemId());
        if (item == null || !reportId.equals(item.getReportId())) {
            throw new ServiceException("触发项不存在或不属于本报告", HealthErrorCode.CRITICAL_INVALID);
        }
        if (!HealthReportItem.YES.equals(item.getCriticalFlag())) {
            throw new ServiceException("该明细未判定为危急值，禁止登记（07 §7）", HealthErrorCode.CRITICAL_INVALID);
        }
        Long exists = criticalMapper.selectCount(Wrappers.<HealthCriticalLog>lambdaQuery()
            .eq(HealthCriticalLog::getReportItemId, item.getId()));
        if (exists != null && exists > 0) {
            throw new ServiceException("该触发项已登记危急值，请直接处置", HealthErrorCode.CRITICAL_INVALID);
        }
        String ruleCode = judgeEngine.resolveCriticalRule(item.getItemName(), item.getResult());
        if (ruleCode == null) {
            throw new ServiceException("无法匹配危急值规则码，请核对项目与结果", HealthErrorCode.CRITICAL_INVALID);
        }
        String notifyWay = StrUtil.blankToDefault(bo.getNotifyWay(), HealthCriticalLog.NOTIFY_BOTH);
        if (!HealthCriticalLog.NOTIFY_WAYS.contains(notifyWay)) {
            throw new ServiceException("通知方式取值非法：" + notifyWay, HealthErrorCode.CRITICAL_INVALID);
        }

        HealthCriticalLog logRow = new HealthCriticalLog();
        logRow.setReportItemId(item.getId());
        logRow.setReportId(reportId);
        logRow.setMemberId(report.getMemberId());
        logRow.setArchiveNo(report.getArchiveNo());
        logRow.setItemName(item.getItemName());
        logRow.setCriticalValue(item.getResult());
        logRow.setRuleCode(ruleCode);
        logRow.setReportTime(bo.getReportTime() != null ? bo.getReportTime()
            : (item.getCheckTime() != null ? item.getCheckTime() : LocalDateTime.now()));
        logRow.setReceiver(bo.getReceiver());
        logRow.setReceiverConfirmTime(bo.getReceiverConfirmTime());
        logRow.setNotifyWay(notifyWay);
        logRow.setHandleResult(bo.getHandleResult());
        criticalMapper.insert(logRow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, CriticalHandleBo bo) {
        HealthCriticalLog logRow = criticalMapper.selectById(id);
        if (logRow == null) {
            throw new ServiceException("危急值登记不存在：" + id, HealthErrorCode.CRITICAL_INVALID);
        }
        if (StrUtil.isNotBlank(bo.getNotifyWay()) && !HealthCriticalLog.NOTIFY_WAYS.contains(bo.getNotifyWay())) {
            throw new ServiceException("通知方式取值非法：" + bo.getNotifyWay(), HealthErrorCode.CRITICAL_INVALID);
        }
        if (bo.getHandleResult() != null && StrUtil.isBlank(bo.getHandleResult())) {
            throw new ServiceException("处置结果不能为空白", HealthErrorCode.CRITICAL_INVALID);
        }
        if (StrUtil.isNotBlank(bo.getReceiver())) {
            logRow.setReceiver(bo.getReceiver());
        }
        if (bo.getReceiverConfirmTime() != null) {
            logRow.setReceiverConfirmTime(bo.getReceiverConfirmTime());
        }
        if (StrUtil.isNotBlank(bo.getNotifyWay())) {
            logRow.setNotifyWay(bo.getNotifyWay());
        }
        if (bo.getHandleResult() != null) {
            logRow.setHandleResult(bo.getHandleResult());
        }
        if (StrUtil.isNotBlank(bo.getTrackBy())) {
            logRow.setTrackBy(bo.getTrackBy());
        }
        if (bo.getTrackTime() != null) {
            logRow.setTrackTime(bo.getTrackTime());
        }
        if (bo.getTrackRemark() != null) {
            logRow.setTrackRemark(bo.getTrackRemark());
        }
        criticalMapper.updateById(logRow);
    }

}
