-- -----------------------------------------------------------------------------
-- 7. 禁忌规则表（08 §2.7，非冻结表）
--    rule_code 语义锚定 18 §3.E 组编号；E10（入职禁查乙肝两对半）不入本表，
--    作为 t_health_kb_rule 知识规则存在（17 决策 29）。
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `t_health_checkitem_forbid` (
  `id`            bigint       NOT NULL                COMMENT '雪花主键',
  `rule_code`     varchar(32)  NOT NULL                COMMENT '规则编码，全局唯一，锚定 18 E 组（如 E1/E3/E6）',
  `scope`         varchar(12)  NOT NULL                COMMENT '作用范围：CHECKITEM/CHECKGROUP/SETMEAL',
  `target_id`     int          NOT NULL                COMMENT '按 scope 关联冻结表主键',
  `condition_json` varchar(500) NOT NULL               COMMENT '触发条件，键固定 sex/pregnancy/married/hasSexHistory/ageMin/ageMax/inMenstruation/metalImplant，值为数组或布尔',
  `action`        char(1)      NOT NULL                COMMENT '处置档：0 拦截 / 1 提示 / 2 延后补检（18 §3.E，禁止新增）',
  `tip_text`      varchar(255) NOT NULL                COMMENT '可读原因，必须能直接展示给用户（18 E13）',
  `priority`      int          NOT NULL DEFAULT 100    COMMENT '优先级，冲突时小者生效',
  `enabled`       char(1)      NOT NULL DEFAULT '1'    COMMENT '是否启用：1 启用 / 0 停用',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forbid_rule_code` (`rule_code`),
  KEY `uk_forbid_scope_target` (`scope`, `target_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '禁忌规则';

-- 示例种子（幂等）：锚定 18 §3.E 常见组；scope/target_id 指向 02-seed.sql 的套餐与检查项
-- E6 空腹提示（套餐级提示档）
INSERT INTO `t_health_checkitem_forbid` (`id`,`rule_code`,`scope`,`target_id`,`condition_json`,`action`,`tip_text`,`priority`,`enabled`)
SELECT 9001,'E6','SETMEAL',t.id,'{}','1','本套餐含空腹项目：前一日 22 点后禁食，当日晨禁食禁饮',100,'1'
FROM `t_setmeal` t
WHERE NOT EXISTS (SELECT 1 FROM `t_health_checkitem_forbid` f WHERE f.`rule_code`='E6' AND f.`scope`='SETMEAL' AND f.`target_id`=t.id)
LIMIT 1;
