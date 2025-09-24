package com.example.employee_api.service;

import com.example.employee_api.dto.ReportRequest;
import com.example.employee_api.dto.ReportResponse;
import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.repository.*;
import com.example.employee_api.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import java.time.format.DateTimeParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;
    
    @Autowired
    private SalaryHistoryRepository salaryHistoryRepository;
    
    @Autowired
    private BonusRepository bonusRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    // Create a new report
    public ReportResponse createReport(ReportRequest request, String createdBy) {
        Report report = new Report(request.getType(), request.getTitle(), createdBy);
        report.setDescription(request.getDescription());
        report.setScheduled(request.isScheduled());
        report.setCronExpression(request.getCronExpression());
        
        // Convert parameters map to string map
        if (request.getParameters() != null) {
            Map<String, String> stringParams = request.getParameters().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().toString()
                ));
            report.setParameters(stringParams);
        }
        
        report = reportRepository.save(report);
        
        // Generate report data based on type
        try {
            generateReportData(report, request);
            report.setStatus(ReportStatus.COMPLETED);
        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
        }
        
        report = reportRepository.save(report);
        return convertToResponse(report);
    }

    // Get all reports with pagination
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::convertToResponse);
    }

    // Get report by ID
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return convertToResponse(report);
    }

    // Get reports by type
    public Page<ReportResponse> getReportsByType(ReportType type, Pageable pageable) {
        return reportRepository.findByType(type, pageable).map(this::convertToResponse);
    }

    // Get reports by status
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable).map(this::convertToResponse);
    }

    // Get user's reports
    public Page<ReportResponse> getUserReports(String username, Pageable pageable) {
        return reportRepository.findByCreatedBy(username, pageable).map(this::convertToResponse);
    }

    // Delete report
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report not found with id: " + id);
        }
        reportRepository.deleteById(id);
    }

    // Generate Employee Report
    private Map<String, Object> generateEmployeeReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();
        
        // Basic employee statistics
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long inactiveEmployees = employeeRepository.countByStatus(EmployeeStatus.INACTIVE);
        long terminatedEmployees = employeeRepository.countByStatus(EmployeeStatus.TERMINATED);
        
        data.put("totalEmployees", totalEmployees);
        data.put("activeEmployees", activeEmployees);
        data.put("inactiveEmployees", inactiveEmployees);
        data.put("terminatedEmployees", terminatedEmployees);
        
        // Employee distribution by department
        List<Object[]> departmentDistribution = employeeRepository.countEmployeesByDepartment();
        Map<String, Long> departmentCounts = new HashMap<>();
        for (Object[] row : departmentDistribution) {
            departmentCounts.put((String) row[0], (Long) row[1]);
        }
        data.put("departmentDistribution", departmentCounts);
        
        // Recent hires (if date range provided)
        if (startDate != null && endDate != null) {
            List<Employee> recentHires = employeeRepository.findByHireDateBetween(startDate, endDate);
            data.put("recentHires", recentHires.size());
            data.put("recentHiresList", recentHires.stream()
                .map(emp -> Map.of(
                    "id", emp.getId(),
                    "name", emp.getFirstName() + " " + emp.getLastName(),
                    "hireDate", emp.getHireDate(),
                    "department", emp.getDepartment() != null ? emp.getDepartment().getName() : "N/A"
                ))
                .collect(Collectors.toList()));
        }
        
        return data;
    }

    // Generate Department Report
    private Map<String, Object> generateDepartmentReport() {
        Map<String, Object> data = new HashMap<>();
        
        long totalDepartments = departmentRepository.count();
        data.put("totalDepartments", totalDepartments);
        
        // Department employee counts
        List<Object[]> departmentCounts = employeeRepository.countEmployeesByDepartment();
        List<Map<String, Object>> departmentStats = new ArrayList<>();
        
        for (Object[] row : departmentCounts) {
            Map<String, Object> dept = new HashMap<>();
            dept.put("departmentName", row[0]);
            dept.put("employeeCount", row[1]);
            departmentStats.add(dept);
        }
        
        data.put("departmentStatistics", departmentStats);
        
        // Department budget analysis (if budget data exists)
        List<Department> departments = departmentRepository.findAll();
        BigDecimal totalBudget = departments.stream()
            .filter(dept -> dept.getBudget() != null)
            .map(Department::getBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("totalBudget", totalBudget);
        data.put("departmentsWithBudget", departments.stream()
            .filter(dept -> dept.getBudget() != null)
            .count());
        
        return data;
    }

    // Generate Attendance Report (Leave-based)
    private Map<String, Object> generateAttendanceReport(int year) {
        Map<String, Object> data = new HashMap<>();
        
        // Basic leave statistics
        long totalLeaveRequests = leaveRequestRepository.count();
        long approvedLeaves = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.APPROVED);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING);
        long rejectedLeaves = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.REJECTED);
        
        data.put("totalLeaveRequests", totalLeaveRequests);
        data.put("approvedLeaves", approvedLeaves);
        data.put("pendingLeaves", pendingLeaves);
        data.put("rejectedLeaves", rejectedLeaves);
        data.put("year", year);
        
        // Approval rate
        double approvalRate = totalLeaveRequests > 0 ? 
            (double) approvedLeaves / totalLeaveRequests * 100 : 0.0;
        data.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
        
        return data;
    }

    // Generate Performance Report
    private Map<String, Object> generatePerformanceReport(int year) {
        Map<String, Object> data = new HashMap<>();
        
        // Basic performance statistics
        long totalReviews = performanceReviewRepository.count();
        long completedReviews = performanceReviewRepository.countByStatus(ReviewStatus.COMPLETED);
        long pendingReviews = performanceReviewRepository.countByStatus(ReviewStatus.PENDING);
        
        data.put("totalReviews", totalReviews);
        data.put("completedReviews", completedReviews);
        data.put("pendingReviews", pendingReviews);
        data.put("year", year);
        
        // Completion rate
        double completionRate = totalReviews > 0 ? 
            (double) completedReviews / totalReviews * 100 : 0.0;
        data.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        return data;
    }

    // Generate Payroll Report
    private Map<String, Object> generatePayrollReport(int year) {
        Map<String, Object> data = new HashMap<>();
        
        // Get salary history and bonus data
        List<SalaryHistory> allSalaryHistory = salaryHistoryRepository.findAll();
        List<Bonus> allBonuses = bonusRepository.findAll();
        
        BigDecimal totalSalaries = allSalaryHistory.stream()
            .map(sh -> sh.getNewSalary())
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalBonuses = allBonuses.stream()
            .map(Bonus::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("totalSalaries", totalSalaries);
        data.put("totalBonuses", totalBonuses);
        data.put("salaryRecords", allSalaryHistory.size());
        data.put("bonusRecords", allBonuses.size());
        data.put("year", year);
        
        return data;
    }

    // Generate Turnover Analysis
    private Map<String, Object> generateTurnoverReport(int year) {
        Map<String, Object> data = new HashMap<>();
        
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        
        // Basic statistics using available methods
        List<Employee> allEmployees = employeeRepository.findAll();
        long newHires = allEmployees.stream()
            .filter(emp -> emp.getHireDate() != null)
            .filter(emp -> !emp.getHireDate().isBefore(startOfYear) && !emp.getHireDate().isAfter(endOfYear))
            .count();
        
        long terminations = employeeRepository.countByStatus(EmployeeStatus.TERMINATED);
        long currentEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        
        data.put("newHires", newHires);
        data.put("terminations", terminations);
        data.put("currentEmployees", currentEmployees);
        data.put("year", year);
        
        // Simple turnover rate calculation
        double turnoverRate = currentEmployees > 0 ? 
            (double) terminations / currentEmployees * 100 : 0.0;
        data.put("turnoverRate", Math.round(turnoverRate * 100.0) / 100.0);
        
        return data;
    }

    // Generate Demographics Report
    private Map<String, Object> generateDemographicsReport() {
        Map<String, Object> data = new HashMap<>();
        
        // Status distribution
        Map<String, Long> statusDistribution = Arrays.stream(EmployeeStatus.values())
            .collect(Collectors.toMap(
                EmployeeStatus::name,
                status -> employeeRepository.countByStatus(status)
            ));
        data.put("statusDistribution", statusDistribution);
        
        // Department distribution
        List<Object[]> deptDistribution = employeeRepository.countEmployeesByDepartment();
        Map<String, Long> departmentCounts = new HashMap<>();
        for (Object[] row : deptDistribution) {
            departmentCounts.put((String) row[0], (Long) row[1]);
        }
        data.put("departmentDistribution", departmentCounts);
        
        return data;
    }

    // Generate report data based on type
    private void generateReportData(Report report, ReportRequest request) {
        try {
            Map<String, Object> reportData = new HashMap<>();
            
            switch (report.getType()) {
                case EMPLOYEES:
                    reportData = generateEmployeeReport(request.getStartDate(), request.getEndDate());
                    break;
                case DEPARTMENTS:
                    reportData = generateDepartmentReport();
                    break;
                case ATTENDANCE:
                    int attendanceYear = request.getStartDate() != null ? 
                        request.getStartDate().getYear() : Year.now().getValue();
                    reportData = generateAttendanceReport(attendanceYear);
                    break;
                case PERFORMANCE:
                    int perfYear = request.getStartDate() != null ? 
                        request.getStartDate().getYear() : Year.now().getValue();
                    reportData = generatePerformanceReport(perfYear);
                    break;
                case PAYROLL:
                    int payrollYear = request.getStartDate() != null ? 
                        request.getStartDate().getYear() : Year.now().getValue();
                    reportData = generatePayrollReport(payrollYear);
                    break;
                case TURNOVER:
                    int turnoverYear = request.getStartDate() != null ? 
                        request.getStartDate().getYear() : Year.now().getValue();
                    reportData = generateTurnoverReport(turnoverYear);
                    break;
                case DEMOGRAPHICS:
                    reportData = generateDemographicsReport();
                    break;
                case CUSTOM:
                    reportData = generateCustomReport(request.getParameters());
                    break;
            }
            
            report.setReportData(objectMapper.writeValueAsString(reportData));
            report.setFileFormat("JSON");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report data: " + e.getMessage(), e);
        }
    }

    // Generate custom report based on parameters
    private Map<String, Object> generateCustomReport(Map<String, Object> parameters) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Custom report generation not implemented yet");
        data.put("parameters", parameters);
        return data;
    }

    // Convert Report entity to ReportResponse DTO
    @SuppressWarnings("unchecked")
    private ReportResponse convertToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setType(report.getType());
        response.setTitle(report.getTitle());
        response.setDescription(report.getDescription());
        response.setStatus(report.getStatus());
        response.setCreatedAt(report.getCreatedAt());
        response.setCompletedAt(report.getCompletedAt());
        response.setCreatedBy(report.getCreatedBy());
        response.setErrorMessage(report.getErrorMessage());
        response.setFileSizeBytes(report.getFileSizeBytes());
        response.setFileFormat(report.getFileFormat());
        
        // Parse report data if available
        if (report.getReportData() != null) {
            try {
                Map<String, Object> data = objectMapper.readValue(report.getReportData(), Map.class);
                response.setData(data);
            } catch (Exception e) {
                // Handle parsing error silently
            }
        }
        
        // Set download URL if report is completed
        if (report.getStatus() == ReportStatus.COMPLETED) {
            response.setDownloadUrl("/api/reports/" + report.getId() + "/download");
        }
        
        return response;
    }

    // Get reports by date range
    public List<ReportResponse> getReportsByDateRange(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // Use the existing pageable method without pagination
            Pageable pageable = Pageable.unpaged();
            Page<Report> reportPage = reportRepository.findByCreatedAtBetween(
                start.atStartOfDay(), 
                end.atTime(23, 59, 59),
                pageable
            );
            
            return reportPage.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: YYYY-MM-DD");
        }
    }

    // Get reports by creator username
    public List<ReportResponse> getReportsByCreatedBy(String username) {
        // Use the existing pageable method without pagination
        Pageable pageable = Pageable.unpaged();
        Page<Report> reportPage = reportRepository.findByCreatedBy(username, pageable);
        return reportPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}