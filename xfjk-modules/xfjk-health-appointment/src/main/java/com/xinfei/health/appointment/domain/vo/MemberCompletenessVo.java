package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 档案完整度视图（07 §5.2：已填/应填 实时计算，不落库；
 * unmarkedFields = 空白且未标缺项，missingFields = 已确认缺失）
 *
 * @author xinfei
 */
@Data
public class MemberCompletenessVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 必填清单出处（sys_config 键 health:profile:required）
     */
    private String requiredKey;

    /**
     * 应填字段总数
     */
    private Integer total;

    /**
     * 已填（含已标缺项豁免）字段数
     */
    private Integer filled;

    /**
     * 「已填/应填」展示串
     */
    private String text;

    /**
     * 空白且未标缺项的字段（红色提示项）
     */
    private List<String> unmarkedFields;

    /**
     * 已标缺项的字段及原因
     */
    private List<MissingItem> missingFields;

    @Data
    public static class MissingItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String fieldKey;

        private String reason;

        private String markedBy;

    }

}
