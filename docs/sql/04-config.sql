-- =============================================================================
-- 心飞健康 sys_config 业务参数种子（幂等，可重复执行）
-- 依据：docs/requirements/12-系统设置.md §4.2 + §4.3 + 09-健康评估.md §2.6
-- 规模：1组 23 键 + 2组 17 键 + 3组 18 键 = 58 个键
-- 规则：12 §4.1 —— 键名 health:<域>:<语义>，一律 config_type='Y'（内置，禁止后台删除）；
--       remark 写「出处 + 读取方」；读取一律走框架 ConfigUtils，禁止 @Value 散落字面量
-- 实测（192.168.20.130/xfjk，MySQL 8.0.46）：
--   sys_config PK=config_id（**无自增**，必须显式给值）；**config_key 上无唯一索引**
--     ⇒ 12 §11 规定的「按 config_key 先删后插」是唯一正确写法，ON DUPLICATE KEY UPDATE 在本表不生效
--   现库存量仅框架 5 行（config_id 1/2/3/5/11，键 sys.index.skinName、sys.user.initPassword、
--     sys.index.sideTheme、sys.account.registerUser、sys.oss.previewListResource）⇒ 本脚本一律不动
--   §4.3 表中列出的这 3 个 sys.* 框架键实测已存在且值一致（123456 / false / true），故不重复插入
-- config_id 从 100 起（现库 max=11，无冲突）；tenant_id 默认 '000000' 不显式写
-- 待确认项：health:number:archive_region_code 取 000000 占位（17 待确认 14），正式部署前必须落实，
--           否则跨机构档案号语义丢失；本脚本在末尾把它单独 SELECT 出来提醒
-- 安全：只触碰 config_key LIKE 'health:%' 的行；禁止在生产库执行（04 §7、红线）
-- =============================================================================

-- 0. 执行前自检：期望 0 行。命中 = 库里存在本清单之外的 health: 键（未登记先补 12 §4）
SELECT config_id, config_key, config_name FROM sys_config
 WHERE config_key LIKE 'health:%'
   AND config_key NOT IN ('health:critical:wbc:lower', 'health:critical:wbc:upper', 'health:critical:hb:lower', 'health:critical:hb:upper', 'health:critical:plt:lower', 'health:critical:plt:upper', 'health:qc:dept_deadline_hours', 'health:qc:summary_deadline_days', 'health:followup:hypertension:times_per_year', 'health:followup:bp:sbp_normal', 'health:followup:bp:sbp_elder', 'health:followup:bp:dbp', 'health:followup:bp:age_rule', 'health:followup:target:bmi_max', 'health:followup:target:hr_min', 'health:followup:target:hr_max', 'health:followup:target:exercise_times_min', 'health:followup:target:exercise_min_once_min', 'health:followup:trigger_rate_target', 'health:stat:score:weight:abnormal', 'health:stat:score:weight:positive', 'health:stat:score:weight:major', 'health:stat:miss_rate_item_denominator', 'health:scale:scl90.scoring.method', 'health:scale:scl90.total.max', 'health:scale:scl90.screen.total_gt', 'health:scale:scl90.screen.positive_count_gt', 'health:scale:scl90.screen.factor_gt', 'health:scale:scl90.positive.item_ge', 'health:scale:scl90.total.avg_divisor', 'health:scale:scl90.norm.exceed', 'health:scale:tcm.items.per_subscale', 'health:scale:tcm.scale.levels', 'health:scale:tcm.peace.is_ge', 'health:scale:tcm.peace.all_lt', 'health:scale:tcm.peace.basic_lt', 'health:scale:tcm.deviate.is_ge', 'health:scale:tcm.deviate.tendency_ge', 'health:scale:tcm.combine.count_ge', 'health:scale:tcm.combine.score_ge', 'health:profile:required', 'health:import:member_max_rows', 'health:import:ordersetting_max_rows', 'health:number:archive_prefix', 'health:number:archive_region_code', 'health:number:exam_prefix', 'health:number:report_prefix', 'health:number:batch_prefix', 'health:sms:code_length', 'health:sms:expire_minutes', 'health:report:watermark_flag', 'health:export:mask_level', 'health:img:orphan_keep_days', 'health:stat:trend_months', 'health:stat:hot_setmeal_top', 'health:stat:max_range_days', 'health:stat:export_max_rows', 'health:stat:cache_seconds');

