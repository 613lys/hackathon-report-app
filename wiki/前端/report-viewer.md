# ReportViewerComponent

## 概述

`ReportViewerComponent` 是 Maker/Checker 的主工作台：负责登录入口、报表挑选与执行、审批提交、审计轨迹查看、Excel 导出以及待办/历史面板，集中调度 `ReportService` 与 `AuthService` 完成所有前端业务动作。@frontend/src/app/components/report/report-viewer.component.ts#30-430

## UI 分区

| 区块 | 说明 | Source |
| ---- | ---- | ------ |
| 登录区域 | 显示当前登录用户或展示用户名/密码表单与错误提示，支持一键退出。 | @frontend/src/app/components/report/report-viewer.component.html#4-25 |
| 报表选择 | 仅对拥有 MAKER 角色显示下拉框、执行按钮与 Excel 导出。 | @frontend/src/app/components/report/report-viewer.component.html#27-38 |
| 当前运行 + 审批提交 | 展示选中报表对应运行状态、时间戳，并允许 Maker 在 `Generated` 状态下提交审批、下载结果、查看审计表。 | @frontend/src/app/components/report/report-viewer.component.html#40-87 |
| 我的提交历史 | Maker 用于刷新/查看所有运行记录，并针对 Approved 运行导出或跳转流程。 | @frontend/src/app/components/report/report-viewer.component.html#89-127 |
| 查询结果与描述 | 展示中文描述、SQL 预览、表格/空结果提示。 | @frontend/src/app/components/report/report-viewer.component.html#133-161 |
| Checker 待办 & 审批 | 包含列表选择、审批表单、意见输入及实时审计表。 | @frontend/src/app/components/report/report-viewer.component.html#163-235 |
| Checker 历史 | 历史审批列表与导出/流程按钮。 | @frontend/src/app/components/report/report-viewer.component.html#239-279 |

## 数据与状态流

![ReportViewer 数据流](../assets/report-viewer-flow.svg)

> 图示来源：`wiki/assets/report-viewer-flow.svg`。若需修改样式，可直接更新该 SVG 文件。

- 组件启动时先检测登录状态，若已登录将并行加载报表、Maker 历史和 Checker 待办。@frontend/src/app/components/report/report-viewer.component.ts#110-217
- 所有 API 交互封装在 `ReportService` 内，组件只负责订阅 Observable 并更新本地状态与错误信息。@frontend/src/app/components/report/report-viewer.component.ts#145-410

## 关键交互

| 方法 | 作用 | 细节 |
| ---- | ---- | ---- |
| `login()` | 调用 `AuthService.login` 并在成功后刷新所有面板 | 处理 loading/error 状态，失败时输出后端 message。@frontend/src/app/components/report/report-viewer.component.ts#119-135 |
| `loadMakerRuns*()` | 根据当前用户角色加载最新运行，并在选中报表后补齐当前运行与审计轨迹 | `loadMakerRunsIfNeeded`、`loadMakerRuns`、`loadCurrentRunForSelectedReport`、`loadCurrentRunAudit` 组合。@frontend/src/app/components/report/report-viewer.component.ts#137-192 |
| `submitCurrentRun()` | Maker 将 `Generated` 运行推进到审批队列 | 成功后刷新当前运行并展示提示。@frontend/src/app/components/report/report-viewer.component.ts#194-208 |
| `loadCheckerRuns*()` | Checker 视角加载待办、审计与历史记录 | 自动选中首条待办并允许复用 `ReportService` 的审计接口。@frontend/src/app/components/report/report-viewer.component.ts#211-264 |
| `decideSelectedRun()` | Checker 批准/拒绝运行并刷新列表 | 失败会展示后端返回的 message。@frontend/src/app/components/report/report-viewer.component.ts#275-293 |
| `runReport()` | 触发报表执行并更新 `reportData` 与最新运行 | 运行完成后自动刷新当前运行，保证 Maker 能立即提交。@frontend/src/app/components/report/report-viewer.component.ts#348-364 |
| `export*()` | 下载报表或运行的 Excel | 根据报表名称生成中文文件名并触发浏览器下载。@frontend/src/app/components/report/report-viewer.component.ts#366-411 |

## Maker / Checker 能力矩阵

| 功能 | Maker | Checker |
| ---- | ----- | ------- |
| 执行报表、查看结果 | ✅ `runReport()` + 数据表格 | ❌ |
| 提交审批、查看审计 | ✅ 当前运行卡片 + 审计表 | 只读审计（通过待办面板） |
| 待审批列表与批注 | ❌ | ✅ 列表 + 决策 + Comment。@frontend/src/app/components/report/report-viewer.component.html#163-235 |
| 历史记录导出 | Maker 按状态导出 | Checker 历史区块导出。@frontend/src/app/components/report/report-viewer.component.html#89-279 |

## 错误与 UX 处理

- 每个异步请求都设置独立的 `error` 状态（如 `makerRunsError`、`checkerError`、`exportError`），模板根据状态显示提示并避免阻塞其他区域。@frontend/src/app/components/report/report-viewer.component.ts#145-411
- 登录/审批/导出按钮使用 `loggingIn` 或决策状态禁用，防止重复提交。@frontend/src/app/components/report/report-viewer.component.html#21-204
- 审计轨迹为空时显示友好空状态，并提供“查看完整审批流程”导航到 `ReportRunFlowComponent`。@frontend/src/app/components/report/report-viewer.component.html#58-235

## 相关文档

- [ReportRunFlowComponent](report-run-flow.md)
- [Auth Stack](auth-stack.md)
- [Report REST API](../api/report-api.md)
