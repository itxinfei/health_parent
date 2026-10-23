package com.xinfei.health.appointment.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.xinfei.health.appointment.domain.Setmeal;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐视图对象
 *
 * <p>checkGroups / imgUrl 仅在详情场景填充，列表查询为 null；
 * imgUrl 由 img（OSS 主键）解析而来，旧种子数据存的是七牛文件名，解析不出时为空。
 *
 * @author xinfei
 */
@Data
@AutoMapper(target = Setmeal.class)
public class SetmealVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐编码
     */
    private String code;

    /**
     * 助记码
     */
    private String helpCode;

    /**
     * 性别限制（0 不限 1 男 2 女）
     */
    private String sex;

    /**
     * 年龄范围
     */
    private String age;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 说明
     */
    private String remark;

    /**
     * 注意事项
     */
    private String attention;

    /**
     * 图片 OSS 主键（原样存库值）
     */
    private String img;

    /**
     * 图片可访问地址
     */
    private String imgUrl;

    /**
     * 套餐包含的检查组（三级详情）
     */
    private List<CheckgroupVo> checkGroups;

    /**
     * 扩展信息（08 §2.5）
     */
    private com.xinfei.health.appointment.domain.HealthSetmealExt ext;

}
