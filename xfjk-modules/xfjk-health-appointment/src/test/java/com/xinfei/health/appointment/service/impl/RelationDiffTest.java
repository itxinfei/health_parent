package com.xinfei.health.appointment.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 关联差异计算（08 §4.1「删旧 + 补新」，修正原系统只删不增的缺陷）
 *
 * @author xinfei
 */
@Tag("dev")
@DisplayName("关联差异计算")
class RelationDiffTest {

    @Test
    @DisplayName("旧组合同时含移除项与新增项时两侧都算出来（回归原系统只删不增缺陷）")
    void computeBothRemoveAndAdd() {
        RelationDiff diff = RelationDiff.compute(List.of(1, 2, 3), List.of(2, 3, 4, 5));

        assertEquals(List.of(1), diff.toRemove());
        assertEquals(List.of(4, 5), diff.toAdd());
    }

    @Test
    @DisplayName("新集合清空时删除全部旧关联且不补插")
    void computeRemoveAll() {
        RelationDiff diff = RelationDiff.compute(List.of(1, 2), List.of());

        assertEquals(List.of(1, 2), diff.toRemove());
        assertTrue(diff.toAdd().isEmpty());
    }

    @Test
    @DisplayName("旧集合为空时全部视为新增")
    void computeAddAll() {
        RelationDiff diff = RelationDiff.compute(List.of(), List.of(7, 8));

        assertTrue(diff.toRemove().isEmpty());
        assertEquals(List.of(7, 8), diff.toAdd());
    }

    @Test
    @DisplayName("集合一致时两侧都为空（幂等，重复提交不产生多余 SQL）")
    void computeNoChange() {
        RelationDiff diff = RelationDiff.compute(List.of(1, 2, 3), List.of(3, 2, 1));

        assertTrue(diff.toRemove().isEmpty());
        assertTrue(diff.toAdd().isEmpty());
    }

    @Test
    @DisplayName("新集合含重复 id 时去重，避免关联表重复插入")
    void computeDeduplicatesNewIds() {
        RelationDiff diff = RelationDiff.compute(List.of(), List.of(9, 9, 10));

        assertEquals(List.of(9, 10), diff.toAdd());
    }

}
