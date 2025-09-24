package com.example.employee_api.controller;

import com.example.employee_api.model.Employee;
import com.example.employee_api.service.EmployeeHierarchyService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing employee hierarchy relationships
 */
@RestController
@RequestMapping("/api/employees/hierarchy")
@PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('SUPER_ADMIN')")
public class EmployeeHierarchyController {

    private final EmployeeHierarchyService hierarchyService;

    public EmployeeHierarchyController(EmployeeHierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    /**
     * Assign a manager to an employee
     */
    @PostMapping("/{employeeId}/manager/{managerId}")
    public ResponseEntity<Employee> assignManager(
            @PathVariable @NotNull Long employeeId,
            @PathVariable @NotNull Long managerId) {
        Employee updatedEmployee = hierarchyService.assignManager(employeeId, managerId);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Remove manager from an employee
     */
    @DeleteMapping("/{employeeId}/manager")
    public ResponseEntity<Employee> removeManager(@PathVariable @NotNull Long employeeId) {
        Employee updatedEmployee = hierarchyService.removeManager(employeeId);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Get all direct subordinates of a manager
     */
    @GetMapping("/{managerId}/subordinates")
    public ResponseEntity<List<Employee>> getSubordinates(@PathVariable @NotNull Long managerId) {
        List<Employee> subordinates = hierarchyService.getSubordinates(managerId);
        return ResponseEntity.ok(subordinates);
    }

    /**
     * Get the reporting chain for an employee
     */
    @GetMapping("/{employeeId}/reporting-chain")
    public ResponseEntity<List<Employee>> getReportingChain(@PathVariable @NotNull Long employeeId) {
        List<Employee> reportingChain = hierarchyService.getReportingChain(employeeId);
        return ResponseEntity.ok(reportingChain);
    }

    /**
     * Get organizational chart
     */
    @GetMapping("/org-chart")
    public ResponseEntity<Map<String, Object>> getOrganizationalChart() {
        Map<String, Object> orgChart = hierarchyService.getOrganizationalChart();
        return ResponseEntity.ok(orgChart);
    }

    /**
     * Get all managers in the organization
     */
    @GetMapping("/managers")
    public ResponseEntity<List<Employee>> getAllManagers() {
        List<Employee> managers = hierarchyService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    /**
     * Get hierarchy statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getHierarchyStatistics() {
        Map<String, Object> statistics = hierarchyService.getHierarchyStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * DTO for manager assignment request
     */
    public static class ManagerAssignmentRequest {
        @NotNull(message = "Manager ID is required")
        private Long managerId;

        public ManagerAssignmentRequest() {}

        public ManagerAssignmentRequest(Long managerId) {
            this.managerId = managerId;
        }

        public Long getManagerId() {
            return managerId;
        }

        public void setManagerId(Long managerId) {
            this.managerId = managerId;
        }
    }
}