package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.InventoryVelocityAnalysisCalculator;
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
@Import(InventoryVelocityAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class InventoryVelocityAnalysisCalculatorTest {

    @Autowired
    InventoryVelocityAnalysisCalculator calculator;

    @Test
    void shouldReturnAllProductsIncludingUnsold() {
        List<Map<String, Object>> result = calculator.calculate(11L, null);

        assertThat(result).hasSize(5);
    }

    @Test
    void shouldReturnProductsOrderedByTotalSoldDesc() {
        List<Map<String, Object>> result = calculator.calculate(11L, null);

        long first = ((Number) result.get(0).get("total_sold")).longValue();
        for (int i = 1; i < result.size(); i++) {
            long next = ((Number) result.get(i).get("total_sold")).longValue();
            assertThat(first).isGreaterThanOrEqualTo(next);
            first = next;
        }
    }

    @Test
    void shouldCalculateUnitProfitCorrectly() {
        List<Map<String, Object>> result = calculator.calculate(11L, null);

        result.forEach(row -> {
            BigDecimal price = (BigDecimal) row.get("price");
            BigDecimal cost = (BigDecimal) row.get("cost");
            BigDecimal unitProfit = (BigDecimal) row.get("unit_profit");
            assertThat(unitProfit).isEqualByComparingTo(price.subtract(cost));
        });
    }
}
