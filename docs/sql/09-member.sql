-- =============================================================================
-- 9. 会员档案侧建表 + 菜单（07 §2.2~2.5、§2.12、§3.1、§5.8；B6，2026-10-24）
-- 范围：会员/档案 6 张表 + 会员管理菜单族；报告侧（07 §2.6~2.11）留 B7，不在本文件。
-- 约定：04 §4 —— 雪花 BIGINT 主键、审计 6 列、del_flag 逻辑删除、无物理外键、无 tenant_id。
-- 幂等：CREATE TABLE IF NOT EXISTS；菜单先删后插（按 menu_id 显式范围）。
-- 安全：仅内网 dev 库执行；禁止生产（04 §7、红线）。
-- =============================================================================

-- 9.1 一号制档案号（07 §2.2；唯一性只认本表，t_member.fileNumber 仅冗余展示）
CREATE TABLE IF NOT EXISTS `t_health_member_archive_no` (
  `id`          bigint       NOT NULL                COMMENT '雪花主键',
  `member_id`   int          NOT NULL                COMMENT '关联 t_member.id，一人一号',
  `file_number` varchar(32)  NOT NULL                COMMENT '档案号：FA+6位区划+yyyyMMdd+5位序号（04-config 三键配置）',
  `bind_source` varchar(12)  NOT NULL                COMMENT '建档来源（字典 health_bind_source）：注册/团检导入/人工建档',
  `remark`      varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept` bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_archive_no_member` (`member_id`),
  UNIQUE KEY `uk_archive_no_number` (`file_number`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员档案号（一号制）';

-- 9.2 动态信息/档案主体（07 §2.3，1:1 t_member；member_status 停用标记唯一落点 §5.8）
CREATE TABLE IF NOT EXISTS `t_health_member_profile` (
  `id`                   bigint       NOT NULL                COMMENT '雪花主键',
  `member_id`            int          NOT NULL                COMMENT '关联 t_member.id',
  -- 体格
  `height`               decimal(6,1)  DEFAULT NULL           COMMENT '身高 cm',
  `weight`               decimal(8,1)  DEFAULT NULL           COMMENT '体重 kg',
  `bmi`                  decimal(7,2)  DEFAULT NULL           COMMENT 'BMI，服务端计算，不信任前端值',
  `waist`                decimal(6,1)  DEFAULT NULL           COMMENT '腰围 cm',
  `hip`                  decimal(6,1)  DEFAULT NULL           COMMENT '臀围 cm',
  `whtr`                 decimal(7,4)  DEFAULT NULL           COMMENT '腰臀比',
  `blood_pressure_high`  smallint      DEFAULT NULL           COMMENT '收缩压 mmHg，尾数须为偶（6007）',
  `blood_pressure_low`   smallint      DEFAULT NULL           COMMENT '舒张压 mmHg，尾数须为偶（6007）',
  `heart_rate`           smallint      DEFAULT NULL           COMMENT '心率 次/分',
  `vision_left`          decimal(5,2)  DEFAULT NULL           COMMENT '左眼视力',
  `vision_right`         decimal(5,2)  DEFAULT NULL           COMMENT '右眼视力',
  `color_vision`         varchar(12)   DEFAULT NULL           COMMENT '色觉',
  -- 基本
  `blood_type`           varchar(4)    DEFAULT NULL           COMMENT '血型（字典 health_blood_type）',
  `marital_status`       char(1)       DEFAULT NULL           COMMENT '婚姻（字典 health_marital_status）：0未知/1未婚/2已婚/3离异/4丧偶',
  `sex_history`          char(1)       DEFAULT NULL           COMMENT '性生活史（字典 health_sex_history）：0未知/1有/2无，18 E3 判据',
  `pregnancy_status`     char(1)       DEFAULT NULL           COMMENT '妊娠状态（字典 health_pregnancy_status）：0无/1怀孕/2备孕/3未知，18 E1/E2 判据',
  `menstruation_flag`    char(1)       DEFAULT NULL           COMMENT '1 经期（18 E4 判据，须带日期）',
  `menstruation_date`    date          DEFAULT NULL           COMMENT '末次经期首日',
  `occupation`           varchar(64)   DEFAULT NULL           COMMENT '职业',
  `edu_level`            varchar(32)   DEFAULT NULL           COMMENT '文化程度',
  `job_category`         varchar(32)   DEFAULT NULL           COMMENT '职业类别',
  `address`              varchar(255)  DEFAULT NULL           COMMENT '住址',
  `phone_self`           varchar(16)   DEFAULT NULL           COMMENT '本人电话',
  `phone_contact`        varchar(16)   DEFAULT NULL           COMMENT '联系电话',
  `contact_name`         varchar(32)   DEFAULT NULL           COMMENT '紧急联系人',
  -- 生活方式
  `smoking`              char(1)       DEFAULT NULL           COMMENT '吸烟：0不吸/1吸/2已戒',
  `smoking_count`        smallint      DEFAULT NULL           COMMENT '吸烟量 支/日',
  `drinking`             char(1)       DEFAULT NULL           COMMENT '饮酒：0不饮/1饮/2已戒',
  `drinking_count`       smallint      DEFAULT NULL           COMMENT '饮酒量 两/日',
  `exercise_freq`        smallint      DEFAULT NULL           COMMENT '运动频次 次/周',
  `exercise_time`        smallint      DEFAULT NULL           COMMENT '运动时长 分钟/次',
  `diet_habit`           varchar(32)   DEFAULT NULL           COMMENT '饮食习惯（字典 health_diet_type）',
  `sleep_hours`          decimal(4,1)  DEFAULT NULL           COMMENT '睡眠时长 小时',
  `salt_level`           char(1)       DEFAULT NULL           COMMENT '口味咸淡（字典 health_salt_intake，10 随访同值域）',
  `psych_adjust`         char(1)       DEFAULT NULL           COMMENT '心理调适（字典 health_psych_adjust）',
  `compliance`           char(1)       DEFAULT NULL           COMMENT '医嘱依从（字典 health_medical_compliance）',
  -- 既往（标志位，明细走 t_health_member_history）
  `surgeon_history_flag` char(1)       DEFAULT NULL           COMMENT '1 手术史',
  `trauma_flag`          char(1)       DEFAULT NULL           COMMENT '1 外伤史',
  `transfusion_flag`     char(1)       DEFAULT NULL           COMMENT '1 输血史',
  `drug_allergy_flag`    char(1)       DEFAULT NULL           COMMENT '1 药物过敏史',
  `menarche_age`         smallint      DEFAULT NULL           COMMENT '初潮年龄（女）',
  `menopause_age`        smallint      DEFAULT NULL           COMMENT '绝经年龄（女）',
  `parity_count`         smallint      DEFAULT NULL           COMMENT '生育次数',
  -- 状态与时效
  `member_status`        char(1)       NOT NULL DEFAULT '0'   COMMENT '会员状态（sys_normal_disable）：0 正常 / 1 停用（§5.8 唯一落点）',
  `data_time`            datetime      DEFAULT NULL           COMMENT '本条数据采集时间（≠建档时间，18 D11 历次对比依赖）',
  `remark`               varchar(255)  DEFAULT NULL           COMMENT '备注',
  `create_dept`          bigint        DEFAULT NULL           COMMENT '创建部门',
  `create_by`            bigint        DEFAULT NULL           COMMENT '创建人',
  `create_time`          datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`            bigint        DEFAULT NULL           COMMENT '更新人',
  `update_time`          datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`             char(1)       NOT NULL DEFAULT '0'   COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_member` (`member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员动态信息（1:1）';

-- 9.3 缺项标记（07 §2.4；留空=未采集，本表行=已确认缺失，二者禁止混同）
CREATE TABLE IF NOT EXISTS `t_health_member_missing` (
  `id`          bigint       NOT NULL                COMMENT '雪花主键',
  `member_id`   int          NOT NULL                COMMENT '关联 t_member.id',
  `field_key`   varchar(64)  NOT NULL                COMMENT '缺失字段名或病史类型（如 history.family）',
  `reason`      varchar(255) DEFAULT NULL            COMMENT '缺失原因：受检者不知/拒绝提供/历史遗失',
  `marked_by`   varchar(32)  DEFAULT NULL            COMMENT '标记人',
  `mark_time`   datetime     DEFAULT NULL            COMMENT '标记时间',
  `create_dept` bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_missing_member_field` (`member_id`, `field_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '档案缺项标记';

-- 9.4 病史（07 §2.5，1:N 按 type 分组）
CREATE TABLE IF NOT EXISTS `t_health_member_history` (
  `id`            bigint       NOT NULL                COMMENT '雪花主键',
  `member_id`     int          NOT NULL                COMMENT '关联 t_member.id',
  `type`          varchar(12)  NOT NULL                COMMENT 'disease/family/allergy/surgery/medication',
  `content`       varchar(500) NOT NULL                COMMENT '自由文本描述',
  `disease_code`  varchar(32)  DEFAULT NULL            COMMENT '引用 t_health_disease（11，B8 后启用）',
  `relation`      varchar(12)  DEFAULT NULL            COMMENT '家族史亲属关系（字典 health_relation）',
  `allergen`      varchar(64)  DEFAULT NULL            COMMENT '过敏原',
  `allergy_type`  varchar(32)  DEFAULT NULL            COMMENT '过敏类型',
  `severity`      varchar(12)  DEFAULT NULL            COMMENT '严重程度（字典 health_severity）',
  `surgery_name`  varchar(128) DEFAULT NULL            COMMENT '手术名称',
  `surgery_date`  date         DEFAULT NULL            COMMENT '手术日期',
  `hospital`      varchar(128) DEFAULT NULL            COMMENT '就诊医院',
  `drug_name`     varchar(128) DEFAULT NULL            COMMENT '药品名称',
  `drug_dose`     varchar(64)  DEFAULT NULL            COMMENT '剂量',
  `drug_freq`     varchar(64)  DEFAULT NULL            COMMENT '频次',
  `start_date`    date         DEFAULT NULL            COMMENT '用药开始日期',
  `end_date`      date         DEFAULT NULL            COMMENT '用药结束日期',
  `happen_date`   date         DEFAULT NULL            COMMENT '发生日期',
  `confirm_flag`  char(1)      NOT NULL DEFAULT '1'    COMMENT '1 已复核 / 0 待复核（C 端自填默认 0）',
  `remark`        varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept`   bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`   datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`      char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  KEY `idx_history_member_type` (`member_id`, `type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员病史';

-- 9.5 会员标签（07 §2.12）
CREATE TABLE IF NOT EXISTS `t_health_member_tag` (
  `id`          bigint       NOT NULL                COMMENT '雪花主键',
  `name`        varchar(32)  NOT NULL                COMMENT '标签名，唯一',
  `type`        varchar(12)  NOT NULL DEFAULT 'manual' COMMENT 'manual/auto/crowd',
  `color`       varchar(16)  DEFAULT NULL            COMMENT '展示色',
  `rule_json`   text                                 COMMENT '自动标签条件（B6 只存不解析）',
  `enabled`     char(1)      NOT NULL DEFAULT '1'    COMMENT '1 启用 / 0 停用',
  `remark`      varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept` bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员标签';

-- 9.6 会员-标签关联（07 §2.12）
CREATE TABLE IF NOT EXISTS `t_health_member_tag_rel` (
  `id`          bigint       NOT NULL                COMMENT '雪花主键',
  `tag_id`      bigint       NOT NULL                COMMENT '关联 t_health_member_tag.id',
  `member_id`   int          NOT NULL                COMMENT '关联 t_member.id',
  `source`      varchar(12)  NOT NULL DEFAULT '人工' COMMENT '评估/报告/人工',
  `source_id`   bigint       DEFAULT NULL            COMMENT '来源对象（评估/报告 id）',
  `create_dept` bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_rel_tag_member` (`tag_id`, `member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员-标签关联';

-- -----------------------------------------------------------------------------
-- 9.7 菜单族迁移说明（B7 纠偏，2026-10-24）
--     B6 曾把会员菜单放在 2070~2079，与 12 §5.1「07 占 2100~2199」冲突。
--     B7 已按文档段位迁至 2100（会员管理）/ 2110（会员档案）+ 2111~2118 按钮，
--     菜单 INSERT 统一由 10-report.sql §10.9 产出；本文件只清理历史 2070 段。
-- -----------------------------------------------------------------------------
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 2070 AND 2079;

-- 9.8 自检
SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'xfjk' AND TABLE_NAME LIKE 't_health_member%' ORDER BY TABLE_NAME;
SELECT menu_id, menu_name, menu_type, perms FROM sys_menu WHERE menu_id BETWEEN 2100 AND 2118 ORDER BY menu_id;
