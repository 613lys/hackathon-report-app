package com.legacy.report.stress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legacy.report.model.ReportRun;
import com.legacy.report.repository.ReportAuditEventRepository;
import com.legacy.report.repository.ReportRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LongAdder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReportWorkflowStressTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper;
    private final ReportRunRepository reportRunRepository;
    private final ReportAuditEventRepository reportAuditEventRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl;
    private String makerToken;
    private String checkerToken;

    @Autowired
    ReportWorkflowStressTest(ObjectMapper objectMapper,
                             ReportRunRepository reportRunRepository,
                             ReportAuditEventRepository reportAuditEventRepository) {
        this.objectMapper = objectMapper;
        this.reportRunRepository = reportRunRepository;
        this.reportAuditEventRepository = reportAuditEventRepository;
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        reportAuditEventRepository.deleteAll();
        reportRunRepository.deleteAll();
        makerToken = login("maker1", "123456");
        checkerToken = login("checker1", "123456");
    }

    @Test
    void makerCheckerWorkflowUnderLoad() throws InterruptedException {
        int virtualUsers = Integer.parseInt(System.getProperty("stress.virtualUsers", "20"));
        int iterations = Integer.parseInt(System.getProperty("stress.iterations", "2"));
        int maxLatencyMs = Integer.parseInt(System.getProperty("stress.maxLatencyMs", "3000"));

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(virtualUsers, 8));
        CountDownLatch latch = new CountDownLatch(virtualUsers);
        AtomicLong maxLatencyObserved = new AtomicLong(0);
        LongAdder failures = new LongAdder();

        for (int i = 0; i < virtualUsers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        long duration = runWorkflowScenario(1L);
                        maxLatencyObserved.updateAndGet(current -> Math.max(current, duration));
                    }
                } catch (Exception ex) {
                    failures.increment();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(2, TimeUnit.MINUTES);
        executor.shutdownNow();

        assertThat(completed).as("并发任务需在超时内完成").isTrue();
        assertThat(failures.sum()).as("压测过程中不能有失败").isZero();
        assertThat(maxLatencyObserved.get()).as("最大延迟须低于阈值").isLessThanOrEqualTo(maxLatencyMs);
    }

    private long runWorkflowScenario(Long reportId) {
        long start = System.currentTimeMillis();
        executeReport(reportId);
        Long runId = fetchLatestRun(reportId);
        submitRun(runId);
        approveRun(runId);
        return System.currentTimeMillis() - start;
    }

    private void executeReport(Long reportId) {
        ResponseEntity<String> response = exchange(HttpMethod.POST, "/api/reports/" + reportId + "/execute", null, makerToken, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private Long fetchLatestRun(Long reportId) {
        ResponseEntity<ReportRun> response = exchange(HttpMethod.GET,
                "/api/report-runs/my-latest?reportId=" + reportId,
                null,
                makerToken,
                ReportRun.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getId();
    }

    private void submitRun(Long runId) {
        ResponseEntity<Void> response = exchange(HttpMethod.POST,
                "/api/report-runs/" + runId + "/submit",
                Map.of(),
                makerToken,
                Void.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private void approveRun(Long runId) {
        ResponseEntity<Void> response = exchange(HttpMethod.POST,
                "/api/report-runs/" + runId + "/decision",
                Map.of("decision", "APPROVED", "comment", "stress" + runId),
                checkerToken,
                Void.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private String login(String username, String password) {
        ResponseEntity<String> response = exchange(HttpMethod.POST, "/api/auth/login", Map.of(
                "username", username,
                "password", password
        ), null, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            return node.path("token").asText();
        } catch (Exception e) {
            throw new IllegalStateException("无法解析登录响应", e);
        }
    }

    private <T> ResponseEntity<T> exchange(HttpMethod method, String path, Object body, String token, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl + path, method, entity, responseType);
    }
}
