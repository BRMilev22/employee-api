package com.example.employee_api.controller;

import com.example.employee_api.model.Employee;
import com.example.employee_api.model.EmployeeStatusHistory;
import com.example.employee_api.service.EmployeeLifecycleService;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing employee lifecycle and status changes
 */
@RestController
@RequestMapping("/api/employees/lifecycle")
@PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('SUPER_ADMIN')")
public class EmployeeLifecycleController {

    private final EmployeeLifecycleService lifecycleService;

    public EmployeeLifecycleController(EmployeeLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    /**
     * Activate an employee
     */
    @PostMapping("/{employeeId}/activate")
    public ResponseEntity<Employee> activateEmployee(
            @PathVariable @NotNull Long employeeId,
            @RequestParam(required = false) String reason) {
        Employee employee = lifecycleService.activateEmployee(employeeId, reason);
        return ResponseEntity.ok(employee);
    }

    /**
     * Deactivate an employee
     */
    @PostMapping("/{employeeId}/deactivate")
    public ResponseEntity<Employee> deactivateEmployee(
            @PathVariable @NotNull Long employeeId,
            @RequestParam(required = false) String reason) {
        Employee employee = lifecycleService.deactivateEmployee(employeeId, reason);
        return ResponseEntity.ok(employee);
    }

    /**
     * Terminate an employee
     */
    @PostMapping("/{employeeId}/terminate")
    public ResponseEntity<Employee> terminateEmployee(
            @PathVariable @NotNull Long employeeId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminationDate) {
        Employee employee = lifecycleService.terminateEmployee(employeeId, reason, terminationDate);
        return ResponseEntity.ok(employee);
    }

    /**
     * Start onboarding process for an employee
     */
    @PostMapping("/{employeeId}/onboard")
    public ResponseEntity<Employee> onboardEmployee(
            @PathVariable @NotNull Long employeeId,
            @RequestParam(required = false) String reason) {
        Employee employee = lifecycleService.onboardEmployee(employeeId, reason);
        return ResponseEntity.ok(employee);
    }

    /**
     * Start offboarding process for an employee
     */
    @PostMapping("/{employeeId}/offboard")
    public ResponseEntity<Employee> offboardEmployee(
            @PathVariable @NotNull Long employeeId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastWorkingDay) {
        Employee employee = lifecycleService.offboardEmployee(employeeId, reason, lastWorkingDay);
        return ResponseEntity.ok(employee);
    }

    /**
     * Get status history for an employee
     */
    @GetMapping("/{employeeId}/status-history")
    public ResponseEntity<List<EmployeeStatusHistory>> getEmployeeStatusHistory(
            @PathVariable @NotNull Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size <= 0) {
            // Return all history without pagination
            List<EmployeeStatusHistory> history = lifecycleService.getEmployeeStatusHistory(employeeId);
            return ResponseEntity.ok(history);
        } else {
            // Return paginated history
            Pageable pageable = PageRequest.of(page, size);
            Page<EmployeeStatusHistory> historyPage = lifecycleService.getEmployeeStatusHistory(employeeId, pageable);
            return ResponseEntity.ok(historyPage.getContent());
        }
    }

    /**
     * Get employees activated in a date range
     */
    @GetMapping("/activated")
    public ResponseEntity<List<Employee>> getActivatedEmployees(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Employee> employees = lifecycleService.getEmployeesActivatedBetween(startDate, endDate);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employees terminated in a date range
     */
    @GetMapping("/terminated")
    public ResponseEntity<List<Employee>> getTerminatedEmployees(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Employee> employees = lifecycleService.getEmployeesTerminatedBetween(startDate, endDate);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get status change statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatusChangeStatistics() {
        EmployeeLifecycleService.StatusChangeStatistics stats = lifecycleService.getStatusChangeStatistics();
        
        Map<String, Object> response = Map.of(
            "totalChanges", stats.getTotalChanges(),
            "activations", stats.getActivations(),
            "deactivations", stats.getDeactivations(),
            "terminations", stats.getTerminations(),
            "onboardings", stats.getOnboardings()
        );
        
        return ResponseEntity.ok(response);
    }
}