-- 1. 先删后插（禁止无 WHERE 全表删除）
DELETE FROM `sys_config` WHERE `config_key` LIKE 'health:%';

INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_dept`, `create_by`, `create_time`, `remark`) VALUES

-- 阈值类（12 §4.2 —— 改这里，不改代码）
(100, '危急值-白细胞下限', 'health:critical:wbc:lower', '1.0', 'Y', 103, 1, NOW(), '出处 18 D4；读取方 07 §5 危急值判定'),
(101, '危急值-白细胞上限', 'health:critical:wbc:upper', '30', 'Y', 103, 1, NOW(), '出处 18 D4；读取方 07 §5 危急值判定'),
(102, '危急值-血红蛋白下限', 'health:critical:hb:lower', '60', 'Y', 103, 1, NOW(), '出处 18 D4；读取方 07 §5 危急值判定'),
(103, '危急值-血红蛋白上限', 'health:critical:hb:upper', '200', 'Y', 103, 1, NOW(), '出处 18 D4；读取方 07 §5 危急值判定'),
(104, '危急值-血小板下限', 'health:critical:plt:lower', '30', 'Y', 103, 1, NOW(), '出处 18 D4；读取方 07 §5 危急值判定'),
(105, '危急值-血小板上限', 'health:critical:plt:upper', '1000', 'Y', 103, 1, NOW(), '出处 18 D4；读取方 07 §5 危急值判定'),
(106, '科室小结时限（小时）', 'health:qc:dept_deadline_hours', '24', 'Y', 103, 1, NOW(), '出处 18 C8（科室自查当日）；读取方 07 质控链；12 §6 qcTimeoutRemindJob'),
(107, '主检审核时限（天）', 'health:qc:summary_deadline_days', '3', 'Y', 103, 1, NOW(), '出处 18 C8（主检审核 3 日内）；读取方 07 质控链；12 §6 qcTimeoutRemindJob'),
(108, '高血压年度随访次数下限', 'health:followup:hypertension:times_per_year', '4', 'Y', 103, 1, NOW(), '出处 18 B1；17 决策 44；读取方 10 §4.1 下发校验（低于 4 → 8009）'),
(109, '收缩压正常上限（<65 岁）', 'health:followup:bp:sbp_normal', '140', 'Y', 103, 1, NOW(), '出处 18 B9；读取方 10 §4.2'),
(110, '收缩压正常上限（≥65 岁）', 'health:followup:bp:sbp_elder', '150', 'Y', 103, 1, NOW(), '出处 18 B9；读取方 10 §4.2'),
(111, '舒张压正常上限', 'health:followup:bp:dbp', '90', 'Y', 103, 1, NOW(), '出处 18 B9；读取方 10 §4.2'),
(112, '高龄血压判定年龄界', 'health:followup:bp:age_rule', '65', 'Y', 103, 1, NOW(), '出处 18 B9；读取方 10 §4.2（≥65 用 sbp_elder）'),
(113, 'BMI 控制目标上限', 'health:followup:target:bmi_max', '24', 'Y', 103, 1, NOW(), '出处 18 B10；读取方 10 §4.3'),
(114, '心率控制目标下限', 'health:followup:target:hr_min', '60', 'Y', 103, 1, NOW(), '出处 18 B10；读取方 10 §4.3'),
(115, '心率控制目标上限', 'health:followup:target:hr_max', '100', 'Y', 103, 1, NOW(), '出处 18 B10；读取方 10 §4.3'),
(116, '每周运动次数目标下限', 'health:followup:target:exercise_times_min', '5', 'Y', 103, 1, NOW(), '出处 18 B10；读取方 10 §4.3'),
(117, '单次运动时长目标下限（分钟）', 'health:followup:target:exercise_min_once_min', '30', 'Y', 103, 1, NOW(), '出处 18 B10；读取方 10 §4.3'),
(118, '随访计划触发率目标（%）', 'health:followup:trigger_rate_target', '85', 'Y', 103, 1, NOW(), '出处 18 B11；读取方 10 §4.1；06 工作台指标'),
(119, '报告分值-异常项权重', 'health:stat:score:weight:abnormal', '1', 'Y', 103, 1, NOW(), '出处 18 G6；读取方 06 §5.4（每条 abnormal_flag 非 0）'),
(120, '报告分值-重大阳性权重', 'health:stat:score:weight:positive', '2', 'Y', 103, 1, NOW(), '出处 18 G6；读取方 06 §5.4（每条 major_positive=1）'),
(121, '报告分值-危急值权重', 'health:stat:score:weight:major', '5', 'Y', 103, 1, NOW(), '出处 18 G6；读取方 06 §5.4（每条 critical_flag=1）'),
(122, '漏检率分母口径', 'health:stat:miss_rate_item_denominator', 'SETMEAL', 'Y', 103, 1, NOW(), '出处 18 G5；读取方 06 §5.5（值域 SETMEAL/REPORT）'),

-- 量表阈值类（09 §2.6 为唯一清单（键前缀 health:scale:），12 §4.2 不重复列举）
(123, 'SCL-90 记分法', 'health:scale:scl90.scoring.method', '1to5', 'Y', 103, 1, NOW(), '出处 18 A8；读取方 09 算分引擎（项目内唯一记分法，禁 0~4 混用）'),
(124, 'SCL-90 总分上限', 'health:scale:scl90.total.max', '450', 'Y', 103, 1, NOW(), '出处 18 A15 推导；读取方 09（90×5）'),
(125, 'SCL-90 筛查阳性-总分阈值（严格大于）', 'health:scale:scl90.screen.total_gt', '160', 'Y', 103, 1, NOW(), '出处 18 A10；读取方 09 §4 筛查判定'),
(126, 'SCL-90 筛查阳性-阳性项目数阈值（严格大于）', 'health:scale:scl90.screen.positive_count_gt', '43', 'Y', 103, 1, NOW(), '出处 18 A10；读取方 09 §4 筛查判定'),
(127, 'SCL-90 筛查阳性-因子分阈值（严格大于）', 'health:scale:scl90.screen.factor_gt', '2', 'Y', 103, 1, NOW(), '出处 18 A10；读取方 09 §4 筛查判定'),
(128, 'SCL-90 单项计为阳性项目的分值下界', 'health:scale:scl90.positive.item_ge', '2', 'Y', 103, 1, NOW(), '出处 18 A15；读取方 09 阳性项目数计算'),
(129, 'SCL-90 总均分分母', 'health:scale:scl90.total.avg_divisor', '90', 'Y', 103, 1, NOW(), '出处 18 A15；读取方 09 总均分计算'),
(130, 'SCL-90 超常模判定方式', 'health:scale:scl90.norm.exceed', 'upper', 'Y', 103, 1, NOW(), '出处 09 §4.1 定稿；读取方 09（取 norm_mean + norm_sd）'),
(131, '中医体质各子量表条目数', 'health:scale:tcm.items.per_subscale', '7,8', 'Y', 103, 1, NOW(), '出处 18 A1；读取方 09 转化分计算'),
(132, '中医体质评分级数', 'health:scale:tcm.scale.levels', '5', 'Y', 103, 1, NOW(), '出处 18 A1；读取方 09（分母 = 条数 ×(级数−1)）'),
(133, '平和质「是」转化分下界', 'health:scale:tcm.peace.is_ge', '60', 'Y', 103, 1, NOW(), '出处 18 A4；读取方 09 §4.1.2'),
(134, '平和质「是」其余体质上界（不含）', 'health:scale:tcm.peace.all_lt', '30', 'Y', 103, 1, NOW(), '出处 18 A4；读取方 09 §4.1.2'),
(135, '平和质「基本是」其余上界（不含）', 'health:scale:tcm.peace.basic_lt', '40', 'Y', 103, 1, NOW(), '出处 18 A4；读取方 09 §4.1.2'),
(136, '偏颇质「是」转化分下界', 'health:scale:tcm.deviate.is_ge', '40', 'Y', 103, 1, NOW(), '出处 18 A5；读取方 09 §4.1.2'),
(137, '偏颇质「倾向是」下界', 'health:scale:tcm.deviate.tendency_ge', '30', 'Y', 103, 1, NOW(), '出处 18 A5；读取方 09 §4.1.2'),
(138, '兼夹倾向体质种数下界', 'health:scale:tcm.combine.count_ge', '2', 'Y', 103, 1, NOW(), '出处 18 A6；读取方 09 §4.1.2'),
(139, '兼夹倾向计入的转化分下界', 'health:scale:tcm.combine.score_ge', '30', 'Y', 103, 1, NOW(), '出处 18 A6；读取方 09 §4.1.2'),

-- 业务参数类（12 §4.3）
(140, '档案完整度必填清单', 'health:profile:required', 'name,sex,idCard,birthday,phoneNumber,marital_status,sex_history,pregnancy_status', 'Y', 103, 1, NOW(), '出处 18 F2；读取方 07 §5 完整度计算'),
(141, '团检名单单次导入行数上限', 'health:import:member_max_rows', '2000', 'Y', 103, 1, NOW(), '出处 18 H2（超出 20008）；读取方 20 §6'),
(142, '预约设置批量导入行数上限（本期约定，取一年天数）', 'health:import:ordersetting_max_rows', '366', 'Y', 103, 1, NOW(), '出处 本期约定，非 18 口径；读取方 08 预约设置'),
(143, '档案号前缀', 'health:number:archive_prefix', 'FA', 'Y', 103, 1, NOW(), '出处 18 F5；07 §2.2；读取方 07 档案号生成'),
(144, '档案号 6 位区划码（占位，待业务方给值）', 'health:number:archive_region_code', '000000', 'Y', 103, 1, NOW(), '出处 17 待确认 14（正式部署前必须落实）；读取方 07 档案号生成'),
(145, '体检号前缀', 'health:number:exam_prefix', 'EX', 'Y', 103, 1, NOW(), '出处 08 §2.4；读取方 08 体检号生成'),
(146, '报告号前缀', 'health:number:report_prefix', 'RP', 'Y', 103, 1, NOW(), '出处 07 §2.6；读取方 07 报告号生成'),
(147, '批次号前缀', 'health:number:batch_prefix', 'TB', 'Y', 103, 1, NOW(), '出处 20 §2.3；读取方 20 批次号生成'),
(148, '业务验证码位数', 'health:sms:code_length', '4', 'Y', 103, 1, NOW(), '出处 05 §6；08 §5.3；读取方 05/08 短信校验（与框架登录验证码同用 4 位，但两个开关不同一）'),
(149, '业务验证码有效期（分钟）', 'health:sms:expire_minutes', '5', 'Y', 103, 1, NOW(), '出处 05 §6；12 §7.2；读取方 05 业务短信（框架登录短信走 Constants.CAPTCHA_EXPIRATION=2，不是本键）'),
(150, '发放件水印开关', 'health:report:watermark_flag', '1', 'Y', 103, 1, NOW(), '出处 18 F11；读取方 07 打印/发放'),
(151, '导出默认脱敏级别', 'health:export:mask_level', '部分脱敏', 'Y', 103, 1, NOW(), '出处 本期约定；值域 health_export_mask；读取方 07/06/20 导出'),
(152, '孤儿图片保护期（天）', 'health:img:orphan_keep_days', '7', 'Y', 103, 1, NOW(), '出处 本期约定；12 §6.2 竞态保护；读取方 12 §6.2 healthImgCleanJob'),
(153, '工作台趋势滚动月数', 'health:stat:trend_months', '12', 'Y', 103, 1, NOW(), '出处 06 §4.4；读取方 06 工作台与会员增长报表（替代硬编码「近 12 个月」）'),
(154, '热门套餐 TopN', 'health:stat:hot_setmeal_top', '4', 'Y', 103, 1, NOW(), '出处 06 §4.4；15 §6 缺陷 5；读取方 06 工作台（替代 LIMIT 0,4）'),
(155, '自定义统计区间跨度上限（天）', 'health:stat:max_range_days', '730', 'Y', 103, 1, NOW(), '出处 06 §2.3/§6（超限 6103）；读取方 06 报表'),
(156, '统计报表单次导出行数上限', 'health:stat:export_max_rows', '20000', 'Y', 103, 1, NOW(), '出处 06 §10（超限 6104）；读取方 06 导出（与 health:import:* 分列，不共用）'),
(157, '工作台概览/待办缓存 TTL（秒）', 'health:stat:cache_seconds', '60', 'Y', 103, 1, NOW(), '出处 06 §3.3（R9 实时聚合为主）；读取方 06 工作台');

-- 2. 回查
SELECT COUNT(*) AS health_key_cnt FROM sys_config WHERE config_key LIKE 'health:%';        -- 期望 58
SELECT COUNT(*) AS not_builtin_cnt FROM sys_config WHERE config_key LIKE 'health:%' AND config_type <> 'Y';  -- 期望 0
SELECT COUNT(*) AS framework_rows FROM sys_config WHERE config_key LIKE 'sys.%';            -- 期望 5（框架行未被误删）
SELECT config_key, config_value FROM sys_config WHERE config_key = 'health:number:archive_region_code';  -- 17 待确认 14，部署前须改
