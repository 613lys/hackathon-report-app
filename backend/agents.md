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

## 测试执行

### 单元测试

单元测试位于 `src/test/java`，使用 JUnit 5 + Mockito 测试 Service 层和 Repository 层：

```bash
# 运行所有单元测试
./gradlew test

# 查看 HTML 报告
open build/reports/tests/test/index.html

# 运行单个测试类
./gradlew test --tests "*AuthServiceTest"

# 运行测试并生成 Jacoco 覆盖率报告
./gradlew test jacocoTestReport

# 查看覆盖率报告（验证 80% 行覆盖/70% 分支覆盖阈值）
open build/reports/jacoco/test/html/index.html
```

**测试覆盖范围：**
- `AuthServiceTest` - 认证登录逻辑
- `CurrentUserServiceTest` - 当前用户上下文
- `ReportRunServiceTest` - 报表运行 Maker/Checker 流程
- `ReportServiceTest` - 报表查询与执行
- `ReportRunRepositoryTest` - 报表运行数据访问

**测试报告位置：**
- HTML 报告：`build/reports/tests/test/index.html`
- XML 结果：`build/test-results/test/`
- Jacoco 覆盖率：`build/reports/jacoco/test/html/index.html`
- 归档报告：`src/test/report/html-report/`

### API 回归测试

API 测试位于 `src/apiTest/java`，使用 RestAssured + Spring Boot Test 覆盖所有后端接口：

```bash
# 运行所有 API 测试（使用随机端口启动应用）
./gradlew apiTest

# 查看 HTML 报告
open build/reports/tests/apiTest/index.html
```

**支持的系统属性参数：**

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `apiBaseUrl` | `http://localhost:{randomPort}` | 外部部署测试时指定 URL |
| `apiTest.username` | `maker1` | Maker 测试账号 |
| `apiTest.password` | `123456` | 测试账号密码 |
| `apiTest.checkerUsername` | `checker1` | Checker 测试账号 |

```bash
# 示例：测试外部部署
./gradlew apiTest -DapiBaseUrl=http://localhost:8080

# 示例：使用自定义账号
./gradlew apiTest -DapiTest.username=myMaker -DapiTest.password=secret
```

**测试报告位置：**
- HTML 报告：`build/reports/tests/apiTest/index.html`
- XML 结果：`build/test-results/apiTest/`
- 详细文档：`src/apiTest/report/API_TEST_REPORT.md`
