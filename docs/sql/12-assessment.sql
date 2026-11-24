-- -----------------------------------------------------------------------------
-- 12. 健康评估模块数据表（09 §2，14 张，非冻结表）
--    通用约束（04 §4）：雪花 id、下划线列名、审计列 + del_flag。
--    口径：阈值/常模/因子归属一律来自 18 §3.A（A1~A16），代码零魔法数（18 A13）。
-- -----------------------------------------------------------------------------

-- 12.1 问卷/量表定义（09 §2.1）
CREATE TABLE IF NOT EXISTS `t_health_questionnaire` (
  `id`                bigint        NOT NULL                COMMENT '雪花主键',
  `code`              varchar(64)   NOT NULL                COMMENT '问卷编码（唯一）',
  `name`              varchar(128)  NOT NULL                COMMENT '名称',
  `category`          varchar(16)   NOT NULL                COMMENT 'scale 标准量表 / question 普通问卷',
  `scale_type`        varchar(16)   DEFAULT NULL            COMMENT 'scl90 / tcm9 / custom；category=scale 必填',
  `version`           int           NOT NULL DEFAULT 1      COMMENT '同 code 下递增版本',
  `intro`             varchar(1024) DEFAULT NULL            COMMENT '简介',
  `instruction`       varchar(512)  DEFAULT NULL            COMMENT '填表说明（C 端作答页首屏）',
  `question_count`    int           NOT NULL                COMMENT '题目数（发布时与实际强校验，7004）',
  `scoring_type`      varchar(16)   NOT NULL                COMMENT 'sum / factor / scale（09 §3.2）',
  `answer_min_score`  decimal(6,2)  DEFAULT NULL            COMMENT '答题分值下界',
  `answer_max_score`  decimal(6,2)  DEFAULT NULL            COMMENT '答题分值上界',
  `status`            varchar(16)   NOT NULL DEFAULT 'draft' COMMENT 'draft / published / disabled',
  `is_public`         tinyint       NOT NULL DEFAULT 0      COMMENT '1 = C 端可作答',
  `publish_time`      datetime      DEFAULT NULL            COMMENT '首次发布时间',
  `sort`              int           NOT NULL DEFAULT 0      COMMENT '排序',
  `remark`            varchar(512)  DEFAULT NULL            COMMENT '备注',
  `create_dept`       bigint        DEFAULT NULL,
  `create_by`         bigint        DEFAULT NULL,
  `create_time`       datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`         bigint        DEFAULT NULL,
  `update_time`       datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`          char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_scale_type` (`scale_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '问卷/量表定义';

-- 12.2 题目（09 §2.2）
CREATE TABLE IF NOT EXISTS `t_health_question` (
  `id`               bigint        NOT NULL                COMMENT '雪花主键',
  `questionnaire_id` bigint        NOT NULL                COMMENT '关联 t_health_questionnaire.id',
  `item_no`          int           DEFAULT NULL            COMMENT '标准量表条目序号（SCL-90 1~90 = 18 A14；中医 1~60）',
  `seq`              int           NOT NULL DEFAULT 0      COMMENT '展示顺序',
  `type`             varchar(16)   NOT NULL                COMMENT 'single / multi / text',
  `content`          varchar(512)  NOT NULL                COMMENT '题干原文（标准量表禁止改写）',
  `required`         tinyint       NOT NULL DEFAULT 1      COMMENT '1 必答',
  `factor_code`      varchar(32)   DEFAULT NULL            COMMENT '因子归属（09 §2.4）；量表类必填',
  `reverse_flag`     tinyint       NOT NULL DEFAULT 0      COMMENT '1 反向计分（18 A2）',
  `jump_rule`        varchar(255)  DEFAULT NULL            COMMENT '本期一律为空（跳题不做）',
  `sort`             int           NOT NULL DEFAULT 0      COMMENT '排序',
  `remark`           varchar(512)  DEFAULT NULL            COMMENT '备注',
  `create_dept`      bigint        DEFAULT NULL,
  `create_by`        bigint        DEFAULT NULL,
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`        bigint        DEFAULT NULL,
  `update_time`      datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`         char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_questionnaire` (`questionnaire_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '问卷题目';

-- 12.3 选项（09 §2.3）
CREATE TABLE IF NOT EXISTS `t_health_question_option` (
  `id`           bigint        NOT NULL                COMMENT '雪花主键',
  `question_id`  bigint        NOT NULL                COMMENT '关联 t_health_question.id',
  `seq`          int           NOT NULL DEFAULT 0      COMMENT '选项顺序',
  `content`      varchar(256)  NOT NULL                COMMENT '选项文案',
  `score`        decimal(6,2)  NOT NULL                COMMENT '分值，须落在 answer_min~max（7011）',
  `factor_code`  varchar(32)   DEFAULT NULL            COMMENT '选项级因子覆盖（默认继承题目）',
  `is_other`     tinyint       NOT NULL DEFAULT 0      COMMENT '1 = 其他（需配套自由文本）',
  `create_dept`  bigint        DEFAULT NULL,
  `create_by`    bigint        DEFAULT NULL,
  `create_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`    bigint        DEFAULT NULL,
  `update_time`  datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`     char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题目选项';

-- 12.4 量表因子/维度（09 §2.4）
CREATE TABLE IF NOT EXISTS `t_health_scale_factor` (
  `id`               bigint        NOT NULL                COMMENT '雪花主键',
  `questionnaire_id` bigint        NOT NULL                COMMENT '关联 t_health_questionnaire.id',
  `code`             varchar(32)   NOT NULL                COMMENT '因子码（09 §2.4 枚举）',
  `name`             varchar(64)   NOT NULL                COMMENT '因子名',
  `item_count`       int           NOT NULL                COMMENT '因子项目数（18 A8 分母）',
  `weight`           decimal(6,4)  NOT NULL DEFAULT 1.0000 COMMENT '预留权重，本期一律 1',
  `norm_mean`        decimal(6,4)  DEFAULT NULL            COMMENT '常模均值 X̄（18 A11）',
  `norm_sd`          decimal(6,4)  DEFAULT NULL            COMMENT '常模标准差 SD（18 A11）',
  `extra_flag`       tinyint       NOT NULL DEFAULT 0      COMMENT '1 附加因子（18 A14 第 10 因子）',
  `sort`             int           NOT NULL DEFAULT 0      COMMENT '排序',
  `create_dept`      bigint        DEFAULT NULL,
  `create_by`        bigint        DEFAULT NULL,
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`        bigint        DEFAULT NULL,
  `update_time`      datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`         char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qid_factor` (`questionnaire_id`, `code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '量表因子/维度';

-- 12.5 分级/判定规则（09 §2.5；区间左闭右开）
CREATE TABLE IF NOT EXISTS `t_health_scale_rule` (
  `id`               bigint        NOT NULL                COMMENT '雪花主键',
  `questionnaire_id` bigint        NOT NULL                COMMENT '关联 t_health_questionnaire.id',
  `rule_type`        varchar(24)   NOT NULL                COMMENT 'factor_level/total_level/tcm_convert/screen/severity',
  `factor_code`      varchar(32)   DEFAULT NULL            COMMENT '空 = 总分或全局',
  `min_value`        decimal(10,2) DEFAULT NULL            COMMENT '区间下界（含）',
  `max_value`        decimal(10,2) DEFAULT NULL            COMMENT '区间上界（不含），NULL 无上界',
  `level_code`       varchar(32)   NOT NULL                COMMENT 'yes/basic/no/tendency/normal/sub_health/mild/moderate/severe',
  `conclusion`       varchar(255)  NOT NULL                COMMENT '结论文案模板',
  `suggestion_id`    bigint        DEFAULT NULL            COMMENT '关联 t_health_suggestion.id',
  `sort`             int           NOT NULL DEFAULT 0      COMMENT '同区间多条取第一条',
  `create_dept`      bigint        DEFAULT NULL,
  `create_by`        bigint        DEFAULT NULL,
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`        bigint        DEFAULT NULL,
  `update_time`      datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`         char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_qid_rule` (`questionnaire_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '量表分级/判定规则';

-- 12.6 阈值配置（09 §2.6、18 A13）
CREATE TABLE IF NOT EXISTS `t_health_scale_config` (
  `id`               bigint        NOT NULL                COMMENT '雪花主键',
  `questionnaire_id` bigint        NOT NULL                COMMENT '关联 t_health_questionnaire.id',
  `config_key`       varchar(64)   NOT NULL                COMMENT '配置键（09 §2.6 表）',
  `config_value`     varchar(255)  NOT NULL                COMMENT '配置值',
  `value_type`       varchar(16)   NOT NULL                COMMENT 'int / decimal / string / json',
  `source_ref`       varchar(64)   NOT NULL                COMMENT '口径出处（18 Axx）',
  `cfg_version`      varchar(32)   DEFAULT NULL            COMMENT '口径版本号',
  `create_dept`      bigint        DEFAULT NULL,
  `create_by`        bigint        DEFAULT NULL,
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`        bigint        DEFAULT NULL,
  `update_time`      datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`         char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qid_cfgkey` (`questionnaire_id`, `config_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '量表阈值配置（18 A13）';

-- 12.7 评估记录（09 §2.7）
CREATE TABLE IF NOT EXISTS `t_health_assessment_record` (
  `id`                     bigint        NOT NULL                COMMENT '雪花主键',
  `record_no`              varchar(32)   NOT NULL                COMMENT 'EV+yyyyMMdd+6 位序号',
  `member_id`              bigint        NOT NULL                COMMENT '关联 t_member.id（6001）',
  `questionnaire_id`       bigint        NOT NULL                COMMENT '关联 t_health_questionnaire.id',
  `questionnaire_version`  int           NOT NULL                COMMENT '版本快照',
  `scale_type`             varchar(16)   DEFAULT NULL            COMMENT '快照',
  `status`                 varchar(16)   NOT NULL DEFAULT 'doing' COMMENT 'doing/submitted/scored/invalid',
  `source`                 varchar(16)   NOT NULL                COMMENT 'app/admin/batch',
  `answer_count`           int           NOT NULL DEFAULT 0      COMMENT '实际作答题数',
  `total_score`            decimal(10,2) DEFAULT NULL            COMMENT 'SCL-90 原始分之和',
  `avg_score`              decimal(6,2)  DEFAULT NULL            COMMENT '总均分',
  `positive_count`         int           DEFAULT NULL            COMMENT '阳性项目数（A15）',
  `positive_flag`          tinyint       DEFAULT NULL            COMMENT '筛查阳性（A10）',
  `severity`               varchar(16)   DEFAULT NULL            COMMENT '心理严重度（A12）',
  `tcm_primary`            varchar(32)   DEFAULT NULL            COMMENT '主体质因子码',
  `tcm_level`              varchar(16)   DEFAULT NULL            COMMENT 'yes/basic/no/tendency',
  `tcm_combine_json`       varchar(512)  DEFAULT NULL            COMMENT '兼夹倾向清单（A6）',
  `conclusion`             varchar(512)  DEFAULT NULL            COMMENT '结论文案',
  `level`                  varchar(32)   DEFAULT NULL            COMMENT '主等级码',
  `factor_scores_json`     text          DEFAULT NULL            COMMENT '因子分快照',
  `suggestion_json`        text          DEFAULT NULL            COMMENT '建议快照',
  `crowd_ids`              varchar(255)  DEFAULT NULL            COMMENT '命中人群 id 逗号串',
  `cfg_version`            varchar(32)   DEFAULT NULL            COMMENT '算分口径版本',
  `start_time`             datetime      DEFAULT NULL            COMMENT '开始作答',
  `submit_time`            datetime      DEFAULT NULL            COMMENT '提交时间',
  `duration_sec`           int           DEFAULT NULL            COMMENT '作答时长',
  `operator_id`            bigint        DEFAULT NULL            COMMENT '后台代填经手人',
  `operator_name`          varchar(64)   DEFAULT NULL            COMMENT '后台代填经手人名',
  `invalid_reason`         varchar(255)  DEFAULT NULL            COMMENT '无效原因（invalid 必填）',
  `create_dept`            bigint        DEFAULT NULL,
  `create_by`              bigint        DEFAULT NULL,
  `create_time`            datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`              bigint        DEFAULT NULL,
  `update_time`            datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`               char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_member` (`member_id`),
  KEY `idx_questionnaire` (`questionnaire_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评估记录';

-- 12.8 作答明细（09 §2.8）
CREATE TABLE IF NOT EXISTS `t_health_assessment_answer` (
  `id`            bigint        NOT NULL                COMMENT '雪花主键',
  `record_id`     bigint        NOT NULL                COMMENT '关联 t_health_assessment_record.id',
  `question_id`   bigint        NOT NULL                COMMENT '关联 t_health_question.id',
  `item_no`       int           DEFAULT NULL            COMMENT '题干序号快照',
  `option_ids`    varchar(255)  DEFAULT NULL            COMMENT '多选逗号串',
  `option_count`  int           NOT NULL DEFAULT 0      COMMENT '选项数',
  `answer_text`   varchar(1000) DEFAULT NULL            COMMENT '填空/其他文本',
  `raw_score`     decimal(6,2)  DEFAULT NULL            COMMENT '选项原始分',
  `score`         decimal(6,2)  DEFAULT NULL            COMMENT '反向映射后计入总分（A2）',
  `reverse_flag`  tinyint       NOT NULL DEFAULT 0      COMMENT '快照',
  `create_dept`   bigint        DEFAULT NULL,
  `create_by`     bigint        DEFAULT NULL,
  `create_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint        DEFAULT NULL,
  `update_time`   datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`      char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_question` (`record_id`, `question_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评估作答明细';

-- 12.9 因子得分（09 §2.9）
CREATE TABLE IF NOT EXISTS `t_health_assessment_factor` (
  `id`               bigint        NOT NULL                COMMENT '雪花主键',
  `record_id`        bigint        NOT NULL                COMMENT '关联 t_health_assessment_record.id',
  `factor_code`      varchar(32)   NOT NULL                COMMENT '快照',
  `factor_name`      varchar(64)   NOT NULL                COMMENT '快照',
  `item_count`       int           NOT NULL                COMMENT '实际计分条目数（A8 分母）',
  `raw_sum`          decimal(10,2) NOT NULL                COMMENT '因子原始分之和',
  `factor_score`     decimal(6,3)  DEFAULT NULL            COMMENT '因子分 = raw_sum / item_count',
  `convert_score`    decimal(6,2)  DEFAULT NULL            COMMENT '中医转化分（A3）；SCL-90 NULL',
  `level_code`       varchar(32)   DEFAULT NULL            COMMENT '命中等级',
  `conclusion`       varchar(255)  DEFAULT NULL            COMMENT '命中结论',
  `norm_upper`       decimal(6,4)  DEFAULT NULL            COMMENT 'norm_mean + norm_sd 快照',
  `norm_exceed_flag` tinyint       DEFAULT NULL            COMMENT 'factor_score > norm_upper',
  `sort`             int           NOT NULL DEFAULT 0      COMMENT '排序',
  `create_dept`      bigint        DEFAULT NULL,
  `create_by`        bigint        DEFAULT NULL,
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`        bigint        DEFAULT NULL,
  `update_time`      datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`         char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_record` (`record_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评估因子得分';

-- 12.10 评估建议（09 §2.10）
CREATE TABLE IF NOT EXISTS `t_health_suggestion` (
  `id`               bigint       NOT NULL                COMMENT '雪花主键',
  `questionnaire_id` bigint       DEFAULT NULL            COMMENT '空 = 通用',
  `factor_code`      varchar(32)  DEFAULT NULL            COMMENT '空 = 不限定',
  `level_code`       varchar(32)  DEFAULT NULL            COMMENT '空 = 不限定',
  `type`             varchar(16)  NOT NULL                COMMENT 'diet/exercise/psych/followup/general',
  `content`          text         NOT NULL                COMMENT '建议文案',
  `ref_disease_id`   bigint       DEFAULT NULL            COMMENT '关联 t_health_disease.id（11）',
  `sort`             int          NOT NULL DEFAULT 0      COMMENT '排序',
  `create_dept`      bigint       DEFAULT NULL,
  `create_by`        bigint       DEFAULT NULL,
  `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`        bigint       DEFAULT NULL,
  `update_time`      datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`         char(1)      NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_qid` (`questionnaire_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评估建议';

-- 12.11 指标库（09 §2.11）
CREATE TABLE IF NOT EXISTS `t_health_indicator` (
  `id`           bigint        NOT NULL                COMMENT '雪花主键',
  `code`         varchar(64)   NOT NULL                COMMENT '指标编码（唯一）',
  `name`         varchar(128)  NOT NULL                COMMENT '指标名',
  `unit`         varchar(32)   DEFAULT NULL            COMMENT '单位',
  `ref_min`      decimal(12,4) DEFAULT NULL            COMMENT '参考下限',
  `ref_max`      decimal(12,4) DEFAULT NULL            COMMENT '参考上限',
  `ref_range`    varchar(64)   DEFAULT NULL            COMMENT '展示串',
  `value_type`   varchar(16)   NOT NULL                COMMENT 'numeric/text/enum',
  `category`     varchar(64)   DEFAULT NULL            COMMENT '分类',
  `sort`         int           NOT NULL DEFAULT 0      COMMENT '排序',
  `create_dept`  bigint        DEFAULT NULL,
  `create_by`    bigint        DEFAULT NULL,
  `create_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`    bigint        DEFAULT NULL,
  `update_time`  datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`     char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评估指标库';

-- 12.12 人群分类（09 §2.12）
CREATE TABLE IF NOT EXISTS `t_health_crowd` (
  `id`           bigint        NOT NULL                COMMENT '雪花主键',
  `code`         varchar(64)   NOT NULL                COMMENT '人群编码（唯一）',
  `name`         varchar(128)  NOT NULL                COMMENT '人群名',
  `description`  varchar(512)  DEFAULT NULL            COMMENT '描述',
  `status`       varchar(16)   NOT NULL DEFAULT '0'    COMMENT '0 启用 / 1 停用',
  `member_count` int           NOT NULL DEFAULT 0      COMMENT '冗余，归入后重算',
  `create_dept`  bigint        DEFAULT NULL,
  `create_by`    bigint        DEFAULT NULL,
  `create_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`    bigint        DEFAULT NULL,
  `update_time`  datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`     char(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '人群分类';

-- 12.13 人群规则条件（09 §2.12）
CREATE TABLE IF NOT EXISTS `t_health_crowd_rule` (
  `id`           bigint       NOT NULL                COMMENT '雪花主键',
  `crowd_id`     bigint       NOT NULL                COMMENT '关联 t_health_crowd.id',
  `source_type`  varchar(16)  NOT NULL                COMMENT 'factor/indicator/report/profile/assessment',
  `source_code`  varchar(64)  NOT NULL                COMMENT '因子码/指标码/报告字段名',
  `operator`     varchar(16)  NOT NULL                COMMENT 'gt/ge/lt/le/eq/ne/in/between',
  `value_min`    decimal(12,4) DEFAULT NULL           COMMENT '数值下界',
  `value_max`    decimal(12,4) DEFAULT NULL           COMMENT '数值上界',
  `value_text`   varchar(255) DEFAULT NULL            COMMENT '文本值',
  `group_no`     int          NOT NULL DEFAULT 1      COMMENT '组号：组内 AND，组间 OR',
  `logic`        varchar(8)   NOT NULL DEFAULT 'AND'  COMMENT 'AND/OR',
  `sort`         int          NOT NULL DEFAULT 0      COMMENT '排序',
  `create_dept`  bigint       DEFAULT NULL,
  `create_by`    bigint       DEFAULT NULL,
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`    bigint       DEFAULT NULL,
  `update_time`  datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag`     char(1)      NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_crowd` (`crowd_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '人群规则条件';

-- 12.14 会员-人群关联（09 §2.12，source_record_id 必填可追溯）
CREATE TABLE IF NOT EXISTS `t_health_member_crowd` (
  `id`               bigint      NOT NULL                COMMENT '雪花主键',
  `member_id`        bigint      NOT NULL                COMMENT '关联 t_member.id',
  `crowd_id`         bigint      NOT NULL                COMMENT '关联 t_health_crowd.id',
  `source_record_id` bigint      NOT NULL                COMMENT '来源评估记录 id（禁止无来源归类）',
  `join_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_crowd_source` (`member_id`, `crowd_id`, `source_record_id`),
  KEY `idx_crowd` (`crowd_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员-人群关联';
