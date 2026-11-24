# AGENTS.md — xfjk 协作约定（AI / 人 通用）

本仓库是「心飞健康」（xfjk），底座 RuoYi-Vue-Plus 5.6.2。任何 agent 接手前先读这两份，再动手：

1. **`docs/技术选型与版本基线.md`** —— 版本基线，**锁死**。改 `pom.xml` / `package.json` / `docker-compose*.yml` 的版本前必读。
2. **`docs/requirements/README.md`** —— 需求与开发规范总纲索引；实现进度只看 `docs/requirements/STATUS.md`。

## 版本铁律（违反即打回）

- JDK **25**、Spring Boot **4.1.3**、Jackson **3**（`tools.jackson`）、MySQL **8.4.11**（tag 写死补丁号，不用 9.x）。
- **禁止**把 Spring Boot 改回 `4.1.1`/`4.1.0`/`4.1.2`，**禁止**擅自改到 `4.2.x`（4.2 刚 GA，Spring Cloud 2026.0 仍 RC）。变更须走基线文档 §5 流程：批准 → 落地 → 回写基线表 + `17` 决策记录 + `CHANGELOG`。
- 禁止在无关业务改动里顺带升降依赖版本或改 `package-lock.json`。
- 禁止回退 Jackson 3 到 `com.fasterxml.jackson`。

## 代码与门禁

- 包路径：`com.xinfei.health.<module>`；跨模块只经 `service` 接口。
- 日期用 `java.time`，禁止 `java.util.Date`（Entity 映射除外）。
- 提交前跑门禁：`powershell -ExecutionPolicy Bypass -File scripts\check-deny.ps1`，要求 **ERROR 0**。
- 新测试类须加 `@Tag("dev")`，跑测试用 `-DskipTests=false`（根 pom 默认 `skipTests=true`）。

## 工装

- JDK：`D:\Program Files\Java\jdk-25.0.3`；Maven：`D:\devops\apache-maven-3.9.16\bin\mvn.cmd`。
- 后端 dev：`xfjk-admin` 8083（8081 常被占）；前端 dev：`xfjk-web` 8080（`/dev-api` → 8083）。
- 跑含中文的 SQL 必须显式 `--default-character-set=utf8mb4`，否则菜单名会双重编码乱码。

## 提交

- 中文提交信息，格式 `type: 简述`（`feat` / `fix` / `docs` / `chore` / `build` / `refactor` / `test`）。
- 不提交构建产物、IDE 目录、备份副本、密钥文件（`.gitignore` 已覆盖，新增例外需说明）。
