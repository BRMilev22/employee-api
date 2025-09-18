package com.example.employee_api.controller;

import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.dto.search.EmployeeSearchCriteria;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    /**
     * GET /api/employees - Get all employees
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * GET /api/employees/{id} - Get employee by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * POST /api/employees - Create a new employee
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }
    
    /**
     * PUT /api/employees/{id} - Update an existing employee
     */
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, 
                                                 @Valid @RequestBody Employee employeeDetails) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    /**
     * DELETE /api/employees/{id} - Delete an employee
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/employees/job-title/{jobTitle} - Get employees by job title
     */
    @GetMapping("/job-title/{jobTitle}")
    public ResponseEntity<List<Employee>> getEmployeesByJobTitle(@PathVariable String jobTitle) {
        List<Employee> employees = employeeService.getEmployeesByJobTitle(jobTitle);
        return ResponseEntity.ok(employees);
    }
    
    // ========== ADVANCED SEARCH ENDPOINTS ==========
    
    /**
     * POST /api/employees/search - Advanced search with comprehensive filtering
     */
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<Employee>> searchEmployees(@Valid @RequestBody EmployeeSearchCriteria criteria) {
        PagedResponse<Employee> result = employeeService.searchEmployees(criteria);
        return ResponseEntity.ok(result);
    }
    
    /**
     * GET /api/employees/search - Quick search with query parameters
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<Employee>> quickSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        PagedResponse<Employee> result = employeeService.quickSearch(
            name, email, departmentId, status, page, size);
        return ResponseEntity.ok(result);
    }
    
    /**
     * GET /api/employees/global-search - Global text search across multiple fields
     */
    @GetMapping("/global-search")
    public ResponseEntity<PagedResponse<Employee>> globalSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        PagedResponse<Employee> result = employeeService.globalSearch(q, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }
    
    /**
     * GET /api/employees/department/{departmentId} - Get employees by department with pagination
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<PagedResponse<Employee>> getEmployeesByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        PagedResponse<Employee> result = employeeService.getEmployeesByDepartment(
            departmentId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }
    
    /**
     * GET /api/employees/salary-range - Get employees by salary range
     */
    @GetMapping("/salary-range")
    public ResponseEntity<List<Employee>> getEmployeesBySalaryRange(
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary) {
        
        List<Employee> employees = employeeService.getEmployeesBySalaryRange(minSalary, maxSalary);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * GET /api/employees/hired-between - Get employees hired within date range
     */
    @GetMapping("/hired-between")
    public ResponseEntity<List<Employee>> getEmployeesHiredBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Employee> employees = employeeService.getEmployeesHiredBetween(startDate, endDate);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * GET /api/employees/statistics - Get employee statistics and analytics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEmployeeStatistics() {
        Map<String, Object> statistics = employeeService.getEmployeeStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // ========== SPECIALIZED SEARCH ENDPOINTS ==========
    
    /**
     * GET /api/employees/recent/created - Get recently created employees
     */
    @GetMapping("/recent/created")
    public ResponseEntity<List<Employee>> getRecentlyCreatedEmployees(
            @RequestParam(defaultValue = "7") int days) {
        
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        // Note: This would require adding the method to the repository
        return ResponseEntity.ok(List.of()); // Placeholder
    }
    
    /**
     * GET /api/employees/by-status/{status} - Get employees by status with pagination
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Employee>> getEmployeesByStatus(@PathVariable EmployeeStatus status) {
        List<Employee> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * GET /api/employees/without-manager - Get employees without a manager
     */
    @GetMapping("/without-manager")
    public ResponseEntity<List<Employee>> getEmployeesWithoutManager() {
        // This would use the repository method findEmployeesWithoutManager()
        return ResponseEntity.ok(List.of()); // Placeholder
    }
    
    /**
     * GET /api/employees/by-location - Get employees by location (city/state)
     */
    @GetMapping("/by-location")
    public ResponseEntity<List<Employee>> getEmployeesByLocation(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode) {
        
        // This would use location-based repository methods
        return ResponseEntity.ok(List.of()); // Placeholder
    }
    
    /**
     * GET /api/employees/export - Export employees data
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportEmployees(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status) {
        
        // This would implement export functionality
        return ResponseEntity.ok("Export functionality to be implemented");
    }
}