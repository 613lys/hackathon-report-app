package com.legacy.report.dto;

import com.legacy.report.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Execution context for report calculation.
 * Contains user information and report parameters.
 */
public class ReportContext {
    
    private final User currentUser;
    private final Map<String, Object> parameters;
    
    public ReportContext(User currentUser) {
        this(currentUser, new HashMap<>());
    }
    
    public ReportContext(User currentUser, Map<String, Object> parameters) {
        this.currentUser = currentUser;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }
}
