package com.xinfei.health.appointment.service.impl;

import com.xinfei.health.appointment.domain.HealthOrderExt;

import java.util.Map;
import java.util.Set;

import static com.xinfei.health.appointment.domain.HealthOrderExt.*;

/**
 * 报告级流程状态机（08 §3.2，口径 18 C4）
 *
 * <p>只编码合法迁移边；非法迁移由调用方返回 5006。
 *
 * @author xinfei
 */
public final class OrderFlowStateMachine {

    /**
     * 合法迁移边：key 当前状态 → value 允许到达的状态集合
     */
    private static final Map<String, Set<String>> EDGES = Map.of(
        FLOW_BOOKED, Set.of(FLOW_REGISTERED, FLOW_CANCELLED),
        FLOW_REGISTERED, Set.of(FLOW_IN_EXAM, FLOW_POSTPONED),
        FLOW_IN_EXAM, Set.of(FLOW_PENDING_FINAL, FLOW_POSTPONED),
        FLOW_PENDING_FINAL, Set.of(FLOW_FINALIZED, FLOW_IN_EXAM),
        FLOW_FINALIZED, Set.of(FLOW_PRINTED, FLOW_PENDING_FINAL),
        FLOW_PRINTED, Set.of(FLOW_ISSUED),
        FLOW_CANCELLED, Set.of(FLOW_BOOKED),
        FLOW_POSTPONED, Set.of(FLOW_REGISTERED, FLOW_IN_EXAM)
    );

    /**
     * 判断迁移是否合法
     */
    public static boolean canTransit(String from, String to) {
        if (from == null || to == null) {
            return false;
        }
        Set<String> targets = EDGES.get(from);
        return targets != null && targets.contains(to);
    }

    /**
     * 是否允许到诊登记：仅 已预约（08 §5.5）
     */
    public static boolean canRegister(String flowStatus) {
        return FLOW_BOOKED.equals(flowStatus);
    }

    /**
     * 是否允许取消：仅 已预约；已到诊及之后须先撤销登记（08 §5.6）
     */
    public static boolean canCancel(String flowStatus) {
        return FLOW_BOOKED.equals(flowStatus);
    }

    /**
     * 是否允许延期：已到诊 / 检查中（08 §3.2）
     */
    public static boolean canPostpone(String flowStatus) {
        return FLOW_REGISTERED.equals(flowStatus) || FLOW_IN_EXAM.equals(flowStatus);
    }

    private OrderFlowStateMachine() {
    }

}
