# xfjk · Xinfei Health

The back office for a medical examination center. From maintaining examination items, assembling packages and scheduling appointments, through to report generation, critical-value alerts and health assessments.

The foundation is RuoYi-Vue-Plus, but the business layer was rewritten. Examination centers have their own rules: packages are assembled from item groups, appointments respect a daily capacity, any critical value in a report must reach a physician immediately, and a client who finishes a checkup expects an assessment they can actually understand. A generic admin panel gives you none of that.

## Two business lines

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

## Modules

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

## Stack

| Layer | Choice |
|---|---|
| Foundation | RuoYi-Vue-Plus (MIT) |
| JDK | 25 |
| Framework | Spring Boot 4.1.1 |
| Auth | Sa-Token + JWT |
| ORM | MyBatis-Plus |
| Database | MySQL (PostgreSQL switchable) |
| Cache | Redis + Redisson |

See the [upstream project](https://gitee.com/dromara/RuoYi-Vue-Plus) and [docs](https://plus-doc.dromara.org).

## Getting started

```bash
mvn clean package -DskipTests
```

Schema scripts and configuration are under `docs/requirements/` and `script/`; see [`docs/requirements/README.md`](docs/requirements/README.md).

## License

Apache-2.0. See [LICENSE](LICENSE). RuoYi-Vue-Plus retains its MIT license; see [NOTICE](NOTICE).

## Contact

QQ 747011882 · 747011882@qq.com
