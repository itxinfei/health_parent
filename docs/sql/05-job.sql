-- =============================================================================
-- 心飞健康 定时任务登记脚本（**只读**，不写任何 sj_* 表）
-- 依据：docs/requirements/12-系统设置.md §6.1~§6.3、§11
-- 为什么没有 INSERT：12 §11 明确「不直接写 sj_* 表，任务注册走 SnailJob 控制台」。
--   sj_job 的 bucket_index / next_trigger_at / job_status 等列由服务端状态机派生，
--   手工 INSERT 会绕过注册与分片计算，服务端不会认领该任务，还会污染调度统计。
--   本文件的作用是：把控制台该填的每个值固化成可复制的口径 + 提供注册前后的只读核对。
--
-- 实测环境（192.168.20.130/xfjk，2026-10-22）：
--   sj_* 共 23 张表**就在 xfjk 业务库内**（information_schema 按 table_name like 'sj\_%' 计数），
--     与 sys_* 同库不同前缀，分三族：调度 sj_job*、重试 sj_retry*、工作流 sj_workflow*
--   sj_namespace：id=1 unique_id='dev'（Development）、id=2 unique_id='prod'
--   sj_group_config：group_name='xinfei_group' 在 dev / prod 各一行（id=1 / 2）
--   sj_system_user：仅 admin（role=2）
--   sj_job_executor、sj_server_node：**均 0 行** ⇒ 客户端与服务端当前都没接入（与端口不可达一致）
--   sj_job：现仅 1 行 = demo-job / executor_info=testJobExecutor / task_type=1 /
--            trigger_type=2 / trigger_interval='60' / job_status=1 / namespace_id='dev'
--            —— 框架自带示例，与新任务不重名，禁止删（删了示例就没人知道执行器怎么挂）
--   客户端配置（xfjk-admin/src/main/resources/application-dev.yml，token 值不入库）：
--            snail-job.enabled=true、group='xinfei_group'、namespace=${spring.profiles.active}（实测 dev）、
--            server.host=192.168.20.130、server.port=17888、client port=2${server.port}
--   服务端（xfjk-extend/xfjk-snailjob-server/src/main/resources/application.yml）：
--            server.port=8800、server.servlet.context-path=/snail-job
--   ⚠ 阻塞：2026-10-22 从开发机 curl 192.168.20.130 的 8800 / 17888 均 TCP 超时（exit 28），
--            同一主机 3306 与 9000 正常 ⇒ 这两个端口未开放或未发布，**服务端当前不可接入**。
--            注册前须先在 130 上启动 xfjk-snailjob-server（或放通 8800/17888），
--            再验证 http://192.168.20.130:8800/snail-job 能出控制台页面。
--
-- 执行器实现口径（12 §6.3）：
--   写法样板 = xfjk-modules/xfjk-job/src/main/java/com/xinfei/job/snailjob/TestAnnoJobExecutor.java
--   （@Component + @JobExecutor(name="...")，方法 ExecuteResult jobExecute(JobArgs jobArgs)）
--   该模块只放框架示例，**本项目 6 个业务执行器一律写在 xfjk-health-appointment 内**，
--   类名 = 下表的 executor_info（SnailJob 按 @JobExecutor 的 name 匹配，不是按类全限定名）。
--   任务方法禁止依赖 HTTP 上下文与登录用户，需要操作人时固定写 'system' 并留痕。
--   xfjk-job 已在 xfjk-admin 依赖里（实测 pom 第 81 行），故客户端会随主应用启动注册组。
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 0. 注册前只读自检（期望：health 前缀 6 个任务名全部**未出现**，即返回 0 行）
--    命中任意一行说明库里已有同名任务，改走控制台「修改」而不是再新建，避免双任务并存
-- -----------------------------------------------------------------------------
SELECT id, namespace_id, group_name, job_name, executor_info, job_status, trigger_interval
  FROM sj_job
 WHERE job_name IN ('health-img-orphan-clean', 'health-followup-task-generate',
                    'health-followup-overdue-mark', 'health-qc-timeout-remind',
                    'health-report-export-archive', 'health-assessment-recompute-retry')
   AND deleted = 0;


