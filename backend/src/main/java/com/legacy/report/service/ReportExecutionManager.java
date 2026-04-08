package com.legacy.report.service;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.config.ReportExecutionProperties;
import com.legacy.report.config.ReportExecutionProperties.ExecutionMode;
import com.legacy.report.dto.ReportContext;
import com.legacy.report.model.Report;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Central execution manager for all reports.
 * Routes execution between SQL (legacy) and Java calculators based on feature toggle.
 */
@Service
public class ReportExecutionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportExecutionManager.class);
    
    private final ReportExecutionProperties properties;
    private final LegacySqlExecutor legacySqlExecutor;
    private final ReportService reportService;
    private final List<ReportCalculator> calculators;
    private final ExecutorService dualModeExecutor;
    private final Counter dualMatchCounter;
    private final Counter dualMismatchCounter;
    
    @Autowired
    public ReportExecutionManager(
            ReportExecutionProperties properties,
            LegacySqlExecutor legacySqlExecutor,
            ReportService reportService,
            List<ReportCalculator> calculators) {
        this.properties = properties;
        this.legacySqlExecutor = legacySqlExecutor;
        this.reportService = reportService;
        this.calculators = calculators != null ? calculators : Collections.emptyList();
        this.dualModeExecutor = Executors.newFixedThreadPool(2);
        this.dualMatchCounter = Counter.builder("report_dual_mode_match_total")
                .description("Number of dual executions where Java and SQL results matched")
                .register(Metrics.globalRegistry);
        this.dualMismatchCounter = Counter.builder("report_dual_mode_mismatch_total")
                .description("Number of dual executions where Java and SQL results mismatched")
                .register(Metrics.globalRegistry);
        
        logger.info("Initialized ReportExecutionManager with {} calculators, default mode: {}", 
                this.calculators.size(), properties.getMode());
    }
    
    /**
     * Executes a report based on the configured execution mode.
     * 
     * @param reportId the report ID
     * @param ctx the execution context
     * @return the report data
     */
    public List<Map<String, Object>> execute(Long reportId, ReportContext ctx) {
        ExecutionMode mode = properties.getModeForReport(reportId);
        logger.debug("Executing report {} with mode {}", reportId, mode);
        
        switch (mode) {
            case SQL:
                return executeSql(reportId, ctx);
            case JAVA:
                return executeJava(reportId, ctx);
            case DUAL:
                return executeDual(reportId, ctx);
            default:
                throw new IllegalStateException("Unknown execution mode: " + mode);
        }
    }
    
    /**
     * Gets the execution mode for a report (for audit/logging purposes).
     * 
     * @param reportId the report ID
     * @return the execution mode
     */
    public ExecutionMode getExecutionMode(Long reportId) {
        return properties.getModeForReport(reportId);
    }
    
    private List<Map<String, Object>> executeSql(Long reportId, ReportContext ctx) {
        logger.info("Executing report {} via SQL (legacy)", reportId);
        Report report = reportService.getReportById(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found: " + reportId);
        }
        return legacySqlExecutor.execute(report.getSql(), reportId);
    }
    
    private List<Map<String, Object>> executeJava(Long reportId, ReportContext ctx) {
        logger.info("Executing report {} via Java calculator", reportId);
        ReportCalculator calculator = findCalculator(reportId);
        if (calculator == null) {
            throw new RuntimeException("No calculator registered for report: " + reportId);
        }
        return calculator.calculate(reportId, ctx);
    }
    
    private List<Map<String, Object>> executeDual(Long reportId, ReportContext ctx) {
        logger.info("Executing report {} in DUAL mode", reportId);
        
        // Execute Java as primary result
        List<Map<String, Object>> javaResult = executeJava(reportId, ctx);
        
        // Execute SQL asynchronously for comparison
        dualModeExecutor.submit(() -> {
            try {
                List<Map<String, Object>> sqlResult = executeSql(reportId, ctx);
                compareAndLog(reportId, javaResult, sqlResult);
            } catch (Exception e) {
                logger.error("DUAL mode SQL execution failed for report {}", reportId, e);
            }
        });
        
        return javaResult;
    }
    
    private ReportCalculator findCalculator(Long reportId) {
        for (ReportCalculator calculator : calculators) {
            if (calculator.supports(reportId)) {
                return calculator;
            }
        }
        return null;
    }
    
    private void compareAndLog(Long reportId, 
                              List<Map<String, Object>> javaResult, 
                              List<Map<String, Object>> sqlResult) {
        int javaSize = javaResult != null ? javaResult.size() : 0;
        int sqlSize = sqlResult != null ? sqlResult.size() : 0;
        
        boolean sizeMatch = javaSize == sqlSize;
        
        List<String> javaNormalized = normalize(javaResult);
        List<String> sqlNormalized = normalize(sqlResult);

        if (!sizeMatch || !javaNormalized.equals(sqlNormalized)) {
            logMismatch(reportId, javaSize, sqlSize, javaNormalized, sqlNormalized);
            dualMismatchCounter.increment();
            return;
        }

        dualMatchCounter.increment();
        logger.info("DUAL mode check passed for report {} ({} rows)", reportId, javaSize);
    }

    private List<String> normalize(List<Map<String, Object>> rows) {
        if (rows == null) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(row -> row.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                        .map(e -> e.getKey() + "=" + safeToString(e.getValue()))
                        .collect(Collectors.joining("|")))
                .sorted()
                .collect(Collectors.toList());
    }

    private String safeToString(Object value) {
        return value == null ? "null" : value.toString();
    }

    private void logMismatch(Long reportId,
                             int javaSize,
                             int sqlSize,
                             List<String> javaNormalized,
                             List<String> sqlNormalized) {
        logger.warn("DUAL mode mismatch for report {} (java={}, sql={})", reportId, javaSize, sqlSize);

        List<String> javaOnly = new ArrayList<>(javaNormalized);
        javaOnly.removeAll(sqlNormalized);

        List<String> sqlOnly = new ArrayList<>(sqlNormalized);
        sqlOnly.removeAll(javaNormalized);

        Optional<String> javaSample = javaOnly.stream().findFirst();
        Optional<String> sqlSample = sqlOnly.stream().findFirst();

        javaSample.ifPresent(sample ->
                logger.warn("Sample row present only in Java result for report {} -> {}", reportId, sample));
        sqlSample.ifPresent(sample ->
                logger.warn("Sample row present only in SQL result for report {} -> {}", reportId, sample));
    }
}
