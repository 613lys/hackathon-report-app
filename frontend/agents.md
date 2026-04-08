# Frontend Agent Guide

## 核心框架

- **Angular 17 (Standalone Components)**：应用入口由 `app.component.ts` + `app.routes.ts` 驱动，组件位于 `src/app/components`。
- **关键组件**：`report-viewer.component` 展示报表数据，`report-run-flow.component` 负责 Maker/Checker 流程，配套 CSS/HTML 提供表格与审批 UI。
- **服务层**：`report.service.ts` 封装 `/api/reports*` 与 `/api/report-runs*` 请求，`auth.service.ts` + `auth.interceptor.ts` 维护 JWT，`auth.guard.ts` 保护路由。

> 更详细的前端结构图与依赖可参考 [wiki/前端/_index.md](../wiki/前端/_index.md) 与总览 [wiki/index.md](../wiki/index.md)。

## 开发节奏

1. `npm install && npm start` 启动开发服务器，代理 `/api` 至 `http://localhost:8080`。
2. 新增/调整组件或服务时，请在 wiki 前端章节补充说明，保持文档与实现一致。
3. 遵循规则：**修改前端代码时不要动现有测试；若必须调整测试，请不要同时修改前端实现。**
