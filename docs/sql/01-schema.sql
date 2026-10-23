-- =============================================================================
-- 心飞健康 业务库建表脚本（幂等，可重复执行）
-- 依据：docs/requirements/04 §2 原库冻结业务表、03 §13 SQL 与初始化脚本
-- 来源：legacy/backend/health.sql（列名/类型/主键/外键照抄，结构冻结禁止 ALTER）
-- 环境适配：utf8 -> utf8mb4 + utf8mb4_general_ci；去掉 int 显示宽度（MySQL 8.0 已废弃）
-- 禁止：改动 t_* 冻结表结构、新增审计列、在生产库执行 DDL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 检查项
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_checkitem` (
  `id`        int          NOT NULL AUTO_INCREMENT,
  `code`      varchar(16)  DEFAULT NULL,
  `name`      varchar(32)  DEFAULT NULL,
  `sex`       char(1)      DEFAULT NULL,
  `age`       varchar(32)  DEFAULT NULL,
  `price`     float        DEFAULT NULL,
  `type`      char(1)      DEFAULT NULL COMMENT '查检项类型,分为检查和检验两种',
  `attention` varchar(128) DEFAULT NULL,
  `remark`    varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检查项';

-- -----------------------------------------------------------------------------
-- 2. 检查组
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_checkgroup` (
  `id`        int          NOT NULL AUTO_INCREMENT,
  `code`      varchar(32)  DEFAULT NULL,
  `name`      varchar(32)  DEFAULT NULL,
  `helpCode`  varchar(32)  DEFAULT NULL,
  `sex`       char(1)      DEFAULT NULL,
  `remark`    varchar(128) DEFAULT NULL,
  `attention` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检查组';

-- -----------------------------------------------------------------------------
-- 3. 检查组-检查项关联
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_checkgroup_checkitem` (
  `checkgroup_id` int NOT NULL DEFAULT 0,
  `checkitem_id`  int NOT NULL DEFAULT 0,
  PRIMARY KEY (`checkgroup_id`, `checkitem_id`),
  KEY `item_id` (`checkitem_id`),
  CONSTRAINT `group_id` FOREIGN KEY (`checkgroup_id`) REFERENCES `t_checkgroup` (`id`),
  CONSTRAINT `item_id`  FOREIGN KEY (`checkitem_id`)  REFERENCES `t_checkitem` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检查组-检查项关联';

-- -----------------------------------------------------------------------------
-- 4. 套餐
--    img 列原存 Qiniu 文件名，新系统存 OSS 对象主键 ossId（列宽与语义兼容，见 03 §10）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_setmeal` (
  `id`        int           NOT NULL AUTO_INCREMENT,
  `name`      varchar(128)  DEFAULT NULL,
  `code`      varchar(8)    DEFAULT NULL,
  `helpCode`  varchar(16)   DEFAULT NULL,
  `sex`       char(1)       DEFAULT NULL,
  `age`       varchar(32)   DEFAULT NULL,
  `price`     float         DEFAULT NULL,
  `remark`    varchar(128)  DEFAULT NULL,
  `attention` varchar(128)  DEFAULT NULL,
  `img`       varchar(128)  DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '套餐';

-- -----------------------------------------------------------------------------
-- 5. 套餐-检查组关联
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_setmeal_checkgroup` (
  `setmeal_id`    int NOT NULL DEFAULT 0,
  `checkgroup_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`setmeal_id`, `checkgroup_id`),
  KEY `checkgroup_key` (`checkgroup_id`),
  CONSTRAINT `checkgroup_key` FOREIGN KEY (`checkgroup_id`) REFERENCES `t_checkgroup` (`id`),
  CONSTRAINT `setmeal_key`    FOREIGN KEY (`setmeal_id`)    REFERENCES `t_setmeal` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '套餐-检查组关联';

-- -----------------------------------------------------------------------------
-- 6. 会员（C 端基础档案，见 07）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_member` (
  `id`          int          NOT NULL AUTO_INCREMENT,
  `fileNumber`  varchar(32)  DEFAULT NULL,
  `name`        varchar(32)  DEFAULT NULL,
  `sex`         varchar(8)   DEFAULT NULL,
  `idCard`      varchar(18)  DEFAULT NULL,
  `phoneNumber` varchar(11)  DEFAULT NULL,
  `regTime`     date         DEFAULT NULL,
  `password`    varchar(32)  DEFAULT NULL,
  `email`       varchar(32)  DEFAULT NULL,
  `birthday`    date         DEFAULT NULL,
  `remark`      varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员';

-- -----------------------------------------------------------------------------
-- 7. 预约名额设置（reservations 可空，扣减须 COALESCE，见 08 §4.3）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ordersetting` (
  `id`           int   NOT NULL AUTO_INCREMENT,
  `orderDate`    date  DEFAULT NULL COMMENT '约预日期',
  `number`       int   DEFAULT NULL COMMENT '可预约人数',
  `reservations` int   DEFAULT NULL COMMENT '已预约人数',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '预约名额设置';

-- -----------------------------------------------------------------------------
-- 8. 预约订单
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_order` (
  `id`          int         NOT NULL AUTO_INCREMENT,
  `member_id`   int         DEFAULT NULL COMMENT '员会id',
  `orderDate`   date        DEFAULT NULL COMMENT '约预日期',
  `orderType`   varchar(8)  DEFAULT NULL COMMENT '约预类型 电话预约/微信预约',
  `orderStatus` varchar(8)  DEFAULT NULL COMMENT '预约状态（是否到诊）',
  `setmeal_id`  int         DEFAULT NULL COMMENT '餐套id',
  PRIMARY KEY (`id`),
  KEY `key_member_id`  (`member_id`),
  KEY `key_setmeal_id` (`setmeal_id`),
  CONSTRAINT `key_member_id`  FOREIGN KEY (`member_id`)  REFERENCES `t_member` (`id`),
  CONSTRAINT `key_setmeal_id` FOREIGN KEY (`setmeal_id`) REFERENCES `t_setmeal` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '预约订单';
