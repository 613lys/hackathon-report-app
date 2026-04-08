package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.EmployeePerformanceMetricsCalculator;
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
@Import(EmployeePerformanceMetricsCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class EmployeePerformanceMetricsCalculatorTest {

    @Autowired
    EmployeePerformanceMetricsCalculator calculator;

    @Test
    void shouldClassifyEmployeesBySalaryTier() {
        List<Map<String, Object>> result = calculator.calculate(9L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            String tier = (String) row.get("salary_tier");
            assertThat(tier).isIn("High", "Medium", "Standard");
        });
    }

    @Test
    void shouldContainRequiredFields() {
        List<Map<String, Object>> result = calculator.calculate(9L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            assertThat(row).containsKey("department");
            assertThat(row).containsKey("employee_name");
            assertThat(row).containsKey("position");
            assertThat(row).containsKey("salary");
            assertThat(row).containsKey("budget_percentage");
            assertThat(row).containsKey("salary_tier");
        });
    }

    @Test
    void shouldClassifyHighSalaryEmployeeCorrectly() {
        List<Map<String, Object>> result = calculator.calculate(9L, null);

        result.stream()
                .filter(row -> "Bob Martin".equals(row.get("employee_name")))
                .findFirst()
                .ifPresent(row -> {
                    assertThat(row.get("salary_tier")).isEqualTo("High");
                    assertThat(row.get("position")).isEqualTo("Senior Engineer");
                });
    }
}
