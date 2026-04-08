# Backend Agent Guide

## 核心框架

- **Spring Boot 3.2**：`ReportApplication` 作为入口，`controller → service → repository` 分层。REST 接口位于 `com.legacy.report.controller`。
- **Spring Security + JWT**：`SecurityConfig`、`JwtAuthenticationFilter/Provider` 负责认证，依赖 H2 预置账号（admin/maker1/checker1，密码 123456）。
- **Data Access**：报表主流程使用 `ReportDao + JdbcTemplate`，运行与审计依赖 `Spring Data JPA`。
- **Excel 导出**：`ReportExcelExportService` 基于 JXLS 模板输出 `.xlsx`。

> 需要快速了解整体架构、数据流与风险，请参考 [wiki/architecture.md](../wiki/architecture.md)、[wiki/后端/_index.md](../wiki/后端/_index.md) 以及数据库文档 [wiki/数据库/_index.md](../wiki/数据库/_index.md)。

## 开发节奏

1. `./gradlew bootRun` 启动后端，默认使用内存 H2；如需外部数据库，请修改 `application.yml`。
2. 控制器/服务改动必须同步更新对应 wiki 模块（如 `wiki/后端/report-service.md`）。
3. 遵循规则：**调整业务代码时不要改动现有测试；若修改测试，请勿同时改动前后端实现。**
