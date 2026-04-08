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
import java.util.stream.Collectors;

@Component
public class DepartmentBudgetAnalysisCalculator implements ReportCalculator {

    private static final Long REPORT_ID = 4L;

    private final JdbcTemplate jdbcTemplate;

    public DepartmentBudgetAnalysisCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> calculate(Long reportId, ReportContext ctx) {
        List<DepartmentRecord> departments = fetchDepartments();
        List<EmployeeRecord> employees = fetchActiveEmployees();

        Map<Long, List<EmployeeRecord>> empByDept = employees.stream()
                .filter(emp -> emp.getDepartmentId() != null)
                .collect(Collectors.groupingBy(EmployeeRecord::getDepartmentId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (DepartmentRecord dept : departments) {
            List<EmployeeRecord> empList = empByDept.getOrDefault(dept.getId(), List.of());

            long count = empList.size();
            BigDecimal totalSalary = empList.stream()
                    .map(EmployeeRecord::getSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal budgetVariance = dept.getBudget().subtract(totalSalary);
            BigDecimal utilization = dept.getBudget().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                    totalSalary.divide(dept.getBudget(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", dept.getName());
            row.put("manager", dept.getManager());
            row.put("budget", dept.getBudget());
            row.put("location", dept.getLocation());
            row.put("employee_count", count);
            row.put("total_salary_cost", totalSalary.setScale(2, RoundingMode.HALF_UP));
            row.put("budget_variance", budgetVariance.setScale(2, RoundingMode.HALF_UP));
            row.put("budget_utilization_percent", utilization);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("budget_utilization_percent")).reversed());
        return rows;
    }

    @Override
    public boolean supports(Long reportId) {
        return REPORT_ID.equals(reportId);
    }

    private List<DepartmentRecord> fetchDepartments() {
        String sql = "SELECT id, name, manager, budget, location FROM department";
        return jdbcTemplate.query(sql, new DepartmentRowMapper());
    }

    private List<EmployeeRecord> fetchActiveEmployees() {
        String sql = "SELECT id, name, department_id, position, salary, hire_date, status FROM employee WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(sql, new EmployeeRowMapper());
    }

    private static class DepartmentRowMapper implements RowMapper<DepartmentRecord> {
        @Override
        public DepartmentRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentRecord record = new DepartmentRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setManager(rs.getString("manager"));
            record.setBudget(rs.getBigDecimal("budget"));
            record.setLocation(rs.getString("location"));
            return record;
        }
    }

    private static class EmployeeRowMapper implements RowMapper<EmployeeRecord> {
        @Override
        public EmployeeRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            EmployeeRecord record = new EmployeeRecord();
            record.setId(rs.getLong("id"));
            record.setName(rs.getString("name"));
            record.setDepartmentId(rs.getLong("department_id"));
            record.setPosition(rs.getString("position"));
            record.setSalary(rs.getBigDecimal("salary"));
            record.setHireDate(rs.getDate("hire_date").toLocalDate());
            record.setStatus(rs.getString("status"));
            return record;
        }
    }
}
