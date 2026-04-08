# 单元测试报告目录

本目录包含单元测试的所有报告和相关文档。

---

## 📁 目录结构

```
src/test/report/
├── README.md                    # 本文件 - 目录说明
└── html-report/                 # HTML 可视化测试报告
    ├── index.html               # 报告首页
    ├── classes/                 # 测试类详细结果
    │   ├── com.legacy.report.service.AuthServiceTest.html
    │   ├── com.legacy.report.service.CurrentUserServiceTest.html
    │   ├── com.legacy.report.service.ReportRunServiceTest.html
    │   ├── com.legacy.report.service.ReportServiceTest.html
    │   └── com.legacy.report.repository.ReportRunRepositoryTest.html
    ├── packages/                # 包级别统计
    ├── css/                     # 样式文件
    └── js/                      # JavaScript 文件
```

---

## 📊 测试概览

- **测试位置**: `src/test/java`
- **测试类**: 5 个
  - `AuthServiceTest` - 认证服务测试
  - `CurrentUserServiceTest` - 当前用户服务测试
  - `ReportRunServiceTest` - 报表运行服务测试
  - `ReportServiceTest` - 报表服务测试
  - `ReportRunRepositoryTest` - 报表运行仓储测试
- **总测试数**: 详见 HTML 报告
- **状态**: ✅ **BUILD SUCCESSFUL**

---

## 🌐 如何查看报告

### 方式 1: HTML 可视化报告（推荐）
```bash
# 在浏览器中打开
start src/test/report/html-report/index.html
```

或直接在 IDE 中打开 `html-report/index.html`

---

## 📝 测试代码位置

- **测试代码**: `src/test/java/com/legacy/report/`
- **夹具数据**: `src/test/resources/fixtures/`

---

## 🚀 重新运行测试

```bash
cd backend
.\gradlew.bat test
```

运行后会更新 `build/reports/tests/test/` 下的报告，需要手动同步到本目录。

---

## 📅 报告生成时间

2026-04-08
