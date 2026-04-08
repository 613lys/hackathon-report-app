package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.DepartmentBudgetAnalysisCalculator;
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
@Import(DepartmentBudgetAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class DepartmentBudgetAnalysisCalculatorTest {

    @Autowired
    DepartmentBudgetAnalysisCalculator calculator;

    @Test
    void shouldReturnDepartmentsOrderedByBudgetUtilizationDesc() {
        List<Map<String, Object>> result = calculator.calculate(4L, null);

        assertThat(result).isNotEmpty();
        BigDecimal firstUtil = (BigDecimal) result.get(0).get("budget_utilization_percent");
        for (int i = 1; i < result.size(); i++) {
            BigDecimal nextUtil = (BigDecimal) result.get(i).get("budget_utilization_percent");
            assertThat(firstUtil).isGreaterThanOrEqualTo(nextUtil);
            firstUtil = nextUtil;
        }
    }

    @Test
    void shouldCalculateBudgetVariance() {
        List<Map<String, Object>> result = calculator.calculate(4L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            assertThat(row).containsKey("department");
            assertThat(row).containsKey("manager");
            assertThat(row).containsKey("budget");
            assertThat(row).containsKey("employee_count");
            assertThat(row).containsKey("total_salary_cost");
            assertThat(row).containsKey("budget_variance");
            assertThat(row).containsKey("budget_utilization_percent");

            BigDecimal budget = (BigDecimal) row.get("budget");
            BigDecimal salaryCost = (BigDecimal) row.get("total_salary_cost");
            BigDecimal variance = (BigDecimal) row.get("budget_variance");
            assertThat(variance).isEqualByComparingTo(budget.subtract(salaryCost));
        });
    }

    @Test
    void shouldCountActiveEmployeesPerDepartment() {
        List<Map<String, Object>> result = calculator.calculate(4L, null);

        assertThat(result).hasSize(5);
        result.forEach(row -> {
            long count = (long) row.get("employee_count");
            assertThat(count).isEqualTo(1L);
        });
    }
}
