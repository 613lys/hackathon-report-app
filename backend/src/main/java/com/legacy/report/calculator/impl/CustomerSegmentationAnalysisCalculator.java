package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.CustomerRecord;
import com.legacy.report.calculator.model.TransactionRecord;
import com.legacy.report.dto.ReportContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomerSegmentationAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 6L;

    private final JdbcTemplate jdbcTemplate;

    public CustomerSegmentationAnalysisCalculator(JdbcTemplate jdbcTemplate) {
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

            long incomeTx = txList.stream().filter(tx -> "INCOME".equals(tx.getType())).count();
            long expenseTx = txList.stream().filter(tx -> "EXPENSE".equals(tx.getType())).count();
            BigDecimal totalIncome = txList.stream()
                    .filter(tx -> "INCOME".equals(tx.getType()))
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExpense = txList.stream()
                    .filter(tx -> "EXPENSE".equals(tx.getType()))
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String segment;
            if (totalIncome.compareTo(new BigDecimal("15000")) > 0) {
                segment = "High Value";
            } else if (totalIncome.compareTo(new BigDecimal("8000")) > 0) {
                segment = "Medium Value";
            } else {
                segment = "Low Value";
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", customer.getName());
            row.put("type", customer.getType());
            row.put("credit_score", customer.getCreditScore());
            row.put("account_balance", customer.getAccountBalance());
            row.put("income_transactions", incomeTx);
            row.put("expense_transactions", expenseTx);
            row.put("total_income", totalIncome);
            row.put("total_expense", totalExpense);
            row.put("value_segment", segment);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("total_income")).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<CustomerRecord> fetchCustomers() {
        String sql = "SELECT id, name, type, credit_score, account_balance FROM customer";
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
            record.setAccountBalance(rs.getBigDecimal("account_balance"));
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
