package com.legacy.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legacy.report.dto.ReportContext;
import com.legacy.report.exception.ReportExportException;
import com.legacy.report.model.Report;
import com.legacy.report.model.ReportRun;
import com.legacy.report.model.User;
import com.legacy.report.repository.ReportRunRepository;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportExcelExportService.class);

    @Value("${report.templates.base-path:classpath:report-templates}")
    private String templatesBasePath;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRunRepository reportRunRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ReportExecutionManager reportExecutionManager;

    public byte[] exportLatestByReportId(Long reportId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");

        Report report = reportService.getReportById(reportId);
        if (report == null) {
            throw new ReportExportException("报表不存在");
        }

        List<Map<String, Object>> data = executeThroughManager(reportId, currentUser);

        Map<String, Object> meta = new HashMap<>();
        meta.put("reportId", report.getId());
        meta.put("reportName", report.getName());

        byte[] bytes = renderWithTemplate(report.getId(), data, meta);

        auditService.recordEvent(
                null,
                report.getId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                "ExportedLatest",
                null
        );

        return bytes;
    }

    public byte[] exportByRunId(Long runId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));

        Report report = reportService.getReportById(run.getReportId());
        if (report == null) {
            throw new ReportExportException("报表不存在");
        }

        List<Map<String, Object>> data;
        if (run.getResultSnapshot() != null && !run.getResultSnapshot().isBlank()) {
            try {
                JavaType type = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, Map.class);
                data = objectMapper.readValue(run.getResultSnapshot(), type);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse result snapshot for run {}. Fallback to re-execute SQL.", runId, e);
                data = executeThroughManager(report.getId(), currentUser);
            }
        } else {
            data = executeThroughManager(report.getId(), currentUser);
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("reportId", run.getReportId());
        meta.put("reportName", run.getReportName());
        meta.put("runId", run.getId());
        meta.put("status", run.getStatus());

        byte[] bytes = renderWithTemplate(run.getReportId(), data, meta);

        auditService.recordEvent(
                run.getId(),
                run.getReportId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                "ExportedRun",
                null
        );

        return bytes;
    }

    private byte[] renderWithTemplate(Long reportId,
                                      List<Map<String, Object>> data,
                                      Map<String, Object> meta) {
        String base = templatesBasePath;
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        String location = base + "report-" + reportId + ".xlsx";

        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists() || !resource.isReadable()) {
            logger.error("Template not found or not readable for reportId={} at {}", reportId, location);
            throw new ReportExportException("报表模板文件不存在或不可读");
        }

        try (InputStream is = resource.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.putVar("data", data);
            context.putVar("meta", meta);
            JxlsHelper.getInstance().processTemplate(is, os, context);
            return os.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to render Excel template for reportId={}", reportId, e);
            throw new ReportExportException("生成报表导出文件失败", e);
        }
    }

    private List<Map<String, Object>> executeThroughManager(Long reportId, User currentUser) {
        ReportContext context = new ReportContext(currentUser);
        return reportExecutionManager.execute(reportId, context);
    }
}
