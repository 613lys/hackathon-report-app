# 后端测试套件 – 设计

## 背景

Spring Boot 后端向 Maker/Checker 角色暴露了认证、报表管理与报表执行流程。目前自动化测试仅涵盖少量集成用例，缺乏可量化覆盖率。没有结构化的压测来模拟并发报表执行，API 验证也主要靠人工。Gradle 已负责构建流程，因此可以扩展 Jacoco 报告与独立的测试源码目录（如 `src/stressTest/java`、`src/apiTest/java`）。H2 内存数据库配合预置用户可在无外部依赖情况下进行确定性验证。

## 目标 / 非目标

**目标：**

- 借助 Jacoco 阈值为服务、仓储、工具层提供确定性的单测覆盖率。
- 搭建可扩展的压测框架，模拟高并发的 Maker/Checker/报表流程。
- 实施覆盖全部后端接口的 API 回归测试，包含正、负向场景。
- 将所有测试套件纳入 Gradle，便于 CI 统一执行与产出覆盖率报告。

**非目标：**

- 替换现有业务逻辑或数据模型——测试仅观察行为，不改变流程。
- 构建分布式压测平台；压测将使用单 JVM（或 Gradle 管理的）方式。
- 开发前端或端到端自动化——范围限定在后端服务与 API 层。

## 设计决策

1. **通过 Gradle 集成 Jacoco**
   - 在 `build.gradle` 启用 Jacoco 插件，配置 `jacocoTestReport`，并使用 `jacocoTestCoverageVerification` 设定最小覆盖率（如 80% 行覆盖 / 70% 分支覆盖）。
   - 报告输出到 `build/reports/jacoco`，供 CI 归档。

2. **测试源码结构**
   - 继续在 `src/test/java` 下编写标准单测（JUnit 5 + Mockito）。
   - 新增 `stressTest` 与 `apiTest` 源码目录，各自维护依赖（压测可用 Reactor/虚拟线程，API 可用 RestAssured/WebTestClient）。
   - 注册 `stressTest`、`apiTest` Gradle 任务，依赖 `testClasses`，必要时通过 SpringBootTest 共享应用上下文。

3. **压测框架**
   - 使用虚拟线程或受控 ForkJoin 池模拟数百并发用户执行报表流程。
   - 每个场景种子化 H2 数据，迭代间通过事务回滚避免数据串扰。
   - 采集延迟分位数、失败次数等指标，超过阈值即失败构建。

4. **API 测试策略**
   - 通过 `@SpringBootTest(webEnvironment = RANDOM_PORT)` 在随机端口启动应用。
   - 使用 RestAssured 覆盖 `/api/auth`、`/api/reports`、`/api/report-runs` 以及审计、鉴权失败等场景。
   - 对外部集成（如文件导出）使用 Spring Profile 的 Mock，保持测试可重复。

5. **夹具与工具**
   - 在 `src/test/resources/fixtures` 统一管理 Maker/Checker 账号及样例报表载荷。
   - 提供 `ReportRun` 数据构造器，以及登录/令牌获取的辅助方法，供压测与 API 套件共享。

## 风险与取舍

- **构建时间上升** → 通过 Gradle 并行执行测试，并将压测安排到夜间 CI，而单测/API 在 PR 阶段运行。
- **压测场景不稳定** → 采用确定性数据，除非需要参数化，否则禁用随机性，并在准备阶段设置充足超时与重试。
- **覆盖率限制阻塞 PR** → 初期可采用警告或较低阈值，待基线稳定后再逐步提高。
- **CI 资源受限** → 通过环境变量控制并发量；本地默认使用较低负载，而 CI/夜间任务可配置更高值。
