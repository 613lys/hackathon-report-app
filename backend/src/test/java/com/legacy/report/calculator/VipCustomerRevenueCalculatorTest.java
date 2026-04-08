package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.VipCustomerRevenueCalculator;
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
@Import(VipCustomerRevenueCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class VipCustomerRevenueCalculatorTest {

    @Autowired
    VipCustomerRevenueCalculator calculator;

    @Test
    void shouldCalculateVipCustomerRevenue() {
        List<Map<String, Object>> result = calculator.calculate(2L, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0))
                .containsEntry("name", "Customer C")
                .containsEntry("net_profit", new BigDecimal("15000.00"));

        Map<String, Object> second = result.get(1);
        assertThat(second)
                .containsEntry("name", "Customer A")
                .containsEntry("net_profit", new BigDecimal("3000.00"));
    }
}
