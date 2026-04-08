package com.legacy.report.service;

import com.legacy.report.dao.ReportDao;
import com.legacy.report.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportService reportService;

    private Report report;

    @BeforeEach
    void setUp() {
        report = new Report();
        report.setId(1L);
        report.setName("Test");
        report.setSql("SELECT 1");
    }

    @Test
    void getAllReportsDelegatesToDao() {
        when(reportDao.findAll()).thenReturn(List.of(report));

        List<Report> results = reportService.getAllReports();

        assertThat(results).containsExactly(report);
    }

    @Test
    void getReportByIdDelegatesToDao() {
        when(reportDao.findById(1L)).thenReturn(report);

        Report result = reportService.getReportById(1L);

        assertThat(result).isSameAs(report);
    }

    @Test
    void runReportDelegatesToDao() {
        when(reportDao.executeSql("SELECT 1")).thenReturn(List.of(Map.of("value", 1)));

        List<Map<String, Object>> data = reportService.runReport("SELECT 1");

        assertThat(data).hasSize(1);
    }

    @Test
    void createReportValidatesName() {
        Report invalid = new Report();
        invalid.setSql("SELECT 1");
        assertThrows(RuntimeException.class, () -> reportService.createReport(invalid));
    }

    @Test
    void createReportValidatesSql() {
        Report invalid = new Report();
        invalid.setName("r");
        assertThrows(RuntimeException.class, () -> reportService.createReport(invalid));
    }

    @Test
    void createReportPersistsWhenFieldsPresent() {
        reportService.createReport(report);

        verify(reportDao).save(report);
    }

    @Test
    void generateReportReturnsDataFromDao() {
        when(reportDao.findById(1L)).thenReturn(report);
        when(reportDao.executeSql("SELECT 1")).thenReturn(List.of(Map.of("value", 1)));

        Map<String, Object> result = reportService.generateReport(1L, null);

        assertThat(result)
                .containsEntry("reportName", "Test")
                .containsEntry("count", 1);
    }

    @Test
    void generateReportAppendsParamsWhenPresent() {
        when(reportDao.findById(1L)).thenReturn(report);
        when(reportDao.executeSql("SELECT 1 WHERE status='READY'"))
                .thenReturn(List.of(Map.of("value", 1)));

        Map<String, Object> result = reportService.generateReport(1L, "status='READY'");

        assertThat(result)
                .containsEntry("reportName", "Test")
                .containsEntry("count", 1);
    }

    @Test
    void generateReportIgnoresEmptyParams() {
        when(reportDao.findById(1L)).thenReturn(report);
        when(reportDao.executeSql("SELECT 1")).thenReturn(List.of(Map.of("value", 1)));

        Map<String, Object> result = reportService.generateReport(1L, "");

        assertThat(result)
                .containsEntry("reportName", "Test")
                .containsEntry("count", 1);
    }

    @Test
    void generateReportThrowsWhenReportMissing() {
        when(reportDao.findById(10L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> reportService.generateReport(10L, null));
    }
}
