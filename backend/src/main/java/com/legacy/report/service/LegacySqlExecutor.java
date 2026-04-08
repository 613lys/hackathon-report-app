package com.legacy.report.service;

import com.legacy.report.dao.ReportDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Wrapper for legacy SQL execution.
 * Isolates the direct SQL execution path for controlled access and audit logging.
 * Should only be used through ReportExecutionManager.
 */
@Service
public class LegacySqlExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(LegacySqlExecutor.class);
    
    private final ReportDao reportDao;
    
    @Autowired
    public LegacySqlExecutor(ReportDao reportDao) {
        this.reportDao = reportDao;
    }
    
    /**
     * Executes SQL and logs for audit purposes.
     * 
     * @param sql the SQL to execute
     * @param reportId the report ID for audit context
     * @return the query results
     */
    public List<Map<String, Object>> execute(String sql, Long reportId) {
        logger.info("Legacy SQL execution for report {}: {}", reportId, 
                sql.length() > 100 ? sql.substring(0, 100) + "..." : sql);
        
        try {
            List<Map<String, Object>> results = reportDao.executeSql(sql);
            logger.debug("Legacy SQL execution completed for report {}: {} rows", 
                    reportId, results != null ? results.size() : 0);
            return results;
        } catch (Exception e) {
            logger.error("Legacy SQL execution failed for report {}: {}", reportId, e.getMessage());
            throw new ReportExecutionException("SQL execution failed for report " + reportId, e);
        }
    }
    
    /**
     * Custom exception for report execution failures.
     */
    public static class ReportExecutionException extends RuntimeException {
        public ReportExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
