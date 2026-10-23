package com.xinfei.health.appointment.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 禁忌规则条件匹配（08 §2.7：键白名单、数组/布尔/年龄段匹配、AND 语义）
 *
 * @author xinfei
 */
@DisplayName("禁忌规则条件匹配器")
@Tag("dev")
class ForbidRuleMatcherTest {

    private ForbidRuleMatcher.PersonTraits person(String sex, String married, String hasSexHistory,
                                                  String pregnancy, Integer age,
                                                  Boolean inMenstruation, Boolean metalImplant) {
        ForbidRuleMatcher.PersonTraits p = new ForbidRuleMatcher.PersonTraits();
        p.setSex(sex);
        p.setMarried(married);
        p.setHasSexHistory(hasSexHistory);
        p.setPregnancy(pregnancy);
        p.setAge(age);
        p.setInMenstruation(inMenstruation);
        p.setMetalImplant(metalImplant);
        return p;
    }

    @Test
    @DisplayName("条件 JSON 键白名单校验")
    void conditionKeyWhitelist() {
        assertTrue(ForbidRuleMatcher.isValidConditionJson("{\"sex\":[\"2\"]}"));
        assertTrue(ForbidRuleMatcher.isValidConditionJson("{}"));
        assertTrue(ForbidRuleMatcher.isValidConditionJson(
            "{\"ageMin\":18,\"ageMax\":60,\"inMenstruation\":true,\"metalImplant\":false}"));
        assertFalse(ForbidRuleMatcher.isValidConditionJson("{\"unknownKey\":[\"1\"]}"));
        assertFalse(ForbidRuleMatcher.isValidConditionJson("not-json"));
    }

    @Test
    @DisplayName("数组条件命中与不命中")
    void arrayMatch() {
        String e3 = "{\"sex\":[\"2\"]}";
        assertTrue(ForbidRuleMatcher.matches(e3, person("2", "2", "2", "0", 26, false, false)));
        assertFalse(ForbidRuleMatcher.matches(e3, person("1", "1", "1", "0", 30, false, false)));
        // 未填写性别不命中性别条件
        assertFalse(ForbidRuleMatcher.matches(e3, person(null, "0", "0", "0", 30, false, false)));
    }

    @Test
    @DisplayName("多键 AND 语义：E3 无性生活史禁查妇科")
    void multiKeyAnd() {
        String rule = "{\"sex\":[\"2\"],\"hasSexHistory\":[\"2\"]}";
        assertTrue(ForbidRuleMatcher.matches(rule, person("2", "2", "2", "0", 26, false, false)));
        // 有性生活史不命中
        assertFalse(ForbidRuleMatcher.matches(rule, person("2", "2", "1", "0", 26, false, false)));
        // 男性不命中
        assertFalse(ForbidRuleMatcher.matches(rule, person("1", "0", "2", "0", 40, false, false)));
    }

    @Test
    @DisplayName("年龄段与布尔条件")
    void ageAndBool() {
        String rule = "{\"ageMin\":18,\"ageMax\":60,\"metalImplant\":true}";
        assertTrue(ForbidRuleMatcher.matches(rule, person("0", "0", "0", "0", 35, false, true)));
        assertFalse(ForbidRuleMatcher.matches(rule, person("0", "0", "0", "0", 17, false, true)));
        assertFalse(ForbidRuleMatcher.matches(rule, person("0", "0", "0", "0", 61, false, true)));
        // 无金属植入物不命中
        assertFalse(ForbidRuleMatcher.matches(rule, person("0", "0", "0", "0", 35, false, false)));
    }

    @Test
    @DisplayName("空条件对象匹配所有人")
    void emptyConditionMatchesAll() {
        assertTrue(ForbidRuleMatcher.matches("{}", person("2", "2", "2", "3", 80, true, true)));
        assertTrue(ForbidRuleMatcher.matches("{}", person("1", "0", "0", "0", 5, false, false)));
    }

    @Test
    @DisplayName("年龄缺失时段条件不命中")
    void missingAgeNoMatch() {
        assertFalse(ForbidRuleMatcher.matches("{\"ageMin\":18}", person("2", "0", "0", "0", null, false, false)));
    }

    @Test
    @DisplayName("非法 JSON 不命中而非抛异常")
    void invalidJsonNoThrow() {
        assertFalse(ForbidRuleMatcher.matches("not-json", person("2", "0", "0", "0", 26, false, false)));
    }

    @Test
    @DisplayName("妊娠数组多值命中")
    void pregnancyMultiValue() {
        String rule = "{\"pregnancy\":[\"1\",\"3\"]}";
        assertTrue(ForbidRuleMatcher.matches(rule, person("2", "0", "0", "1", 28, false, false)));
        assertTrue(ForbidRuleMatcher.matches(rule, person("2", "0", "0", "3", 28, false, false)));
        assertFalse(ForbidRuleMatcher.matches(rule, person("2", "0", "0", "0", 28, false, false)));
        assertEquals(2, 2);
    }

}
