# report_config SQL 文档

## 表结构概览

`report_config` 保存可执行的预置 SQL 模板，字段包括：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | BIGINT | 模板主键 |
| name | VARCHAR(100) | 报表英文名称（前端映射到中文名） |
| sql | TEXT | 具体 SQL 模板。可能包含多语句与聚合逻辑 |
| description | VARCHAR(500) | 描述 |
| is_deleted | INT | 逻辑删除标记 |
| create_time | TIMESTAMP | 创建时间 |

## SQL 模板解析

下面根据 `backend/src/main/resources/data.sql` 中的预置数据，对每条报表 SQL 的目的、主要操作、潜在风险进行说明。

### 1. Customer Transaction Analysis

```sql
SELECT c.id AS customer_id,
       c.name AS customer_name,
       c.type AS customer_type,
       c.status AS customer_status,
       c.email,
       c.phone,
       c.address,
       c.credit_score,
       (SELECT COUNT(*) FROM transaction t WHERE t.customer_id = c.id) AS transaction_count,
       (SELECT SUM(t.amount) FROM transaction t WHERE t.customer_id = c.id) AS total_amount,
       (SELECT AVG(t.amount) FROM transaction t WHERE t.customer_id = c.id) AS avg_amount,
       c.account_balance,
       CASE
         WHEN c.credit_score >= 750 THEN 'Platinum'
         WHEN c.credit_score >= 650 THEN 'Gold'
         WHEN c.credit_score >= 550 THEN 'Silver'
         ELSE 'Standard'
       END AS credit_tier,
       CASE
         WHEN c.account_balance >= 100000 THEN 'High Value'
         WHEN c.account_balance >= 50000 THEN 'Medium Value'
         ELSE 'Growing'
       END AS relationship_segment
FROM customer c
WHERE c.is_deleted = 0 OR c.is_deleted IS NULL;
```

- **目标**：综合客户视角的交易统计，包含交易次数、金额、信用评分段、关系分层。
- **特点**：多次子查询（COUNT/SUM/AVG）按客户聚合；CASE 对信用与余额分层。
- **风险**：依赖 `transaction` 表完整性；WHERE 条件允许软删除为空。

### 2. VIP Customer Revenue Report

```sql
SELECT c.id AS customer_id,
       c.name AS vip_name,
       c.status,
       SUM(o.total_amount) AS total_revenue,
       AVG(o.total_amount) AS avg_order_value,
       COUNT(o.id) AS order_count,
       SUM(o.total_amount) - SUM(COALESCE(t.amount, 0)) AS profit_estimate,
       CASE
         WHEN SUM(o.total_amount) >= 500000 THEN 'Diamond VIP'
         WHEN SUM(o.total_amount) >= 200000 THEN 'Platinum VIP'
         WHEN SUM(o.total_amount) >= 100000 THEN 'Gold VIP'
         ELSE 'Silver VIP'
       END AS vip_segment,
       MAX(o.order_date) AS last_order_date,
       MIN(o.order_date) AS first_order_date,
       c.account_balance
FROM customer c
JOIN orders o ON o.customer_id = c.id
LEFT JOIN transaction t ON t.customer_id = c.id
WHERE c.type = 'VIP'
GROUP BY c.id, c.name, c.status, c.account_balance;
```

- **目标**：针对 VIP 客户统计订单金额、盈利估算、VIP 等级。
- **特点**：与 `orders` 表联结，LEFT JOIN `transaction` 估算利润；使用 CASE 分级。
- **风险**：订单数据量大时 SUM/COUNT 沉重；需要确保 `transaction.amount` 与订单金额可比较。

### 3. Merchant Performance Analysis

```sql
SELECT m.id AS merchant_id,
       m.name AS merchant_name,
       m.category,
       m.status,
       COUNT(t.id) AS transaction_count,
       SUM(t.amount) AS total_volume,
       AVG(t.amount) AS avg_ticket_size,
       MAX(t.amount) AS max_transaction,
       MIN(t.amount) AS min_transaction,
       (SUM(t.amount) * m.commission_rate) AS commission_estimate,
       CASE
         WHEN COUNT(t.id) >= 1000 THEN 'Tier 1'
         WHEN COUNT(t.id) >= 500 THEN 'Tier 2'
         WHEN COUNT(t.id) >= 100 THEN 'Tier 3'
         ELSE 'Emerging'
       END AS merchant_tier,
       CURRENT_DATE AS snapshot_date
FROM merchant m
LEFT JOIN transaction t ON t.merchant_id = m.id
GROUP BY m.id, m.name, m.category, m.status, m.commission_rate;
```

