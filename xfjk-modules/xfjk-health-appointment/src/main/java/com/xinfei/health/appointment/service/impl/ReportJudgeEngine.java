package com.xinfei.health.appointment.service.impl;

import com.xinfei.common.core.exception.ServiceException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.HealthCheckitemRef;
import com.xinfei.health.appointment.domain.HealthCriticalLog;
import com.xinfei.health.appointment.domain.HealthReportItem;
import com.xinfei.health.appointment.mapper.HealthCheckitemRefMapper;
import com.xinfei.system.service.ISysConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 结果判定引擎（07 §5.3 三条分支 + §5.5 危急值，18 D1~D3/D4）
 *
 * <p>判定值只由本引擎产出，前端传入的 abnormalFlag 仅对文本/影像项生效（07 §7）。
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ReportJudgeEngine {

    private static final String CFG_WBC_LOW = "health:critical:wbc:lower";
    private static final String CFG_WBC_HIGH = "health:critical:wbc:upper";
    private static final String CFG_HB_LOW = "health:critical:hb:lower";
    private static final String CFG_HB_HIGH = "health:critical:hb:upper";
    private static final String CFG_PLT_LOW = "health:critical:plt:lower";
    private static final String CFG_PLT_HIGH = "health:critical:plt:upper";

    private final HealthCheckitemRefMapper refMapper;
    private final ISysConfigService configService;

    /**
     * 判定结果
     */
    @Data
    public static class Outcome {
        private String refRange;
        private BigDecimal refMin;
        private BigDecimal refMax;
        private String refConfigured;
        private String abnormalFlag;
        private String qualitative;
        private String abnormalGrade;
        private String criticalFlag;
        private String criticalRuleCode;
        private String tip;
    }

    /**
     * 判定入口
     *
     * @param resultType     0 数值 / 1 定性 / 2 文本 / 3 影像
     * @param result         原始结果
     * @param qualitative    定性项：1 阳性 / 2 阴性 / 3 弱阳性
     * @param doctorFlag     文本/影像项医生选择的异常标志
     * @param edgeFlagInput  医生标注临界值
     * @param checkitemId    检查项 id（可为空）
     * @param itemName       项目名称快照（危急值规则按名称关键词匹配）
     * @param sex            会员性别 0/1/2
     * @param age            体检时年龄
     * @param physioState    生理状态：1 妊娠 / 2 备孕 / 其他
     * @throws ServiceException 数值项无法解析抛 6008（解析路径按行捕获）
     */
    public Outcome judge(String resultType, String result, String qualitative, String doctorFlag,
                         String edgeFlagInput, Integer checkitemId, String itemName,
                         String sex, Integer age, String physioState) {
        Outcome outcome = new Outcome();
        outcome.setRefConfigured(HealthReportItem.NO);
        outcome.setCriticalFlag(HealthReportItem.NO);
        boolean edge = HealthReportItem.YES.equals(edgeFlagInput);

        if (HealthReportItem.TYPE_NUMERIC.equals(resultType)) {
            judgeNumeric(outcome, result, checkitemId, itemName, sex, age, physioState, edge);
        } else if (HealthReportItem.TYPE_QUALITATIVE.equals(resultType)) {
            judgeQualitative(outcome, qualitative, checkitemId, sex, age, physioState, edge);
        } else {
            // 文本/影像：不做数值判定，由医生选择（07 §5.3 第 3 条）；未选择按「异常」待确认
            String flag = doctorFlag == null || doctorFlag.isBlank()
                ? HealthReportItem.FLAG_ABNORMAL : doctorFlag;
            outcome.setAbnormalFlag(flag);
            outcome.setAbnormalGrade(HealthReportItem.FLAG_NORMAL.equals(flag) ? null : "B");
            if (edge) {
                outcome.setTip("临界值，建议复查");
            }
        }
        return outcome;
    }

    private void judgeNumeric(Outcome outcome, String result, Integer checkitemId, String itemName,
                              String sex, Integer age, String physioState, boolean edge) {
        BigDecimal value = parseDecimal(result);
        if (value == null) {
            throw new ServiceException("数值型结果无法解析：" + result, HealthErrorCode.RESULT_NOT_NUMERIC);
        }
        HealthCheckitemRef ref = matchRef(checkitemId, sex, age, physioState);
        if (ref != null) {
            outcome.setRefRange(ref.getRefText());
            outcome.setRefMin(ref.getRefMin());
            outcome.setRefMax(ref.getRefMax());
        }
        boolean hasInterval = ref != null && (ref.getRefMin() != null || ref.getRefMax() != null);
        outcome.setRefConfigured(hasInterval ? HealthReportItem.YES : HealthReportItem.NO);

        StringBuilder tip = new StringBuilder();
        if (!hasInterval) {
            // 18 D2：无匹配区间只存原值，绝不判正常
            outcome.setAbnormalFlag(HealthReportItem.FLAG_ABNORMAL);
            tip.append("参考区间未配置，结果仅存原值，请人工确认判定");
        } else {
            String flag = HealthReportItem.FLAG_NORMAL;
            if (ref.getRefMin() != null && value.compareTo(ref.getRefMin()) < 0) {
                flag = HealthReportItem.FLAG_LOW;
                tip.append("低于参考下限 ").append(ref.getRefText());
            } else if (ref.getRefMax() != null && value.compareTo(ref.getRefMax()) > 0) {
                flag = HealthReportItem.FLAG_HIGH;
                tip.append("高于参考上限 ").append(ref.getRefText());
            }
            outcome.setAbnormalFlag(flag);
            // 18 D3：基础值异常者须给复查提示
            if (!HealthReportItem.FLAG_NORMAL.equals(flag)) {
                tip.append("；建议复查");
            }
        }

        // 18 D4：危急值线按配置判定，命中即置 critical_flag
        String ruleCode = hitCriticalRule(itemName, value);
        if (ruleCode != null) {
            outcome.setCriticalFlag(HealthReportItem.YES);
            outcome.setCriticalRuleCode(ruleCode);
            tip.insert(0, "危急值：请在 30 分钟内登记并通知接收医生；");
        }
        outcome.setAbnormalGrade(resolveGrade(outcome.getCriticalFlag(), outcome.getAbnormalFlag()));
        if (edge) {
            tip.append(tip.length() > 0 ? "；" : "").append("临界值，建议复查");
        }
        outcome.setTip(tip.length() == 0 ? null : tip.toString());
    }

    private void judgeQualitative(Outcome outcome, String qualitative, Integer checkitemId,
                                  String sex, Integer age, String physioState, boolean edge) {
        String q = qualitative;
        if (q == null || q.isBlank()) {
            throw new ServiceException("定性项须给出阳性/阴性/弱阳性", HealthErrorCode.RESULT_NOT_NUMERIC);
        }
        outcome.setQualitative(q);
        HealthCheckitemRef ref = matchRef(checkitemId, sex, age, physioState);
        if (ref != null) {
            outcome.setRefRange(ref.getRefText());
            outcome.setRefConfigured(HealthReportItem.YES);
        }
        String flag = switch (q) {
            case "1" -> HealthReportItem.FLAG_POSITIVE;
            case "2" -> HealthReportItem.FLAG_NEGATIVE;
            case "3" -> HealthReportItem.FLAG_WEAK_POSITIVE;
            default -> throw new ServiceException("定性结果越界：" + q, HealthErrorCode.RESULT_NOT_NUMERIC);
        };
        outcome.setAbnormalFlag(flag);
        // 阳性/弱阳性计入异常统计（18 D1 注）：判定列仍存 4/6，统计按枚举归并
        outcome.setAbnormalGrade(HealthReportItem.FLAG_NEGATIVE.equals(flag) ? null : "B");
        StringBuilder tip = new StringBuilder();
        if (!HealthReportItem.FLAG_NEGATIVE.equals(flag)) {
            tip.append("阳性结果，建议复查或进一步检查");
        }
        if (edge) {
            tip.append(tip.length() > 0 ? "；" : "").append("临界值，建议复查");
        }
        outcome.setTip(tip.length() == 0 ? null : tip.toString());
    }

    /**
     * 参考区间匹配（18 D2）：性别 / 年龄闭区间 / 生理状态；多条命中取 priority 最小、特异性最高
     */
    private HealthCheckitemRef matchRef(Integer checkitemId, String sex, Integer age, String physioState) {
        if (checkitemId == null) {
            return null;
        }
        List<HealthCheckitemRef> list = refMapper.selectList(
            Wrappers.<HealthCheckitemRef>lambdaQuery().eq(HealthCheckitemRef::getCheckitemId, checkitemId));
        return list.stream()
            .filter(r -> matchSex(r.getSex(), sex))
            .filter(r -> matchAge(r.getAgeMin(), r.getAgeMax(), age))
            .filter(r -> matchPhysio(r.getPhysioState(), physioState))
            .sorted(Comparator
                .comparingInt((HealthCheckitemRef r) -> r.getPriority() == null ? 100 : r.getPriority())
                .thenComparing(Comparator.comparingInt(this::specificity).reversed()))
            .findFirst()
            .orElse(null);
    }

    private boolean matchSex(String refSex, String sex) {
        return refSex == null || "0".equals(refSex) || (sex != null && refSex.equals(sex));
    }

    private boolean matchAge(Integer from, Integer to, Integer age) {
        if (from == null && to == null) {
            return true;
        }
        int value = age == null ? 0 : age;
        return (from == null || value >= from) && (to == null || value <= to);
    }

    private boolean matchPhysio(String refState, String physioState) {
        if (refState == null || "0".equals(refState)) {
            return true;
        }
        return refState.equals(physioState);
    }

    private int specificity(HealthCheckitemRef ref) {
        int score = 0;
        if (ref.getSex() != null && !"0".equals(ref.getSex())) {
            score += 2;
        }
        if (ref.getAgeMin() != null || ref.getAgeMax() != null) {
            score += 2;
        }
        if (ref.getPhysioState() != null && !"0".equals(ref.getPhysioState())) {
            score += 2;
        }
        return score;
    }

    /**
     * 危急值命中（18 D4）：仅 WBC/HB/PLT 三项有配置线，按项目名称关键词识别
     */
    public String resolveCriticalRule(String itemName, String result) {
        BigDecimal value = parseDecimal(result);
        return value == null ? null : hitCriticalRule(itemName, value);
    }

    private String hitCriticalRule(String itemName, BigDecimal value) {
        if (itemName == null) {
            return null;
        }
        if (itemName.contains("白细胞")) {
            return compareRule(value, CFG_WBC_LOW, CFG_WBC_HIGH, HealthCriticalLog.RULE_WBC_LOW, HealthCriticalLog.RULE_WBC_HIGH);
        }
        if (itemName.contains("血红蛋白")) {
            return compareRule(value, CFG_HB_LOW, CFG_HB_HIGH, HealthCriticalLog.RULE_HB_LOW, HealthCriticalLog.RULE_HB_HIGH);
        }
        if (itemName.contains("血小板")) {
            return compareRule(value, CFG_PLT_LOW, CFG_PLT_HIGH, HealthCriticalLog.RULE_PLT_LOW, HealthCriticalLog.RULE_PLT_HIGH);
        }
        return null;
    }

    private String compareRule(BigDecimal value, String lowKey, String highKey, String lowRule, String highRule) {
        BigDecimal low = readDecimal(lowKey);
        BigDecimal high = readDecimal(highKey);
        if (low != null && value.compareTo(low) < 0) {
            return lowRule;
        }
        if (high != null && value.compareTo(high) > 0) {
            return highRule;
        }
        return null;
    }

    private String resolveGrade(String criticalFlag, String abnormalFlag) {
        if (HealthReportItem.YES.equals(criticalFlag)) {
            return "A";
        }
        return HealthReportItem.FLAG_NORMAL.equals(abnormalFlag) ? null : "B";
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim().replace(",", "");
        // 容忍结果串带单位后缀（如 "5.2 g/L"）
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^-?\\d+(\\.\\d+)?$").matcher(text);
        if (m.matches()) {
            return new BigDecimal(text);
        }
        java.util.regex.Matcher prefix = java.util.regex.Pattern.compile("^(-?\\d+(?:\\.\\d+)?)\\s*\\S*$").matcher(text);
        return prefix.matches() ? new BigDecimal(prefix.group(1)) : null;
    }

    private BigDecimal readDecimal(String key) {
        try {
            String value = configService.selectConfigByKey(key);
            return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
        } catch (Exception e) {
            log.warn("危急值配置读取失败 {} => {}", key, e.getMessage());
            return null;
        }
    }

}
