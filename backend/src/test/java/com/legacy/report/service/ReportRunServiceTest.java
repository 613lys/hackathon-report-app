package com.legacy.report.service;

import com.legacy.report.model.Report;
import com.legacy.report.model.ReportRun;
import com.legacy.report.model.User;
import com.legacy.report.repository.ReportAuditEventRepository;
import com.legacy.report.repository.ReportRunRepository;
import com.legacy.report.test.fixtures.TestDataFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportRunServiceTest {

    @Mock
    private ReportService reportService;

    @Mock
    private ReportRunRepository reportRunRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ReportAuditEventRepository reportAuditEventRepository;

    @InjectMocks
    private ReportRunService reportRunService;

    @BeforeEach
    void setUp() {
        reportRunService.setMeterRegistry(new SimpleMeterRegistry());
        ReflectionTestUtils.setField(reportRunService, "objectMapper", new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @Test
    void setMeterRegistryHandlesNull() {
        reportRunService.setMeterRegistry(null);
        assertThat(ReflectionTestUtils.getField(reportRunService, "generatedCounter")).isNotNull();
    }

    @Test
    void executeReportWithRunCreatesRunAndAudits() {
        User maker = TestDataFactory.makerUser();
        Report report = new Report();
        report.setId(1L);
        report.setName("Baseline");
        report.setSql("SELECT 1");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportService.getReportById(1L)).thenReturn(report);
        when(reportService.runReport("SELECT 1")).thenReturn(List.of(Map.of("value", 1)));
        when(reportRunRepository.save(any(ReportRun.class))).thenAnswer(invocation -> {
            ReportRun run = invocation.getArgument(0);
            run.setId(99L);
            return run;
        });

        List<Map<String, Object>> result = reportRunService.executeReportWithRun(1L);

        assertThat(result).hasSize(1);
        verify(auditService).recordEvent(99L, 1L, maker.getUsername(), maker.getRole(), "Generated", null);
    }

    @Test
    void executeReportWithRunSkipsMetricsWhenCountersNull() {
        ReflectionTestUtils.setField(reportRunService, "generatedCounter", null);
        User maker = TestDataFactory.makerUser();
        Report report = new Report();
        report.setId(2L);
        report.setName("NoMetrics");
        report.setSql("SELECT 1");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportService.getReportById(2L)).thenReturn(report);
        when(reportService.runReport("SELECT 1")).thenReturn(List.of(Map.of("value", 1)));
        when(reportRunRepository.save(any(ReportRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Map<String, Object>> result = reportRunService.executeReportWithRun(2L);
        assertThat(result).hasSize(1);
    }

    @Test
    void executeReportWithRunThrowsWhenReportMissing() {
        User maker = TestDataFactory.makerUser();
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportService.getReportById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> reportRunService.executeReportWithRun(1L));
    }

    @Test
    void executeReportWithRunHandlesSnapshotFailureGracefully() throws Exception {
        User maker = TestDataFactory.makerUser();
        Report report = new Report();
        report.setId(1L);
        report.setName("Baseline");
        report.setSql("SELECT 1");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportService.getReportById(1L)).thenReturn(report);
        List<Map<String, Object>> payload = List.of(Map.of("value", 1));
        when(reportService.runReport("SELECT 1")).thenReturn(payload);

        com.fasterxml.jackson.databind.ObjectMapper failingMapper = org.mockito.Mockito.mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        org.mockito.Mockito.when(failingMapper.writeValueAsString(payload))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});
        Object originalMapper = ReflectionTestUtils.getField(reportRunService, "objectMapper");
        ReflectionTestUtils.setField(reportRunService, "objectMapper", failingMapper);
        when(reportRunRepository.save(any(ReportRun.class))).thenAnswer(invocation -> {
            ReportRun run = invocation.getArgument(0);
            run.setId(100L);
            return run;
        });

        try {
            reportRunService.executeReportWithRun(1L);
            ArgumentCaptor<ReportRun> runCaptor = ArgumentCaptor.forClass(ReportRun.class);
            verify(reportRunRepository).save(runCaptor.capture());
            assertThat(runCaptor.getValue().getResultSnapshot()).isNull();
        } finally {
            ReflectionTestUtils.setField(reportRunService, "objectMapper", originalMapper);
        }
    }

    @Test
    void submitRun_shouldUpdateStatusAndRecordAudit() {
        User maker = TestDataFactory.makerUser();
        ReportRun generated = TestDataFactory.generatedRun();

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportRunRepository.findById(generated.getId())).thenReturn(Optional.of(generated));
        when(reportRunRepository.save(any(ReportRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportRun result = reportRunService.submitRun(generated.getId());

        assertEquals("Submitted", result.getStatus());
        assertNotNull(result.getSubmittedAt());

        ArgumentCaptor<ReportRun> runCaptor = ArgumentCaptor.forClass(ReportRun.class);
        verify(reportRunRepository).save(runCaptor.capture());
        assertThat(runCaptor.getValue().getSubmittedAt()).isNotNull();

        verify(auditService).recordEvent(
                result.getId(),
                result.getReportId(),
                maker.getUsername(),
                maker.getRole(),
                "Submitted",
                null
        );
    }

    @Test
    void submitRun_shouldRejectWhenMakerMismatch() {
        User maker = TestDataFactory.makerUser();
        ReportRun generated = TestDataFactory.generatedRun();
        generated.setMakerUsername("other-maker");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportRunRepository.findById(generated.getId())).thenReturn(Optional.of(generated));

        Long runId = generated.getId();
        assertThrows(RuntimeException.class, () -> reportRunService.submitRun(runId));
    }

    @Test
    void submitRun_shouldRejectWhenStatusNotGenerated() {
        User maker = TestDataFactory.makerUser();
        ReportRun generated = TestDataFactory.generatedRun();
        generated.setStatus("Submitted");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportRunRepository.findById(generated.getId())).thenReturn(Optional.of(generated));

        assertThrows(RuntimeException.class, () -> reportRunService.submitRun(generated.getId()));
    }

    @Test
    void submitRunSkipsCounterWhenNull() {
        ReflectionTestUtils.setField(reportRunService, "submittedCounter", null);
        submitRun_shouldUpdateStatusAndRecordAudit();
    }

    @Test
    void decideRun_shouldRequireCommentWhenRejecting() {
        User checker = TestDataFactory.checkerUser();
        ReportRun submitted = TestDataFactory.submittedRun();

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(checker);
        doNothing().when(currentUserService).requireRole(checker, "CHECKER");
        when(reportRunRepository.findById(submitted.getId())).thenReturn(Optional.of(submitted));

        Long submittedId = submitted.getId();
        assertThrows(RuntimeException.class, () -> reportRunService.decideRun(submittedId, false, "   "));
    }

    @Test
    void decideRun_shouldApproveAndRecordAudit() {
        User checker = TestDataFactory.checkerUser();
        ReportRun submitted = TestDataFactory.submittedRun();
        submitted.setStatus("Submitted");
        submitted.setSubmittedAt(LocalDateTime.now().minusMinutes(5));

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(checker);
        doNothing().when(currentUserService).requireRole(checker, "CHECKER");
        when(reportRunRepository.findById(submitted.getId())).thenReturn(Optional.of(submitted));
        when(reportRunRepository.save(any(ReportRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportRun approved = reportRunService.decideRun(submitted.getId(), true, "looks good");

        assertEquals("Approved", approved.getStatus());
        assertEquals(checker.getUsername(), approved.getCheckerUsername());

        verify(auditService).recordEvent(
                approved.getId(),
                approved.getReportId(),
                checker.getUsername(),
                checker.getRole(),
                "Approved",
                "looks good"
        );
    }

    @Test
    void getLatestRunForCurrentMakerReturnsNewest() {
        User maker = TestDataFactory.makerUser();
        ReportRun latest = TestDataFactory.generatedRun();
        ReportRun older = TestDataFactory.generatedRun();
        older.setGeneratedAt(LocalDateTime.now().minusDays(1));

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportRunRepository.findByMakerUsernameAndReportIdOrderByGeneratedAtDesc(maker.getUsername(), 1L))
                .thenReturn(List.of(latest, older));

        ReportRun result = reportRunService.getLatestRunForCurrentMaker(1L);

        assertThat(result).isSameAs(latest);
    }

    @Test
    void getLatestRunForCurrentMakerThrowsWhenEmpty() {
        User maker = TestDataFactory.makerUser();
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        when(reportRunRepository.findByMakerUsernameAndReportIdOrderByGeneratedAtDesc(maker.getUsername(), 2L))
                .thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> reportRunService.getLatestRunForCurrentMaker(2L));
    }

    @Test
    void makerAndCheckerListingDelegatesToRepositories() {
        User maker = TestDataFactory.makerUser();
        User checker = TestDataFactory.checkerUser();

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(maker);
        doNothing().when(currentUserService).requireRole(maker, "MAKER");
        reportRunService.getRunsForCurrentMaker();
        verify(reportRunRepository).findByMakerUsernameOrderByGeneratedAtDesc(maker.getUsername());

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(checker);
        doNothing().when(currentUserService).requireRole(checker, "CHECKER");
        reportRunService.getSubmittedRunsForChecker();
        verify(reportRunRepository).findByStatusOrderBySubmittedAtAsc("Submitted");

        reportRunService.getHistoryRunsForCurrentChecker();
        verify(reportRunRepository).findByCheckerUsernameOrderByDecidedAtDesc(checker.getUsername());
    }

    @Test
    void getAuditEventsForRunReturnsRepositoryResult() {
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(TestDataFactory.makerUser());
        reportRunService.getAuditEventsForRun(10L);
        verify(reportAuditEventRepository).findByReportRunIdOrderByEventTimeAsc(10L);
    }
}