- **目标**：评估商家交易表现与佣金。
- **特点**：LEFT JOIN 允许无交易的商家；使用 commission_rate 估算佣金。
- **风险**：当 transaction 表数据大时需索引 `merchant_id`；`CURRENT_DATE` 维度可再拆分。

### 4. Department Budget Analysis

```sql
SELECT d.id AS department_id,
       d.name AS department_name,
       d.manager,
       d.budget,
       SUM(e.salary) AS total_salary,
       AVG(e.salary) AS avg_salary,
       COUNT(e.id) AS employee_count,
       (d.budget - SUM(e.salary)) AS budget_variance,
       CASE
         WHEN d.budget - SUM(e.salary) >= 50000 THEN 'Under Budget'
         WHEN d.budget - SUM(e.salary) BETWEEN 0 AND 49999 THEN 'On Track'
         ELSE 'Over Budget'
       END AS budget_status,
       MAX(e.hire_date) AS newest_hire,
       MIN(e.hire_date) AS earliest_hire
FROM department d
LEFT JOIN employee e ON e.department_id = d.id
GROUP BY d.id, d.name, d.manager, d.budget;
```

- **目标**：对部门预算 vs 薪资成本进行对比，得出差异与状态。
- **风险**：`SUM(e.salary)` 可能包含 NULL，需要 COALESCE；无过滤条件时扫描所有部门。

### 5. Product Profitability Report

```sql
SELECT p.id AS product_id,
       p.name AS product_name,
       p.category,
       p.price,
       p.cost,
       p.stock_quantity,
       SUM(oi.quantity) AS total_units_sold,
       SUM(oi.total_price) AS total_revenue,
       SUM(oi.total_price) - SUM(p.cost * oi.quantity) AS gross_profit,
       (SUM(oi.total_price) - SUM(p.cost * oi.quantity)) / NULLIF(SUM(oi.total_price), 0) AS gross_margin,
       AVG(oi.unit_price) AS avg_unit_price,
       MAX(oi.unit_price) AS max_unit_price,
       MIN(oi.unit_price) AS min_unit_price,
       CURRENT_DATE AS snapshot_date
FROM product p
LEFT JOIN order_items oi ON oi.product_id = p.id
GROUP BY p.id, p.name, p.category, p.price, p.cost, p.stock_quantity;
```

- **目标**：分析产品销量、收入、毛利润和毛利率。
- **风险**：`SUM(p.cost * oi.quantity)` 会重复成本，应改为 `SUM(oi.quantity * p.cost)`（当前等价）；`NULLIF` 防止除零。

### 6. Customer Segmentation Analysis

```sql
SELECT c.id,
       c.name,
       c.type,
       c.status,
       c.credit_score,
       c.account_balance,
       CASE
         WHEN c.account_balance >= 100000 AND c.credit_score >= 700 THEN 'Elite Prime'
         WHEN c.account_balance >= 50000 AND c.credit_score >= 650 THEN 'Growth Champion'
         WHEN c.account_balance >= 20000 AND c.credit_score >= 600 THEN 'Emerging Potential'
         ELSE 'Mass Market'
       END AS segment,
       SUM(t.amount) AS total_spent,
       COUNT(t.id) AS transaction_count,
       AVG(t.amount) AS avg_transaction_value,
       MAX(t.amount) AS max_transaction,
       MIN(t.amount) AS min_transaction,
       (SELECT COUNT(*) FROM orders o WHERE o.customer_id = c.id) AS order_count,
       (SELECT MAX(o.order_date) FROM orders o WHERE o.customer_id = c.id) AS last_order_date
FROM customer c
LEFT JOIN transaction t ON t.customer_id = c.id
GROUP BY c.id, c.name, c.type, c.status, c.credit_score, c.account_balance;
```

