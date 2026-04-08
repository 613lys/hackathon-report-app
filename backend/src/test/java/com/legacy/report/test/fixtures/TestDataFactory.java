package com.legacy.report.test.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.legacy.report.model.Report;
import com.legacy.report.model.ReportRun;
import com.legacy.report.model.User;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供简单的测试夹具，避免在各个测试类中重复构造 Maker/Checker、报表与报表运行对象。
 */
public final class TestDataFactory {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Map<String, User> USERS = new HashMap<>();
    private static final Map<String, Report> REPORTS = new HashMap<>();
    private static final Map<String, ReportRun> REPORT_RUNS = new HashMap<>();

    static {
        try (InputStream inputStream = new ClassPathResource("fixtures/test-data.json").getInputStream()) {
            JsonNode root = MAPPER.readTree(inputStream);
            root.path("users").fields().forEachRemaining(entry -> USERS.put(entry.getKey(), cloneUser(readUser(entry.getValue()))));
            root.path("reports").fields().forEachRemaining(entry -> REPORTS.put(entry.getKey(), cloneReport(readReport(entry.getValue()))));
            root.path("reportRuns").fields().forEachRemaining(entry -> REPORT_RUNS.put(entry.getKey(), cloneReportRun(readReportRun(entry.getValue()))));
        } catch (IOException e) {
            throw new IllegalStateException("加载测试夹具失败", e);
        }
    }

    private TestDataFactory() {
    }

    public static User makerUser() {
        return cloneUser(USERS.get("maker"));
    }

    public static User checkerUser() {
        return cloneUser(USERS.get("checker"));
    }

    public static Report baselineReport() {
        return cloneReport(REPORTS.get("baseline"));
    }

    public static ReportRun generatedRun() {
        return cloneReportRun(REPORT_RUNS.get("generated"));
    }

    public static ReportRun submittedRun() {
        return cloneReportRun(REPORT_RUNS.get("submitted"));
    }

    private static User readUser(JsonNode node) {
        try {
            return MAPPER.treeToValue(node, User.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Report readReport(JsonNode node) {
        try {
            return MAPPER.treeToValue(node, Report.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ReportRun readReportRun(JsonNode node) {
        try {
            return MAPPER.treeToValue(node, ReportRun.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static User cloneUser(User template) {
        if (template == null) {
            return null;
        }
        User clone = new User();
        clone.setId(template.getId());
        clone.setUsername(template.getUsername());
        clone.setPassword(template.getPassword());
        clone.setRole(template.getRole());
        return clone;
    }

    private static Report cloneReport(Report template) {
        if (template == null) {
            return null;
        }
        Report clone = new Report();
        clone.setId(template.getId());
        clone.setName(template.getName());
        clone.setSql(template.getSql());
        clone.setDescription(template.getDescription());
        return clone;
    }

    private static ReportRun cloneReportRun(ReportRun template) {
        if (template == null) {
            return null;
        }
        ReportRun clone = new ReportRun();
        clone.setId(template.getId());
        clone.setReportId(template.getReportId());
        clone.setReportName(template.getReportName());
        clone.setStatus(template.getStatus());
        clone.setMakerUsername(template.getMakerUsername());
        clone.setCheckerUsername(template.getCheckerUsername());
        clone.setGeneratedAt(template.getGeneratedAt());
        clone.setSubmittedAt(template.getSubmittedAt());
        clone.setDecidedAt(template.getDecidedAt());
        clone.setParametersJson(template.getParametersJson());
        clone.setResultSnapshot(template.getResultSnapshot());
        return clone;
    }
}
