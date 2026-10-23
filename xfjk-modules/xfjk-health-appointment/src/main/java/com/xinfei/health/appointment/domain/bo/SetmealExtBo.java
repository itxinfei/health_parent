package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 套餐扩展信息入参（08 §2.5、§4.3：并入主请求体）
 *
 * <p>适用人群四维以结构化字段为准，suitableDesc 仅展示（18 E11）。
 *
 * @author xinfei
 */
@Data
public class SetmealExtBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 0 不限 / 1 男 / 2 女
     */
    @Size(max = 1, message = "性别取值不合法")
    private String sex;

    /**
     * 适用年龄下限
     */
    private Integer ageMin;

    /**
     * 适用年龄上限
     */
    private Integer ageMax;

    /**
     * 0 不限 / 1 限已婚 / 2 限未婚
     */
    @Size(max = 1, message = "婚姻限制取值不合法")
    private String marriedLimit;

    /**
     * 0 不限 / 1 需有性生活史 / 2 需无性生活史
     */
    @Size(max = 1, message = "性生活史限制取值不合法")
    private String hasSexHistory;

    /**
     * 1 含放射/妊娠禁忌项，须在下单时警示
     */
    @Size(max = 1, message = "妊娠警示取值不合法")
    private String pregnancyWarn;

    /**
     * 适宜人群展示文案（不得作为校验依据）
     */
    @Size(max = 500, message = "适宜人群文案不能超过{max}个字符")
    private String suitableDesc;

    /**
     * 默认正常结论词
     */
    private Long defaultConclusionId;

    /**
     * 默认正常处理意见模板
     */
    private Long defaultAdviceId;

    /**
     * 检前须知
     */
    @Size(max = 1000, message = "检前须知不能超过{max}个字符")
    private String noticeText;

}
