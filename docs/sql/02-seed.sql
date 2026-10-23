-- =============================================================================
-- 心飞健康 种子脚本（幂等，可重复执行）
-- 依据：docs/requirements/03 §13、04 §7
-- 内容：① health:* 菜单与权限点（sys_menu）② 原系统示例业务数据（t_*）
-- 说明：超级管理员 role_id=1 在框架内走 *:*:* 通配，不需要 sys_role_menu 绑定；
--       非超级角色按需经「角色管理」勾选下发，故本脚本不写 sys_role_menu。
-- 禁止：改动 t_* 冻结表结构；在生产库执行 DDL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 预约管理菜单与权限点（08 §3.1~3.3）
--    C 型菜单沿用框架惯例承载 list 权限；F 型承载 query/add/edit/remove
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_dept`,`create_by`,`create_time`,`remark`)
VALUES (2000,'预约管理',0,5,'appointment','','',1,0,'M','0','0','','date',103,1,NOW(),'体检基础数据与预约管理目录')
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`),`parent_id`=VALUES(`parent_id`),`order_num`=VALUES(`order_num`),`path`=VALUES(`path`),`menu_type`=VALUES(`menu_type`),`icon`=VALUES(`icon`),`remark`=VALUES(`remark`);

INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_dept`,`create_by`,`create_time`,`remark`)
VALUES
(2010,'检查项管理',2000,1,'checkitem','','',1,0,'C','0','0','health:checkitem:list','list',103,1,NOW(),'静态页 /pages/checkitem.html'),
(2011,'检查项查询',2010,1,'#','',     '',1,0,'F','0','0','health:checkitem:query','#',103,1,NOW(),''),
(2012,'检查项新增',2010,2,'#','',     '',1,0,'F','0','0','health:checkitem:add','#',103,1,NOW(),''),
(2013,'检查项修改',2010,3,'#','',     '',1,0,'F','0','0','health:checkitem:edit','#',103,1,NOW(),''),
(2014,'检查项删除',2010,4,'#','',     '',1,0,'F','0','0','health:checkitem:remove','#',103,1,NOW(),''),
(2020,'检查组管理',2000,2,'checkgroup','','',1,0,'C','0','0','health:checkgroup:list','tree-table',103,1,NOW(),'静态页 /pages/checkgroup.html'),
(2021,'检查组查询',2020,1,'#','',     '',1,0,'F','0','0','health:checkgroup:query','#',103,1,NOW(),''),
(2022,'检查组新增',2020,2,'#','',     '',1,0,'F','0','0','health:checkgroup:add','#',103,1,NOW(),''),
(2023,'检查组修改',2020,3,'#','',     '',1,0,'F','0','0','health:checkgroup:edit','#',103,1,NOW(),''),
(2024,'检查组删除',2020,4,'#','',     '',1,0,'F','0','0','health:checkgroup:remove','#',103,1,NOW(),''),
(2030,'套餐管理',2000,3,'setmeal','','',1,0,'C','0','0','health:setmeal:list','shopping',103,1,NOW(),'静态页 /pages/setmeal.html'),
(2031,'套餐查询',2030,1,'#','',     '',1,0,'F','0','0','health:setmeal:query','#',103,1,NOW(),''),
(2032,'套餐新增',2030,2,'#','',     '',1,0,'F','0','0','health:setmeal:add','#',103,1,NOW(),''),
(2033,'套餐修改',2030,3,'#','',     '',1,0,'F','0','0','health:setmeal:edit','#',103,1,NOW(),''),
(2034,'套餐删除',2030,4,'#','',     '',1,0,'F','0','0','health:setmeal:remove','#',103,1,NOW(),''),
(2040,'预约设置',2000,4,'ordersetting','','',1,0,'C','0','0','health:ordersetting:list','date-range',103,1,NOW(),'静态页 /pages/ordersetting.html'),
(2041,'名额修改',2040,1,'#','',     '',1,0,'F','0','0','health:ordersetting:edit','#',103,1,NOW(),''),
(2042,'名额导入',2040,2,'#','',     '',1,0,'F','0','0','health:ordersetting:import','#',103,1,NOW(),''),
(2050,'预约列表',2000,5,'orders','','',1,0,'C','0','0','health:order:list','log',103,1,NOW(),'静态页 /pages/orders.html'),
(2051,'订单查询',2050,1,'#','',     '',1,0,'F','0','0','health:order:query','#',103,1,NOW(),''),
(2052,'订单流转',2050,2,'#','',     '',1,0,'F','0','0','health:order:edit','#',103,1,NOW(),''),
(2053,'订单取消',2050,3,'#','',     '',1,0,'F','0','0','health:order:cancel','#',103,1,NOW(),''),
(2060,'禁忌规则',2000,6,'forbids','','',1,0,'C','0','0','health:forbid:list','warning',103,1,NOW(),'静态页 /pages/forbids.html；试算走 health:order:query'),
(2061,'禁忌新增',2060,1,'#','',     '',1,0,'F','0','0','health:forbid:add','#',103,1,NOW(),''),
(2062,'禁忌修改',2060,2,'#','',     '',1,0,'F','0','0','health:forbid:edit','#',103,1,NOW(),''),
(2063,'禁忌删除',2060,3,'#','',     '',1,0,'F','0','0','health:forbid:remove','#',103,1,NOW(),'')
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`),`parent_id`=VALUES(`parent_id`),`order_num`=VALUES(`order_num`),`path`=VALUES(`path`),`menu_type`=VALUES(`menu_type`),`perms`=VALUES(`perms`),`icon`=VALUES(`icon`),`remark`=VALUES(`remark`);

