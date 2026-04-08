## 1. 建立全局设计系统

- [x] 1.1 在 `frontend/src/styles.css` 中添加 `:root` CSS变量：主色调蓝色系（--primary-50 至 --primary-700）
- [x] 1.2 在 `frontend/src/styles.css` 中添加语义化颜色变量：--success、--warning、--danger、--info 及对应浅色
- [x] 1.3 在 `frontend/src/styles.css` 中添加灰度系统变量（--gray-50 至 --gray-900）
- [x] 1.4 在 `frontend/src/styles.css` 中添加阴影变量（--shadow-sm / md / lg）
- [x] 1.5 在 `frontend/src/styles.css` 中添加圆角变量（--radius-sm / md / lg / xl）
- [x] 1.6 在 `frontend/src/styles.css` 中添加间距变量（--space-1 至 --space-8）
- [x] 1.7 更新 `body` 全局样式：系统字体栈、背景色、行高、font-smoothing
- [x] 1.8 更新全局 `h1/h2/h3/p` 排版样式，使用设计系统变量

## 2. 优化登录组件样式

- [x] 2.1 更新 `login.component.ts` 内联样式：`.login-container` 圆角卡片（--radius-xl、--shadow-lg）
- [x] 2.2 更新 `.login-form label` 样式：flex列排列，使用设计系统间距变量
- [x] 2.3 更新 `.login-form input` 样式：圆角输入框，聚焦蓝色光环效果（box-shadow: 0 0 0 3px）
- [x] 2.4 更新登录 `button` 样式：蓝色填充、圆角、悬停上浮动画（transform + transition）
- [x] 2.5 更新 `.error` 样式：浅红背景 + 左侧红色边框，替代纯红文字

## 3. 优化报表管理主界面样式

- [x] 3.1 更新 `report-viewer.component.css` 中 `.section` 样式：白色卡片、圆角、浅阴影、悬停加深
- [x] 3.2 更新 `.auth-section` 为 flex 水平排列布局（用户信息与退出按钮同行）
- [x] 3.3 更新所有 `select / input / textarea` 样式：统一边框、圆角、聚焦光环
- [x] 3.4 更新 `button` 全局样式：统一内边距、圆角、transition 过渡
- [x] 3.5 实现主按钮样式（蓝色填充 --primary-600，悬停 --primary-700，上浮效果）
- [x] 3.6 实现次要按钮样式（灰白背景、灰色边框，用于刷新/退出类操作）
- [x] 3.7 实现禁用按钮样式（灰色、not-allowed 光标，移除 transform/shadow）
- [x] 3.8 更新 `table` 样式：border-collapse separate、圆角、overflow hidden
- [x] 3.9 更新 `th` 样式：浅灰背景替代绿色、深灰文字、大写小字、双底边框
- [x] 3.10 更新 `td` 样式：统一内边距，行悬停浅灰背景，末行无底边框
- [x] 3.11 更新 `.checker-section` 为 CSS Grid 双栏布局（320px + 1fr）
- [x] 3.12 更新 `.checker-list` 和 `.checker-detail` 样式：浅灰/白色背景卡片
- [x] 3.13 更新 `.audit-section` 样式：顶部分割线，与上方内容区分
- [x] 3.14 更新 `.report-desc` 样式：蓝色左边框 + 浅蓝背景
- [x] 3.15 更新 `.sql-preview` 样式：深色背景代码块（--gray-900）、等宽字体
- [x] 3.16 更新 `.info` 提示样式：浅蓝背景 + 蓝色左边框
- [x] 3.17 更新 `.error` 提示样式：浅红背景 + 红色左边框

## 4. 优化审批流程时间线样式

- [x] 4.1 更新 `report-run-flow.component.ts` 内联样式：`.flow-container` 最大宽度、居中
- [x] 4.2 更新 `.back` 按钮样式：次要按钮样式（灰色系）
- [x] 4.3 更新 `.timeline` 样式：`::before` 渐变连接线（蓝→灰）
- [x] 4.4 更新 `.timeline li::before` 圆形节点：蓝色背景、白色边框、阴影
- [x] 4.5 更新首个节点（`:nth-child(1)::before`）：更大尺寸 + 外发光效果
- [x] 4.6 更新末尾节点（`:last-child::before`）：绿色背景（--success）
- [x] 4.7 更新 `.content` 样式：白色卡片、圆角、阴影
- [x] 4.8 更新 `.time` 样式：小号灰色字体
- [x] 4.9 更新 `.type` 样式：粗体、深色字体
- [x] 4.10 更新 `.comment` 样式：浅灰背景块、蓝色左边框
- [x] 4.11 更新 `.error` 样式：与全局错误样式保持一致

## 5. 响应式适配

- [x] 5.1 在 `report-viewer.component.css` 中添加 `@media (max-width: 768px)` 断点
- [x] 5.2 移动端下 `.checker-section` 折叠为单列（grid-template-columns: 1fr）
- [x] 5.3 移动端下按钮宽度 100%、垂直排列
- [x] 5.4 移动端下表格字体缩小（13px）、内边距减少
- [x] 5.5 移动端下时间线缩小节点尺寸和左偏移

## 6. 验证与检查

- [x] 6.1 运行 `npm start` 确认前端编译无错误
- [x] 6.2 登录页面视觉检查：蓝色主题、卡片布局、聚焦效果
- [x] 6.3 Maker视图检查：报表选择卡片、执行按钮、表格样式
- [x] 6.4 Checker视图检查：双栏布局、审批操作、历史记录表格
- [x] 6.5 审批流程时间线页面检查：渐变线、节点颜色、卡片内容
- [x] 6.6 调整浏览器窗口至 768px 以下，验证响应式布局
- [x] 6.7 确认所有文字内容未发生变化
