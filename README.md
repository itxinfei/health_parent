# 心飞健康

> 心飞健康 · 完整健康管理平台，基于 RuoYi-Vue-Plus 构建。

## 平台简介

心飞健康是一套完整的健康管理平台后端，围绕体检与健康管理业务提供：

- **体检管理**：检查项、检查组、套餐管理
- **预约管理**：体检预约与预约设置
- **报告管理**：体检报告统计与查询
- **系统管理**：用户、角色、菜单、字典等基础能力（由底座提供）

## 技术栈

| 类别 | 选型 |
|---|---|
| 基础框架 | RuoYi-Vue-Plus |
| 语言 / JDK | Java 25 |
| 核心框架 | Spring Boot 4.1.1 |
| 权限认证 | Sa-Token + JWT |
| ORM | MyBatis-Plus |
| 数据库 | MySQL（可切换 PostgreSQL 等） |
| 缓存 | Redis + Redisson |

> 底座 Dromara RuoYi-Vue-Plus 遵循 MIT 协议开源，详见 [官方仓库](https://gitee.com/dromara/RuoYi-Vue-Plus) 与 [文档](https://plus-doc.dromara.org)。

## 项目文档

需求与开发规范见 [`docs/requirements/`](docs/requirements/README.md)（总纲索引）。

## 快速开始

```bash
# 构建
mvn clean package -DskipTests
```

数据库初始化脚本与配置说明见项目文档。

## License

[MIT](LICENSE)
