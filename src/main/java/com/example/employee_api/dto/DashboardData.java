package com.example.employee_api.dto;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardData {
    private Map<String, Object> employeeMetrics;
    private Map<String, Object> departmentMetrics;
    private Map<String, Object> leaveMetrics;
    private Map<String, Object> performanceMetrics;
    private Map<String, Object> payrollMetrics;
    private Map<String, Object> recentActivities;

    // Key Performance Indicators
    private Long totalEmployees;
    private Long activeEmployees;
    private BigDecimal averageSalary;
    private Double employeeTurnoverRate;
    private Double leaveApprovalRate;
    private Integer totalDepartments;
    private Integer totalPositions;
    private Integer activeReports;
    private Integer pendingLeaveRequests;
    private Integer pendingPerformanceReviews;

    // Constructors
    public DashboardData() {}

    // Getters and Setters
    public Map<String, Object> getEmployeeMetrics() {
        return employeeMetrics;
    }

    public void setEmployeeMetrics(Map<String, Object> employeeMetrics) {
        this.employeeMetrics = employeeMetrics;
    }

    public Map<String, Object> getDepartmentMetrics() {
        return departmentMetrics;
    }

    public void setDepartmentMetrics(Map<String, Object> departmentMetrics) {
        this.departmentMetrics = departmentMetrics;
    }

    public Map<String, Object> getLeaveMetrics() {
        return leaveMetrics;
    }

    public void setLeaveMetrics(Map<String, Object> leaveMetrics) {
        this.leaveMetrics = leaveMetrics;
    }

    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }

    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    public Map<String, Object> getPayrollMetrics() {
        return payrollMetrics;
    }

    public void setPayrollMetrics(Map<String, Object> payrollMetrics) {
        this.payrollMetrics = payrollMetrics;
    }

    public Map<String, Object> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(Map<String, Object> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public Long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Long getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(Long activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public BigDecimal getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(BigDecimal averageSalary) {
        this.averageSalary = averageSalary;
    }

    public Double getEmployeeTurnoverRate() {
        return employeeTurnoverRate;
    }

    public void setEmployeeTurnoverRate(Double employeeTurnoverRate) {
        this.employeeTurnoverRate = employeeTurnoverRate;
    }

    public Double getLeaveApprovalRate() {
        return leaveApprovalRate;
    }

    public void setLeaveApprovalRate(Double leaveApprovalRate) {
        this.leaveApprovalRate = leaveApprovalRate;
    }

    public Integer getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(Integer totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public Integer getTotalPositions() {
        return totalPositions;
    }

    public void setTotalPositions(Integer totalPositions) {
        this.totalPositions = totalPositions;
    }

    public Integer getActiveReports() {
        return activeReports;
    }

    public void setActiveReports(Integer activeReports) {
        this.activeReports = activeReports;
    }

    public Integer getPendingLeaveRequests() {
        return pendingLeaveRequests;
    }

    public void setPendingLeaveRequests(Integer pendingLeaveRequests) {
        this.pendingLeaveRequests = pendingLeaveRequests;
    }

    public Integer getPendingPerformanceReviews() {
        return pendingPerformanceReviews;
    }

    public void setPendingPerformanceReviews(Integer pendingPerformanceReviews) {
        this.pendingPerformanceReviews = pendingPerformanceReviews;
    }
}