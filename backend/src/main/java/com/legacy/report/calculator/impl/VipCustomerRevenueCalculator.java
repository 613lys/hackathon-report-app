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
public class VipCustomerRevenueCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 2L;

    private final JdbcTemplate jdbcTemplate;

    public VipCustomerRevenueCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<CustomerRecord> vipCustomers = fetchVipCustomers();
        List<TransactionRecord> transactions = fetchAllTransactions();

        Map<Long, List<TransactionRecord>> txByCustomer = transactions.stream()
                .collect(Collectors.groupingBy(TransactionRecord::getCustomerId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (CustomerRecord customer : vipCustomers) {
            List<TransactionRecord> txList = txByCustomer.getOrDefault(customer.getId(), List.of());

            BigDecimal income = sumByType(txList, "INCOME");
            BigDecimal expense = sumByType(txList, "EXPENSE");
            BigDecimal netProfit = income.subtract(expense);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", customer.getName());
            row.put("email", customer.getEmail());
            row.put("account_balance", customer.getAccountBalance());
            row.put("income", income.setScale(2, RoundingMode.HALF_UP));
            row.put("expense", expense.setScale(2, RoundingMode.HALF_UP));
            row.put("net_profit", netProfit.setScale(2, RoundingMode.HALF_UP));
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("net_profit")).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<CustomerRecord> fetchVipCustomers() {
        String sql = "SELECT id, name, type, email, account_balance FROM customer WHERE type = 'VIP'";
        return jdbcTemplate.query(sql, new VipCustomerRowMapper());
    }

    private List<TransactionRecord> fetchAllTransactions() {
        String sql = "SELECT customer_id, amount, type, status FROM transaction";
        return jdbcTemplate.query(sql, new TransactionRowMapper());
    }

    private BigDecimal sumByType(List<TransactionRecord> transactions, String type) {
        return transactions.stream()
                .filter(tx -> type.equalsIgnoreCase(tx.getType()))
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static class VipCustomerRowMapper implements RowMapper<CustomerRecord> {
        @Override
        public CustomerRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            CustomerRecord record = new CustomerRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setType(rs.getString("type"));
            record.setEmail(rs.getString("email"));
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
