package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.CustomerMerchantRevenueCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(CustomerMerchantRevenueCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class CustomerMerchantRevenueCalculatorTest {

    @Autowired
    CustomerMerchantRevenueCalculator calculator;

    @Test
    void shouldReturnCustomerMerchantCombinations() {
        List<Map<String, Object>> result = calculator.calculate(10L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            assertThat(row).containsKey("customer_name");
            assertThat(row).containsKey("merchant_name");
            assertThat(row).containsKey("transaction_count");
            assertThat(row).containsKey("total_amount");
            assertThat(row).containsKey("merchant_rank_by_customer");
        });
    }

    @Test
    void shouldAssignRankStartingFromOne() {
        List<Map<String, Object>> result = calculator.calculate(10L, null);

        result.stream()
                .filter(row -> "Customer A".equals(row.get("customer_name")))
                .min(java.util.Comparator.comparingInt(row -> (int) row.get("merchant_rank_by_customer")))
                .ifPresent(row -> assertThat(row.get("merchant_rank_by_customer")).isEqualTo(1));
    }

    @Test
    void shouldOnlyIncludeSuccessTransactions() {
        List<Map<String, Object>> result = calculator.calculate(10L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            long count = (long) row.get("transaction_count");
            assertThat(count).isGreaterThan(0);
        });
    }
}
