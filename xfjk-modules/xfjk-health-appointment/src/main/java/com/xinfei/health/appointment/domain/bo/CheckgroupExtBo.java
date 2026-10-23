package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 检查组扩展信息入参（08 §2.4、§4.2：并入主请求体）
 *
 * @author xinfei
 */
@Data
public class CheckgroupExtBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 承检科室
     */
    @NotNull(message = "承检科室不能为空")
    private Long deptId;

    /**
     * 标本
     */
    private String specimen;

    /**
     * 1 需双科室分检
     */
    private String dualDeptFlag;

    /**
     * 组级空腹标记
     */
    private String isFasting;

    /**
     * 组级默认结论词
     */
    private Long defaultConclusionId;

    /**
     * 套餐内顺序（指引单打印序）
     */
    private Integer sortNo;

}
