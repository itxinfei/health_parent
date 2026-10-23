package com.xinfei.health.appointment.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.xinfei.health.appointment.domain.Checkgroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 检查组业务对象（新增/编辑/查询共用）
 *
 * @author xinfei
 */
@Data
@AutoMapper(target = Checkgroup.class, reverseConvertGenerate = false)
public class CheckgroupBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（编辑必填）
     */
    private Integer id;

    /**
     * 检查组编码（唯一）
     */
    @NotBlank(message = "检查组编码不能为空")
    @Size(max = 32, message = "检查组编码长度不能超过{max}个字符")
    private String code;

    /**
     * 检查组名称
     */
    @NotBlank(message = "检查组名称不能为空")
    @Size(max = 32, message = "检查组名称长度不能超过{max}个字符")
    private String name;

    /**
     * 助记码
     */
    @Size(max = 32, message = "助记码长度不能超过{max}个字符")
    private String helpCode;

    /**
     * 性别限制（0 不限 1 男 2 女）
     */
    @Size(max = 1, message = "性别取值不合法")
    private String sex;

    /**
     * 说明
     */
    @Size(max = 128, message = "说明长度不能超过{max}个字符")
    private String remark;

    /**
     * 注意事项
     */
    @Size(max = 128, message = "注意事项长度不能超过{max}个字符")
    private String attention;

    /**
     * 关联检查项 id 集合（空表示清空关联）
     */
    private List<Integer> checkItemIds;

    /**
     * 扩展信息（08 §4.2：并入主请求体，编辑走 upsert）
     */
    @jakarta.validation.Valid
    private CheckgroupExtBo ext;

}
