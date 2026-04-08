package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.OrderRecord;
import com.legacy.report.dto.ReportContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderFulfillmentAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 8L;

    private final JdbcTemplate jdbcTemplate;

    public OrderFulfillmentAnalysisCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<OrderRecord> orders = fetchOrders();

        Map<LocalDate, List<OrderRecord>> ordersByDate = orders.stream()
                .filter(o -> o.getOrderDate() != null)
                .collect(Collectors.groupingBy(OrderRecord::getOrderDate));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<OrderRecord>> entry : ordersByDate.entrySet()) {
            List<OrderRecord> orderList = entry.getValue();

            long total = orderList.size();
            BigDecimal totalValue = orderList.stream()
                    .map(OrderRecord::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long completed = orderList.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
            long processing = orderList.stream().filter(o -> "PROCESSING".equals(o.getStatus())).count();
            long pending = orderList.stream().filter(o -> "PENDING".equals(o.getStatus())).count();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("order_month", entry.getKey().toString());
            row.put("total_orders", total);
            row.put("total_order_value", totalValue);
            row.put("completed_orders", completed);
            row.put("processing_orders", processing);
            row.put("pending_orders", pending);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (String) row.get("order_month")));
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<OrderRecord> fetchOrders() {
        String sql = "SELECT id, customer_id, order_date, total_amount, status FROM orders";
        return jdbcTemplate.query(sql, new OrderRowMapper());
    }

    private static class OrderRowMapper implements RowMapper<OrderRecord> {
        @Override
        public OrderRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderRecord record = new OrderRecord();
            record.setId(rs.getLong("id"));
            record.setCustomerId(rs.getLong("customer_id"));
            record.setOrderDate(rs.getDate("order_date") != null ? rs.getDate("order_date").toLocalDate() : null);
            record.setTotalAmount(rs.getBigDecimal("total_amount"));
            record.setStatus(rs.getString("status"));
            return record;
        }
    }
}
