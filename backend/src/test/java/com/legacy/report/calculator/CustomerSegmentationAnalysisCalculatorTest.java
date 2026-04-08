package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.CustomerSegmentationAnalysisCalculator;
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

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(CustomerSegmentationAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class CustomerSegmentationAnalysisCalculatorTest {

    @Autowired
    CustomerSegmentationAnalysisCalculator calculator;

    @Test
    void shouldClassifyCustomersByValueSegment() {
        List<Map<String, Object>> result = calculator.calculate(6L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            String segment = (String) row.get("value_segment");
            assertThat(segment).isIn("High Value", "Medium Value", "Low Value");
        });
    }

    @Test
    void shouldReturnCustomersOrderedByTotalIncomeDesc() {
        List<Map<String, Object>> result = calculator.calculate(6L, null);

        assertThat(result).isNotEmpty();
        BigDecimal prev = (BigDecimal) result.get(0).get("total_income");
        for (int i = 1; i < result.size(); i++) {
            BigDecimal curr = (BigDecimal) result.get(i).get("total_income");
            assertThat(prev).isGreaterThanOrEqualTo(curr);
            prev = curr;
        }
    }

    @Test
    void shouldCustomerDWithHighestIncomeBeHighValue() {
        List<Map<String, Object>> result = calculator.calculate(6L, null);

        assertThat(result.get(0)).containsEntry("name", "Customer D");
        assertThat(result.get(0)).containsEntry("value_segment", "High Value");
    }
}
