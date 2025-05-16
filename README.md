
<div align="center">

![Logo](docs/Logo.png)

# 🏥 传智健康  
**健康管理机构业务系统 | 实现健康管理可视化、会员管理专业化、评估数字化、干预流程化**  

  <!-- 技术栈滚动徽章 -->
  <div style="white-space: nowrap; overflow-x: auto; padding: 10px 0;">
    <img alt="JDK" src="https://img.shields.io/badge/JDK-1.8%2B-brightgreen">
    <img alt="Maven" src="https://img.shields.io/badge/maven-3.6.3%2B-yellowgreen">
    <img alt="License" src="https://img.shields.io/badge/license-Apache-green">
    <img alt="前端" src="https://img.shields.io/badge/前端-Vue.js%20+%20Element%20UI-blueviolet">
    <img alt="服务层" src="https://img.shields.io/badge/服务层-Spring%20Boot%20+%20MyBatis-orange">
    <img alt="数据库" src="https://img.shields.io/badge/数据库-MySQL%208.0%20+%20Redis-yellowgreen">
    <img alt="中间件" src="https://img.shields.io/badge/中间件-RabbitMQ%20+%20Nginx-blue">
  </div>
</div>



## 🌐 项目演示  
**在线体验地址**：  
[http://manager-health-java.itheima.net/pages/main.html](http://manager-health-java.itheima.net/pages/main.html)

---

## 📌 项目介绍  
本项目是一款面向健康管理机构的业务系统，旨在实现以下核心目标：  
- **工作内容可视化**：通过数据大屏和报表展示运营情况。  
- **会员管理专业化**：全生命周期会员管理，支持健康档案、评估与干预。  
- **健康评估数字化**：基于医学模型自动计算健康风险评分。  
- **健康干预流程化**：标准化干预方案与执行跟踪。  
- **知识库集成化**：整合健康知识库，辅助健康管理师决策。  

---

## 📷 项目后台截图  
### 管理界面概览  
![管理界面](https://broadscope-dialogue-new.oss-cn-beijing.aliyuncs.com/output/20250516/212e4ebbbd380ee958574a6289f57ef0.png?Expires=1778903766&OSSAccessKeyId=LTAI5tL97mBYzVcjkG1cUyin&Signature=6Lin8QGpHZaFz3GbqbQMD7JQVCI%3D)  

### 核心功能模块  
| 检查项管理 | 检查组管理 | 运营数据统计 |
|------------|------------|--------------|
| ![检查项管理](https://broadscope-dialogue-new.oss-cn-beijing.aliyuncs.com/output/20250516/cfd9d39edc84b0f9ed0523345c88cc07.png?Expires=1778903766&OSSAccessKeyId=LTAI5tL97mBYzVcjkG1cUyin&Signature=T%2FW%2FHK%2BVSMqnKSWAX73D7XouPbU%3D) | ![检查组管理](https://broadscope-dialogue-new.oss-cn-beijing.aliyuncs.com/output/20250516/4d37c2ea76e415ac3b382eb5b715fc04.png?Expires=1778903766&OSSAccessKeyId=LTAI5tL97mBYzVcjkG1cUyin&Signature=RJ6dIdk38mzkpURSPWZuyVfMzRI%3D) | ![运营数据统计](https://broadscope-dialogue-new.oss-cn-beijing.aliyuncs.com/output/20250516/554e285ffde5d2b990fc7d4da597b973.png?Expires=1778903766&OSSAccessKeyId=LTAI5tL97mBYzVcjkG1cUyin&Signature=duKpqLOjIdMe1nJhXEUDrieU9u8%3D) |

---

## 📐 技术架构  
### 系统架构全景  
![技术架构图](https://broadscope-dialogue-new.oss-cn-beijing.aliyuncs.com/output/20250516/28022534dcd0f73ef5a4cad89402efa2.png?Expires=1778903767&OSSAccessKeyId=LTAI5tL97mBYzVcjkG1cUyin&Signature=ooZnJvX36JL%2B9rNC%2BCq%2BDsa6X9k%3D)  

### 功能架构  
![功能架构](https://broadscope-dialogue-new.oss-cn-beijing.aliyuncs.com/output/20250516/e935eba7763a9c46e42a8b7c14850ba7.png?Expires=1778903767&OSSAccessKeyId=LTAI5tL97mBYzVcjkG1cUyin&Signature=CngMRXDjbg9RRAMV%2FhMX3LxEZkA%3D)  

---

## 🧰 技术亮点  
| 层级         | 技术选型                                                                 |
|--------------|--------------------------------------------------------------------------|
| **前端**     | Vue.js + Element UI + ECharts                                             |
| **服务层**   | Spring Boot 2.x + MyBatis + MapStruct                                     |
| **数据层**   | MySQL 5.7+ + Redis 6.x                                                    |
| **中间件**   | RabbitMQ + FastDFS + Nginx                                                |
| **安全**     | Spring Security + JWT + HTTPS                                             |

---

## 📥 部署指南  
### 快速部署步骤  
```bash
# 1. 导入数据库
mysql -u root -p -e "source health.sql"

# 2. 启动服务
mvn clean install
java -jar health-manage.jar

# 3. 访问演示地址
open http://localhost:8080/pages/main.html
```

---

---
### 🔗 项目链接 & 社区支持  
![微信公众号二维码](docs/心飞为你飞.jpg)  
🚀 项目地址：[https://gitee.com/itxinfei/health_parent](https://gitee.com/itxinfei/health_parent)  
👥 QQ交流群：[661543188](https://qm.qq.com/cgi-bin/qm/qr?k=gNgch-wCkfUu-QbI7DZSudrax2BN7vY0&jump_from=webapi&authKey=QHSRnxQvu+h5S3AXGn/DSHrVPiFQAYEk6bSlCE1lS276SFjQAUagV4FG7bHf0OSM)  
📧 邮箱支持：[747011882@qq.com](http://mail.qq.com/cgi-bin/qm_share?t=qm_mailme&email=f0hLSE9OTkdHTT8ODlEcEBI)  

