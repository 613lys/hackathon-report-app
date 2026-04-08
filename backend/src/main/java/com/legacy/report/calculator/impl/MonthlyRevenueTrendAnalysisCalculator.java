package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.TransactionRecord;
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
public class MonthlyRevenueTrendAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 7L;

    private final JdbcTemplate jdbcTemplate;

    public MonthlyRevenueTrendAnalysisCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<TransactionRecord> transactions = fetchSuccessTransactions();

        Map<LocalDate, List<TransactionRecord>> txByDate = transactions.stream()
                .filter(tx -> tx.getTransactionDate() != null)
                .collect(Collectors.groupingBy(TransactionRecord::getTransactionDate));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<TransactionRecord>> entry : txByDate.entrySet()) {
            List<TransactionRecord> txList = entry.getValue();

            long count = txList.size();
            BigDecimal totalIncome = txList.stream()
                    .filter(tx -> "INCOME".equals(tx.getType()))
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExpense = txList.stream()
                    .filter(tx -> "EXPENSE".equals(tx.getType()))
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", entry.getKey().toString());
            row.put("transaction_count", count);
            row.put("total_income", totalIncome);
            row.put("total_expense", totalExpense);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (String) row.get("month")));
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<TransactionRecord> fetchSuccessTransactions() {
        String sql = "SELECT customer_id, amount, type, status, transaction_date FROM transaction WHERE status = 'SUCCESS'";
        return jdbcTemplate.query(sql, new TransactionRowMapper());
    }

    private static class TransactionRowMapper implements RowMapper<TransactionRecord> {
        @Override
        public TransactionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransactionRecord record = new TransactionRecord();
            record.setCustomerId(rs.getLong("customer_id"));
            record.setAmount(rs.getBigDecimal("amount"));
            record.setType(rs.getString("type"));
            record.setStatus(rs.getString("status"));
            record.setTransactionDate(rs.getDate("transaction_date") != null ?
                    rs.getDate("transaction_date").toLocalDate() : null);
            return record;
        }
    }
}