-- -----------------------------------------------------------------------------
-- 2. 示例业务数据
--    数据源：legacy/backend/health.sql（原系统演示库），按外键依赖顺序插入
--    INSERT IGNORE 保证幂等；t_setmeal.img 保留原始 Qiniu 文件名，
--    新系统该列语义为 ossId，旧值解析不出时前端显示占位图，重新上传即覆盖
-- -----------------------------------------------------------------------------
-- t_checkitem
INSERT IGNORE INTO `t_checkitem` VALUES ('28', '0001', '身高', '0', '0-100', '5', '1', '无', '身高');
INSERT IGNORE INTO `t_checkitem` VALUES ('29', '0002', '体重', '0', '0-100', '5', '1', '无', '体重');
INSERT IGNORE INTO `t_checkitem` VALUES ('30', '0003', '体重指数', '0', '0-100', '5', '1', '无', '体重指数');
INSERT IGNORE INTO `t_checkitem` VALUES ('31', '0004', '收缩压', '0', '0-100', '5', '1', '无', '收缩压');
INSERT IGNORE INTO `t_checkitem` VALUES ('32', '0005', '舒张压', '0', '0-100', '5', '1', '无', '舒张压');
INSERT IGNORE INTO `t_checkitem` VALUES ('33', '0006', '裸视力（右）', '0', '0-100', '5', '1', '无', '裸视力（右）');
INSERT IGNORE INTO `t_checkitem` VALUES ('34', '0007', '裸视力（左）', '0', '0-100', '5', '1', '无', '裸视力（左）');
INSERT IGNORE INTO `t_checkitem` VALUES ('35', '0008', '矫正视力（右）', '0', '0-100', '5', '1', '无', '矫正视力（右）');
INSERT IGNORE INTO `t_checkitem` VALUES ('36', '0009', '矫正视力（左）', '0', '0-100', '5', '1', '无', '矫正视力（左）');
INSERT IGNORE INTO `t_checkitem` VALUES ('37', '0010', '色觉', '0', '0-100', '5', '1', '无', '色觉');
INSERT IGNORE INTO `t_checkitem` VALUES ('38', '0011', '白细胞计数', '0', '0-100', '10', '2', '无', '白细胞计数');
INSERT IGNORE INTO `t_checkitem` VALUES ('39', '0012', '红细胞计数', '0', '0-100', '10', '2', null, '红细胞计数');
INSERT IGNORE INTO `t_checkitem` VALUES ('40', '0013', '血红蛋白', '0', '0-100', '10', '2', null, '血红蛋白');
INSERT IGNORE INTO `t_checkitem` VALUES ('41', '0014', '红细胞压积', '0', '0-100', '10', '2', null, '红细胞压积');
INSERT IGNORE INTO `t_checkitem` VALUES ('42', '0015', '平均红细胞体积', '0', '0-100', '10', '2', null, '平均红细胞体积');
INSERT IGNORE INTO `t_checkitem` VALUES ('43', '0016', '平均红细胞血红蛋白含量', '0', '0-100', '10', '2', null, '平均红细胞血红蛋白含量');
INSERT IGNORE INTO `t_checkitem` VALUES ('44', '0017', '平均红细胞血红蛋白浓度', '0', '0-100', '10', '2', null, '平均红细胞血红蛋白浓度');
INSERT IGNORE INTO `t_checkitem` VALUES ('45', '0018', '红细胞分布宽度-变异系数', '0', '0-100', '10', '2', null, '红细胞分布宽度-变异系数');
INSERT IGNORE INTO `t_checkitem` VALUES ('46', '0019', '血小板计数', '0', '0-100', '10', '2', null, '血小板计数');
INSERT IGNORE INTO `t_checkitem` VALUES ('47', '0020', '平均血小板体积', '0', '0-100', '10', '2', null, '平均血小板体积');
INSERT IGNORE INTO `t_checkitem` VALUES ('48', '0021', '血小板分布宽度', '0', '0-100', '10', '2', null, '血小板分布宽度');
INSERT IGNORE INTO `t_checkitem` VALUES ('49', '0022', '淋巴细胞百分比', '0', '0-100', '10', '2', null, '淋巴细胞百分比');
INSERT IGNORE INTO `t_checkitem` VALUES ('50', '0023', '中间细胞百分比', '0', '0-100', '10', '2', null, '中间细胞百分比');
INSERT IGNORE INTO `t_checkitem` VALUES ('51', '0024', '中性粒细胞百分比', '0', '0-100', '10', '2', null, '中性粒细胞百分比');
INSERT IGNORE INTO `t_checkitem` VALUES ('52', '0025', '淋巴细胞绝对值', '0', '0-100', '10', '2', null, '淋巴细胞绝对值');
INSERT IGNORE INTO `t_checkitem` VALUES ('53', '0026', '中间细胞绝对值', '0', '0-100', '10', '2', null, '中间细胞绝对值');
INSERT IGNORE INTO `t_checkitem` VALUES ('54', '0027', '中性粒细胞绝对值', '0', '0-100', '10', '2', null, '中性粒细胞绝对值');
INSERT IGNORE INTO `t_checkitem` VALUES ('55', '0028', '红细胞分布宽度-标准差', '0', '0-100', '10', '2', null, '红细胞分布宽度-标准差');
INSERT IGNORE INTO `t_checkitem` VALUES ('56', '0029', '血小板压积', '0', '0-100', '10', '2', null, '血小板压积');
INSERT IGNORE INTO `t_checkitem` VALUES ('57', '0030', '尿比重', '0', '0-100', '10', '2', null, '尿比重');
INSERT IGNORE INTO `t_checkitem` VALUES ('58', '0031', '尿酸碱度', '0', '0-100', '10', '2', null, '尿酸碱度');
INSERT IGNORE INTO `t_checkitem` VALUES ('59', '0032', '尿白细胞', '0', '0-100', '10', '2', null, '尿白细胞');
INSERT IGNORE INTO `t_checkitem` VALUES ('60', '0033', '尿亚硝酸盐', '0', '0-100', '10', '2', null, '尿亚硝酸盐');
INSERT IGNORE INTO `t_checkitem` VALUES ('61', '0034', '尿蛋白质', '0', '0-100', '10', '2', null, '尿蛋白质');
INSERT IGNORE INTO `t_checkitem` VALUES ('62', '0035', '尿糖', '0', '0-100', '10', '2', null, '尿糖');
INSERT IGNORE INTO `t_checkitem` VALUES ('63', '0036', '尿酮体', '0', '0-100', '10', '2', null, '尿酮体');
INSERT IGNORE INTO `t_checkitem` VALUES ('64', '0037', '尿胆原', '0', '0-100', '10', '2', null, '尿胆原');
INSERT IGNORE INTO `t_checkitem` VALUES ('65', '0038', '尿胆红素', '0', '0-100', '10', '2', null, '尿胆红素');
INSERT IGNORE INTO `t_checkitem` VALUES ('66', '0039', '尿隐血', '0', '0-100', '10', '2', null, '尿隐血');
INSERT IGNORE INTO `t_checkitem` VALUES ('67', '0040', '尿镜检红细胞', '0', '0-100', '10', '2', null, '尿镜检红细胞');
INSERT IGNORE INTO `t_checkitem` VALUES ('68', '0041', '尿镜检白细胞', '0', '0-100', '10', '2', null, '尿镜检白细胞');
INSERT IGNORE INTO `t_checkitem` VALUES ('69', '0042', '上皮细胞', '0', '0-100', '10', '2', null, '上皮细胞');
INSERT IGNORE INTO `t_checkitem` VALUES ('70', '0043', '无机盐类', '0', '0-100', '10', '2', null, '无机盐类');
INSERT IGNORE INTO `t_checkitem` VALUES ('71', '0044', '尿镜检蛋白定性', '0', '0-100', '10', '2', null, '尿镜检蛋白定性');
INSERT IGNORE INTO `t_checkitem` VALUES ('72', '0045', '丙氨酸氨基转移酶', '0', '0-100', '10', '2', null, '丙氨酸氨基转移酶');
INSERT IGNORE INTO `t_checkitem` VALUES ('73', '0046', '天门冬氨酸氨基转移酶', '0', '0-100', '10', '2', null, '天门冬氨酸氨基转移酶');
INSERT IGNORE INTO `t_checkitem` VALUES ('74', '0047', 'Y-谷氨酰转移酶', '0', '0-100', '10', '2', null, 'Y-谷氨酰转移酶');
INSERT IGNORE INTO `t_checkitem` VALUES ('75', '0048', '尿素', '0', '0-100', '10', '2', null, '尿素');
INSERT IGNORE INTO `t_checkitem` VALUES ('76', '0049', '肌酐', '0', '0-100', '10', '2', null, '肌酐');
INSERT IGNORE INTO `t_checkitem` VALUES ('77', '0050', '尿酸', '0', '0-100', '10', '2', null, '尿酸');
INSERT IGNORE INTO `t_checkitem` VALUES ('78', '0051', '总胆固醇', '0', '0-100', '10', '2', null, '总胆固醇');
INSERT IGNORE INTO `t_checkitem` VALUES ('79', '0052', '甘油三酯', '0', '0-100', '10', '2', null, '甘油三酯');
INSERT IGNORE INTO `t_checkitem` VALUES ('80', '0053', '高密度脂蛋白胆固醇', '0', '0-100', '10', '2', null, '高密度脂蛋白胆固醇');
INSERT IGNORE INTO `t_checkitem` VALUES ('81', '0054', '低密度脂蛋白胆固醇', '0', '0-100', '10', '2', null, '低密度脂蛋白胆固醇');
INSERT IGNORE INTO `t_checkitem` VALUES ('82', '0055', '磷酸肌酸激酶', '0', '0-100', '10', '2', null, '磷酸肌酸激酶');
INSERT IGNORE INTO `t_checkitem` VALUES ('83', '0056', '磷酸肌酸激酶同工酶', '0', '0-100', '10', '2', null, '磷酸肌酸激酶同工酶');
INSERT IGNORE INTO `t_checkitem` VALUES ('84', '0057', '乳酸脱氢酶', '0', '0-100', '10', '2', null, '乳酸脱氢酶');
INSERT IGNORE INTO `t_checkitem` VALUES ('85', '0058', '三碘甲状腺原氨酸', '0', '0-100', '10', '2', null, '三碘甲状腺原氨酸');
INSERT IGNORE INTO `t_checkitem` VALUES ('86', '0059', '甲状腺素', '0', '0-100', '10', '2', null, '甲状腺素');
INSERT IGNORE INTO `t_checkitem` VALUES ('87', '0060', '促甲状腺激素', '0', '0-100', '10', '2', null, '促甲状腺激素');
INSERT IGNORE INTO `t_checkitem` VALUES ('88', '0061', '子宫', '2', '0-100', '10', '2', null, '子宫');
INSERT IGNORE INTO `t_checkitem` VALUES ('89', '0062', '附件', '2', '0-100', '10', '2', null, '附件');
INSERT IGNORE INTO `t_checkitem` VALUES ('90', '0063', '总胆红素', '0', '0-100', '10', '2', null, '总胆红素');
INSERT IGNORE INTO `t_checkitem` VALUES ('91', '0064', '直接胆红素', '0', '0-100', '10', '2', null, '直接胆红素');
INSERT IGNORE INTO `t_checkitem` VALUES ('92', '0065', '间接胆红素', '0', '0-100', '10', '2', null, '间接胆红素');

