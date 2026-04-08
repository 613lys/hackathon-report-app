package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.CustomerRecord;
import com.legacy.report.calculator.model.TransactionRecord;
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
public class CustomerTransactionAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public CustomerTransactionAnalysisCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<CustomerRecord> customers = fetchCustomers();
        List<TransactionRecord> transactions = fetchSuccessTransactions();

        Map<Long, List<TransactionRecord>> txByCustomer = transactions.stream()
                .collect(Collectors.groupingBy(TransactionRecord::getCustomerId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (CustomerRecord customer : customers) {
            List<TransactionRecord> txList = txByCustomer.get(customer.getId());
            if (txList == null || txList.isEmpty()) {
                continue;
            }

            BigDecimal totalAmount = txList.stream()
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long count = txList.size();
            BigDecimal avg = count == 0 ? BigDecimal.ZERO :
                    totalAmount.divide(BigDecimal.valueOf(count), 12, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", customer.getName());
            row.put("type", customer.getType());
            row.put("credit_score", customer.getCreditScore());
            row.put("total_amount", totalAmount);
            row.put("tx_count", count);
            row.put("avg_transaction", avg);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("total_amount")).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<CustomerRecord> fetchCustomers() {
        String sql = "SELECT id, name, type, credit_score FROM customer";
        return jdbcTemplate.query(sql, new CustomerRowMapper());
    }

    private List<TransactionRecord> fetchSuccessTransactions() {
        String sql = "SELECT customer_id, amount, type, status FROM transaction WHERE status = 'SUCCESS'";
        return jdbcTemplate.query(sql, new TransactionRowMapper());
    }

    private static class CustomerRowMapper implements RowMapper<CustomerRecord> {
        @Override
        public CustomerRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            CustomerRecord record = new CustomerRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setType(rs.getString("type"));
            record.setCreditScore(rs.getInt("credit_score"));
            return record;
        }
    }

    private static class TransactionRowMapper implements RowMapper<TransactionRecord> {
        @Override
        public TransactionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransactionRecord record = new TransactionRecord();
            record.setCustomerId(rs.getLong("customer_id"));
            record.setAmount(rs.getBigDecimal("amount"));
            record.setType(rs.getString("type"));
            record.setStatus(rs.getString("status"));
            return record;
        }
    }
}
