# 心飞健康（xfjk）

**简体中文** · [English](README.en.md)

体检中心的业务后台。从检查项维护、套餐配置、预约排期，一路做到报告生成、危急值提醒和健康评估。

底座是 RuoYi-Vue-Plus，但业务层重写过——体检这个行业有自己的规矩：套餐是按检查组拼的，预约要卡每日容量，报告里出现危急值必须立刻推到医生那儿，客户体检完还要给一份能看懂的评估建议。这些东西通用后台给不了。

## 两条业务线

**体检预约**

| 环节 | 能力 |
|---|---|
| 项目维护 | 检查项、检查组、套餐（检查组按需组合） |
| 排期 | 预约设置、每日人数上限、禁约规则 |
| 预约 | 下单、改约、取消、到检登记 |
| 报告 | 报告查询、报告统计 |
| 危急值 | 危急值标记与提醒 |
| 通知 | 短信下发（预约确认、到检提醒） |
| 会员 | 会员档案与历史体检记录 |

**健康评估**

量表、问卷、题目、指标、人群分类、评估结果、健康建议——一套完整的评估链路，从用户填问卷到输出分档建议。

这两条线在同一个库里，用户一次体检的数据既能出报告，也能进评估。

## 模块

```
xfjk-admin       管理端启动入口与 Web 层
xfjk-app         移动端接口层（C 端用户用）
xfjk-web         Web 层封装
xfjk-common      通用能力
xfjk-extend      扩展能力
xfjk-modules
├── xfjk-health-appointment    体检预约（14 个控制器）
├── xfjk-health-assessment     健康评估（8 个控制器）
├── xfjk-system                系统管理
└── xfjk-job                   定时任务
```

管理端和移动端分成两层接口，是因为两者的权限模型和数据结构不一样：后台要看到全部数据、要能改；C 端只看自己的、只能提交。

## 技术栈

| 类别 | 选型 |
|---|---|
| 底座 | RuoYi-Vue-Plus（MIT） |
| JDK | 25 |
| 框架 | Spring Boot 4.1.1 |
| 认证 | Sa-Token + JWT |
| ORM | MyBatis-Plus |
| 数据库 | MySQL（可切 PostgreSQL） |
| 缓存 | Redis + Redisson |

底座 Dromara RuoYi-Vue-Plus 遵循 MIT 协议，见[官方仓库](https://gitee.com/dromara/RuoYi-Vue-Plus)与[文档](https://plus-doc.dromara.org)。

## 跑起来

```bash
mvn clean package -DskipTests
```

建库脚本和配置说明在 `docs/requirements/` 与 `script/` 下，需求与开发规范见 [`docs/requirements/README.md`](docs/requirements/README.md)。

## 常见问题

**MySQL 5.7 导 SQL 报错**

用 8.0。脚本里的排序规则是 `utf8mb4_0900_ai_ci`，5.7 不认这个 collation，报错信息还指不到点子上（会说是语法错误）。

**登录返回 401，账号密码没错**

Sa-Token 的会话默认存 Redis，Redis 没起或者配置里的库号不对，就会这样。先确认能 `ping` 通 Redis，再看 `application.yml` 里的 `sa-token` 与 redis 配置是否指向同一个库。

**管理端能登，移动端登不上**

两套接口层（`xfjk-admin` 与 `xfjk-app`）的鉴权是分开的，token 不通用。移动端要走 `xfjk-app` 的登录接口重新换 token，不能拿后台的 token 直接调。

**能不能换 PostgreSQL**

底座 RuoYi-Vue-Plus 支持，但要自己改驱动、方言和建表脚本，体检业务里的分页和批量插入都依赖 MyBatis-Plus 的数据库方言。没试过，别按"改个配置就行"来估工。

## 路线图

- **当前**：体检预约主链路（项目 → 套餐 → 排期 → 预约 → 报告）
- **下一步**：健康评估模块联调，评估结果与体检报告打通
- **之后**：C 端小程序、危急值实时推送、体检数据看板

时间不承诺，按实际进度推。

## 许可

Apache-2.0，见 [LICENSE](LICENSE)。

底座 RuoYi-Vue-Plus 的 MIT 授权继续有效，归属声明见 [NOTICE](NOTICE)。

## 联系

QQ 747011882 ｜ 邮箱 747011882@qq.com
