package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.CustomerRecord;
import com.legacy.report.calculator.model.MerchantRecord;
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
public class CustomerMerchantRevenueCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 10L;

    private final JdbcTemplate jdbcTemplate;

    public CustomerMerchantRevenueCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<CustomerRecord> customers = fetchCustomers();
        List<MerchantRecord> merchants = fetchMerchants();
        List<TransactionRecord> transactions = fetchSuccessTransactions();

        Map<Long, String> customerNames = customers.stream()
                .collect(Collectors.toMap(CustomerRecord::getId, CustomerRecord::getName));
        Map<Long, MerchantRecord> merchantMap = merchants.stream()
                .collect(Collectors.toMap(MerchantRecord::getId, m -> m));

        Map<Long, Map<Long, List<TransactionRecord>>> txByCustomerMerchant = transactions.stream()
                .filter(tx -> tx.getMerchantId() != null)
                .collect(Collectors.groupingBy(TransactionRecord::getCustomerId,
                        Collectors.groupingBy(TransactionRecord::getMerchantId)));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<TransactionRecord>>> custEntry : txByCustomerMerchant.entrySet()) {
            Long customerId = custEntry.getKey();
            String customerName = customerNames.getOrDefault(customerId, "Unknown");

            List<Map<String, Object>> customerRows = new ArrayList<>();
            for (Map.Entry<Long, List<TransactionRecord>> merchEntry : custEntry.getValue().entrySet()) {
                Long merchantId = merchEntry.getKey();
                MerchantRecord merchant = merchantMap.get(merchantId);
                if (merchant == null) {
                    continue;
                }

                List<TransactionRecord> txList = merchEntry.getValue();
                long count = txList.size();
                BigDecimal total = txList.stream()
                        .map(TransactionRecord::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avg = count == 0 ? BigDecimal.ZERO :
                        total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("customer_name", customerName);
                row.put("merchant_name", merchant.getName());
                row.put("merchant_category", merchant.getCategory());
                row.put("transaction_count", count);
                row.put("total_amount", total);
                row.put("avg_transaction", avg);
                customerRows.add(row);
            }

            customerRows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("total_amount")).reversed());
            int rank = 1;
            for (Map<String, Object> row : customerRows) {
                row.put("merchant_rank_by_customer", rank++);
                rows.add(row);
            }
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (String) row.get("customer_name"))
                .thenComparing((Map<String, Object> row) -> ((BigDecimal) row.get("total_amount")).doubleValue()).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<CustomerRecord> fetchCustomers() {
        String sql = "SELECT id, name FROM customer";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CustomerRecord record = new CustomerRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            return record;
        });
    }

    private List<MerchantRecord> fetchMerchants() {
        String sql = "SELECT id, name, category, commission_rate FROM merchant";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MerchantRecord record = new MerchantRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setCategory(rs.getString("category"));
            record.setCommissionRate(rs.getBigDecimal("commission_rate"));
            return record;
        });
    }

    private List<TransactionRecord> fetchSuccessTransactions() {
        String sql = "SELECT customer_id, amount, type, status, merchant_id FROM transaction WHERE status = 'SUCCESS'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TransactionRecord record = new TransactionRecord();
            record.setCustomerId(rs.getLong("customer_id"));
            record.setAmount(rs.getBigDecimal("amount"));
            record.setType(rs.getString("type"));
            record.setStatus(rs.getString("status"));
            record.setMerchantId(rs.getLong("merchant_id"));
            return record;
        });
    }
}
