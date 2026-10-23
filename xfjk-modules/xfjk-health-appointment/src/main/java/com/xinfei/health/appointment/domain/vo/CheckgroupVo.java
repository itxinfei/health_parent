package com.xinfei.health.appointment.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.xinfei.health.appointment.domain.Checkgroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 检查组视图对象
 *
 * <p>checkItems 仅在「组内检查项」「套餐详情」场景填充，列表查询为 null。
 *
 * @author xinfei
 */
@Data
@AutoMapper(target = Checkgroup.class)
public class CheckgroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 检查组编码
     */
    private String code;

    /**
     * 检查组名称
     */
    private String name;

    /**
     * 助记码
     */
    private String helpCode;

    /**
     * 性别限制（0 不限 1 男 2 女）
     */
    private String sex;

    /**
     * 说明
     */
    private String remark;

    /**
     * 注意事项
     */
    private String attention;

    /**
     * 组内检查项
     */
    private List<CheckitemVo> checkItems;

    /**
     * 扩展信息（08 §2.4）
     */
    private com.xinfei.health.appointment.domain.HealthCheckgroupExt ext;

}
