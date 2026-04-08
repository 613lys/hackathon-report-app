package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.OrderItemRecord;
import com.legacy.report.calculator.model.ProductRecord;
import com.legacy.report.dto.ReportContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductProfitabilityReportCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 5L;

    private final JdbcTemplate jdbcTemplate;

    public ProductProfitabilityReportCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<ProductRecord> products = fetchProducts();
        List<OrderItemRecord> orderItems = fetchOrderItems();

        Map<Long, List<OrderItemRecord>> itemsByProduct = orderItems.stream()
                .filter(item -> item.getProductId() != null)
                .collect(Collectors.groupingBy(OrderItemRecord::getProductId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (ProductRecord product : products) {
            List<OrderItemRecord> items = itemsByProduct.getOrDefault(product.getId(), List.of());

            long totalSold = items.stream().mapToLong(OrderItemRecord::getQuantity).sum();
            if (totalSold == 0) {
                continue;
            }

            BigDecimal totalRevenue = items.stream()
                    .map(OrderItemRecord::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCost = product.getCost().multiply(BigDecimal.valueOf(totalSold));
            BigDecimal totalProfit = totalRevenue.subtract(totalCost);
            BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                    totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", product.getName());
            row.put("category", product.getCategory());
            row.put("price", product.getPrice());
            row.put("cost", product.getCost());
            row.put("stock_quantity", product.getStockQuantity());
            row.put("total_sold", totalSold);
            row.put("total_revenue", totalRevenue.setScale(2, RoundingMode.HALF_UP));
            row.put("total_cost", totalCost.setScale(2, RoundingMode.HALF_UP));
            row.put("total_profit", totalProfit.setScale(2, RoundingMode.HALF_UP));
            row.put("profit_margin_percent", profitMargin);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("total_profit")).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<ProductRecord> fetchProducts() {
        String sql = "SELECT id, name, category, price, cost, stock_quantity FROM product";
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }

    private List<OrderItemRecord> fetchOrderItems() {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price, total_price FROM order_items";
        return jdbcTemplate.query(sql, new OrderItemRowMapper());
    }

    private static class ProductRowMapper implements RowMapper<ProductRecord> {
        @Override
        public ProductRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductRecord record = new ProductRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setCategory(rs.getString("category"));
            record.setPrice(rs.getBigDecimal("price"));
            record.setCost(rs.getBigDecimal("cost"));
            record.setStockQuantity(rs.getInt("stock_quantity"));
            return record;
        }
    }

    private static class OrderItemRowMapper implements RowMapper<OrderItemRecord> {
        @Override
        public OrderItemRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderItemRecord record = new OrderItemRecord();
            record.setId(rs.getLong("id"));
            record.setOrderId(rs.getLong("order_id"));
            record.setProductId(rs.getLong("product_id"));
            record.setQuantity(rs.getInt("quantity"));
            record.setUnitPrice(rs.getBigDecimal("unit_price"));
            record.setTotalPrice(rs.getBigDecimal("total_price"));
            return record;
        }
    }
}
