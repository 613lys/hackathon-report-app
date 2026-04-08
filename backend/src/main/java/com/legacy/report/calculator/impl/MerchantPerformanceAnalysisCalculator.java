package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
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
public class MerchantPerformanceAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 3L;

    private final JdbcTemplate jdbcTemplate;

    public MerchantPerformanceAnalysisCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<MerchantRecord> merchants = fetchMerchants();
        List<TransactionRecord> transactions = fetchSuccessTransactionsWithMerchant();

        Map<Long, List<TransactionRecord>> txByMerchant = transactions.stream()
                .filter(tx -> tx.getMerchantId() != null)
                .collect(Collectors.groupingBy(TransactionRecord::getMerchantId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (MerchantRecord merchant : merchants) {
            List<TransactionRecord> txList = txByMerchant.getOrDefault(merchant.getId(), List.of());

            long count = txList.size();
            BigDecimal totalVolume = txList.stream()
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgAmount = count == 0 ? BigDecimal.ZERO :
                    totalVolume.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            BigDecimal commission = totalVolume.multiply(merchant.getCommissionRate());

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("merchant_name", merchant.getName());
            row.put("category", merchant.getCategory());
            row.put("transaction_count", count);
            row.put("total_volume", totalVolume.setScale(2, RoundingMode.HALF_UP));
            row.put("avg_transaction_amount", avgAmount);
            row.put("estimated_commission", commission.setScale(2, RoundingMode.HALF_UP));
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("total_volume")).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<MerchantRecord> fetchMerchants() {
        String sql = "SELECT id, name, category, commission_rate FROM merchant";
        return jdbcTemplate.query(sql, new MerchantRowMapper());
    }

    private List<TransactionRecord> fetchSuccessTransactionsWithMerchant() {
        String sql = "SELECT customer_id, amount, type, status, merchant_id FROM transaction WHERE status = 'SUCCESS'";
        return jdbcTemplate.query(sql, new TransactionRowMapper());
    }

    private static class MerchantRowMapper implements RowMapper<MerchantRecord> {
        @Override
        public MerchantRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            MerchantRecord record = new MerchantRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setCategory(rs.getString("category"));
            record.setCommissionRate(rs.getBigDecimal("commission_rate"));
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
            record.setMerchantId(rs.getLong("merchant_id"));
            return record;
        }
    }
}
