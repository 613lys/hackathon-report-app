package com.legacy.report.calculator;

import com.legacy.report.calculator.impl.OrderFulfillmentAnalysisCalculator;
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
@Import(OrderFulfillmentAnalysisCalculator.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@Sql({"classpath:schema.sql", "classpath:data.sql"})
class OrderFulfillmentAnalysisCalculatorTest {

    @Autowired
    OrderFulfillmentAnalysisCalculator calculator;

    @Test
    void shouldGroupOrdersByDate() {
        List<Map<String, Object>> result = calculator.calculate(8L, null);

        assertThat(result).isNotEmpty();
        result.forEach(row -> {
            assertThat(row).containsKey("order_month");
            assertThat(row).containsKey("total_orders");
            assertThat(row).containsKey("total_order_value");
            assertThat(row).containsKey("completed_orders");
            assertThat(row).containsKey("processing_orders");
            assertThat(row).containsKey("pending_orders");
        });
    }

    @Test
    void shouldReturnRowsOrderedByMonthAsc() {
        List<Map<String, Object>> result = calculator.calculate(8L, null);

        assertThat(result).isNotEmpty();
        String prev = (String) result.get(0).get("order_month");
        for (int i = 1; i < result.size(); i++) {
            String curr = (String) result.get(i).get("order_month");
            assertThat(prev.compareTo(curr)).isLessThanOrEqualTo(0);
            prev = curr;
        }
    }

    @Test
    void shouldCountOrdersByStatus() {
        List<Map<String, Object>> result = calculator.calculate(8L, null);

        long totalOrders = result.stream().mapToLong(r -> (long) r.get("total_orders")).sum();
        long totalCompleted = result.stream().mapToLong(r -> (long) r.get("completed_orders")).sum();
        long totalProcessing = result.stream().mapToLong(r -> (long) r.get("processing_orders")).sum();
        long totalPending = result.stream().mapToLong(r -> (long) r.get("pending_orders")).sum();

        assertThat(totalOrders).isEqualTo(totalCompleted + totalProcessing + totalPending);
        assertThat(totalOrders).isEqualTo(5L);
    }
}
