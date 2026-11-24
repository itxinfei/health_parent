# xfjk · Xinfei Health

[简体中文](README.md) · **English**

<p>
  <a href="https://gitee.com/itxinfei/xfjk/stargazers"><img src="https://gitee.com/itxinfei/xfjk/badge/star.svg?theme=gvp" alt="star"></a>
  <a href="https://gitee.com/itxinfei/xfjk/members"><img src="https://gitee.com/itxinfei/xfjk/badge/fork.svg?theme=gvp" alt="fork"></a>
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue" alt="License">
  <img src="https://img.shields.io/badge/business-medical%20exam-important" alt="Business">
  <br>
  <img src="https://img.shields.io/badge/JDK-25%20LTS-orange?logo=openjdk&logoColor=white" alt="JDK">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.3-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Sa--Token-1.46-red" alt="Sa-Token">
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white" alt="Redis">
</p>

The back office for a medical examination center. From maintaining examination items, assembling packages and scheduling appointments, through to report generation, critical-value alerts and health assessments.

The foundation is RuoYi-Vue-Plus, but the business layer was rewritten. Examination centers have their own rules: packages are assembled from item groups, appointments respect a daily capacity, any critical value in a report must reach a physician immediately, and a client who finishes a checkup expects an assessment they can actually understand. A generic admin panel gives you none of that.

## 1. Two business lines

**Examination appointments**

| Stage | Capability |
|---|---|
| Items | Examination items, item groups, packages (groups combined on demand) |
| Scheduling | Appointment settings, daily capacity, blackout rules |
| Booking | Create, reschedule, cancel, arrival check-in |
| Reports | Query and statistics |
| Critical values | Flagging and alerting |
| Notifications | SMS (confirmation, arrival reminder) |
| Members | Member profiles and examination history |

**Health assessment**

Scales, questionnaires, questions, indicators, population classification, assessment results, health recommendations — a full assessment chain from questionnaire to graded advice.

Both lines share one database: a single checkup yields both a report and assessment input.

## 2. Modules

```
xfjk-admin       admin entry point and web layer
xfjk-app         mobile API layer (end users)
xfjk-web         web layer wrapper
xfjk-common      shared utilities
xfjk-extend      extensions
xfjk-modules
├── xfjk-health-appointment    appointments (14 controllers)
├── xfjk-health-assessment     assessments (8 controllers)
├── xfjk-system                system management
└── xfjk-job                   scheduled jobs
```

Admin and mobile run on separate API layers because their permission models and data shapes differ: the back office sees and edits everything, end users see only their own records and can only submit.

## 3. Stack

| Layer | Choice |
|---|---|
| Foundation | RuoYi-Vue-Plus (MIT) |
| JDK | 25 |
| Framework | Spring Boot 4.1.3 |
| Auth | Sa-Token + JWT |
| ORM | MyBatis-Plus |
| Database | MySQL 9.7 LTS (8.4.11 fallback, PostgreSQL switchable) |
| Cache | Redis + Redisson |

See the [upstream project](https://gitee.com/dromara/RuoYi-Vue-Plus) and [docs](https://plus-doc.dromara.org).

## 4. Getting started

```bash
mvn clean package -DskipTests
```

Schema scripts and configuration are under `docs/requirements/` and `script/`; see [`docs/requirements/README.md`](docs/requirements/README.md).

## 5. License

Apache-2.0. See [LICENSE](LICENSE). RuoYi-Vue-Plus retains its MIT license; see [NOTICE](NOTICE).

## 6. Contact

QQ 747011882 · 747011882@qq.com