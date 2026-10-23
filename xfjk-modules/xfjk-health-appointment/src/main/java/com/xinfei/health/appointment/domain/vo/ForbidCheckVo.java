package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 禁忌试算结果（08 §6.2：pass/hitCount/hits/warns/delays）
 *
 * @author xinfei
 */
@Data
public class ForbidCheckVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否放行（无拦截级命中即 true）
     */
    private boolean pass;

    /**
     * 拦截级命中数
     */
    private int hitCount;

    /**
     * 拦截级（action=0）
     */
    private List<ForbidHitVo> hits;

    /**
     * 提示级（action=1）
     */
    private List<ForbidHitVo> warns;

    /**
     * 延后补检档（action=2）
     */
    private List<ForbidHitVo> delays;

    /**
     * 单条命中
     */
    @Data
    public static class ForbidHitVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 规则编码（18 E 组）
         */
        private String ruleCode;

        /**
         * 处置档
         */
        private String action;

        /**
         * 目标主键
         */
        private Integer targetId;

        /**
         * 目标名称（检查项/检查组/套餐名）
         */
        private String itemName;

        /**
         * 可读原因（18 E13：必须能直接展示）
         */
        private String tipText;

    }

}
