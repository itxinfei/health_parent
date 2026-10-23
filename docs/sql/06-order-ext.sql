-- -----------------------------------------------------------------------------
-- 6. 预约管理扩展表（08 §2.6/§2.9，非冻结表，允许按需求演进）
--    t_order 及其余冻结表结构见 01-schema.sql，禁止在此改动。
-- -----------------------------------------------------------------------------

-- 6.1 订单流转扩展（1:1 t_order，08 §2.6）
CREATE TABLE IF NOT EXISTS `t_health_order_ext` (
  `id`              bigint       NOT NULL                COMMENT '雪花主键',
  `order_id`        int          NOT NULL                COMMENT '关联 t_order.id',
  `flow_status`     varchar(16)  NOT NULL DEFAULT '已预约' COMMENT '流程状态：已预约/已到诊/检查中/待总检/已总检/报告已打印/报告已发放/已取消/延期（08 §3.2）',
  `exam_no`         varchar(32)  DEFAULT NULL            COMMENT '体检号，到诊登记时生成：EX+yyyyMMdd+4位序号',
  `batch_id`        bigint       DEFAULT NULL            COMMENT '团检批次 id（20 §5.2）',
  `batch_member_id` bigint       DEFAULT NULL            COMMENT '团检名单行 id',
  `cancel_reason`   varchar(255) DEFAULT NULL            COMMENT '取消原因（取消时必填）',
  `cancel_by`       varchar(64)  DEFAULT NULL            COMMENT '取消操作人',
  `cancel_time`     datetime     DEFAULT NULL            COMMENT '取消时间',
  `postpone_count`  int          NOT NULL DEFAULT 0      COMMENT '延期次数',
  `postpone_until`  date         DEFAULT NULL            COMMENT '延期至日期',
  `print_time`      datetime     DEFAULT NULL            COMMENT '报告打印时间',
  `issue_time`      datetime     DEFAULT NULL            COMMENT '报告发放时间',
  `register_by`     varchar(64)  DEFAULT NULL            COMMENT '到诊登记人',
  `register_time`   datetime     DEFAULT NULL            COMMENT '到诊登记时间',
  `report_id`       bigint       DEFAULT NULL            COMMENT '关联 t_health_report.id（07，报告草稿）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ext_order_id` (`order_id`),
  KEY `uk_ext_flow_status` (`flow_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单流转扩展';

-- 6.2 订单流水（08 §2.9，只插入不更新）
CREATE TABLE IF NOT EXISTS `t_health_order_log` (
  `id`            bigint       NOT NULL                COMMENT '雪花主键',
  `order_id`      int          NOT NULL                COMMENT '关联 t_order.id',
  `action`        varchar(12)  NOT NULL                COMMENT '动作：预约/到诊/取消/改期/延期/延期恢复/备单/打印/发放/换人',
  `from_status`   varchar(16)  DEFAULT NULL            COMMENT '迁移前流程状态',
  `to_status`     varchar(16)  DEFAULT NULL            COMMENT '迁移后流程状态',
  `operator`      varchar(64)  DEFAULT NULL            COMMENT '操作人',
  `operator_role` varchar(32)  DEFAULT NULL            COMMENT '操作人角色',
  `remark`        varchar(255) DEFAULT NULL            COMMENT '备注',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `uk_log_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单流水（只插入不更新）';
