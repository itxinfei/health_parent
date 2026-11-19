<h1 align="center">心飞健康 · xfjk</h1>

<h4 align="center">体检中心的业务后台 —— 从检查项维护到危急值提醒，一条链路走通</h4>

<p align="center">
  <a href="https://gitee.com/itxinfei/xfjk/stargazers"><img src="https://gitee.com/itxinfei/xfjk/badge/star.svg?theme=gvp" alt="star"></a>
  <a href="https://gitee.com/itxinfei/xfjk/members"><img src="https://gitee.com/itxinfei/xfjk/badge/fork.svg?theme=gvp" alt="fork"></a>
  <img src="https://img.shields.io/badge/JDK-25-orange.svg" alt="jdk">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F.svg?logo=springboot&logoColor=white" alt="spring">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5-red.svg" alt="mp">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql&logoColor=white" alt="mysql">
  <img src="https://img.shields.io/badge/Redis-7-DC382D.svg?logo=redis&logoColor=white" alt="redis">
  <img src="https://img.shields.io/badge/%E4%B8%9A%E5%8A%A1-%E4%BD%93%E6%A3%80%E9%A2%84%E7%BA%A6%20%7C%20%E5%81%A5%E5%BA%B7%E8%AF%84%E4%BC%B0-important.svg" alt="business">
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="license">
</p>

<p align="center">
  <b>简体中文</b> · <a href="README.en.md">English</a>
</p>

<p align="center">
  <a href="#一这是什么">这是什么</a> ·
  <a href="#二两条业务线">业务能力</a> ·
  <a href="#三模块结构">模块</a> ·
  <a href="#五快速开始">快速开始</a> ·
  <a href="#六常见问题">常见问题</a> ·
  <a href="https://gitee.com/itxinfei/xfjk/releases">版本发布</a>
</p>

---

## 一、这是什么

体检中心的业务后台。从检查项维护、套餐配置、预约排期，一路做到报告生成、危急值提醒和健康评估。

底座是 RuoYi-Vue-Plus，但业务层重写过——体检这个行业有自己的规矩：套餐是按检查组拼的，预约要卡每日容量，报告里出现危急值必须立刻推到医生那儿，客户体检完还要给一份能看懂的评估建议。这些东西通用后台给不了，只能在业务层一条一条磨出来。

两条业务线共用一个库：用户一次体检的数据既能出报告，也能进评估，不用在两个系统之间导数据。

## 二、两条业务线

### 1、体检预约

| 环节 | 能力 |
|---|---|
| 项目维护 | 检查项、检查组、套餐（检查组按需组合） |
| 排期 | 预约设置、每日人数上限、禁约规则 |
| 预约 | 下单、改约、取消、到检登记 |
| 报告 | 报告查询、报告统计 |
| 危急值 | 危急值标记与提醒 |
| 通知 | 短信下发（预约确认、到检提醒） |
| 会员 | 会员档案与历史体检记录 |

### 2、健康评估

- **量表与问卷** —— 量表、问卷、题目、选项成套管理
- **指标计算** —— 按问卷答案算分，落到人群分类
- **结果分档** —— 评估结果按档输出，不是一句"注意身体"
- **健康建议** —— 每档对应一组可执行建议，能直接印进报告

评估链路从用户填问卷一路走到分档建议，中间不做人工干预。

## 三、模块结构

```
xfjk/
├── xfjk-admin          管理端启动入口与 Web 层
├── xfjk-app            移动端接口层（C 端用户用）
├── xfjk-web            Web 层封装
├── xfjk-common         通用能力
├── xfjk-extend         扩展能力
└── xfjk-modules
    ├── xfjk-health-appointment    体检预约（14 个控制器）
    ├── xfjk-health-assessment     健康评估（8 个控制器）
    ├── xfjk-system                系统管理
    └── xfjk-job                   定时任务
```

管理端和移动端分成两层接口，是因为两者的权限模型和数据结构不一样：后台要看到全部数据、要能改；C 端只看自己的、只能提交。混在一层，迟早要靠一堆 `if (isAdmin)` 来补。

## 四、技术栈

| 类别 | 选型 |
|---|---|
| 底座 | [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus)（MIT） |
| JDK | 25 |
| 框架 | Spring Boot 4.1.1 |
| 认证 | Sa-Token + JWT |
| ORM | MyBatis-Plus |
| 数据库 | MySQL 8.0（可切 PostgreSQL） |
| 缓存 | Redis + Redisson |

底座遵循 MIT 协议，文档见 [plus-doc.dromara.org](https://plus-doc.dromara.org)。

## 五、快速开始

1. 准备 JDK 25、Maven、MySQL 8.0、Redis
2. 执行建库脚本（`script/` 下）
3. 改数据源与 Redis 配置，打包启动

```bash
mvn clean package -DskipTests
java -jar xfjk-admin/target/xfjk-admin.jar
```

需求与开发规范见 [`docs/requirements/README.md`](docs/requirements/README.md)，配置说明在 `docs/requirements/` 与 `script/` 下。

## 六、常见问题

**1、MySQL 5.7 导 SQL 报错**

用 8.0。脚本里的排序规则是 `utf8mb4_0900_ai_ci`，5.7 不认这个 collation，报错信息还指不到点子上（会说是语法错误）。

**2、登录返回 401，账号密码没错**

Sa-Token 的会话默认存 Redis，Redis 没起或者配置里的库号不对，就会这样。先确认能 `ping` 通 Redis，再看 `application.yml` 里的 `sa-token` 与 redis 配置是否指向同一个库。

**3、管理端能登，移动端登不上**

两套接口层（`xfjk-admin` 与 `xfjk-app`）的鉴权是分开的，token 不通用。移动端要走 `xfjk-app` 的登录接口重新换 token，不能拿后台的 token 直接调。

**4、能不能换 PostgreSQL**

底座支持，但要自己改驱动、方言和建表脚本，体检业务里的分页和批量插入都依赖 MyBatis-Plus 的数据库方言。没试过，别按"改个配置就行"来估工。

## 七、路线图

- **当前**：体检预约主链路（项目 → 套餐 → 排期 → 预约 → 报告）
- **下一步**：健康评估模块联调，评估结果与体检报告打通
- **之后**：C 端小程序、危急值实时推送、体检数据看板

时间不承诺，按实际进度推。

## 八、参与贡献

欢迎提 Issue 和 PR，规则见 [CONTRIBUTING.md](CONTRIBUTING.md)，安全策略见 [SECURITY.md](SECURITY.md)，版本变更见 [CHANGELOG.md](CHANGELOG.md)。

## 九、开源协议

Apache-2.0，见 [LICENSE](LICENSE)。

底座 RuoYi-Vue-Plus 的 MIT 授权继续有效，归属声明见 [NOTICE](NOTICE)。

## 十、交流

作者：心飞为你飞（itxinfei）

<p align="center">
  <a href="https://qm.qq.com/cgi-bin/qm/qr?k=0&group_code=661543188"><img src="https://img.shields.io/badge/QQ%E7%BE%A4-661543188-blue.svg?logo=tencentqq&logoColor=white" alt="QQ群"></a>
  <a href="mailto:747011882@qq.com"><img src="https://img.shields.io/badge/email-747011882%40qq.com-orange.svg" alt="email"></a>
</p>
