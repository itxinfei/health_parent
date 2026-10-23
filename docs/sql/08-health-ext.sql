-- -----------------------------------------------------------------------------
-- 8. 健康档案扩展表（08 §2.2~2.5，非冻结表）
--    ext 与参考区间子表：支撑结果类型、科室、标本、空腹/放射/必检标记、
--    参考区间多套匹配（18 D2）与检前须知（13）。
-- -----------------------------------------------------------------------------

-- 8.1 检查项元数据（1:1 t_checkitem，08 §2.2）
CREATE TABLE IF NOT EXISTS `t_health_checkitem_ext` (
  `id`                    bigint       NOT NULL                COMMENT '雪花主键',
  `checkitem_id`          int          NOT NULL                COMMENT '关联 t_checkitem.id',
  `result_type`           char(1)      NOT NULL                COMMENT '结果类型：0 数值 / 1 定性 / 2 文本描述 / 3 影像结论',
  `unit`                  varchar(32)  DEFAULT NULL            COMMENT '单位；result_type=0 时必填',
  `dept_id`               bigint       NOT NULL                COMMENT '所属科室，关联 sys_dept',
  `specimen`              varchar(32)  DEFAULT NULL            COMMENT '标本类型（字典 health_specimen）',
  `is_fasting`            char(1)      NOT NULL DEFAULT '0'    COMMENT '1 需空腹',
  `is_radiation`          char(1)      NOT NULL DEFAULT '0'    COMMENT '1 含射线',
  `is_required`           char(1)      NOT NULL DEFAULT '0'    COMMENT '1 必检，不允许弃检',
  `dual_dept_flag`        char(1)      NOT NULL DEFAULT '0'    COMMENT '1 需两科室分别分检',
  `sort_no`               int          NOT NULL DEFAULT 0      COMMENT '组内排序',
  `default_conclusion_id` bigint       DEFAULT NULL            COMMENT '默认结论词，引用 t_health_conclusion_dict.id',
  `notice_text`           varchar(500) DEFAULT NULL            COMMENT '检前须知条目',
  `create_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkitem_ext_item` (`checkitem_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检查项元数据扩展';

-- 8.2 参考区间（1:N，08 §2.3；匹配不到不得默认判正常，18 D2）
CREATE TABLE IF NOT EXISTS `t_health_checkitem_ref` (
  `id`           bigint        NOT NULL                COMMENT '雪花主键',
  `checkitem_id` int           NOT NULL                COMMENT '关联 t_checkitem.id',
  `sex`          char(1)       NOT NULL DEFAULT '0'    COMMENT '0 通用 / 1 男 / 2 女',
  `age_min`      int           DEFAULT NULL            COMMENT '年龄下限闭区间，NULL 不限',
  `age_max`      int           DEFAULT NULL            COMMENT '年龄上限闭区间，NULL 不限',
  `physio_state` char(1)       NOT NULL DEFAULT '0'    COMMENT '0 通用 / 1 妊娠 / 2 备孕',
  `ref_text`     varchar(128)  NOT NULL                COMMENT '区间原文，报告直接展示',
  `ref_min`      decimal(12,4) DEFAULT NULL            COMMENT '下限（定性项可空）',
  `ref_max`      decimal(12,4) DEFAULT NULL            COMMENT '上限（定性项可空）',
  `priority`     int           NOT NULL DEFAULT 100    COMMENT '多条命中取最小值',
  `create_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `uk_checkitem_ref_item` (`checkitem_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检查项参考区间';

-- 8.3 检查组元数据（1:1 t_checkgroup，08 §2.4）
CREATE TABLE IF NOT EXISTS `t_health_checkgroup_ext` (
  `id`                    bigint      NOT NULL                COMMENT '雪花主键',
  `checkgroup_id`         int         NOT NULL                COMMENT '关联 t_checkgroup.id',
  `dept_id`               bigint      NOT NULL                COMMENT '承检科室',
  `specimen`              varchar(32) DEFAULT NULL            COMMENT '标本',
  `dual_dept_flag`        char(1)     NOT NULL DEFAULT '0'    COMMENT '1 需双科室分检',
  `is_fasting`            char(1)     NOT NULL DEFAULT '0'    COMMENT '组级空腹标记',
  `default_conclusion_id` bigint      DEFAULT NULL            COMMENT '组级默认结论词',
  `sort_no`               int         NOT NULL DEFAULT 0      COMMENT '套餐内顺序（指引单打印序）',
  `create_time`           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkgroup_ext_group` (`checkgroup_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检查组元数据扩展';

-- 8.4 套餐扩展（1:1 t_setmeal，08 §2.5）
CREATE TABLE IF NOT EXISTS `t_health_setmeal_ext` (
  `id`                    bigint        NOT NULL                COMMENT '雪花主键',
  `setmeal_id`            int           NOT NULL                COMMENT '关联 t_setmeal.id',
  `sex`                   char(1)       NOT NULL DEFAULT '0'    COMMENT '0 不限 / 1 男 / 2 女',
  `age_min`               int           DEFAULT NULL            COMMENT '适用年龄下限',
  `age_max`               int           DEFAULT NULL            COMMENT '适用年龄上限',
  `married_limit`         char(1)       NOT NULL DEFAULT '0'    COMMENT '0 不限 / 1 限已婚 / 2 限未婚',
  `has_sex_history`       char(1)       NOT NULL DEFAULT '0'    COMMENT '0 不限 / 1 需有性生活史 / 2 需无性生活史（18 E3）',
  `pregnancy_warn`        char(1)       NOT NULL DEFAULT '0'    COMMENT '1 含放射/妊娠禁忌项，下单须警示（18 E1）',
  `suitable_desc`         varchar(500)  DEFAULT NULL            COMMENT '适宜人群展示文案，不得作为校验依据（18 E11）',
  `default_conclusion_id` bigint        DEFAULT NULL            COMMENT '默认正常结论词（18 F9）',
  `default_advice_id`     bigint        DEFAULT NULL            COMMENT '默认正常处理意见模板',
  `notice_text`           varchar(1000) DEFAULT NULL            COMMENT '检前须知（13 C 端展示）',
  `create_time`           datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setmeal_ext_setmeal` (`setmeal_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '套餐扩展';
