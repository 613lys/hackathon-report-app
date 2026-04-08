package com.legacy.report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for report execution mode.
 * Controls whether reports execute via SQL (legacy), Java calculator, or dual mode.
 */
@Configuration
@ConfigurationProperties(prefix = "report.execution")
public class ReportExecutionProperties {
    
    /**
     * Default execution mode: SQL, JAVA, or DUAL
     */
    private ExecutionMode mode = ExecutionMode.SQL;
    
    /**
     * Per-report overrides. Key: report ID, Value: execution mode
     */
    private Map<Long, ExecutionMode> overrides = new HashMap<>();
    
    public ExecutionMode getMode() {
        return mode;
    }
    
    public void setMode(ExecutionMode mode) {
        this.mode = mode;
    }
    
    public Map<Long, ExecutionMode> getOverrides() {
        return overrides;
    }
    
    public void setOverrides(Map<Long, ExecutionMode> overrides) {
        this.overrides = overrides;
    }
    
    /**
     * Gets the execution mode for a specific report ID.
     * Returns the override if set, otherwise returns the default mode.
     * 
     * @param reportId the report ID
     * @return the execution mode for this report
     */
    public ExecutionMode getModeForReport(Long reportId) {
        return overrides.getOrDefault(reportId, mode);
    }
    
    /**
     * Execution mode enum
     */
    public enum ExecutionMode {
        /**
         * Execute using legacy SQL
         */
        SQL,
        
        /**
         * Execute using Java calculator
         */
        JAVA,
        
        /**
         * Execute both modes and compare results
         */
        DUAL
    }
}
