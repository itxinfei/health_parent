package com.xinfei.health.appointment.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.xinfei.health.appointment.domain.Checkitem;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 检查项业务对象（新增/编辑/查询共用）
 *
 * <p>t_checkitem 是冻结表、无审计列，故不继承框架 BaseEntity。
 *
 * @author xinfei
 */
@Data
@AutoMapper(target = Checkitem.class, reverseConvertGenerate = false)
public class CheckitemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（编辑必填）
     */
    private Integer id;

    /**
     * 检查项编码（唯一）
     */
    @NotBlank(message = "检查项编码不能为空")
    @Size(max = 16, message = "检查项编码长度不能超过{max}个字符")
    private String code;

    /**
     * 检查项名称
     */
    @NotBlank(message = "检查项名称不能为空")
    @Size(max = 32, message = "检查项名称长度不能超过{max}个字符")
    private String name;

    /**
     * 性别限制（0 不限 1 男 2 女）
     */
    @Size(max = 1, message = "性别取值不合法")
    private String sex;

    /**
     * 年龄范围
     */
    @Size(max = 32, message = "年龄范围长度不能超过{max}个字符")
    private String age;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 类型（1 检查 2 检验）
     */
    @Size(max = 1, message = "类型取值不合法")
    private String type;

    /**
     * 注意事项
     */
    @Size(max = 128, message = "注意事项长度不能超过{max}个字符")
    private String attention;

    /**
     * 说明
     */
    @Size(max = 128, message = "说明长度不能超过{max}个字符")
    private String remark;

    /**
     * 扩展信息（08 §4.1：并入主请求体；新增时缺失返回 5007）
     */
    @Valid
    private CheckitemExtBo ext;

    /**
     * 参考区间（新增须至少一条，08 §11 验收）
     */
    @Valid
    private List<CheckitemRefBo> refs;

}
