package com.xinfei.health.appointment.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.xinfei.health.appointment.domain.Checkitem;
import com.xinfei.health.appointment.domain.HealthCheckitemExt;
import com.xinfei.health.appointment.domain.HealthCheckitemRef;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 检查项视图对象
 *
 * @author xinfei
 */
@Data
@AutoMapper(target = Checkitem.class)
public class CheckitemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 检查项编码
     */
    private String code;

    /**
     * 检查项名称
     */
    private String name;

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
     * 类型（1 检查 2 检验）
     */
    private String type;

    /**
     * 注意事项
     */
    private String attention;

    /**
     * 说明
     */
    private String remark;

    /**
     * 扩展信息（08 §4.1）
     */
    private HealthCheckitemExt ext;

    /**
     * 参考区间（08 §2.3）
     */
    private List<HealthCheckitemRef> refs;

}
