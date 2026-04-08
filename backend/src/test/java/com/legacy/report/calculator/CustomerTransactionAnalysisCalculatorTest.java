package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.CustomerTransactionAnalysisCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(CustomerTransactionAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@org.springframework.test.context.jdbc.Sql({"classpath:schema.sql", "classpath:data.sql"})
class CustomerTransactionAnalysisCalculatorTest {

    @Autowired
    CustomerTransactionAnalysisCalculator calculator;

    @Test
    void shouldCalculateCustomerTransactionMetricsInDescendingOrder() {
        List<Map<String, Object>> result = calculator.calculate(1L, null);

        assertThat(result).hasSize(4);
        assertThat(result.get(0))
                .containsEntry("name", "Customer D")
                .containsEntry("total_amount", new BigDecimal("20000.00"));

        Map<String, Object> second = result.get(1);
        assertThat(second)
                .containsEntry("name", "Customer A")
                .containsEntry("tx_count", 3L);
        BigDecimal expectedAvg = new BigDecimal("17000")
                .divide(BigDecimal.valueOf(3), 12, RoundingMode.HALF_UP);
        assertThat((BigDecimal) second.get("avg_transaction"))
                .isEqualByComparingTo(expectedAvg);
    }
}
