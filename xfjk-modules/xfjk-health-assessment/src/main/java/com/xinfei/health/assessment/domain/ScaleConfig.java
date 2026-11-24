package com.xinfei.health.assessment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xinfei.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 量表阈值配置 t_health_scale_config（09 §2.6，18 A13）
 *
 * @author xinfei
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_health_scale_config")
public class ScaleConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    @TableField("value_type")
    private String valueType;

    @TableField("source_ref")
    private String sourceRef;

    @TableField("cfg_version")
    private String cfgVersion;

    @TableLogic
    @TableField("del_flag")
    private String delFlag;

    public static final String VALUE_INT = "int";
    public static final String VALUE_DECIMAL = "decimal";
    public static final String VALUE_STRING = "string";
    public static final String VALUE_JSON = "json";

}
