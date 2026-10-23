package com.xinfei.health.appointment.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.xinfei.health.appointment.domain.Setmeal;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐业务对象（新增/编辑/查询共用）
 *
 * @author xinfei
 */
@Data
@AutoMapper(target = Setmeal.class, reverseConvertGenerate = false)
public class SetmealBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键（编辑必填）
     */
    private Integer id;

    /**
     * 套餐名称
     */
    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 128, message = "套餐名称长度不能超过{max}个字符")
    private String name;

    /**
     * 套餐编码（唯一）
     */
    @NotBlank(message = "套餐编码不能为空")
    @Size(max = 8, message = "套餐编码长度不能超过{max}个字符")
    private String code;

    /**
     * 助记码
     */
    @NotBlank(message = "助记码不能为空")
    @Size(max = 16, message = "助记码长度不能超过{max}个字符")
    private String helpCode;

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
     * 图片：OSS 对象主键 ossId 字符串
     */
    @Size(max = 128, message = "图片标识长度不能超过{max}个字符")
    private String img;

    /**
     * 关联检查组 id 集合（空表示清空关联）
     */
    private List<Integer> checkGroupIds;

    /**
     * 扩展信息（08 §4.3：并入主请求体，编辑走 upsert）
     */
    @jakarta.validation.Valid
    private SetmealExtBo ext;

}
