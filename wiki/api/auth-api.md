# Auth REST API

**Base URL**: `http://localhost:8080/api/auth` **Auth**: `/login` & `/logout` 无需 Token，其余端点必须携带 `Authorization: Bearer <JWT>`。

## Endpoints

| Method | Path | Function | Risk |
| ------ | ---- | -------- | ---- |
| POST | `/login` | 验证用户名密码，返回 JWT + 用户元数据 | 🟡 暴力破解，无限尝试 |
| GET | `/profile` | 根据当前 Principal 返回用户信息 | 🟢 依赖上下文 |
| POST | `/logout` | 返回静态消息（未真正吊销 token） | 🟢 低 |

---

### `POST /login`

认证用户并颁发 JWT。

#### Request

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

#### Response `200` （login）

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "username": "admin",
    "role": "MAKER,CHECKER"
  }
}
```

> 失败会返回 500 RuntimeException；建议改为 401 并隐藏具体原因。

---

### `GET /profile`

返回当前登录用户的 `UserDto`。

```http
GET /api/auth/profile
Authorization: Bearer <token>
```

#### Response `200` （profile）

```json
{
  "username": "maker1",
  "role": "MAKER"
}
```

---

### `POST /logout`

注销接口，目前仅返回固定消息，不会在服务器端吊销 token。

```http
POST /api/auth/logout
```

#### Response `200` （logout）

```json
{
  "message": "Logged out successfully"
}
```

## Error Responses

| Status | Trigger | Response |
| ------ | ------- | -------- |
| 400 | 请求体缺少字段 | `{ "error": "Invalid payload" }`（需自定义） |
| 401 | JWT 缺失或无效 | `{ "error": "Unauthorized" }` |
| 500 | 用户名/密码错误时抛出的 RuntimeException | `{ "error": "Invalid username or password" }` |

## 相关文档

- [Security Layer](../后端/security.md)
- [Report API](report-api.md)
- [后端领域概览](../后端/_index.md)
