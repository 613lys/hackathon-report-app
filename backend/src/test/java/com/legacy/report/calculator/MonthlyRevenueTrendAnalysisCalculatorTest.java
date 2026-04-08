package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.MonthlyRevenueTrendAnalysisCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(MonthlyRevenueTrendAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class MonthlyRevenueTrendAnalysisCalculatorTest {

    @Autowired
    MonthlyRevenueTrendAnalysisCalculator calculator;

    @Test
    void shouldGroupTransactionsByDate() {
        List<Map<String, Object>> result = calculator.calculate(7L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            assertThat(row).containsKey("month");
            assertThat(row).containsKey("transaction_count");
            assertThat(row).containsKey("total_income");
            assertThat(row).containsKey("total_expense");
        });
    }

    @Test
    void shouldReturnRowsOrderedByMonthAsc() {
        List<Map<String, Object>> result = calculator.calculate(7L, null);

        assertThat(result).isNotEmpty();
        String prev = (String) result.get(0).get("month");
        for (int i = 1; i < result.size(); i++) {
            String curr = (String) result.get(i).get("month");
            assertThat(prev.compareTo(curr)).isLessThanOrEqualTo(0);
            prev = curr;
        }
    }

    @Test
    void shouldOnlyIncludeSuccessTransactions() {
        List<Map<String, Object>> result = calculator.calculate(7L, null);

        BigDecimal totalIncome = result.stream()
                .map(r -> (BigDecimal) r.get("total_income"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalIncome).isGreaterThan(BigDecimal.ZERO);
    }
}
