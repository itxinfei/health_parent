-- -----------------------------------------------------------------------------
-- 11. 检查项元数据与参考区间种子（08 §2.2/§2.3、18 D2，B9 批次）
--    t_checkitem 65 行（id 28~92，02-seed.sql 已有）逐项补齐 ext 与至少一条 ref。
--    用途：判定引擎按 性别 + 年龄段 + 生理状态 匹配区间（18 D2），
--    t_health_checkitem_ext 0 行导致 B7 解析回落文本型，本脚本闭合。
--    设计口径（17 §1.16）：
--    * 身高(28)/体重(29) 故意不种 ref —— 负向实测「匹配不到 → 区间未配置 → 不判正常」；
--    * 性别双区间（RBC/HGB/HCT/ALT/AST/GGT/肌酐/尿酸/CK）按 sex=1/2 两条，specificity 自动优先；
--    * 年龄分层演示仅 TSH（儿童 ≤12 岁专用区间）；
--    * 无单位数值项（尿比重/尿酸碱度/视力）unit 以 '-' 占位，正式数据以院方 LIS 为准；
--    * 区间数值为常见体检参考示例，非院方定稿（正式上线前须按 18 §3 D2 L1/L2 来源复核）。
--    幂等：固定主键 + INSERT IGNORE / ON DUPLICATE KEY UPDATE，可重复执行。
-- -----------------------------------------------------------------------------

