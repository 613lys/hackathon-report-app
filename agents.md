# Agents Summary

- **架构**：Angular 前端（4200）+ Spring Boot 后端（8080），使用内置 Gradle Wrapper。
- **环境要求**：Node.js 18+、npm、JDK 17+、Git
访问网络 先运行 `$Env:http_proxy="http://127.0.0.1:7890";$Env:https_proxy="http://127.0.0.1:7890"`
- **运行**：
  - 后端：`cd backend && .\gradlew bootRun`）。默认 H2 数据库预置 admin/maker1/checker1 账号，密码为123456。
  - 前端：`cd frontend && npm start`，开发服代理 `/api` 到 `http://localhost:8080`。
  - 访问 `http://localhost:4200`，登录跳转 Maker `/maker`、Checker `/checker`，可进入 `/reports`。
- **基本联调**：验证不同账号登录、Maker 提交报表与 Checker 审批流程、双端导出 Excel
- **核心 API**：
  - 认证：`POST /api/auth/login`、`GET /api/auth/profile`、`POST /api/auth/logout`。
  - 报表：`GET /api/reports`、`GET /api/reports/{id}`、`POST /api/reports/{id}/execute`、`POST /api/report-runs/{id}/submit`、`POST /api/report-runs/{id}/decision`、`GET /api/report-runs/{id}/audit`。
- **前端能力**：Maker 执行/提交报表并下载；Checker 审批、查看审计、下载；无自定义 SQL UI。
- **更多文档**：参阅 [Wiki](./wiki/index.md) 获取架构与模块细节。
- **开发要求**：
  - 针对前后端的代码进行修改时不要修改已有的测试用例，针对测试用例修改时不要修改已有的前后端代码
  - 遵循 OpenSpec 开发流程；OpenSpec生成的中间文档都使用中文；若实现过程中需要调整方案，必须同步更新对应 spec 的 proposal/design/task 文档。
