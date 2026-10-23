package com.xinfei.health.appointment.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_BOOKED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_CANCELLED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_FINALIZED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_IN_EXAM;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_ISSUED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_PENDING_FINAL;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_POSTPONED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_PRINTED;
import static com.xinfei.health.appointment.domain.HealthOrderExt.FLOW_REGISTERED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 流程状态机合法边校验（08 §3.2）
 *
 * @author xinfei
 */
@DisplayName("订单流程状态机")
@Tag("dev")
class OrderFlowStateMachineTest {

    @Test
    @DisplayName("主链路迁移合法：已预约→…→报告已发放")
    void mainChainLegal() {
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_BOOKED, FLOW_REGISTERED));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_REGISTERED, FLOW_IN_EXAM));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_IN_EXAM, FLOW_PENDING_FINAL));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_PENDING_FINAL, FLOW_FINALIZED));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_FINALIZED, FLOW_PRINTED));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_PRINTED, FLOW_ISSUED));
    }

    @Test
    @DisplayName("回退边合法：打回、总检撤销、取消恢复、延期恢复")
    void rollbackEdgesLegal() {
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_PENDING_FINAL, FLOW_IN_EXAM));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_FINALIZED, FLOW_PENDING_FINAL));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_CANCELLED, FLOW_BOOKED));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_POSTPONED, FLOW_REGISTERED));
        assertTrue(OrderFlowStateMachine.canTransit(FLOW_POSTPONED, FLOW_IN_EXAM));
    }

    @Test
    @DisplayName("非法迁移被拒绝：跳步与逆向")
    void illegalEdgesRejected() {
        assertFalse(OrderFlowStateMachine.canTransit(FLOW_BOOKED, FLOW_IN_EXAM));
        assertFalse(OrderFlowStateMachine.canTransit(FLOW_BOOKED, FLOW_FINALIZED));
        assertFalse(OrderFlowStateMachine.canTransit(FLOW_REGISTERED, FLOW_PENDING_FINAL));
        assertFalse(OrderFlowStateMachine.canTransit(FLOW_ISSUED, FLOW_IN_EXAM));
        assertFalse(OrderFlowStateMachine.canTransit(FLOW_CANCELLED, FLOW_ISSUED));
        assertFalse(OrderFlowStateMachine.canTransit(null, FLOW_BOOKED));
    }

    @Test
    @DisplayName("前置判断：登记与取消仅限已预约，延期限已到诊/检查中")
    void preconditions() {
        assertTrue(OrderFlowStateMachine.canRegister(FLOW_BOOKED));
        assertFalse(OrderFlowStateMachine.canRegister(FLOW_REGISTERED));
        assertTrue(OrderFlowStateMachine.canCancel(FLOW_BOOKED));
        assertFalse(OrderFlowStateMachine.canCancel(FLOW_REGISTERED));
        assertTrue(OrderFlowStateMachine.canPostpone(FLOW_REGISTERED));
        assertTrue(OrderFlowStateMachine.canPostpone(FLOW_IN_EXAM));
        assertFalse(OrderFlowStateMachine.canPostpone(FLOW_BOOKED));
        assertFalse(OrderFlowStateMachine.canPostpone(FLOW_CANCELLED));
    }

}
