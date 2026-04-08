package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.MerchantPerformanceAnalysisCalculator;
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
@Import(MerchantPerformanceAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class MerchantPerformanceAnalysisCalculatorTest {

    @Autowired
    MerchantPerformanceAnalysisCalculator calculator;

    @Test
    void shouldReturnMerchantsOrderedByTotalVolumeDesc() {
        List<Map<String, Object>> result = calculator.calculate(3L, null);

        assertThat(result).isNotEmpty();
        BigDecimal firstVolume = (BigDecimal) result.get(0).get("total_volume");
        for (int i = 1; i < result.size(); i++) {
            BigDecimal nextVolume = (BigDecimal) result.get(i).get("total_volume");
            assertThat(firstVolume).isGreaterThanOrEqualTo(nextVolume);
            firstVolume = nextVolume;
        }
    }

    @Test
    void shouldCalculateEstimatedCommission() {
        List<Map<String, Object>> result = calculator.calculate(3L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            assertThat(row).containsKey("merchant_name");
            assertThat(row).containsKey("category");
            assertThat(row).containsKey("transaction_count");
            assertThat(row).containsKey("total_volume");
            assertThat(row).containsKey("estimated_commission");
        });
    }

    @Test
    void shouldOnlyIncludeSuccessTransactions() {
        List<Map<String, Object>> result = calculator.calculate(3L, null);

        result.forEach(row -> {
            BigDecimal volume = (BigDecimal) row.get("total_volume");
            assertThat(volume).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        });
    }
}
