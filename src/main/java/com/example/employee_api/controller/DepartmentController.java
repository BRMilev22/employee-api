package com.example.employee_api.controller;

import com.example.employee_api.model.Department;
import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {
    
    private final DepartmentService departmentService;
    
    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }
    
    /**
     * GET /api/departments - Get all departments
     */
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }
    
    /**
     * GET /api/departments/{id} - Get department by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }
    
    /**
     * GET /api/departments/code/{code} - Get department by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Department> getDepartmentByCode(@PathVariable String code) {
        Department department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(department);
    }
    
    /**
     * POST /api/departments - Create a new department
     */
    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }
    
    /**
     * PUT /api/departments/{id} - Update an existing department
     */
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, 
                                                     @Valid @RequestBody Department departmentDetails) {
        Department updatedDepartment = departmentService.updateDepartment(id, departmentDetails);
        return ResponseEntity.ok(updatedDepartment);
    }
    
    /**
     * DELETE /api/departments/{id} - Delete a department
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * PUT /api/departments/{id}/manager/{managerId} - Assign manager to department
     */
    @PutMapping("/{id}/manager/{managerId}")
    public ResponseEntity<Department> assignManager(@PathVariable Long id, @PathVariable Long managerId) {
        Department department = departmentService.assignManager(id, managerId);
        return ResponseEntity.ok(department);
    }
    
    /**
     * DELETE /api/departments/{id}/manager - Remove manager from department
     */
    @DeleteMapping("/{id}/manager")
    public ResponseEntity<Department> removeManager(@PathVariable Long id) {
        Department department = departmentService.removeManager(id);
        return ResponseEntity.ok(department);
    }
    
    /**
     * PUT /api/departments/{id}/parent/{parentId} - Set parent department
     */
    @PutMapping("/{id}/parent/{parentId}")
    public ResponseEntity<Department> setParentDepartment(@PathVariable Long id, @PathVariable Long parentId) {
        Department department = departmentService.setParentDepartment(id, parentId);
        return ResponseEntity.ok(department);
    }
    
    /**
     * DELETE /api/departments/{id}/parent - Remove parent department (make it root)
     */
    @DeleteMapping("/{id}/parent")
    public ResponseEntity<Department> removeParentDepartment(@PathVariable Long id) {
        Department department = departmentService.setParentDepartment(id, null);
        return ResponseEntity.ok(department);
    }
    
    /**
     * GET /api/departments/tree - Get department hierarchy tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<Department>> getDepartmentTree() {
        List<Department> rootDepartments = departmentService.getRootDepartments();
        return ResponseEntity.ok(rootDepartments);
    }
    
    /**
     * GET /api/departments/{id}/subdepartments - Get sub-departments
     */
    @GetMapping("/{id}/subdepartments")
    public ResponseEntity<List<Department>> getSubDepartments(@PathVariable Long id) {
        List<Department> subDepartments = departmentService.getSubDepartments(id);
        return ResponseEntity.ok(subDepartments);
    }
    
    /**
     * GET /api/departments/status/{status} - Get departments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Department>> getDepartmentsByStatus(@PathVariable DepartmentStatus status) {
        List<Department> departments = departmentService.getDepartmentsByStatus(status);
        return ResponseEntity.ok(departments);
    }
    
    /**
     * GET /api/departments/search - Search departments by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<Department>> searchDepartments(@RequestParam String query) {
        List<Department> departments = departmentService.searchDepartmentsByName(query);
        return ResponseEntity.ok(departments);
    }
    
    /**
     * GET /api/departments/location/{location} - Get departments by location
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Department>> getDepartmentsByLocation(@PathVariable String location) {
        List<Department> departments = departmentService.getDepartmentsByLocation(location);
        return ResponseEntity.ok(departments);
    }
    
    /**
     * GET /api/departments/budget - Get departments by budget range
     */
    @GetMapping("/budget")
    public ResponseEntity<List<Department>> getDepartmentsByBudget(
            @RequestParam BigDecimal minBudget, 
            @RequestParam BigDecimal maxBudget) {
        List<Department> departments = departmentService.getDepartmentsByBudgetRange(minBudget, maxBudget);
        return ResponseEntity.ok(departments);
    }
    
    /**
     * GET /api/departments/without-manager - Get departments without manager
     */
    @GetMapping("/without-manager")
    public ResponseEntity<List<Department>> getDepartmentsWithoutManager() {
        List<Department> departments = departmentService.getDepartmentsWithoutManager();
        return ResponseEntity.ok(departments);
    }
    
    /**
     * PUT /api/departments/{departmentId}/employees/{employeeId} - Transfer employee to department
     */
    @PutMapping("/{departmentId}/employees/{employeeId}")
    public ResponseEntity<Void> transferEmployeeToDepartment(@PathVariable Long departmentId, 
                                                           @PathVariable Long employeeId) {
        departmentService.transferEmployeeToDepartment(employeeId, departmentId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/departments/{id}/employee-count - Get employee count for department
     */
    @GetMapping("/{id}/employee-count")
    public ResponseEntity<Long> getEmployeeCount(@PathVariable Long id) {
        Long count = departmentService.getEmployeeCount(id);
        return ResponseEntity.ok(count);
    }
}