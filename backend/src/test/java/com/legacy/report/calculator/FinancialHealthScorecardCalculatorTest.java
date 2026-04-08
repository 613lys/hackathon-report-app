package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.FinancialHealthScorecardCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(FinancialHealthScorecardCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class FinancialHealthScorecardCalculatorTest {

    @Autowired
    FinancialHealthScorecardCalculator calculator;

    @Test
    void shouldReturnFiveMetrics() {
        List<Map<String, Object>> result = calculator.calculate(12L, null);

        assertThat(result).hasSize(5);
    }

    @Test
    void shouldContainAllRequiredMetrics() {
        List<Map<String, Object>> result = calculator.calculate(12L, null);

        Set<String> metrics = result.stream()
                .map(row -> (String) row.get("metric"))
                .collect(Collectors.toSet());

        assertThat(metrics).containsExactlyInAnyOrder(
                "Total Revenue",
                "Total Expenses",
                "Net Profit",
                "Active Customers",
                "Average Transaction Value"
        );
    }

    @Test
    void shouldNetProfitEqualRevenueMinusExpenses() {
        List<Map<String, Object>> result = calculator.calculate(12L, null);

        BigDecimal revenue = result.stream()
                .filter(r -> "Total Revenue".equals(r.get("metric")))
                .map(r -> (BigDecimal) r.get("value"))
                .findFirst().orElse(BigDecimal.ZERO);
        BigDecimal expenses = result.stream()
                .filter(r -> "Total Expenses".equals(r.get("metric")))
                .map(r -> (BigDecimal) r.get("value"))
                .findFirst().orElse(BigDecimal.ZERO);
        BigDecimal netProfit = result.stream()
                .filter(r -> "Net Profit".equals(r.get("metric")))
                .map(r -> (BigDecimal) r.get("value"))
                .findFirst().orElse(BigDecimal.ZERO);

        assertThat(netProfit).isEqualByComparingTo(revenue.subtract(expenses));
    }
}
