package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 检查项扩展信息入参（08 §2.2、§4.1：并入主请求体，不单独开 _ext 接口）
 *
 * <p>ext 缺失时新增返回 5007；result_type=0 时 unit 必填。
 *
 * @author xinfei
 */
@Data
public class CheckitemExtBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 结果类型：0 数值 / 1 定性 / 2 文本描述 / 3 影像结论
     */
    @NotBlank(message = "结果类型不能为空")
    private String resultType;

    /**
     * 单位；resultType=0 时必填（5007）
     */
    private String unit;

    /**
     * 所属科室
     */
    @NotNull(message = "所属科室不能为空")
    private Long deptId;

    /**
     * 标本类型
     */
    private String specimen;

    /**
     * 1 需空腹
     */
    private String isFasting;

    /**
     * 1 含射线
     */
    private String isRadiation;

    /**
     * 1 必检
     */
    private String isRequired;

    /**
     * 1 需两科室分检
     */
    private String dualDeptFlag;

    /**
     * 组内排序
     */
    private Integer sortNo;

    /**
     * 默认结论词
     */
    private Long defaultConclusionId;

    /**
     * 检前须知
     */
    private String noticeText;

}