-- -----------------------------------------------------------------------------
-- 1. 控制台注册步骤（登录 http://192.168.20.130:8800/snail-job → admin → 任务管理 → 新建任务）
--    公共字段：命名空间 dev、组 xinfei_group、任务类型 Cluster（集群）、路由策略按默认
--    每行给出 12 §6.1 的六个任务；args_str 即控制台「自定义参数」框该填的值
--
--  1.1 health-img-orphan-clean      孤儿图片清理
--      executor_info  healthImgCleanJob
--      触发类型      CRON：0 0 3 * * ?
--      args_str      dryRun=true          （12 §6.2 第 5 条：默认只统计不删除）
--      幂等          天然幂等，删除动作可重复
--      单批上限      不设（逐条 try/catch，不中断整批）
--      重复执行两次  第二次 scanned/deleted 均为 0
--      上线动作      首轮必须 dryRun=true 跑通并核对 scanned 数，人工确认后才改 dryRun=false
--      前置依赖      依赖 Redis 集合 health:img:uploaded / health:img:used 与 MinIO，
--                    MinIO 真实凭据未落实前（12 §7、任务 #26）该任务只能 dryRun
--
--  1.2 health-followup-task-generate 随访任务生成
--      executor_info  followupTaskGenerateJob
--      触发类型      CRON：0 30 1 * * ?
--      args_str      留空
--      幂等键        (plan_id, member_id, stat_year, seq_no) 唯一索引（18 B12；10 §5.2）
--      失败策略      跳过已存在，记 skipped
--      重复执行两次  第二次 inserted=0、skipped 等于首次 inserted
--
--  1.3 health-followup-overdue-mark  随访逾期标记
--      executor_info  followupOverdueJob
--      触发类型      CRON：0 0 * * * ?（每小时）
--      args_str      留空
--      幂等          条件更新 status='pending' and due_date<curdate() and due_flag=0
--      单批上限      5000（超出留给下一轮，禁止一次全表扫）
--      重复执行两次  第二次 marked=0
--
--  1.4 health-qc-timeout-remind      质控超时提醒
--      executor_info  qcTimeoutRemindJob
--      触发类型      CRON：0 0 9,15 * * ?
--      args_str      留空
--      幂等          每报告每日最多提醒一次：Redis SETNX 到当日 24:00
--      阈值来源      sys_config health:qc:dept_deadline_hours / health:qc:summary_deadline_days
--                    （由 docs/sql/04-config.sql 落库，缺键 → 12003，禁止代码内兜底短路）
--      重复执行两次  第二次 reminded=0（SETNX 命中）
--
--  1.5 health-report-export-archive  发放件归档（本期可选）
--      executor_info  reportArchiveJob
--      触发类型      CRON：0 0 2 * * ?
--      args_str      留空
--      幂等键        report_id + file_type 唯一
--      失败策略      重试 3 次
--      本期口径      18 F11 发放件归档为**可选交付**，未做则本任务保持下线，不要注册成启用
--
--  1.6 health-assessment-recompute-retry 评估算分补偿
--      executor_info  assessmentRetryJob
--      触发类型      按需注册，默认**下线**（job_status=停用）
--      args_str      留空
--      幂等键        record_id
--      处理范围      只处理 status='submitted' 超 10 分钟未 'scored' 的记录（09 §5）
--      重复执行两次  第二次 retried=0
-- -----------------------------------------------------------------------------


-- -----------------------------------------------------------------------------
-- 2. 注册后只读回查（期望 6 行；job_status 语义看控制台开关，不要用数值自判）
-- -----------------------------------------------------------------------------
SELECT job_name, executor_info, trigger_interval, job_status, args_str, description, update_dt
  FROM sj_job
 WHERE job_name LIKE 'health-%'
   AND namespace_id = 'dev'
   AND group_name = 'xinfei_group'
   AND deleted = 0
 ORDER BY job_name;

-- 2.1 组与命名空间自检（期望 xinfei_group 在 dev 下恰有一行，否则客户端连不上组）
SELECT g.group_name, g.namespace_id, n.unique_id, n.name
  FROM sj_group_config g
  JOIN sj_namespace n ON n.unique_id = g.namespace_id
 WHERE g.group_name = 'xinfei_group';

-- 2.2 执行器与节点心跳（实测 2026-10-22 两张表均 0 行 ⇒ 客户端与服务端都未接入，
--     此时即使控制台建了任务也不会触发；列名按 show columns 实测，不要凭记忆改）
SELECT executor_info, group_name, namespace_id, executor_type, update_dt
  FROM sj_job_executor
 ORDER BY group_name, executor_info;

SELECT id, namespace_id, group_name, host_id, host_ip, host_port, expire_at, node_type, update_dt
  FROM sj_server_node
 ORDER BY update_dt DESC;


-- -----------------------------------------------------------------------------
-- 3. 禁止事项（12 §6.3 与 15 红线，逐条对应）
--    3.1 禁止 @Scheduled / Quartz / new Thread + sleep 轮询——本项目定时入口只有 SnailJob
--    3.2 禁止用 sys_job 表述任务（该表不存在，12 §2）；文档与注释统一写 sj_job / SnailJob 控制台
--    3.3 禁止直接 INSERT/UPDATE sj_job 及任何 sj_* 调度状态表完成「注册」
--    3.4 禁止把 token、口令写进本文件或任何入库文档（值在 application-dev.yml，不在文档）
--    3.5 禁止在生产库执行本文件的写操作——本文件本身只读，也不得在生产上随手跑 SELECT 之外的语句
--    3.6 每个任务上线前须交齐：幂等键、单批上限、重复执行两次的预期结果（第二次数值为 0），
--        缺任一项不得在控制台把任务置为启用
-- -----------------------------------------------------------------------------
