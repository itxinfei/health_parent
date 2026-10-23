package com.xinfei.health.appointment.service.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 关联差异计算结果
 *
 * <p>08 §4.1 要求编辑关联必须「删旧 + 补新」，修正原系统只删不增的缺陷；
 * 计算逻辑独立出来便于单测覆盖。
 *
 * @param toRemove 旧关联中不再存在的 id
 * @param toAdd    新关联中新增的 id
 * @author xinfei
 */
record RelationDiff(List<Integer> toRemove, List<Integer> toAdd) {

    static RelationDiff compute(Collection<Integer> oldIds, Collection<Integer> newIds) {
        Set<Integer> oldSet = new LinkedHashSet<>(oldIds);
        Set<Integer> newSet = new LinkedHashSet<>(newIds);
        return new RelationDiff(
            oldSet.stream().filter(id -> !newSet.contains(id)).toList(),
            newSet.stream().filter(id -> !oldSet.contains(id)).toList());
    }

}