-- t_checkgroup
INSERT IGNORE INTO `t_checkgroup` VALUES ('5', '0001', '一般检查', 'YBJC', '0', '一般检查', '无');
INSERT IGNORE INTO `t_checkgroup` VALUES ('6', '0002', '视力色觉', 'SLSJ', '0', '视力色觉', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('7', '0003', '血常规', 'XCG', '0', '血常规', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('8', '0004', '尿常规', 'NCG', '0', '尿常规', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('9', '0005', '肝功三项', 'GGSX', '0', '肝功三项', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('10', '0006', '肾功三项', 'NGSX', '0', '肾功三项', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('11', '0007', '血脂四项', 'XZSX', '0', '血脂四项', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('12', '0008', '心肌酶三项', 'XJMSX', '0', '心肌酶三项', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('13', '0009', '甲功三项', 'JGSX', '0', '甲功三项', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('14', '0010', '子宫附件彩超', 'ZGFJCC', '2', '子宫附件彩超', null);
INSERT IGNORE INTO `t_checkgroup` VALUES ('15', '0011', '胆红素三项', 'DHSSX', '0', '胆红素三项', null);

-- t_checkgroup_checkitem
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('5', '28');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('5', '29');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('5', '30');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('5', '31');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('5', '32');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('6', '33');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('6', '34');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('6', '35');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('6', '36');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('6', '37');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '38');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '39');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '40');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '41');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '42');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '43');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '44');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '45');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '46');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '47');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '48');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '49');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '50');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '51');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '52');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '53');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '54');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '55');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('7', '56');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '57');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '58');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '59');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '60');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '61');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '62');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '63');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '64');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '65');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '66');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '67');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '68');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '69');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '70');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('8', '71');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('9', '72');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('9', '73');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('9', '74');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('10', '75');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('10', '76');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('10', '77');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('11', '78');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('11', '79');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('11', '80');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('11', '81');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('12', '82');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('12', '83');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('12', '84');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('13', '85');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('13', '86');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('13', '87');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('14', '88');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('14', '89');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('15', '90');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('15', '91');
INSERT IGNORE INTO `t_checkgroup_checkitem` VALUES ('15', '92');