- **目标**：基于资产与信用评分给客户分层，并辅以交易指标。
- **特点**：组合 CASE + 聚合；额外子查询统计订单行为。

### 7. Monthly Revenue Trend Analysis

```sql
SELECT DATE_TRUNC('month', t.transaction_date) AS month,
       SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS income,
       SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense,
       SUM(t.amount) AS net_flow,
       COUNT(t.id) AS transaction_count,
       AVG(t.amount) AS avg_transaction,
       MAX(t.amount) AS max_transaction,
       MIN(t.amount) AS min_transaction
FROM transaction t
GROUP BY DATE_TRUNC('month', t.transaction_date)
ORDER BY month;
```

- **目标**：按月汇总收入、支出与净流量。
- **风险**：依赖数据库支持 `DATE_TRUNC`（H2 可兼容，但迁移需注意）。

### 8. Order Fulfillment Analysis

```sql
SELECT o.id AS order_id,
       o.order_date,
       o.total_amount,
       o.status,
       o.shipping_address,
       c.name AS customer_name,
       c.type AS customer_type,
       c.status AS customer_status,
       SUM(oi.total_price) AS item_total,
       COUNT(oi.id) AS item_count,
       SUM(oi.quantity) AS total_quantity,
       CASE
         WHEN o.status = 'COMPLETED' THEN 'Delivered'
         WHEN o.status = 'PENDING' THEN 'In Progress'
         WHEN o.status = 'CANCELLED' THEN 'Cancelled'
         ELSE 'Other'
       END AS fulfillment_status
FROM orders o
LEFT JOIN order_items oi ON oi.order_id = o.id
LEFT JOIN customer c ON o.customer_id = c.id
GROUP BY o.id, o.order_date, o.total_amount, o.status, o.shipping_address, c.name, c.type, c.status;
```

- **目标**：结合订单与项目信息衡量履约情况。
- **风险**：`SUM(oi.total_price)`可能重复订单金额，需小心 double count。

### 9. Employee Performance Metrics

```sql
SELECT e.id AS employee_id,
       e.name AS employee_name,
       e.position,
       e.salary,
       e.status,
       d.name AS department_name,
       d.budget AS department_budget,
       e.hire_date,
       CASE
         WHEN e.salary >= 150000 THEN 'Strategic Talent'
         WHEN e.salary BETWEEN 100000 AND 149999 THEN 'Key Contributor'
         WHEN e.salary BETWEEN 60000 AND 99999 THEN 'Core Professional'
         ELSE 'Developing Talent'
       END AS talent_segment,
       CASE
         WHEN e.status = 'ACTIVE' THEN 'Employed'
         WHEN e.status = 'ON_LEAVE' THEN 'Temporary Leave'
         ELSE 'Inactive'
       END AS status_segment,
       CURRENT_DATE AS snapshot_date
FROM employee e
LEFT JOIN department d ON e.department_id = d.id;
```

- **目标**：按薪资区间划分人才层级。
- **风险**：无 WHERE 过滤，列表可能很大。

### 10. Customer-Merchant Revenue Matrix

```sql
SELECT c.id AS customer_id,
       c.name AS customer_name,
       m.id AS merchant_id,
       m.name AS merchant_name,
       SUM(t.amount) AS total_revenue,
       COUNT(t.id) AS transaction_count,
       AVG(t.amount) AS avg_ticket,
       ROW_NUMBER() OVER (PARTITION BY c.id ORDER BY SUM(t.amount) DESC) AS customer_rank,
       ROW_NUMBER() OVER (PARTITION BY m.id ORDER BY SUM(t.amount) DESC) AS merchant_rank,
       CASE
         WHEN SUM(t.amount) >= 50000 THEN 'Strategic Partner'
         WHEN SUM(t.amount) >= 20000 THEN 'Key Partner'
         WHEN SUM(t.amount) >= 5000 THEN 'Growth Partner'
         ELSE 'Emerging Partner'
       END AS partnership_tier
FROM transaction t
JOIN customer c ON t.customer_id = c.id
JOIN merchant m ON t.merchant_id = m.id
GROUP BY c.id, c.name, m.id, m.name;
```