-- 11.1 一般检查（dept 102 内科）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000001, 28, '0', 'cm',    102, NULL,   '0', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000002, 29, '0', 'kg',    102, NULL,   '0', '0', '0', '0', 2, NULL, NULL, NOW()),
  (1949003100000000003, 30, '0', 'kg/m²', 102, NULL,   '0', '0', '0', '0', 3, NULL, NULL, NOW()),
  (1949003100000000004, 31, '0', 'mmHg',  102, NULL,   '0', '0', '0', '0', 4, NULL, NULL, NOW()),
  (1949003100000000005, 32, '0', 'mmHg',  102, NULL,   '0', '0', '0', '0', 5, NULL, NULL, NOW()),
  (1949003100000000006, 33, '0', '-',     105, NULL,   '0', '0', '0', '0', 6, NULL, NULL, NOW()),
  (1949003100000000007, 34, '0', '-',     105, NULL,   '0', '0', '0', '0', 7, NULL, NULL, NOW()),
  (1949003100000000008, 35, '0', '-',     105, NULL,   '0', '0', '0', '0', 8, NULL, NULL, NOW()),
  (1949003100000000009, 36, '0', '-',     105, NULL,   '0', '0', '0', '0', 9, NULL, NULL, NOW()),
  (1949003100000000010, 37, '1', NULL,    105, NULL,   '0', '0', '0', '0', 10, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.2 血常规（dept 108 检验科，标本全血）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000011, 38, '0', '×10⁹/L',  108, '全血', '0', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000012, 39, '0', '×10¹²/L', 108, '全血', '0', '0', '0', '0', 2, NULL, NULL, NOW()),
  (1949003100000000013, 40, '0', 'g/L',     108, '全血', '0', '0', '0', '0', 3, NULL, NULL, NOW()),
  (1949003100000000014, 41, '0', '%',       108, '全血', '0', '0', '0', '0', 4, NULL, NULL, NOW()),
  (1949003100000000015, 42, '0', 'fL',      108, '全血', '0', '0', '0', '0', 5, NULL, NULL, NOW()),
  (1949003100000000016, 43, '0', 'pg',      108, '全血', '0', '0', '0', '0', 6, NULL, NULL, NOW()),
  (1949003100000000017, 44, '0', 'g/L',     108, '全血', '0', '0', '0', '0', 7, NULL, NULL, NOW()),
  (1949003100000000018, 45, '0', '%',       108, '全血', '0', '0', '0', '0', 8, NULL, NULL, NOW()),
  (1949003100000000019, 46, '0', '×10⁹/L',  108, '全血', '0', '0', '0', '0', 9, NULL, NULL, NOW()),
  (1949003100000000020, 47, '0', 'fL',      108, '全血', '0', '0', '0', '0', 10, NULL, NULL, NOW()),
  (1949003100000000021, 48, '0', '%',       108, '全血', '0', '0', '0', '0', 11, NULL, NULL, NOW()),
  (1949003100000000022, 49, '0', '%',       108, '全血', '0', '0', '0', '0', 12, NULL, NULL, NOW()),
  (1949003100000000023, 50, '0', '%',       108, '全血', '0', '0', '0', '0', 13, NULL, NULL, NOW()),
  (1949003100000000024, 51, '0', '%',       108, '全血', '0', '0', '0', '0', 14, NULL, NULL, NOW()),
  (1949003100000000025, 52, '0', '×10⁹/L',  108, '全血', '0', '0', '0', '0', 15, NULL, NULL, NOW()),
  (1949003100000000026, 53, '0', '×10⁹/L',  108, '全血', '0', '0', '0', '0', 16, NULL, NULL, NOW()),
  (1949003100000000027, 54, '0', '×10⁹/L',  108, '全血', '0', '0', '0', '0', 17, NULL, NULL, NOW()),
  (1949003100000000028, 55, '0', 'fL',      108, '全血', '0', '0', '0', '0', 18, NULL, NULL, NOW()),
  (1949003100000000029, 56, '0', '%',       108, '全血', '0', '0', '0', '0', 19, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.3 尿常规（dept 108 检验科，标本尿液）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000030, 57, '0', '-',    108, '尿液', '0', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000031, 58, '0', '-',    108, '尿液', '0', '0', '0', '0', 2, NULL, NULL, NOW()),
  (1949003100000000032, 59, '1', NULL,   108, '尿液', '0', '0', '0', '0', 3, NULL, NULL, NOW()),
  (1949003100000000033, 60, '1', NULL,   108, '尿液', '0', '0', '0', '0', 4, NULL, NULL, NOW()),
  (1949003100000000034, 61, '1', NULL,   108, '尿液', '0', '0', '0', '0', 5, NULL, NULL, NOW()),
  (1949003100000000035, 62, '1', NULL,   108, '尿液', '0', '0', '0', '0', 6, NULL, NULL, NOW()),
  (1949003100000000036, 63, '1', NULL,   108, '尿液', '0', '0', '0', '0', 7, NULL, NULL, NOW()),
  (1949003100000000037, 64, '1', NULL,   108, '尿液', '0', '0', '0', '0', 8, NULL, NULL, NOW()),
  (1949003100000000038, 65, '1', NULL,   108, '尿液', '0', '0', '0', '0', 9, NULL, NULL, NOW()),
  (1949003100000000039, 66, '1', NULL,   108, '尿液', '0', '0', '0', '0', 10, NULL, NULL, NOW()),
  (1949003100000000040, 67, '0', '/HP',  108, '尿液', '0', '0', '0', '0', 11, NULL, NULL, NOW()),
  (1949003100000000041, 68, '0', '/HP',  108, '尿液', '0', '0', '0', '0', 12, NULL, NULL, NOW()),
  (1949003100000000042, 69, '0', '/HP',  108, '尿液', '0', '0', '0', '0', 13, NULL, NULL, NOW()),
  (1949003100000000043, 70, '1', NULL,   108, '尿液', '0', '0', '0', '0', 14, NULL, NULL, NOW()),
  (1949003100000000044, 71, '1', NULL,   108, '尿液', '0', '0', '0', '0', 15, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.4 生化（dept 108 检验科，标本血清，需空腹）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000045, 72, '0', 'U/L',     108, '血清', '1', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000046, 73, '0', 'U/L',     108, '血清', '1', '0', '0', '0', 2, NULL, NULL, NOW()),
  (1949003100000000047, 74, '0', 'U/L',     108, '血清', '1', '0', '0', '0', 3, NULL, NULL, NOW()),
  (1949003100000000048, 75, '0', 'mmol/L',  108, '血清', '1', '0', '0', '0', 4, NULL, NULL, NOW()),
  (1949003100000000049, 76, '0', 'µmol/L',  108, '血清', '1', '0', '0', '0', 5, NULL, NULL, NOW()),
  (1949003100000000050, 77, '0', 'µmol/L',  108, '血清', '1', '0', '0', '0', 6, NULL, NULL, NOW()),
  (1949003100000000051, 78, '0', 'mmol/L',  108, '血清', '1', '0', '0', '0', 7, NULL, NULL, NOW()),
  (1949003100000000052, 79, '0', 'mmol/L',  108, '血清', '1', '0', '0', '0', 8, NULL, NULL, NOW()),
  (1949003100000000053, 80, '0', 'mmol/L',  108, '血清', '1', '0', '0', '0', 9, NULL, NULL, NOW()),
  (1949003100000000054, 81, '0', 'mmol/L',  108, '血清', '1', '0', '0', '0', 10, NULL, NULL, NOW()),
  (1949003100000000055, 82, '0', 'U/L',     108, '血清', '1', '0', '0', '0', 11, NULL, NULL, NOW()),
  (1949003100000000056, 83, '0', 'U/L',     108, '血清', '1', '0', '0', '0', 12, NULL, NULL, NOW()),
  (1949003100000000057, 84, '0', 'U/L',     108, '血清', '1', '0', '0', '0', 13, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.5 甲状腺功能（dept 108 检验科，标本血清）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000058, 85, '0', 'nmol/L', 108, '血清', '0', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000059, 86, '0', 'nmol/L', 108, '血清', '0', '0', '0', '0', 2, NULL, NULL, NOW()),
  (1949003100000000060, 87, '0', 'mIU/L',  108, '血清', '0', '0', '0', '0', 3, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.6 妇科超声（dept 110 超声科，影像结论）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000061, 88, '3', NULL, 110, NULL, '0', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000062, 89, '3', NULL, 110, NULL, '0', '0', '0', '0', 2, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.7 胆红素三项（dept 108 检验科，标本血清，需空腹）
INSERT INTO `t_health_checkitem_ext`
  (`id`, `checkitem_id`, `result_type`, `unit`, `dept_id`, `specimen`, `is_fasting`, `is_radiation`, `is_required`, `dual_dept_flag`, `sort_no`, `default_conclusion_id`, `notice_text`, `create_time`)
VALUES
  (1949003100000000063, 90, '0', 'µmol/L', 108, '血清', '1', '0', '0', '0', 1, NULL, NULL, NOW()),
  (1949003100000000064, 91, '0', 'µmol/L', 108, '血清', '1', '0', '0', '0', 2, NULL, NULL, NOW()),
  (1949003100000000065, 92, '0', 'µmol/L', 108, '血清', '1', '0', '0', '0', 3, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE
  `result_type` = VALUES(`result_type`), `unit` = VALUES(`unit`), `dept_id` = VALUES(`dept_id`),
  `specimen` = VALUES(`specimen`), `is_fasting` = VALUES(`is_fasting`), `is_radiation` = VALUES(`is_radiation`),
  `is_required` = VALUES(`is_required`), `dual_dept_flag` = VALUES(`dual_dept_flag`), `sort_no` = VALUES(`sort_no`),
  `notice_text` = VALUES(`notice_text`);

-- 11.8 参考区间（checkitem_id 28~92；身高 28/体重 29 故意不种，负向实测「区间未配置」）
INSERT IGNORE INTO `t_health_checkitem_ref`
  (`id`, `checkitem_id`, `sex`, `age_min`, `age_max`, `physio_state`, `ref_text`, `ref_min`, `ref_max`, `priority`, `create_time`)
VALUES
  -- 一般检查
  (1949003200000000001, 30, '0', NULL, NULL, '0', '18.5~23.9 kg/m²',  18.5000, 23.9000, 100, NOW()),
  (1949003200000000002, 31, '0', NULL, NULL, '0', '90~139 mmHg',       90.0000, 139.0000, 100, NOW()),
  (1949003200000000003, 32, '0', NULL, NULL, '0', '60~89 mmHg',        60.0000, 89.0000, 100, NOW()),
  (1949003200000000004, 33, '0', NULL, NULL, '0', '≥1.0',              1.0000, NULL, 100, NOW()),
  (1949003200000000005, 34, '0', NULL, NULL, '0', '≥1.0',              1.0000, NULL, 100, NOW()),
  (1949003200000000006, 35, '0', NULL, NULL, '0', '≥1.0',              1.0000, NULL, 100, NOW()),
  (1949003200000000007, 36, '0', NULL, NULL, '0', '≥1.0',              1.0000, NULL, 100, NOW()),
  (1949003200000000008, 37, '0', NULL, NULL, '0', '正常（红绿色觉正常）', NULL, NULL, 100, NOW()),
  -- 血常规
  (1949003200000000009, 38, '0', NULL, NULL, '0', '3.5~9.5 ×10⁹/L',   3.5000, 9.5000, 100, NOW()),
  (1949003200000000010, 39, '1', NULL, NULL, '0', '4.3~5.8 ×10¹²/L',   4.3000, 5.8000, 100, NOW()),
  (1949003200000000011, 39, '2', NULL, NULL, '0', '3.8~5.1 ×10¹²/L',   3.8000, 5.1000, 100, NOW()),
  (1949003200000000012, 40, '1', NULL, NULL, '0', '130~175 g/L',       130.0000, 175.0000, 100, NOW()),
  (1949003200000000013, 40, '2', NULL, NULL, '0', '115~150 g/L',       115.0000, 150.0000, 100, NOW()),
  (1949003200000000014, 41, '1', NULL, NULL, '0', '40~50 %',           40.0000, 50.0000, 100, NOW()),
  (1949003200000000015, 41, '2', NULL, NULL, '0', '35~45 %',           35.0000, 45.0000, 100, NOW()),
  (1949003200000000016, 42, '0', NULL, NULL, '0', '82~100 fL',         82.0000, 100.0000, 100, NOW()),
  (1949003200000000017, 43, '0', NULL, NULL, '0', '27~34 pg',          27.0000, 34.0000, 100, NOW()),
  (1949003200000000018, 44, '0', NULL, NULL, '0', '316~354 g/L',       316.0000, 354.0000, 100, NOW()),
  (1949003200000000019, 45, '0', NULL, NULL, '0', '11.0~16.0 %',       11.0000, 16.0000, 100, NOW()),
  (1949003200000000020, 46, '0', NULL, NULL, '0', '125~350 ×10⁹/L',    125.0000, 350.0000, 100, NOW()),
  (1949003200000000021, 47, '0', NULL, NULL, '0', '7~13 fL',           7.0000, 13.0000, 100, NOW()),
  (1949003200000000022, 48, '0', NULL, NULL, '0', '9~17 %',            9.0000, 17.0000, 100, NOW()),
  (1949003200000000023, 49, '0', NULL, NULL, '0', '20~50 %',           20.0000, 50.0000, 100, NOW()),
  (1949003200000000024, 50, '0', NULL, NULL, '0', '3~12 %',            3.0000, 12.0000, 100, NOW()),
  (1949003200000000025, 51, '0', NULL, NULL, '0', '40~75 %',           40.0000, 75.0000, 100, NOW()),
  (1949003200000000026, 52, '0', NULL, NULL, '0', '1.1~3.2 ×10⁹/L',    1.1000, 3.2000, 100, NOW()),
  (1949003200000000027, 53, '0', NULL, NULL, '0', '0.1~1.0 ×10⁹/L',    0.1000, 1.0000, 100, NOW()),
  (1949003200000000028, 54, '0', NULL, NULL, '0', '1.8~6.3 ×10⁹/L',    1.8000, 6.3000, 100, NOW()),
  (1949003200000000029, 55, '0', NULL, NULL, '0', '35~56 fL',          35.0000, 56.0000, 100, NOW()),
  (1949003200000000030, 56, '0', NULL, NULL, '0', '0.108~0.282 %',     0.1080, 0.2820, 100, NOW()),
  -- 尿常规
  (1949003200000000031, 57, '0', NULL, NULL, '0', '1.003~1.030',       1.0030, 1.0300, 100, NOW()),
  (1949003200000000032, 58, '0', NULL, NULL, '0', '4.5~8.0',           4.5000, 8.0000, 100, NOW()),
  (1949003200000000033, 59, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000034, 60, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000035, 61, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000036, 62, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000037, 63, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000038, 64, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000039, 65, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000040, 66, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000041, 67, '0', NULL, NULL, '0', '0~3 /HP',           0.0000, 3.0000, 100, NOW()),
  (1949003200000000042, 68, '0', NULL, NULL, '0', '0~5 /HP',           0.0000, 5.0000, 100, NOW()),
  (1949003200000000043, 69, '0', NULL, NULL, '0', '0~5 /HP',           0.0000, 5.0000, 100, NOW()),
  (1949003200000000044, 70, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  (1949003200000000045, 71, '0', NULL, NULL, '0', '阴性',              NULL, NULL, 100, NOW()),
  -- 生化
  (1949003200000000046, 72, '1', NULL, NULL, '0', '9~50 U/L',          9.0000, 50.0000, 100, NOW()),
  (1949003200000000047, 72, '2', NULL, NULL, '0', '7~40 U/L',          7.0000, 40.0000, 100, NOW()),
  (1949003200000000048, 73, '1', NULL, NULL, '0', '15~40 U/L',         15.0000, 40.0000, 100, NOW()),
  (1949003200000000049, 73, '2', NULL, NULL, '0', '13~35 U/L',         13.0000, 35.0000, 100, NOW()),
  (1949003200000000050, 74, '1', NULL, NULL, '0', '10~60 U/L',         10.0000, 60.0000, 100, NOW()),
  (1949003200000000051, 74, '2', NULL, NULL, '0', '7~45 U/L',          7.0000, 45.0000, 100, NOW()),
  (1949003200000000052, 75, '0', NULL, NULL, '0', '2.6~7.5 mmol/L',    2.6000, 7.5000, 100, NOW()),
  (1949003200000000053, 76, '1', NULL, NULL, '0', '57~97 µmol/L',      57.0000, 97.0000, 100, NOW()),
  (1949003200000000054, 76, '2', NULL, NULL, '0', '41~73 µmol/L',      41.0000, 73.0000, 100, NOW()),
  (1949003200000000055, 77, '1', NULL, NULL, '0', '208~428 µmol/L',    208.0000, 428.0000, 100, NOW()),
  (1949003200000000056, 77, '2', NULL, NULL, '0', '155~357 µmol/L',    155.0000, 357.0000, 100, NOW()),
  (1949003200000000057, 78, '0', NULL, NULL, '0', '<5.2 mmol/L',       NULL, 5.2000, 100, NOW()),
  (1949003200000000058, 79, '0', NULL, NULL, '0', '<1.7 mmol/L',       NULL, 1.7000, 100, NOW()),
  (1949003200000000059, 80, '0', NULL, NULL, '0', '>1.0 mmol/L',       1.0000, NULL, 100, NOW()),
  (1949003200000000060, 81, '0', NULL, NULL, '0', '<3.4 mmol/L',       NULL, 3.4000, 100, NOW()),
  (1949003200000000061, 82, '1', NULL, NULL, '0', '50~310 U/L',        50.0000, 310.0000, 100, NOW()),
  (1949003200000000062, 82, '2', NULL, NULL, '0', '40~200 U/L',        40.0000, 200.0000, 100, NOW()),
  (1949003200000000063, 83, '0', NULL, NULL, '0', '0~24 U/L',          0.0000, 24.0000, 100, NOW()),
  (1949003200000000064, 84, '0', NULL, NULL, '0', '120~250 U/L',       120.0000, 250.0000, 100, NOW()),
  -- 甲状腺功能（TSH 年龄分层演示：≤12 岁儿童专用区间，specificity 自动优先）
  (1949003200000000065, 85, '0', NULL, NULL, '0', '1.3~3.1 nmol/L',    1.3000, 3.1000, 100, NOW()),
  (1949003200000000066, 86, '0', NULL, NULL, '0', '66~181 nmol/L',     66.0000, 181.0000, 100, NOW()),
  (1949003200000000067, 87, '0', NULL, NULL, '0', '0.27~4.2 mIU/L',    0.2700, 4.2000, 100, NOW()),
  (1949003200000000068, 87, '0', NULL, 12,    '0', '0.7~6.4 mIU/L',    0.7000, 6.4000, 100, NOW()),
  -- 妇科超声（影像结论）
  (1949003200000000069, 88, '2', NULL, NULL, '0', '未见明显异常',      NULL, NULL, 100, NOW()),
  (1949003200000000070, 89, '2', NULL, NULL, '0', '未见明显异常',      NULL, NULL, 100, NOW()),
  -- 胆红素
  (1949003200000000071, 90, '0', NULL, NULL, '0', '3.4~17.1 µmol/L',   3.4000, 17.1000, 100, NOW()),
  (1949003200000000072, 91, '0', NULL, NULL, '0', '0~6.8 µmol/L',      0.0000, 6.8000, 100, NOW()),
  (1949003200000000073, 92, '0', NULL, NULL, '0', '1.7~10.2 µmol/L',   1.7000, 10.2000, 100, NOW());
