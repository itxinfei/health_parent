package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单项结果录入入参（07 §3.2 POST /health/reports/{id}/items、§5.3）
 *
 * <p>abnormalFlag / ref* / criticalFlag 由服务端判定，前端只传原始结果。
 *
 * @author xinfei
 */
@Data
public class ReportItemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 外部项目可为空 */
    private Integer checkitemId;

    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    /** 0 数值 / 1 定性 / 2 文本 / 3 影像 */
    @NotBlank(message = "结果类型不能为空")
    private String resultType;

    @NotBlank(message = "结果不能为空")
    private String result;

    private String unit;

    @NotNull(message = "承检科室不能为空")
    private Long deptId;

    /** 未检/已检/待查/弃检/延期 */
    @NotBlank(message = "项目状态不能为空")
    private String itemStatus;

    private String discardReason;

    /** 定性项：1 阳性 / 2 阴性 / 3 弱阳性 */
    private String qualitative;

    /** 文本/影像项由医生选择异常标志（07 §5.3 第 3 条），数值/定性项忽略 */
    private String abnormalFlag;

    /** 医生标注重大阳性（18 D7） */
    private String majorPositive;

    /** 医生标注临界值（18 D3） */
    private String edgeFlag;

    private String mergeGroupId;

    private String doctor;

    private String checkDevice;

    private Integer sortNo;

    private String remark;

}
