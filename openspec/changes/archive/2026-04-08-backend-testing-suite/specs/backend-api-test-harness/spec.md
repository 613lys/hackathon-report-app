# 后端 API 测试规范

## 新增需求

### 需求：覆盖全部对外 API

API 回归测试必须对 `/api/auth`、`/api/reports`、`/api/report-runs`、`/api/report-runs/{id}/audit` 等接口发起带鉴权与不带鉴权的请求，验证成功与错误响应。

#### 场景：未授权访问被拦截

- **当** 在没有有效令牌的情况下请求 `/api/reports`
- **则** API 测试应收到 HTTP 401 以及标准错误负载。

### 需求：Maker/Checker 流程端到端校验

API 框架必须通过真实 REST 接口完成 Maker 登录、报表执行、提交、Checker 审批与审计查询，确保编排链路保持可用。

#### 场景：快乐路径产生审计记录

- **当** 框架依次执行 Maker 提交与 Checker 审批的 REST 调用
- **则** 随后对 `/api/report-runs/{id}/audit` 的请求应返回包含执行、提交、审批时间戳的记录。

### 需求：回归套件支持多环境

测试需通过 Gradle/系统属性接收基础 URL、凭证、超时等参数，在本地、QA、预发环境运行时无需改动代码。

#### 场景：环境配置决定基础 URL

- **当** 通过 Gradle 传入 `-DapiBaseUrl`
- **则** API 测试指向指定主机，同时复用相同的请求与断言逻辑。
