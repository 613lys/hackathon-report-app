package com.legacy.report.calculator;

import com.legacy.report.dto.ReportContext;

import java.util.List;
import java.util.Map;

/**
 * Calculator interface for report execution logic.
 * Each report implementation should provide a concrete calculator
 * that handles aggregation, classification, ranking, and KPI calculations in Java.
 */
public interface ReportCalculator {
    
    /**
     * Calculates and returns report data.
     * 
     * @param reportId the report ID
     * @param ctx the execution context containing parameters and user info
     * @return list of data rows, each represented as a column-to-value map
     */
    List<Map<String, Object>> calculate(Long reportId, ReportContext ctx);
    
    /**
     * Returns true if this calculator supports the given report ID.
     * 
     * @param reportId the report ID to check
     * @return true if supported
     */
    boolean supports(Long reportId);
}
