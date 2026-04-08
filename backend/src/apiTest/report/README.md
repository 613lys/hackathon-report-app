# API 回归测试报告目录

本目录包含 API 回归测试的所有报告和相关文档。

---

## 📁 目录结构

```
src/apiTest/report/
├── README.md                    # 本文件 - 目录说明
├── API_TEST_REPORT.md           # 详细的 API 测试报告 (Markdown)
└── html-report/                 # HTML 可视化测试报告
    ├── index.html               # 报告首页
    ├── classes/                 # 测试类详细结果
    │   └── com.legacy.report.api.ReportApiTest.html
    ├── packages/                # 包级别统计
    ├── css/                     # 样式文件
    └── js/                      # JavaScript 文件
```

---

## 📊 测试概览

- **测试类**: `com.legacy.report.api.ReportApiTest`
- **总测试数**: 24 个
- **通过**: 22 个 ✅
- **禁用**: 2 个 ⏸️ (带 TODO)
- **失败**: 0 个 ✅
- **可用率**: 91.7%

---

## 🌐 如何查看报告

### 方式 1: HTML 可视化报告（推荐）
```bash
# 在浏览器中打开
start src/apiTest/report/html-report/index.html
```

或直接在 IDE 中打开 `html-report/index.html`

### 方式 2: Markdown 报告
查看 `API_TEST_REPORT.md` 文件，包含：
- 所有测试端点清单
- 测试场景详情
- Gradle 参数化配置说明
- 已知限制 (TODO)

---

## ✅ 已验证的 API 端点

| 接口组 | 端点数 | 状态 |
|--------|-------|------|
| `/api/auth/*` | 4 | ✅ |
| `/api/reports/*` | 5 | ✅ |
| `/api/report-runs/*` | 14 | ✅ |

**总计**: 23 个端点场景已通过验证

---

## 📝 测试代码位置

- **测试类**: `src/apiTest/java/com/legacy/report/api/ReportApiTest.java`
- **配置**: `src/apiTest/resources/application-test.properties`

---

## 🚀 重新运行测试

```bash
cd backend
.\gradlew.bat apiTest
```

运行后会更新 `build/reports/tests/apiTest/` 下的报告，需要手动同步到本目录。

---

## 📅 报告生成时间

2026-04-08
