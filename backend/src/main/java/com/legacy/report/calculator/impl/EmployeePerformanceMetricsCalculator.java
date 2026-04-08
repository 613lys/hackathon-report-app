package com.legacy.report.calculator.impl;

import com.legacy.report.calculator.ReportCalculator;
import com.legacy.report.calculator.model.DepartmentRecord;
import com.legacy.report.calculator.model.EmployeeRecord;
import com.legacy.report.dto.ReportContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EmployeePerformanceMetricsCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 9L;

    private final JdbcTemplate jdbcTemplate;

    public EmployeePerformanceMetricsCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<EmployeeRecord> employees = fetchActiveEmployees();
        List<DepartmentRecord> departments = fetchDepartments();

        Map<Long, DepartmentRecord> deptMap = new java.util.HashMap<>();
        for (DepartmentRecord dept : departments) {
            deptMap.put(dept.getId(), dept);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (EmployeeRecord emp : employees) {
            DepartmentRecord dept = deptMap.get(emp.getDepartmentId());
            if (dept == null) {
                continue;
            }

            BigDecimal budgetPct = dept.getBudget().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                    emp.getSalary().divide(dept.getBudget(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

            String tier;
            if (emp.getSalary().compareTo(new BigDecimal("80000")) > 0) {
                tier = "High";
            } else if (emp.getSalary().compareTo(new BigDecimal("60000")) > 0) {
                tier = "Medium";
            } else {
                tier = "Standard";
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", dept.getName());
            row.put("employee_name", emp.getName());
            row.put("position", emp.getPosition());
            row.put("salary", emp.getSalary());
            row.put("hire_date", emp.getHireDate().toString());
            row.put("budget", dept.getBudget());
            row.put("budget_percentage", budgetPct);
            row.put("salary_tier", tier);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (String) row.get("department"))
                .thenComparing((Map<String, Object> row) -> ((BigDecimal) row.get("salary")).doubleValue()).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<EmployeeRecord> fetchActiveEmployees() {
        String sql = "SELECT id, name, department_id, position, salary, hire_date, status FROM employee WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            EmployeeRecord record = new EmployeeRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setDepartmentId(rs.getLong("department_id"));
            record.setPosition(rs.getString("position"));
            record.setSalary(rs.getBigDecimal("salary"));
            record.setHireDate(rs.getDate("hire_date").toLocalDate());
            record.setStatus(rs.getString("status"));
            return record;
        });
    }

    private List<DepartmentRecord> fetchDepartments() {
        String sql = "SELECT id, name, manager, budget, location FROM department";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DepartmentRecord record = new DepartmentRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setManager(rs.getString("manager"));
            record.setBudget(rs.getBigDecimal("budget"));
            record.setLocation(rs.getString("location"));
            return record;
        });
    }
}
