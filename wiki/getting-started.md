# Getting Started

## Prerequisites

| Requirement | Version | Check |
| ------------ | ------- | ----- |
| Java JDK | 17+ | `java -version` |
| Node.js + npm | 18+ / 10+ | `node -v && npm -v` |
| Gradle Wrapper | Bundled | `cd backend && ./gradlew --version` |
| Angular CLI (optional) | 17+ | `npm ls -g @angular/cli` |

## Setup

```bash
# Clone
git clone https://github.com/your-org/hackathon-report-app.git
cd hackathon-report-app

# Backend dependencies (Gradle wrapper downloads automatically)
cd backend
./gradlew clean build

# Frontend dependencies
cd ../frontend
npm install

# 环境配置
cp ../backend/src/main/resources/application.yml ../backend/src/main/resources/application.local.yml
# 根据需要修改数据库、JWT secret 与日志路径
```

## Run

```bash
# Backend
cd backend
./gradlew bootRun
# → 服务监听 http://localhost:8080，默认使用 H2 并暴露 /h2-console

# Frontend
cd ../frontend
npm start
# → http://localhost:4200 提供 Maker/Checker 界面
```

## Verify

```bash
# 健康检查
curl http://localhost:8080/actuator/health
# 预期输出 {"status":"UP"}

# 登陆获取 JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
# 响应中应包含 token 字段
```

## Common Issues

| Issue | Cause | Fix |
| ----- | ----- | --- |
| `Access denied` when calling `/api/*` | 缺少 Authorization 头或 token 过期 | 重新调用 `/api/auth/login`，在请求头添加 `Authorization: Bearer <token>` |
| Frontend cannot reach backend | 后端端口或 CORS 错误 | 确认 `application.yml` 中 `server.port=8080`，或在 Angular `environment.ts` 中更新 API 基址 |
| Excel 导出失败，提示模板不存在 | `report-templates/report-{id}.xlsx` 缺失 | 在 `backend/src/main/resources/report-templates/` 中新增对应模板或复制现有模板 |
| H2 数据未持久化 | 使用内存数据库 | 在 `application.yml` 中改为文件型 H2 或外部数据库，并执行 `schema.sql` 初始化 |

## 相关文档

- [Index](index.md)
- [Architecture](architecture.md)
- [Doc Map](doc-map.md)
