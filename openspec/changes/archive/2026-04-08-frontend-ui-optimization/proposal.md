## Why

当前报表管理系统的前端UI使用基础的绿色主题（#4CAF50）和简单的块状布局，视觉层次不够清晰，交互反馈不够现代。为了提升用户体验和专业感，需要对前端样式进行全面优化，建立统一的设计系统。

**主要问题：**
- 颜色系统单调，只有绿色主题，缺乏语义化颜色区分
- 按钮样式单一，没有层次区分（主/次/危险操作）
- 表格和卡片设计过时，缺乏现代感
- 响应式适配不足
- 缺少统一的间距、阴影、圆角规范

**目标：**
- 建立基于CSS变量的设计系统
- 提升视觉专业感和用户体验
- 保持所有现有功能和文字内容不变
- 只修改样式（颜色、布局、间距等）

## What Changes

### 全局样式系统 (`styles.css`)
- 新增CSS变量系统：主色调（专业蓝）、语义化颜色（成功/警告/错误/信息）、灰度系统
- 定义阴影层级（sm/md/lg）和圆角规范（6/8/12/16px）
- 建立间距系统（4-32px）
- 优化全局字体和排版样式

### 登录页面 (`login.component.ts`)
- 现代化卡片布局（圆角+阴影）
- 输入框聚焦蓝色光环效果
- 按钮悬停上浮动画
- 错误信息带左侧边框提示样式

### 报表管理主界面 (`report-viewer.component.css`)
- 卡片式设计：白色背景、柔和阴影、悬停效果
- 按钮层次：主按钮蓝色、次按钮灰白、危险按钮红色
- 表格美化：斑马纹、悬停效果、圆角表头
- Checker视图左右分栏布局
- 响应式适配优化

### 审批流程时间线 (`report-run-flow.component.ts`)
- 渐变连接线（蓝→灰）
- 彩色节点设计（首节点高亮、末节点绿色）
- 卡片式内容展示
- 空状态优化

## Capabilities

### New Capabilities
- `design-system`: 建立基于CSS变量的统一设计系统，包含颜色、间距、阴影、圆角规范

### Modified Capabilities
- 无（本次优化仅涉及样式层面，不改变功能需求）

## Impact

**影响的文件：**
- `frontend/src/styles.css` - 全局样式和设计系统变量
- `frontend/src/app/components/auth/login.component.ts` - 登录组件内联样式
- `frontend/src/app/components/report/report-viewer.component.css` - 报表查看器样式
- `frontend/src/app/components/report/report-run-flow.component.ts` - 审批流程时间线样式

**不改变的：**
- 所有HTML模板文字内容
- 组件逻辑和TypeScript代码
- API接口调用
- 后端代码
- 路由配置