-- t_setmeal
INSERT IGNORE INTO `t_setmeal` VALUES ('5', '入职无忧体检套餐（男女通用）', '0001', 'RZTJ', '0', '18-60', '300', '入职体检套餐', null, '03a36073-a140-4942-9b9b-712cecb144901.jpg');
INSERT IGNORE INTO `t_setmeal` VALUES ('6', '粉红珍爱(女)升级TM12项筛查体检套餐', '0002', 'FHZA', '2', '18-60', '1200', '本套餐针对宫颈(TCT检查、HPV乳头瘤病毒筛查）、乳腺（彩超，癌抗125），甲状腺（彩超，甲功验血）以及胸片，血常规肝功等有全面检查，非常适合女性全面疾病筛查使用。', null, '3bd90d2c-4e82-42a1-a401-882c88b06a1a2.jpg');
INSERT IGNORE INTO `t_setmeal` VALUES ('7', '阳光爸妈升级肿瘤12项筛查（男女单人）体检套餐', '0003', 'YGBM', '0', '55-100', '1400', '本套餐主要针对常见肿瘤筛查，肝肾、颈动脉、脑血栓、颅内血流筛查，以及风湿、颈椎、骨密度检查。', null, 'ee7dcf84-8a3a-4ab9-b981-9c5d272fd58d3.jpg');
INSERT IGNORE INTO `t_setmeal` VALUES ('9', '孕前检查套餐（女）-精英版', '0005', 'YQJCNV', '2', '18-50', '1500', '孕前检查套餐（女）-精英版', null, 'ac3b5a4d-33a5-4f37-bd49-99e06ce17d202.jpg');
INSERT IGNORE INTO `t_setmeal` VALUES ('11', '珍爱高端升级肿瘤12项筛查（男女单人）', '0006', 'ZAGD', '0', '14-20', '2400', '本套餐是一款针对生化五项检查，心，肝，胆，胃，甲状腺，颈椎，肺功能，脑部检查（经颅多普勒）以及癌症筛查，适合大众人群体检的套餐。', null, 'e373b2eb-0e50-4e95-a09b-03f2c1ee1d351.jpg');

