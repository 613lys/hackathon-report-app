package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.TransactionRecord;
import com.legacy.report.dto.ReportContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FinancialHealthScorecardCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 12L;

    private final JdbcTemplate jdbcTemplate;

    public FinancialHealthScorecardCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<TransactionRecord> transactions = fetchSuccessTransactions();

        BigDecimal totalIncome = transactions.stream()
                .filter(tx -> "INCOME".equals(tx.getType()))
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = transactions.stream()
                .filter(tx -> "EXPENSE".equals(tx.getType()))
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netProfit = totalIncome.subtract(totalExpenses);

        long activeCustomers = transactions.stream()
                .map(TransactionRecord::getCustomerId)
                .distinct()
                .count();

        BigDecimal avgTx = transactions.isEmpty() ? BigDecimal.ZERO :
                transactions.stream()
                        .map(TransactionRecord::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(transactions.size()), 2, java.math.RoundingMode.HALF_UP);

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Total Revenue", totalIncome));
        rows.add(createRow("Total Expenses", totalExpenses));
        rows.add(createRow("Net Profit", netProfit));
        rows.add(createRow("Active Customers", BigDecimal.valueOf(activeCustomers)));
        rows.add(createRow("Average Transaction Value", avgTx));

        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private Map<String, Object> createRow(String metric, BigDecimal value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("metric", metric);
        row.put("value", value);
        return row;
    }

    private List<TransactionRecord> fetchSuccessTransactions() {
        String sql = "SELECT customer_id, amount, type, status FROM transaction WHERE status = 'SUCCESS'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TransactionRecord record = new TransactionRecord();
            record.setCustomerId(rs.getLong("customer_id"));
            record.setAmount(rs.getBigDecimal("amount"));
            record.setType(rs.getString("type"));
            record.setStatus(rs.getString("status"));
            return record;
        });
    }
}
