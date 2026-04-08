# 后端测试套件 – 任务清单

## 1. Gradle 与 Jacoco 落地

- [x] 1.1 在后端 `build.gradle` 中应用 Jacoco 插件并配置 `jacocoTestReport` 输出
- [x] 1.2 添加 `jacocoTestCoverageVerification`（80% 行 / 70% 分支）并接入 CI
- [x] 1.3 定义 `stressTest`、`apiTest` 源码集与任务，依赖编译结果且产出报告

## 2. 单元测试扩展

- [x] 2.1 在 `src/test/resources` 补充 Maker/Checker 账号、报表、报表执行夹具/构造器
- [x] 2.2 编写服务层单测，覆盖 Maker 提交流程、Checker 决策与审计日志
- [x] 2.3 编写仓储层单测，验证报表查询、状态过滤与分页逻辑
- [ ] 2.4 确认整体单测覆盖率在本地与 CI 均通过 Jacoco 阈值

## 3. 压测框架

- [x] 3.1 在 `src/stressTest/java` 建立 Spring Boot 上下文并支持可配置虚拟用户
- [x] 3.2 实现 Maker 登录、报表执行、提交、Checker 审批等并发场景并采集指标
- [ ] 3.3 加入环境重置钩子（DB 回滚/重置），并在延迟/错误超阈时让构建失败

## 4. API 回归测试

- [x] 4.1 在 `src/apiTest/java` 创建随机端口启动的测试套件并复用夹具工具
- [x] 4.2 覆盖 `/api/auth`、`/api/reports`、`/api/report-runs`、`/api/report-runs/{id}/audit` 等正向场景
- [x] 4.3 添加未授权、无效请求、Maker/Checker 权限错误等负向用例
- [x] 4.4 通过 Gradle 属性参数化基础 URL/凭证/超时并补充运行文档

## 5. 文档与 CI

- [x] 5.1 更新 README/wiki 测试章节，说明单测/压测/API 套件的执行方式与覆盖率路径
- [x] 5.2 在 CI 中新增对应任务（单测走 PR，压测/API 走夜间）并归档 Jacoco 产物
