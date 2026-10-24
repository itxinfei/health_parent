-- =============================================================================
-- 10. 报告侧建表 + 菜单（07 §2.6~2.11、§3.2、§4、§5.3~5.6；B7，2026-10-24）
-- 范围：6 张 t_health_report* / t_health_critical_log + 结论词库最小集（11 §2.1 前置）
-- 约定：同 09-member.sql —— 雪花 BIGINT 主键、审计 6 列、del_flag 逻辑删除、无物理外键、无 tenant_id。
-- 幂等：CREATE TABLE IF NOT EXISTS；菜单先删后插（2100~2199，12 §5.1 段位）。
-- 安全：仅内网 dev 库执行；禁止生产（04 §7、红线）。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 10.1 结论词库（11 §2.1 的 07 必需子集；B7 先建，B8 补全疾病/建议关联）
--     07 §5.4 要求结论条目必须引用本表；自由文本仅允许 other 类型。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_conclusion_dict` (
  `id`                  bigint       NOT NULL                COMMENT '雪花主键',
  `code`                varchar(64)  NOT NULL                COMMENT '结论词编码，全局唯一',
  `term`                varchar(128) NOT NULL                COMMENT '统一用词（18 F8）',
  `conclusion_type`     varchar(16)  NOT NULL                COMMENT 'disease 疾病诊断 / positive 阳性发现 / other 其他结论',
  `dept_scope`          varchar(255) DEFAULT NULL            COMMENT '适用科室码逗号串，NULL=全科室',
  `abnormal_grade`      varchar(8)   DEFAULT NULL            COMMENT 'A 需立即干预 / B 需进一步检查；正常结论为 NULL',
  `normal_flag`         tinyint      NOT NULL DEFAULT 0      COMMENT '1 属正常结论候选（18 F9）',
  `major_positive_flag` tinyint      NOT NULL DEFAULT 0      COMMENT '1 重大阳性发现候选（18 D7）',
  `merge_group_code`    varchar(32)  DEFAULT NULL            COMMENT '合并组码（18 D9），07 两表的 merge_group_id 存本列值',
  `merge_target_code`   varchar(64)  DEFAULT NULL            COMMENT '合并后落地的结论词 code',
  `synonyms`            varchar(512) DEFAULT NULL            COMMENT '同义词逗号串',
  `basis`               varchar(1024) DEFAULT NULL           COMMENT '结论依据说明',
  `icd_code`            varchar(32)  DEFAULT NULL            COMMENT '人工填写疾病编码',
  `health_education_id` bigint       DEFAULT NULL            COMMENT '关联 t_health_disease（11）',
  `default_advice_id`   bigint       DEFAULT NULL            COMMENT '默认处理意见模板（11 t_health_advice_template）',
  `critical_note`       varchar(255) DEFAULT NULL            COMMENT '危急提示语（判定线在 12 配置）',
  `usage_count`         int          NOT NULL DEFAULT 0      COMMENT '被引用次数冗余',
  `status`              varchar(16)  NOT NULL DEFAULT 'published' COMMENT 'draft/published/disabled',
  `sort`                int          DEFAULT 0               COMMENT '排序',
  `remark`              varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept`         bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`           bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`         datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`            char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conclusion_code` (`code`),
  KEY `idx_conclusion_term` (`term`),
  KEY `idx_conclusion_type_status` (`conclusion_type`, `status`),
  KEY `idx_conclusion_merge_group` (`merge_group_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结论词库（11 §2.1 子集）';

-- -----------------------------------------------------------------------------
-- 10.2 报告主表（07 §2.6）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_report` (
  `id`                bigint       NOT NULL                COMMENT '雪花主键',
  `report_no`         varchar(32)  NOT NULL                COMMENT '报告号：RP+yyyyMMdd+5位序号（04-config health:number:report_prefix）',
  `member_id`         int          NOT NULL                COMMENT '关联 t_member.id',
  `archive_no`        varchar(32)  NOT NULL                COMMENT '冗余档案号，历次对比主关联键（18 F5/D11）',
  `order_id`          int          DEFAULT NULL            COMMENT '来源订单（08），外部导入可为空',
  `exam_no`           varchar(32)  DEFAULT NULL            COMMENT '体检号',
  `batch_id`          bigint       DEFAULT NULL            COMMENT '团检批次（20），非团检为空',
  `check_date`        date         NOT NULL                COMMENT '体检日期',
  `dept_summary_text` text                                 COMMENT '第一段：科室小结汇总（18 F6）',
  `abnormal_summary`  text                                 COMMENT '第二段：异常汇总（18 D8）',
  `conclusion`        varchar(2000) DEFAULT NULL           COMMENT '第三段：总检结论整段文本',
  `basis`             text                                 COMMENT '第四段：依据',
  `advice`            text                                 COMMENT '第五段：健康建议',
  `conclusion_type`   varchar(12)  DEFAULT NULL            COMMENT '报告级结论归类（18 F14，本期前三项）',
  `conclusion_final`  varchar(2000) DEFAULT NULL           COMMENT '总检医生末次修改的结论（18 F7）',
  `conclusion_json`   text                                 COMMENT '末次结论条目化内容 [{dictId,type,grade,adviceIds,itemIds}]',
  `summary`           varchar(2000) DEFAULT NULL           COMMENT '打印用正文快照',
  `doctor`            varchar(32)  DEFAULT NULL            COMMENT '主检医师姓名（展示）',
  `file_url`          varchar(255) DEFAULT NULL            COMMENT '附件 ossId（走 OSS 抽象）',
  `source`            varchar(12)  NOT NULL                COMMENT '上传/接口/解析/团检/手工录入',
  `status`            varchar(16)  NOT NULL                COMMENT '待创建/检查中/待总检/已总检/已打印/已发放',
  `first_check_by`    varchar(32)  DEFAULT NULL            COMMENT '科室自查责任人（18 C8）',
  `first_check_time`  datetime     DEFAULT NULL            COMMENT '科室自查时间',
  `summary_by`        varchar(32)  DEFAULT NULL            COMMENT '主检审核责任人',
  `summary_time`      datetime     DEFAULT NULL            COMMENT '主检审核时间',
  `final_check_by`    varchar(32)  DEFAULT NULL            COMMENT '终审签发责任人',
  `final_check_time`  datetime     DEFAULT NULL            COMMENT '终审签发时间',
  `revoke_flag`       char(1)      NOT NULL DEFAULT '0'    COMMENT '1 已撤销（18 C9）；撤销期间科室方可改结果',
  `revoke_reason`     varchar(255) DEFAULT NULL            COMMENT '撤销原因',
  `revoke_by`         varchar(32)  DEFAULT NULL            COMMENT '撤销人',
  `revoke_time`       datetime     DEFAULT NULL            COMMENT '撤销时间',
  `sign_file_id`      varchar(255) DEFAULT NULL            COMMENT '电子签名/签章 ossId（18 F11）',
  `watermark_flag`    char(1)      NOT NULL DEFAULT '0'    COMMENT '发放件水印开关（18 F11）',
  `source_report_id`  bigint       DEFAULT NULL            COMMENT '上一次报告（按 archive_no 关联，18 D11/F5）',
  `remark`            varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept`       bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`         bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`       datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`          char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_report_archive_no` (`archive_no`),
  KEY `idx_report_member` (`member_id`),
  KEY `idx_report_check_date` (`check_date`),
  KEY `idx_report_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '体检报告主表';

-- -----------------------------------------------------------------------------
-- 10.3 报告明细（07 §2.7；判定值只由服务端产出，禁止前端传入）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_report_item` (
  `id`                 bigint       NOT NULL                COMMENT '雪花主键',
  `report_id`          bigint       NOT NULL                COMMENT '关联 t_health_report.id',
  `checkitem_id`       int          DEFAULT NULL            COMMENT '关联 t_checkitem.id；外部项目为空',
  `dept_id`            bigint       NOT NULL                COMMENT '承检科室（取自 t_health_checkitem_ext）',
  `item_name`          varchar(64)  NOT NULL                COMMENT '结果快照名',
  `result_type`        char(1)      NOT NULL                COMMENT '0 数值 / 1 定性 / 2 文本 / 3 影像',
  `result`             varchar(500) NOT NULL                COMMENT '结果值',
  `unit`               varchar(32)  DEFAULT NULL            COMMENT '单位',
  `ref_range`          varchar(128) DEFAULT NULL            COMMENT '参考区间原文快照',
  `ref_min`            decimal(12,4) DEFAULT NULL           COMMENT '判定用下限快照',
  `ref_max`            decimal(12,4) DEFAULT NULL           COMMENT '判定用上限快照',
  `ref_configured`     char(1)      NOT NULL DEFAULT '0'    COMMENT '0 区间未配置：只存原值，abnormal_flag=3（18 D2）',
  `abnormal_flag`      char(1)      NOT NULL DEFAULT '3'    COMMENT '0正常/1偏高/2偏低/3异常/4阳性/5阴性/6弱阳性',
  `qualitative`        char(1)      DEFAULT NULL            COMMENT '定性：1阳性/2阴性/3弱阳性',
  `edge_flag`          char(1)      NOT NULL DEFAULT '0'    COMMENT '1 临界值，须给复查提示（18 D3）',
  `abnormal_grade`     char(1)      DEFAULT NULL            COMMENT 'A 需立即干预 / B 需进一步检查（18 D6）',
  `critical_flag`      char(1)      NOT NULL DEFAULT '0'    COMMENT '1 危急值（18 D4），触发必写 t_health_critical_log',
  `major_positive`     char(1)      NOT NULL DEFAULT '0'    COMMENT '1 重大阳性（18 D7）',
  `item_status`        varchar(8)   NOT NULL DEFAULT '未检'  COMMENT '未检/已检/待查/弃检/延期（18 C1）',
  `discard_reason`     varchar(255) DEFAULT NULL            COMMENT 'item_status=弃检 时必填（6009）',
  `merge_group_id`     varchar(32)  DEFAULT NULL            COMMENT '多异常合并组（18 D9）',
  `prev_result`        varchar(500) DEFAULT NULL            COMMENT '上次结果快照',
  `prev_abnormal_flag` char(1)      DEFAULT NULL            COMMENT '上次判定快照',
  `prev_ref_range`     varchar(128) DEFAULT NULL            COMMENT '上次区间快照',
  `dual_dept_flag`     char(1)      NOT NULL DEFAULT '0'    COMMENT '1 需双科室分检（18 C3）',
  `peer_dept_status`   varchar(8)   DEFAULT NULL            COMMENT '对侧科室状态',
  `sort_no`            int          NOT NULL DEFAULT 0      COMMENT '指引单/报告打印顺序',
  `doctor`             varchar(32)  DEFAULT NULL            COMMENT '录入人',
  `check_time`         datetime     DEFAULT NULL            COMMENT '录入时间',
  `check_device`       varchar(64)  DEFAULT NULL            COMMENT '设备（人工填写，用于溯源）',
  `remark`             varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept`        bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`          bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`        datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`           char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  KEY `idx_report_item_report` (`report_id`),
  KEY `idx_report_item_dept` (`dept_id`),
  KEY `idx_report_item_checkitem` (`checkitem_id`),
  KEY `idx_report_item_critical` (`critical_flag`, `report_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报告明细';

-- -----------------------------------------------------------------------------
-- 10.4 科室小结（07 §2.8，一报告一科室一行）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_dept_summary` (
  `id`               bigint       NOT NULL                COMMENT '雪花主键',
  `report_id`        bigint       NOT NULL                COMMENT '关联 t_health_report.id',
  `dept_id`          bigint       NOT NULL                COMMENT '关联 sys_dept.id',
  `summary_text`     text                                 COMMENT '科室结论正文（提交时必填，6010）',
  `print_label`      varchar(64)  DEFAULT NULL            COMMENT '科室小结打印标签（18 F10）',
  `last_modify_by`   varchar(32)  DEFAULT NULL            COMMENT '最后修改人（18 F10）',
  `last_modify_time` datetime     DEFAULT NULL            COMMENT '最后修改时间',
  `submit_flag`      char(1)      NOT NULL DEFAULT '0'    COMMENT '1 已提交；提交后受 18 C9 保护',
  `send_back_flag`   char(1)      NOT NULL DEFAULT '0'    COMMENT '1 总检打回（18 C9）',
  `send_back_reason` varchar(255) DEFAULT NULL            COMMENT '打回原因',
  `create_dept`      bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`        bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`      datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`         char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_summary_report_dept` (`report_id`, `dept_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科室小结';

-- -----------------------------------------------------------------------------
-- 10.5 结论条目（07 §2.9；仅一组 final_flag=1 为末次，18 F7）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_report_conclusion` (
  `id`                 bigint       NOT NULL                COMMENT '雪花主键',
  `report_id`          bigint       NOT NULL                COMMENT '关联 t_health_report.id',
  `conclusion_dict_id` bigint       NOT NULL                COMMENT '引用 t_health_conclusion_dict.id（6011）',
  `conclusion_type`    varchar(12)  NOT NULL                COMMENT '疾病诊断/阳性发现/其他结论',
  `item_ids`           varchar(1000) DEFAULT NULL           COMMENT '来源明细 id 列表',
  `advice_ids`         varchar(500) DEFAULT NULL            COMMENT '关联处理意见模板 id 列表',
  `grade`              char(1)      DEFAULT NULL            COMMENT '异常分级 A/B（18 D6）',
  `merge_group_id`     varchar(32)  DEFAULT NULL            COMMENT '合并组（18 D9）',
  `final_flag`         char(1)      NOT NULL DEFAULT '0'    COMMENT '1 末次修改结论，同报告仅一组为 1',
  `sort_no`            int          NOT NULL DEFAULT 0      COMMENT '展示顺序',
  `create_dept`        bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`          bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`        datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`           char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  KEY `idx_conclusion_report` (`report_id`, `final_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报告结论条目';

-- -----------------------------------------------------------------------------
-- 10.6 归档附属资料（07 §2.10；影像资料本期只存导出件）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_report_archive` (
  `id`          bigint       NOT NULL                COMMENT '雪花主键',
  `report_id`   bigint       NOT NULL                COMMENT '关联 t_health_report.id',
  `type`        varchar(24)  NOT NULL                COMMENT '知情同意书/检前问卷/既往报告/影像资料/心电图/病理报告',
  `oss_id`      bigint       DEFAULT NULL            COMMENT 'sys_oss.oss_id',
  `page_count`  int          DEFAULT NULL            COMMENT '页数',
  `upload_by`   varchar(32)  DEFAULT NULL            COMMENT '上传人',
  `upload_time` datetime     DEFAULT NULL            COMMENT '上传时间',
  `remark`      varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_dept` bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  KEY `idx_archive_report` (`report_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报告归档附属资料';

-- -----------------------------------------------------------------------------
-- 10.7 危急值登记（07 §2.11；18 D4~D5）
--     receiver_confirm_time / handle_result 允许为空：07 §3.2.1 以「是否为空」派生
--     confirmed / handled，§6.5 出参样例即 null（§2.11「必填」按登记完成态理解）。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_health_critical_log` (
  `id`                    bigint       NOT NULL                COMMENT '雪花主键',
  `report_item_id`        bigint       NOT NULL                COMMENT '触发项 t_health_report_item.id',
  `report_id`             bigint       NOT NULL                COMMENT '冗余报告 id',
  `member_id`             int          NOT NULL                COMMENT '冗余会员 id',
  `archive_no`            varchar(32)  NOT NULL                COMMENT '冗余档案号',
  `item_name`             varchar(64)  NOT NULL                COMMENT '检查项目（18 D5 四要素之一）',
  `critical_value`        varchar(128) NOT NULL                COMMENT '危急值结果',
  `rule_code`             varchar(32)  NOT NULL                COMMENT '命中规则：D4-WBC-LOW/HIGH、D4-HB-LOW/HIGH、D4-PLT-LOW/HIGH',
  `report_time`           datetime     NOT NULL                COMMENT '报告时间',
  `receiver`              varchar(32)  NOT NULL                COMMENT '接收医生姓名',
  `receiver_confirm_time` datetime     DEFAULT NULL            COMMENT '接收确认时间；空=未送达（18 D5）',
  `notify_way`            varchar(16)  NOT NULL DEFAULT '电话+系统推送' COMMENT '电话/系统推送/电话+系统推送（字典 health_notify_way）',
  `handle_result`         varchar(500) DEFAULT NULL            COMMENT '处置结果；空=未处理（计入 18 G5 分母）',
  `track_by`              varchar(32)  DEFAULT NULL            COMMENT '查访人',
  `track_time`            datetime     DEFAULT NULL            COMMENT '查访时间',
  `track_remark`          varchar(255) DEFAULT NULL            COMMENT '查访备注',
  `create_dept`           bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`             bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`             bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time`           datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`              char(1)      NOT NULL DEFAULT '0'    COMMENT '0 存在 / 2 删除',
  PRIMARY KEY (`id`),
  KEY `idx_critical_report` (`report_id`),
  KEY `idx_critical_rule` (`rule_code`),
  KEY `idx_critical_time` (`report_time`),
  KEY `idx_critical_item` (`report_item_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '危急值登记';

-- -----------------------------------------------------------------------------
-- 10.8 结论词种子（11 §2.1；B7 只种常用十条，B8 在知识库页维护其余）
-- -----------------------------------------------------------------------------
INSERT INTO `t_health_conclusion_dict`
  (`id`, `code`, `term`, `conclusion_type`, `abnormal_grade`, `normal_flag`, `major_positive_flag`, `merge_group_code`, `synonyms`, `basis`, `status`, `sort`, `create_dept`, `create_by`, `create_time`)
VALUES
  (1949000000000000001, 'C-NORMAL',    '未见异常',        'other',    NULL, 1, 0, NULL,      '正常,无异常',       '本次体检各项检查结果均在参考范围内', 'published', 10, 103, 1, NOW()),
  (1949000000000000002, 'C-FAT-LIVER', '脂肪肝',          'disease',  'B',  0, 0, 'MG-LIPID', '轻度脂肪肝,中度脂肪肝', '腹部超声提示肝实质回声增强，结合血脂结果', 'published', 20, 103, 1, NOW()),
  (1949000000000000003, 'C-HYPERTENSION', '血压偏高',     'positive', 'B',  0, 0, 'MG-BP',    '高血压,血压高',     '收缩压或舒张压高于参考上限', 'published', 30, 103, 1, NOW()),
  (1949000000000000004, 'C-HYPERLIPIDEMIA', '血脂异常',   'disease',  'B',  0, 0, 'MG-LIPID', '高血脂,胆固醇偏高', '总胆固醇/甘油三酯/低密度脂蛋白任一项超出参考范围', 'published', 40, 103, 1, NOW()),
  (1949000000000000005, 'C-HYPERGLYCEMIA', '血糖偏高',    'positive', 'B',  0, 0, 'MG-GLU',   '血糖高,空腹血糖受损', '空腹血糖高于参考上限', 'published', 50, 103, 1, NOW()),
  (1949000000000000006, 'C-ANEMIA',    '贫血',            'disease',  'B',  0, 0, 'MG-BLOOD', '血红蛋白偏低',      '血红蛋白低于参考下限', 'published', 60, 103, 1, NOW()),
  (1949000000000000007, 'C-LEUKOPENIA', '白细胞减少',     'disease',  'B',  0, 0, 'MG-BLOOD', '白细胞偏低',        '白细胞计数低于参考下限', 'published', 70, 103, 1, NOW()),
  (1949000000000000008, 'C-THYROID-NODULE', '甲状腺结节', 'positive', 'B',  0, 0, 'MG-THYROID', '甲状腺结节样变',  '甲状腺超声发现结节', 'published', 80, 103, 1, NOW()),
  (1949000000000000009, 'C-LIVER-FUNC', '肝功能异常',    'positive', 'B',  0, 0, 'MG-LIVER', '转氨酶偏高',        '谷丙/谷草转氨酶高于参考上限', 'published', 90, 103, 1, NOW()),
  (1949000000000000010, 'C-OTHER',     '其他结论',        'other',    NULL, 0, 0, NULL,      '补充说明',          NULL, 'published', 999, 103, 1, NOW())
ON DUPLICATE KEY UPDATE `term` = VALUES(`term`), `conclusion_type` = VALUES(`conclusion_type`),
  `abnormal_grade` = VALUES(`abnormal_grade`), `merge_group_code` = VALUES(`merge_group_code`),
  `synonyms` = VALUES(`synonyms`), `basis` = VALUES(`basis`), `status` = VALUES(`status`);

-- -----------------------------------------------------------------------------
-- 10.9 菜单族（12 §5.1 段位 2100~2199；B6 会员菜单于此纠偏到 2100/2110）
--     超级管理员绕过鉴权、测试角色 test1/test2 全量授权 ⇒ 不建 sys_role_menu 行。
-- -----------------------------------------------------------------------------
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 2100 AND 2199;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_dept`, `create_by`, `create_time`) VALUES
(2100, '会员管理', 0, 1, 'member', '', '', 1, 0, 'M', 0, 0, '', 'peoples', 103, 1, NOW()),
(2110, '会员档案', 2100, 1, 'members', 'health/member/index', '', 1, 0, 'C', 0, 0, 'health:member:list', 'user', 103, 1, NOW()),
(2111, '会员查询', 2110, 1, '#', '', '', 1, 0, 'F', 0, 0, 'health:member:query', '#', 103, 1, NOW()),
(2112, '会员新增', 2110, 2, '#', '', '', 1, 0, 'F', 0, 0, 'health:member:add', '#', 103, 1, NOW()),
(2113, '会员修改', 2110, 3, '#', '', '', 1, 0, 'F', 0, 0, 'health:member:edit', '#', 103, 1, NOW()),
(2114, '会员停用恢复', 2110, 4, '#', '', '', 1, 0, 'F', 0, 0, 'health:member:remove', '#', 103, 1, NOW()),
(2115, '会员导出', 2110, 5, '#', '', '', 1, 0, 'F', 0, 0, 'health:member:export', '#', 103, 1, NOW()),
(2116, '病史查询', 2110, 6, '#', '', '', 1, 0, 'F', 0, 0, 'health:memberhistory:list', '#', 103, 1, NOW()),
(2117, '病史维护', 2110, 7, '#', '', '', 1, 0, 'F', 0, 0, 'health:memberhistory:edit', '#', 103, 1, NOW()),
(2118, '标签设置', 2110, 8, '#', '', '', 1, 0, 'F', 0, 0, 'health:membertag:edit', '#', 103, 1, NOW()),
(2120, '体检报告', 2100, 2, 'reports', 'health/report/index', '', 1, 0, 'C', 0, 0, 'health:memberreport:list', 'documentation', 103, 1, NOW()),
(2121, '报告查询', 2120, 1, '#', '', '', 1, 0, 'F', 0, 0, 'health:memberreport:query', '#', 103, 1, NOW()),
(2122, '报告新增', 2120, 2, '#', '', '', 1, 0, 'F', 0, 0, 'health:memberreport:add', '#', 103, 1, NOW()),
(2123, '报告录入', 2120, 3, '#', '', '', 1, 0, 'F', 0, 0, 'health:memberreport:edit', '#', 103, 1, NOW()),
(2130, '总检工作台', 2100, 3, 'summary', 'health/summary/index', '', 1, 0, 'C', 0, 0, 'health:report:summary', 'edit', 103, 1, NOW()),
(2131, '总检提交', 2130, 1, '#', '', '', 1, 0, 'F', 0, 0, 'health:report:summary', '#', 103, 1, NOW()),
(2132, '总检撤销打回', 2130, 2, '#', '', '', 1, 0, 'F', 0, 0, 'health:report:revoke', '#', 103, 1, NOW()),
(2133, '终审签发', 2130, 3, '#', '', '', 1, 0, 'F', 0, 0, 'health:report:finalcheck', '#', 103, 1, NOW()),
(2140, '危急值登记', 2100, 4, 'criticals', 'health/critical/index', '', 1, 0, 'C', 0, 0, 'health:critical:list', 'warning', 103, 1, NOW()),
(2141, '危急值登记', 2140, 1, '#', '', '', 1, 0, 'F', 0, 0, 'health:critical:add', '#', 103, 1, NOW()),
(2142, '危急值处置', 2140, 2, '#', '', '', 1, 0, 'F', 0, 0, 'health:critical:edit', '#', 103, 1, NOW());

-- -----------------------------------------------------------------------------
-- 10.10 自检
-- -----------------------------------------------------------------------------
SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'xfjk'
   AND TABLE_NAME IN ('t_health_report','t_health_report_item','t_health_dept_summary',
                      't_health_report_conclusion','t_health_report_archive',
                      't_health_critical_log','t_health_conclusion_dict')
 ORDER BY TABLE_NAME;
SELECT menu_id, menu_name, parent_id, menu_type, component, perms FROM sys_menu WHERE menu_id BETWEEN 2100 AND 2199 ORDER BY menu_id;