- **目标**：构建客户与商家之间的收入矩阵，识别关键合作关系。
- **风险**：使用窗口函数 `ROW_NUMBER`，需数据库支持（H2 支持）。

### 11. Inventory Velocity Analysis

```sql
SELECT p.id AS product_id,
       p.name AS product_name,
       p.category,
       p.price,
       p.cost,
       p.stock_quantity,
       SUM(oi.quantity) AS total_units_sold,
       SUM(oi.quantity) / NULLIF(p.stock_quantity, 0) AS stock_turnover,
       SUM(oi.total_price) AS total_revenue,
       SUM(oi.total_price) - SUM(p.cost * oi.quantity) AS profit_generated,
       CASE
         WHEN SUM(oi.quantity) >= 1000 THEN 'Fast Moving'
         WHEN SUM(oi.quantity) BETWEEN 500 AND 999 THEN 'Steady Performer'
         WHEN SUM(oi.quantity) BETWEEN 100 AND 499 THEN 'Emerging Product'
         ELSE 'Slow Mover'
       END AS velocity_segment,
       CURRENT_DATE AS snapshot_date
FROM product p
LEFT JOIN order_items oi ON oi.product_id = p.id
GROUP BY p.id, p.name, p.category, p.price, p.cost, p.stock_quantity;
```

- **目标**：量化库存周转速度与盈利贡献。
- **风险**：`NULLIF(p.stock_quantity, 0)` 防止除零；库存为 0 时结果为 NULL。

### 12. Financial Health Scorecard

```sql
SELECT DATE_TRUNC('month', o.order_date) AS month,
       SUM(o.total_amount) AS total_revenue,
       COUNT(o.id) AS order_count,
       AVG(o.total_amount) AS avg_order_value,
       SUM(t.amount) FILTER (WHERE t.type = 'EXPENSE') AS total_expense,
       SUM(t.amount) FILTER (WHERE t.type = 'INCOME') AS total_income,
       SUM(o.total_amount) - SUM(t.amount) FILTER (WHERE t.type = 'EXPENSE') AS net_profit,
       COUNT(DISTINCT c.id) AS active_customers,
       COUNT(DISTINCT m.id) AS active_merchants,
       COUNT(DISTINCT p.id) AS active_products
FROM orders o
LEFT JOIN transaction t ON DATE_TRUNC('month', t.transaction_date) = DATE_TRUNC('month', o.order_date)
LEFT JOIN customer c ON o.customer_id = c.id
LEFT JOIN merchant m ON EXISTS (
    SELECT 1 FROM transaction tx
    WHERE tx.merchant_id = m.id
      AND DATE_TRUNC('month', tx.transaction_date) = DATE_TRUNC('month', o.order_date)
)
LEFT JOIN order_items oi ON oi.order_id = o.id
LEFT JOIN product p ON oi.product_id = p.id
GROUP BY DATE_TRUNC('month', o.order_date)
ORDER BY month;
```

- **目标**：综合月度收入、费用、利润以及活跃客户/商家/产品，形成财务仪表板。
- **风险**：大量 LEFT JOIN + DATE_TRUNC 计算，需优化索引；FILTER 语法要求 PostgreSQL/H2 方言支持。

## 风险与改进建议

1. **SQL 注入**：当前 API 允许 Maker/Checker 直接执行 report_config 中的 SQL，极易被复制修改。应限制为只读模板并在服务端添加参数绑定。  
2. **性能瓶颈**：多处使用 `SUM(oi.total_price)` 等扫描大表，应结合数据库索引或预计算视图。  
3. **函数兼容性**：`DATE_TRUNC`、`FILTER` 等函数依赖特定数据库；若迁移至 MySQL/Oracle 需重写。  
4. **数据质量**：许多模板假设非空字段（如 `transaction.amount`），实际查询应 `COALESCE`。  

## 相关文档

- [Architecture](../architecture.md)
- [Report API](../api/report-api.md)
- [Backend ReportService](../后端/report-service.md)
- [Doc Map](../doc-map.md)