-- t_setmeal_checkgroup
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '5');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '5');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '5');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '5');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('11', '5');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '6');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '6');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '6');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '6');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('11', '6');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '7');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '7');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '7');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '7');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('11', '7');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '8');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '8');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '8');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '8');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('11', '8');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '9');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '9');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '9');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '9');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '10');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '10');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '10');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '10');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('5', '11');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '11');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '11');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '11');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '12');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '12');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '12');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '13');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '13');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '13');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '14');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '14');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '14');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('6', '15');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('7', '15');
INSERT IGNORE INTO `t_setmeal_checkgroup` VALUES ('9', '15');

-- t_member
INSERT IGNORE INTO `t_member` VALUES ('82', null, '小明', '1', '123456789000999999', '18511279942', '2019-03-08', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('83', null, '王美丽', '1', '132333333333333', '13412345678', '2019-03-11', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('84', null, null, null, null, '18511279943', '2019-03-13', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('85', null, null, null, null, null, '2019-03-06', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('86', null, null, null, null, null, '2019-04-04', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('87', null, null, null, null, null, '2019-02-06', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('88', null, null, null, null, null, '2019-04-10', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('89', null, null, null, null, null, '2018-12-01', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('90', null, null, null, null, null, '2018-12-02', null, null, null, null);
INSERT IGNORE INTO `t_member` VALUES ('91', null, null, null, null, null, '2018-02-01', null, null, null, null);

-- t_ordersetting
INSERT IGNORE INTO `t_ordersetting` VALUES ('13', '2019-03-04', '100', '100');
INSERT IGNORE INTO `t_ordersetting` VALUES ('14', '2019-03-05', '200', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('15', '2019-03-06', '300', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('16', '2019-03-07', '200', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('17', '2019-03-08', '200', '1');
INSERT IGNORE INTO `t_ordersetting` VALUES ('18', '2019-03-09', '200', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('19', '2019-03-10', '200', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('20', '2019-03-11', '200', '3');
INSERT IGNORE INTO `t_ordersetting` VALUES ('21', '2019-03-13', '300', '1');
INSERT IGNORE INTO `t_ordersetting` VALUES ('22', '2019-03-14', '600', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('23', '2019-03-15', '500', '1');
INSERT IGNORE INTO `t_ordersetting` VALUES ('24', '2019-03-16', '500', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('25', '2019-03-17', '400', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('26', '2019-03-19', '300', '1');
INSERT IGNORE INTO `t_ordersetting` VALUES ('27', '2019-04-01', '300', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('28', '2019-04-02', '300', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('29', '2019-04-19', '300', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('30', '2019-03-20', '200', '1');
INSERT IGNORE INTO `t_ordersetting` VALUES ('31', '2019-05-01', '400', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('32', '2019-03-28', '200', '1');
INSERT IGNORE INTO `t_ordersetting` VALUES ('33', '2019-04-03', '400', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('34', '2019-09-30', '800', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('35', '2019-04-04', '400', '0');
INSERT IGNORE INTO `t_ordersetting` VALUES ('36', '2019-04-05', '300', '0');

-- t_order
INSERT IGNORE INTO `t_order` VALUES ('2', '82', '2019-03-08', '微信预约', '未到诊', '6');
INSERT IGNORE INTO `t_order` VALUES ('3', '82', '2019-03-11', '微信预约', '未到诊', '11');
INSERT IGNORE INTO `t_order` VALUES ('4', '82', '2019-03-11', '微信预约', '未到诊', '7');
INSERT IGNORE INTO `t_order` VALUES ('5', '83', '2019-03-11', '微信预约', '未到诊', '7');
INSERT IGNORE INTO `t_order` VALUES ('6', '82', '2019-03-13', '微信预约', '未到诊', '6');
INSERT IGNORE INTO `t_order` VALUES ('7', '82', '2019-03-15', '微信预约', '未到诊', '6');
INSERT IGNORE INTO `t_order` VALUES ('8', '82', '2019-03-19', '微信预约', '未到诊', '5');
INSERT IGNORE INTO `t_order` VALUES ('9', '84', '2019-03-20', '微信预约', '未到诊', '6');
INSERT IGNORE INTO `t_order` VALUES ('10', '84', '2019-03-28', '微信预约', '未到诊', '11');

