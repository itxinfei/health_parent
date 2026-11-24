# 实现进度（STATUS）

> 校准时间：**2026-11-24**，依据仓库代码实测（目录、控制器路由、SQL 建表语句），不是文档推演。
> 定位：**「实现现状」的唯一出处**。`README`、`02`、`09` 等文档不再各自复述进度，只引用本文件。
> 维护规则：落地一个新模块、一批接口或一批表之后，**只改本文件**，其余文档保持"目标态"表述不变。
> 变更历史见 [CHANGELOG](./CHANGELOG.md)；总纲索引见 [README](./README.md)。

## 1. 后端模块（`xfjk-modules/`）

| 模块 | 状态 | 实测内容 |
|---|---|---|
| `xfjk-system` | 已存在（框架） | 用户 / 角色 / 菜单 / 部门 / 岗位 / 字典 / 参数 / 日志 |
| `xfjk-generator` | 已存在（框架） | 代码生成器 |
| `xfjk-job` | 已存在（框架） | SnailJob 执行端 |
| `xfjk-health-appointment` | **已落地** | **164 个 Java 文件 / 14 个控制器**。管理侧：`/health/checkitems`、`/health/checkgroups`、`/health/setmeals`、`/health/orders`、`/health/ordersettings`、`/health/forbids`、`/health/members`、`/health/reports`、`/health/criticals`；C 侧：`/app/auth`、`/app/sms`、`/app/setmeals`、`/app/orders`、`/app/ordersettings` |
| `xfjk-health-assessment` | **已落地（工作区，尚未提交）** | **64 个 Java 文件 / 8 个控制器**。`/health/questionnaires`、`/health/questions`、`/health/assessments`、`/health/suggestions`、`/health/indicators`、`/health/crowds`、`/health/scales`（`ScaleController` 类级前缀声明为 `/health`）、`/app/assessments`；含算分引擎 `engine/` 两个类 |
| `xfjk-health-member` | 未建（功能寄居） | 会员接口在 `appointment` 的 `MemberController`（`/health/members`）；报告与危急值在 `ReportController` / `CriticalController` |
| `xfjk-health-intervention` | 未建 | 无代码 |
| `xfjk-health-knowledge` | 未建 | 无代码 |
| `xfjk-health-enterprise` | 未建 | 无代码（`20` 全文未开工） |
| `xfjk-health-stat` | 未建 | 无代码（工作台与统计报表未开工） |

- 目标结构是 7 个 `xfjk-health-*`，**已建 2 个**（appointment、assessment），未建 5 个。
- `xfjk-demo` **不存在**（v2.20 及更早的 `02` §2 模块树把它标为"已存在"，属错误节点，v2.21 已删除）。
- **会员 / 报告 / 危急值域尚未拆出独立模块**，先寄居在 `appointment`。这是为减少跨模块 `service` 接口临时开销的有意取舍，不是遗漏；拆包时机：该域接口继续增长或需要独立迭代时，按 `02` §3 走 `service` 接口先解耦再迁包。

## 2. 数据库（`docs/sql/`）

12 个脚本，`CREATE TABLE` 合计 **45 处**：

| 脚本 | 建表数 | 说明 |
|---|---|---|
| `01-schema.sql` | 8 | 冻结的 `t_*` 原表（检查项 / 检查组 / 套餐 / 预约设置 / 订单 / 会员等） |
| `02-seed.sql` | 0 | 上述 8 表种子数据 |
| `03-dict.sql` | 0 | 业务字典种子 |
| `04-config.sql` | 0 | 参数配置种子 |
| `05-job.sql` | 0 | SnailJob 任务种子 |
| `06-order-ext.sql` | 3 | 订单扩展（项目明细等） |
| `07-forbid.sql` | 1 | 禁忌规则 |
| `08-health-ext.sql` | 4 | 健康侧扩展表 |
| `09-member.sql` | 7 | 会员档案 / 动态信息 / 缺项 / 病史 / 标签 |
| `10-report.sql` | 8 | 报告 / 明细 / 科室小结 / 结论 / 归档 / 危急值 / 结论字典 |
| `11-checkitem-ext-ref.sql` | 0 | 检查项 ext / ref 种子（65 项 + 73 行） |
| `12-assessment.sql` | 14 | 问卷 / 题目 / 选项 / 量表因子与规则 / 评估记录与答案 / 建议 / 指标 / 人群分类 |

- `04` 规划的业务表尚未建的部分：干预（`10`）、知识库（`11`）、团检（`20`）、统计快照与重置留痕以外的 `06` 表。

## 3. 前端

| 工程 | 形态 | 实测状态 |
|---|---|---|
| `xfjk-web` | plus-ui 工程化 SPA（Vite 7.3.2 / Vue 3.5.30 / TS / Element Plus 2.13.5，版本对 `v5.6.2-v2.6.2`） | `src/views/health/` 已落地 **10 页**：检查项、检查组、套餐、预约设置、预约列表、禁忌规则、会员、体检报告、总检工作台、危急值登记 |
| `xfjk-app` | uniapp 3 + Vue 3 + TS（微信小程序 / H5） | `src/pages/` 仅 **4 页**（index / login / order / setmeal），且**仍调旧路径**，未改调 `/app/**` |

- 容器与 CI 未随换栈改写：`Dockerfile.web`、`nginx.conf`、`deploy.yml` 仍是静态页形态，**CI 容器构建当前不可用**；本机 dev 冒烟走 vite 8080 → proxy 8083 fat jar。
- 门禁：`scripts/check-deny.ps1`（v2.21 起随仓库提供）。业务档基线 ERROR 0 / WARN 8（WARN 全部来自 upstream `console.log`）。

## 4. 未开工清单（按文档编号）

| 文档 | 范围 | 状态 |
|---|---|---|
| `06` | 工作台 8 卡 / 6 待办 / 3 趋势图、P01~P12 报表、导出 | 未开工（依赖各业务域 `service` 接口） |
| `10` | 干预模板 / 方案 / 随访 / 运动库 / 膳食库 | 未开工 |
| `11` | 疾病库 / 健康方案库 / 资讯 | 未开工 |
| `20` | 团检四级 + 团体报告 | 未开工（本期范围，见 `17` 决策 26） |
| `13` | C 端剩余页面（档案、评估、干预、资讯） | 部分：仅 index / login / order / setmeal 四页骨架 |

## 5. 与文档的已知偏差（须按本文件为准）

1. `02` §2、`09` §1.1、`README` 旧"实现现状"段曾称评估模块未开工 —— 已于 v2.21 回写，若其它文档仍出现"assessment 未创建"，以本文件为准。
2. `xfjk-demo` 节点已从 `02` §2 删除，历史文档若引用该模块视为无效。
3. 会员 / 报告域未按 `02` §2 规划独立成模块，而是寄居 `appointment`（见 §1 注）。
4. **评估模块与 `12-assessment.sql` 尚未提交**：`xfjk-modules/xfjk-health-assessment/`、`docs/sql/12-assessment.sql`、`pom.xml` 的模块声明目前只在工作区（`git status` 显示未跟踪/已修改），clone 后看不到。提交前须先清 DENY：该模块现有 **ERROR 7**（DENY-003×6、DENY-004×1，集中在 `service/impl` 与 `domain/bo`），默认档基线（ERROR 0）因此被打破。
