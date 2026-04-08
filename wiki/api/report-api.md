# Report & Report Run REST API

**Base URL**: `http://localhost:8080/api` **Auth**: `Authorization: Bearer <JWT>`（`/test*` 接口除外）

## Endpoints

| Method | Path | Function | Risk |
| ------ | ---- | -------- | ---- |
| GET | `/reports` | 列出所有报表配置 | — |
| GET | `/reports/{id}` | 获取指定报表详情 | — |
| POST | `/reports/run` | 执行任意 SQL（危险） | 🔴 SQL 注入 |
| POST | `/reports/generate` | 根据模板执行并返回元信息 | 🟠 参数注入 |
| POST | `/reports` | 创建报表 | 🟢 需校验 |
| POST | `/reports/{id}/execute` | 执行报表并写入运行记录 | — |
| GET | `/reports/{id}/export` | 导出最新运行 Excel | — |
| POST | `/report-runs/{id}/submit` | Maker 提交运行审批 | — |
| POST | `/report-runs/{id}/decision` | Checker 审批运行 | 🟡 审批记录可被暴力枚举 |
| GET | `/report-runs/my-latest?reportId=` | 当前 Maker 最近一次运行 | — |
| GET | `/report-runs/my-runs` | Maker 历史运行 | — |
| GET | `/report-runs/submitted` | Checker 待审列表 | — |
| GET | `/report-runs/checker/history` | Checker 审批历史 | — |
| GET | `/report-runs/{id}/audit` | 查看审计轨迹 | 🟠 任意登录用户可查 |
| GET | `/report-runs/{id}/export` | 导出某次运行 Excel | — |

---

### `GET /reports`

列出所有报表配置。

#### Response `200`

```json
[
  {
    "id": 1,
    "name": "Customer Summary",
    "sql": "SELECT ...",
    "description": "客户分层报表"
  }
]
```

---

### `POST /reports/run`

直接执行请求体中的 SQL，返回 `List<Map>`。**仅用于原型测试**。

#### Request

```http
POST /api/reports/run
Content-Type: application/json
Authorization: Bearer <token>

{
  "sql": "SELECT * FROM customer LIMIT 10"
}
```

**Response** `200`

```json
[
  {"ID": 1, "NAME": "ACME", "TYPE": "VIP"},
  {"ID": 2, "NAME": "Beta", "TYPE": "STD"}
]
```

#### 风险

任何用户可执行 DDL/DML；请下线或增加 SQL 白名单。

---

### `POST /reports/{id}/execute`

执行指定报表并写入 `report_run`，返回 SQL 结果。

```http
POST /api/reports/5/execute
Authorization: Bearer <token>
Content-Type: application/json

{}
```

**Response** `200`

```json
[
  {"REGION": "APAC", "REVENUE": 120000.45},
  {"REGION": "EMEA", "REVENUE": 98000.00}
]
```

---

### `GET /reports/{id}/export`

导出最近一次运行的 Excel。

```http
GET /api/reports/5/export
Authorization: Bearer <token>
Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

**Response** `200`

二进制流（`Content-Disposition: attachment; filename="report-5.xlsx"`）。

---

### `POST /report-runs/{id}/submit`

把状态 `Generated` 的运行提交给 Checker。

```http
POST /api/report-runs/42/submit
Authorization: Bearer <token>
Content-Type: application/json

{}
```

**Response** `200`（空体）。

---

### `POST /report-runs/{id}/decision`

Checker 审批运行。

```http
POST /api/report-runs/42/decision
Authorization: Bearer <checker-token>
Content-Type: application/json

{
  "decision": "REJECTED",
  "comment": "数据缺少对账列"
}
```

**Response** `200`

```json
{
  "id": 42,
  "status": "Rejected",
  "checkerUsername": "checker1",
  "decidedAt": "2024-04-08T05:30:00"
}
```

---

### `GET /report-runs/{id}/audit`

列出运行的审计轨迹。任何已登录用户都可访问，应在生产环境中限制权限。

**Response** `200`

```json
[
  {
    "eventType": "Generated",
    "actorUsername": "maker1",
    "eventTime": "2024-04-08T05:20:00"
  },
  {
    "eventType": "Submitted",
    "actorUsername": "maker1",
    "eventTime": "2024-04-08T05:21:00"
  }
]
```

## Error Responses

| Status | Trigger | Response |
| ------ | ------- | -------- |
| 400 | SQL 模板缺失 / ReportExportException | `{ "error": "REPORT_EXPORT_ERROR", "message": "..." }` |
| 401 | 缺少或无效 JWT | Spring Security 默认 401 JSON |
| 403 | 当前角色不匹配（Maker/Checker） | `{ "timestamp": ..., "status": 403, "error": "Forbidden" }` |
| 500 | 运行不存在、状态不合法等 RuntimeException | Spring Boot 默认错误；建议引入全局异常封装 |

## 相关文档

- [ReportController](../后端/report-controller.md)
- [ReportRunController](../后端/report-run-controller.md)
- [ReportRunService](../后端/report-run-service.md)
- [Security Layer](../后端/security.md)
