package com.legacy.report.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legacy.report.calculator.impl.CustomerMerchantRevenueCalculator;
import com.legacy.report.calculator.impl.CustomerSegmentationAnalysisCalculator;
import com.legacy.report.calculator.impl.CustomerTransactionAnalysisCalculator;
import com.legacy.report.calculator.impl.DepartmentBudgetAnalysisCalculator;
import com.legacy.report.calculator.impl.EmployeePerformanceMetricsCalculator;
import com.legacy.report.calculator.impl.FinancialHealthScorecardCalculator;
import com.legacy.report.calculator.impl.InventoryVelocityAnalysisCalculator;
import com.legacy.report.calculator.impl.MerchantPerformanceAnalysisCalculator;
import com.legacy.report.calculator.impl.MonthlyRevenueTrendAnalysisCalculator;
import com.legacy.report.calculator.impl.OrderFulfillmentAnalysisCalculator;
import com.legacy.report.calculator.impl.ProductProfitabilityReportCalculator;
import com.legacy.report.calculator.impl.VipCustomerRevenueCalculator;
import com.legacy.report.dto.ReportContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({
        CustomerTransactionAnalysisCalculator.class,
        VipCustomerRevenueCalculator.class,
        MerchantPerformanceAnalysisCalculator.class,
        DepartmentBudgetAnalysisCalculator.class,
        ProductProfitabilityReportCalculator.class,
        CustomerSegmentationAnalysisCalculator.class,
        MonthlyRevenueTrendAnalysisCalculator.class,
        OrderFulfillmentAnalysisCalculator.class,
        EmployeePerformanceMetricsCalculator.class,
        CustomerMerchantRevenueCalculator.class,
        InventoryVelocityAnalysisCalculator.class,
        FinancialHealthScorecardCalculator.class
})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class ReportParityTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    List<ReportCalculator> calculators;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void report1ShouldMatchLegacySql() {
        assertParity(1L);
    }

    @Test
    void report2ShouldMatchLegacySql() {
        assertParity(2L);
    }

    @Test
    void report3ShouldMatchLegacySql() {
        assertParity(3L);
    }

    @Test
    void report4ShouldMatchLegacySql() {
        assertParity(4L);
    }

    @Test
    void report5ShouldMatchLegacySql() {
        assertParity(5L);
    }

    @Test
    void report6ShouldMatchLegacySql() {
        assertParity(6L);
    }

    @Test
    void report7ShouldMatchLegacySql() {
        assertParity(7L);
    }

    @Test
    void report8ShouldMatchLegacySql() {
        assertParity(8L);
    }

    @Test
    void report9ShouldMatchLegacySql() {
        assertParity(9L);
    }

    @Test
    void report10ShouldMatchLegacySql() {
        assertParity(10L);
    }

    @Test
    void report11ShouldMatchLegacySql() {
        assertParity(11L);
    }

    @Test
    void report12ShouldMatchLegacySql() {
        assertParity(12L);
    }

    private void assertParity(Long reportId) {
        ReportCalculator calculator = calculators.stream()
                .filter(c -> c.supports(reportId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No calculator found for report " + reportId));

        List<Map<String, Object>> javaResult = calculator.calculate(reportId, new ReportContext(null));
        String sql = jdbcTemplate.queryForObject("SELECT sql FROM report_config WHERE id = ?", String.class, reportId);
        List<Map<String, Object>> sqlResult = jdbcTemplate.queryForList(sql);

        writeSnapshotIfRequested(reportId, sqlResult);

        List<String> normalizedJava = normalize(javaResult);
        List<String> normalizedSql = normalize(sqlResult);

        assertThat(normalizedJava)
                .as("Report %s Java vs SQL parity", reportId)
                .containsExactlyElementsOf(normalizedSql);
    }

    private List<String> normalize(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> {
                    TreeMap<String, String> sorted = new TreeMap<>();
                    row.forEach((k, v) -> sorted.put(k.toLowerCase(), stringify(v)));
                    return sorted.entrySet().stream()
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .collect(Collectors.joining("|"));
                })
                .sorted()
                .collect(Collectors.toList());
    }

    private String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number) {
            return String.valueOf(((Number) value).doubleValue());
        }
        return value.toString();
    }

    private void writeSnapshotIfRequested(Long reportId, List<Map<String, Object>> sqlResult) {
        if (!Boolean.getBoolean("generateSnapshots")) {
            return;
        }
        try {
            Path dir = Paths.get("src", "test", "resources", "snapshots");
            Files.createDirectories(dir);
            Path file = dir.resolve("report-" + reportId + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), sqlResult);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write snapshot for report " + reportId, e);
        }
    }
}
