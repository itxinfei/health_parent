package com.xinfei.health.appointment.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 禁忌规则条件匹配器（08 §2.7、18 §3.E）
 *
 * <p>condition_json 键固定 8 个，值为数组或布尔；多键之间 AND；
 * 空条件对象匹配所有人（运营显式配置的行为）。
 *
 * @author xinfei
 */
public final class ForbidRuleMatcher {

    /**
     * condition_json 允许的键（08 §2.7，未知键 5013）
     */
    public static final Set<String> ALLOWED_KEYS = Set.of(
        "sex", "pregnancy", "married", "hasSexHistory",
        "ageMin", "ageMax", "inMenstruation", "metalImplant"
    );

    /**
     * 体检人四维画像（13 §4.2 试算入参）
     */
    @Data
    public static class PersonTraits {
        /** 性别 0 不限 / 1 男 / 2 女 */
        private String sex;
        /** 婚况 0 不限 / 1 已婚 / 2 未婚 */
        private String married;
        /** 性生活史 0 不限 / 1 有 / 2 无 */
        private String hasSexHistory;
        /** 妊娠状态 0 未孕 / 1 可能妊娠 / 3 妊娠 */
        private String pregnancy;
        /** 年龄 */
        private Integer age;
        /** 经期中 */
        private Boolean inMenstruation;
        /** 体内金属植入物 */
        private Boolean metalImplant;
    }

    /**
     * 校验 condition_json：未知键或解析失败返回 false（调用方抛 5013）
     */
    public static boolean isValidConditionJson(String conditionJson) {
        try {
            JSONObject obj = JSONUtil.parseObj(conditionJson);
            for (String key : obj.keySet()) {
                if (!ALLOWED_KEYS.contains(key)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断体检人是否命中规则条件
     *
     * @param conditionJson 规则条件 JSON
     * @param person        体检人四维
     * @return true 命中
     */
    public static boolean matches(String conditionJson, PersonTraits person) {
        JSONObject condition;
        try {
            condition = JSONUtil.parseObj(conditionJson);
        } catch (Exception e) {
            return false;
        }
        // 空条件对象匹配所有人
        if (condition.isEmpty()) {
            return true;
        }
        if (condition.containsKey("sex") && !matchArray(condition, "sex", person.getSex())) {
            return false;
        }
        if (condition.containsKey("married") && !matchArray(condition, "married", person.getMarried())) {
            return false;
        }
        if (condition.containsKey("hasSexHistory") && !matchArray(condition, "hasSexHistory", person.getHasSexHistory())) {
            return false;
        }
        if (condition.containsKey("pregnancy") && !matchArray(condition, "pregnancy", person.getPregnancy())) {
            return false;
        }
        if (condition.containsKey("ageMin") && !matchAgeMin(condition.getInt("ageMin", null), person.getAge())) {
            return false;
        }
        if (condition.containsKey("ageMax") && !matchAgeMax(condition.getInt("ageMax", null), person.getAge())) {
            return false;
        }
        if (condition.containsKey("inMenstruation")
            && !matchBool(condition.getBool("inMenstruation"), person.getInMenstruation())) {
            return false;
        }
        if (condition.containsKey("metalImplant")
            && !matchBool(condition.getBool("metalImplant"), person.getMetalImplant())) {
            return false;
        }
        return true;
    }

    /**
     * 数组条件：体检人取值在数组内即命中；值为 null 视为未填，不命中
     */
    private static boolean matchArray(JSONObject condition, String key, String personValue) {
        List<String> values = condition.getJSONArray(key).toList(String.class);
        return personValue != null && values.contains(personValue);
    }

    private static boolean matchAgeMin(Integer ageMin, Integer personAge) {
        return ageMin == null || (personAge != null && personAge >= ageMin);
    }

    private static boolean matchAgeMax(Integer ageMax, Integer personAge) {
        return ageMax == null || (personAge != null && personAge <= ageMax);
    }

    private static boolean matchBool(Boolean conditionValue, Boolean personValue) {
        if (conditionValue == null) {
            return true;
        }
        return conditionValue.equals(Boolean.TRUE.equals(personValue));
    }

    private ForbidRuleMatcher() {
    }

}
