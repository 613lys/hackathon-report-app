# System Architecture

> Angular 前端驱动 Maker/Checker 报表流程，Spring Boot 提供受 JWT 保护的 API、报表运行持久化与 Excel 导出，Micrometer 记录运营指标，H2 数据库承担默认存储。设计强调快速演示与可替换性：数据库、模板与监控均可无缝切换。

## System Diagram

```mermaid
flowchart TB
    User((Maker/Checker))
    subgraph FE[Angular Frontend]
        UI[Report Viewer]
        Flow[Report Run Flow Component]
        AuthSvc[Auth + Report Services]
    end
    subgraph BE[Spring Boot Backend]
        AuthCtrl[AuthController]
        ReportCtrl[ReportController]
        RunCtrl[ReportRunController]
        RunSvc[ReportRunService]
        ExcelSvc[ReportExcelExportService]
        AuditSvc[AuditService]
        Repo[ReportDao & Repositories]
        Security[SecurityConfig + JWT Filter]
    end
    subgraph Data[Data Stores]
        H2[(H2 / RDBMS)]
        Templates[(JXLS Templates)]
        Metrics[(Prometheus via Micrometer)]
    end
    User -->|Browser| UI
    UI --> AuthSvc --> AuthCtrl
    UI --> Flow --> ReportCtrl
    Flow --> RunCtrl
    ReportCtrl --> RunSvc
    RunSvc --> AuditSvc --> Repo --> H2
    RunSvc --> ExcelSvc --> Templates
    RunSvc --> Metrics
    Security -. protects .-> AuthCtrl
    Security -. protects .-> ReportCtrl
    Security -. protects .-> RunCtrl
```

## Tech Stack

| Component | Technology | Version | Role |
| --------- | ---------- | ------- | ---- |
| Frontend | Angular CLI | 17.3.x | SPA, Maker/Checker UI |
| Frontend Build | Node + npm | 18+ | Dev server & bundling |
| Backend | Spring Boot | 3.2.4 | REST API, security, services |
| Persistence | Spring Data JPA + JdbcTemplate | 3.2.x | ORM + raw SQL engine |
| Database | H2 (in-memory) | 2.x | Default demo data store |
| Security | Spring Security + JJWT | 6.x / 0.11.5 | JWT issuance & validation |
| Metrics | Micrometer + Prometheus registry | 1.12.x | Report run counters & timers |
| Export | JXLS + Apache POI | 2.14.0 | Excel templating |

## Data Flow

```mermaid
sequenceDiagram
    actor Maker
    actor Checker
    participant UI as Angular UI
    participant API as Spring REST API
    participant RunSvc as ReportRunService
    participant Repo as DB + Templates
    Maker->>UI: 登录 & 选择报表
    UI->>API: POST /api/auth/login
    API-->>UI: JWT token
    UI->>API: POST /api/reports/{id}/execute (Bearer)
    API->>RunSvc: executeReportWithRun
    RunSvc->>Repo: SQL 查询 + 保存 report_run
    RunSvc-->>API: 查询结果
    API-->>UI: 数据表格
    UI->>API: POST /api/report-runs/{id}/submit
    Checker->>UI: 审批队列
    UI->>API: POST /api/report-runs/{id}/decision
    API->>RunSvc: decideRun (Approved/Rejected)
    RunSvc->>Repo: 更新 run + 审计记录
    RunSvc-->>API: 状态
    API-->>UI: 审批反馈
```

## Database Schema

```mermaid
erDiagram
    REPORT_CONFIG {
        BIGINT id PK
        VARCHAR name
        TEXT sql
        VARCHAR description
        INT is_deleted
    }
    REPORT_RUN {
        BIGINT id PK
        BIGINT report_id FK
        VARCHAR status
        VARCHAR maker_username
        VARCHAR checker_username
        TIMESTAMP generated_at
        TIMESTAMP submitted_at
        TIMESTAMP decided_at
        CLOB result_snapshot
    }
    REPORT_AUDIT_EVENT {
        BIGINT id PK
        BIGINT report_run_id FK
        BIGINT report_id FK
        VARCHAR actor_username
        VARCHAR actor_role
        VARCHAR event_type
        TIMESTAMP event_time
        CLOB comment
    }
    USERS {
        BIGINT id PK
        VARCHAR username
        VARCHAR password
        VARCHAR role
    }
    REPORT_CONFIG ||--o{ REPORT_RUN : "template for"
    REPORT_RUN ||--o{ REPORT_AUDIT_EVENT : "captured by"
    USERS ||--o{ REPORT_RUN : "acts as maker/checker"
```

## 目录结构（关键路径）

```text
hackathon-report-app/
├── backend/
│   ├── build.gradle
│   ├── src/main/java/com/legacy/report/
│   │   ├── controller/ (REST endpoints)
│   │   ├── service/ (Report, Run, Excel, Audit)
│   │   ├── security/ (JWT provider + filter)
│   │   └── repository/ (JPA interfaces)
│   └── src/main/resources/
│       ├── application.yml
│       ├── schema.sql & data.sql
│       └── report-templates/
├── frontend/
│   ├── package.json
│   └── src/app/
│       ├── components/ (report viewer, run flow)
│       └── services/ (auth/report)
└── wiki/
```

## 设计模式

| Pattern | Applied In | Purpose |
| ------- | ---------- | ------- |
| Controller-Service-Repository | `controller/*` → `service/*` → `repository/*` | 分层隔离 HTTP、业务与持久化逻辑。 |
| Builder (Micrometer & JWT) | `ReportRunService#setMeterRegistry`, `JwtTokenProvider` | 延迟注入第三方组件，集中配置指标/令牌。 |
| Template Rendering | `ReportExcelExportService#renderWithTemplate` | 通过 JXLS 模板复用导出布局，替换数据上下文。 |
| Strategy (Role checks) | `CurrentUserService#requireRole` | 通过角色字符串决定 Maker/Checker 行为。 |

## 安全风险摘要

| ID | Type | Location | Severity |
| -- | ---- | -------- | -------- |
| VUL-001 | SQL 注入 | `ReportService#runReport` / `ReportController#/reports/run` | 🔴 Critical |
| VUL-002 | 弱口令初始化 | `UserInitializer` 默认密码 123456 | 🟡 Medium |
| VUL-003 | 宽松 CORS + 无限来源 | `SecurityConfig#cors` | 🟡 Medium |
| VUL-004 | 缺少统一异常处理 | 非 `ReportExportException` | 🟢 Low |

> 详细修复方案见各模块文档的 **安全分析**。

## 相关文档

- [Index](index.md)
- [Doc Map](doc-map.md)
- [后端领域概览](后端/_index.md)
- [前端领域概览](前端/_index.md)
