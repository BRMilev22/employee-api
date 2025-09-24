package com.example.employee_api.controller;

import com.example.employee_api.dto.*;
import com.example.employee_api.model.ReportStatus;
import com.example.employee_api.model.ReportType;
import com.example.employee_api.service.AnalyticsService;
import com.example.employee_api.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
public class ReportController {

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private AnalyticsService analyticsService;

    // ==================== STANDARD REPORTS (7 endpoints) ====================

    /**
     * Generate a new report
     * POST /api/reports
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ReportResponse report = reportService.createReport(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * Generate a new report (alternative endpoint for testing)
     * POST /api/reports/generate
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<ReportResponse> generateReport(
            @Valid @RequestBody ReportRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ReportResponse report = reportService.createReport(request, username);
        return ResponseEntity.ok(report);
    }

    /**
     * Get all reports with pagination
     * GET /api/reports
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('USER')")
    public ResponseEntity<Page<ReportResponse>> getAllReports(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ReportResponse> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get report by ID
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        ReportResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * Get reports by type
     * GET /api/reports/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<ReportResponse>> getReportsByType(
            @PathVariable ReportType type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ReportResponse> reports = reportService.getReportsByType(type, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by status
     * GET /api/reports/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ReportResponse>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ReportResponse> reports = reportService.getReportsByStatus(status, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by date range
     * GET /api/reports/date-range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<ReportResponse>> getReportsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        List<ReportResponse> reports = reportService.getReportsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by creator
     * GET /api/reports/created-by/{username}
     */
    @GetMapping("/created-by/{username}")
    public ResponseEntity<List<ReportResponse>> getReportsByCreatedBy(
            @PathVariable String username) {
        
        List<ReportResponse> reports = reportService.getReportsByCreatedBy(username);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get user's reports
     * GET /api/reports/my-reports
     */
    @GetMapping("/my-reports")
    public ResponseEntity<Page<ReportResponse>> getUserReports(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        String username = authentication.getName();
        Page<ReportResponse> reports = reportService.getUserReports(username, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Delete report
     * DELETE /api/reports/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ANALYTICS DASHBOARD (7 endpoints) ====================

    /**
     * Get comprehensive dashboard data
     * GET /api/reports/analytics/dashboard
     */
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<DashboardData> getDashboardData() {
        DashboardData dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get Key Performance Indicators
     * GET /api/reports/analytics/kpis
     */
    @GetMapping("/analytics/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        Map<String, Object> kpis = analyticsService.getKpisAsMap();
        return ResponseEntity.ok(kpis);
    }

    /**
     * Get employee statistics and analytics
     * GET /api/reports/analytics/employees
     */
    @GetMapping("/analytics/employees")
    public ResponseEntity<Map<String, Object>> getEmployeeStatistics() {
        Map<String, Object> stats = analyticsService.getEmployeeStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get department analytics
     * GET /api/reports/analytics/departments
     */
    @GetMapping("/analytics/departments")
    public ResponseEntity<Map<String, Object>> getDepartmentAnalytics() {
        Map<String, Object> analytics = analyticsService.getDepartmentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get leave analytics
     * GET /api/reports/analytics/leave
     */
    @GetMapping("/analytics/leave")
    public ResponseEntity<Map<String, Object>> getLeaveAnalytics() {
        Map<String, Object> analytics = analyticsService.getLeaveAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get performance analytics
     * GET /api/reports/analytics/performance
     */
    @GetMapping("/analytics/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceAnalytics() {
        Map<String, Object> analytics = analyticsService.getPerformanceAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get payroll analytics
     * GET /api/reports/analytics/payroll
     */
    @GetMapping("/analytics/payroll")
    public ResponseEntity<Map<String, Object>> getPayrollAnalytics() {
        Map<String, Object> analytics = analyticsService.getPayrollAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get position/level analytics
     * GET /api/reports/analytics/positions
     */
    @GetMapping("/analytics/positions")
    public ResponseEntity<Map<String, Object>> getPositionAnalytics() {
        Map<String, Object> analytics = analyticsService.getPositionAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get salary analysis
     * GET /api/reports/analytics/salaries
     */
    @GetMapping("/analytics/salaries")
    public ResponseEntity<Map<String, Object>> getSalaryAnalysis() {
        Map<String, Object> analytics = analyticsService.getSalaryAnalysis();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get trend analysis
     * GET /api/reports/analytics/trends
     */
    @GetMapping("/analytics/trends")
    public ResponseEntity<Map<String, Object>> getTrendAnalysis() {
        Map<String, Object> analytics = analyticsService.getTrendAnalysis();
        return ResponseEntity.ok(analytics);
    }

    // ==================== ADDITIONAL UTILITY ENDPOINTS ====================

    /**
     * Download report file
     * GET /api/reports/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        ReportResponse report = reportService.getReportById(id);
        
        if (report.getStatus() != ReportStatus.COMPLETED) {
            return ResponseEntity.badRequest().build();
        }
        
        // In a real implementation, this would return the actual file bytes
        // For now, return the JSON data as bytes
        String reportData = report.getData() != null ? report.getData().toString() : "{}";
        byte[] data = reportData.getBytes();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Content-Disposition", "attachment; filename=\"" + 
            report.getTitle().replaceAll("\\s+", "_") + ".json\"");
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(data);
    }

    /**
     * Get available report types
     * GET /api/reports/types
     */
    @GetMapping("/types")
    public ResponseEntity<ReportType[]> getReportTypes() {
        return ResponseEntity.ok(ReportType.values());
    }

    /**
     * Get available report statuses
     * GET /api/reports/statuses
     */
    @GetMapping("/statuses")
    public ResponseEntity<ReportStatus[]> getReportStatuses() {
        return ResponseEntity.ok(ReportStatus.values());
    }
}