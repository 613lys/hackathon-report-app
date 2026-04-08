package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.ProductProfitabilityReportCalculator;
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
@Import(ProductProfitabilityReportCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class ProductProfitabilityReportCalculatorTest {

    @Autowired
    ProductProfitabilityReportCalculator calculator;

    @Test
    void shouldReturnOnlyProductsWithSales() {
        List<Map<String, Object>> result = calculator.calculate(5L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            long sold = (long) row.get("total_sold");
            assertThat(sold).isGreaterThan(0);
        });
    }

    @Test
    void shouldReturnProductsOrderedByTotalProfitDesc() {
        List<Map<String, Object>> result = calculator.calculate(5L, null);

        assertThat(result).isNotEmpty();
        BigDecimal firstProfit = (BigDecimal) result.get(0).get("total_profit");
        for (int i = 1; i < result.size(); i++) {
            BigDecimal nextProfit = (BigDecimal) result.get(i).get("total_profit");
            assertThat(firstProfit).isGreaterThanOrEqualTo(nextProfit);
            firstProfit = nextProfit;
        }
    }

    @Test
    void shouldCalculateProfitCorrectly() {
        List<Map<String, Object>> result = calculator.calculate(5L, null);

        result.forEach(row -> {
            BigDecimal revenue = (BigDecimal) row.get("total_revenue");
            BigDecimal cost = (BigDecimal) row.get("total_cost");
            BigDecimal profit = (BigDecimal) row.get("total_profit");
            assertThat(profit).isEqualByComparingTo(revenue.subtract(cost));
        });
    }
}
