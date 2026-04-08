package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.OrderItemRecord;
import com.legacy.report.calculator.model.ProductRecord;
import com.legacy.report.dto.ReportContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InventoryVelocityAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 11L;

    private final JdbcTemplate jdbcTemplate;

    public InventoryVelocityAnalysisCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<ProductRecord> products = fetchProducts();
        List<OrderItemRecord> orderItems = fetchOrderItemsWithOrders();

        Map<Long, List<OrderItemRecord>> itemsByProduct = orderItems.stream()
                .filter(item -> item.getProductId() != null)
                .collect(Collectors.groupingBy(OrderItemRecord::getProductId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (ProductRecord product : products) {
            List<OrderItemRecord> items = itemsByProduct.getOrDefault(product.getId(), List.of());

            long totalSold = items.stream().mapToLong(OrderItemRecord::getQuantity).sum();
            BigDecimal unitProfit = product.getPrice().subtract(product.getCost());
            BigDecimal profitMargin = product.getPrice().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                    unitProfit.divide(product.getPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", product.getName());
            row.put("category", product.getCategory());
            row.put("current_stock", product.getStockQuantity());
            row.put("total_sold", totalSold);
            row.put("price", product.getPrice());
            row.put("cost", product.getCost());
            row.put("unit_profit", unitProfit);
            row.put("profit_margin_percent", profitMargin);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> ((Number) row.get("total_sold")).longValue()).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<ProductRecord> fetchProducts() {
        String sql = "SELECT id, name, category, price, cost, stock_quantity FROM product";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductRecord record = new ProductRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setCategory(rs.getString("category"));
            record.setPrice(rs.getBigDecimal("price"));
            record.setCost(rs.getBigDecimal("cost"));
            record.setStockQuantity(rs.getInt("stock_quantity"));
            return record;
        });
    }

    private List<OrderItemRecord> fetchOrderItemsWithOrders() {
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.total_price " +
                "FROM order_items oi JOIN orders o ON oi.order_id = o.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OrderItemRecord record = new OrderItemRecord();
            record.setId(rs.getLong("id"));
            record.setOrderId(rs.getLong("order_id"));
            record.setProductId(rs.getLong("product_id"));
            record.setQuantity(rs.getInt("quantity"));
            record.setUnitPrice(rs.getBigDecimal("unit_price"));
            record.setTotalPrice(rs.getBigDecimal("total_price"));
            return record;
        });
    }
}
