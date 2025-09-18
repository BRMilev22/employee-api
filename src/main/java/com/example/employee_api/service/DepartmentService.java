package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Department;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.repository.DepartmentRepository;
import com.example.employee_api.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }
    
    /**
     * Get all departments
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    /**
     * Get department by ID
     */
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Department not found with id: " + id));
    }
    
    /**
     * Get department by code
     */
    @Transactional(readOnly = true)
    public Department getDepartmentByCode(String departmentCode) {
        return departmentRepository.findByDepartmentCode(departmentCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Department not found with code: " + departmentCode));
    }
    
    /**
     * Create a new department
     */
    public Department createDepartment(Department department) {
        // Validate input
        if (department.getDepartmentCode() == null || department.getDepartmentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Department code cannot be empty");
        }
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name cannot be empty");
        }
        
        // Check if department code already exists
        if (departmentRepository.existsByDepartmentCode(department.getDepartmentCode())) {
            throw new IllegalArgumentException("Department with code '" + department.getDepartmentCode() + "' already exists");
        }
        
        // Set default status if not provided
        if (department.getStatus() == null) {
            department.setStatus(DepartmentStatus.ACTIVE);
        }
        
        return departmentRepository.save(department);
    }
    
    /**
     * Update an existing department
     */
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = getDepartmentById(id);
        
        // Update fields if provided
        if (departmentDetails.getName() != null && !departmentDetails.getName().trim().isEmpty()) {
            department.setName(departmentDetails.getName());
        }
        if (departmentDetails.getDescription() != null) {
            department.setDescription(departmentDetails.getDescription());
        }
        if (departmentDetails.getLocation() != null) {
            department.setLocation(departmentDetails.getLocation());
        }
        if (departmentDetails.getBudget() != null) {
            department.setBudget(departmentDetails.getBudget());
        }
        if (departmentDetails.getStatus() != null) {
            department.setStatus(departmentDetails.getStatus());
        }
        if (departmentDetails.getCostCenter() != null) {
            department.setCostCenter(departmentDetails.getCostCenter());
        }
        if (departmentDetails.getEmail() != null) {
            department.setEmail(departmentDetails.getEmail());
        }
        if (departmentDetails.getPhone() != null) {
            department.setPhone(departmentDetails.getPhone());
        }
        
        return departmentRepository.save(department);
    }
    
    /**
     * Delete a department (soft delete by setting status to DISSOLVED)
     */
    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        
        // Check if department has employees
        if (department.hasEmployees()) {
            throw new IllegalArgumentException("Cannot delete department with active employees. Please transfer employees first.");
        }
        
        // Check if department has sub-departments
        if (department.hasSubDepartments()) {
            throw new IllegalArgumentException("Cannot delete department with sub-departments. Please restructure first.");
        }
        
        department.setStatus(DepartmentStatus.DISSOLVED);
        departmentRepository.save(department);
    }
    
    /**
     * Permanently delete a department from database
     */
    public void permanentlyDeleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        
        if (department.hasEmployees() || department.hasSubDepartments()) {
            throw new IllegalArgumentException("Cannot permanently delete department with employees or sub-departments");
        }
        
        departmentRepository.delete(department);
    }
    
    /**
     * Assign manager to department
     */
    public Department assignManager(Long departmentId, Long managerId) {
        Department department = getDepartmentById(departmentId);
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new EmployeeNotFoundException("Manager not found with id: " + managerId));
        
        department.setManager(manager);
        return departmentRepository.save(department);
    }
    
    /**
     * Remove manager from department
     */
    public Department removeManager(Long departmentId) {
        Department department = getDepartmentById(departmentId);
        department.setManager(null);
        return departmentRepository.save(department);
    }
    
    /**
     * Set parent department (for hierarchy)
     */
    public Department setParentDepartment(Long departmentId, Long parentDepartmentId) {
        Department department = getDepartmentById(departmentId);
        
        if (parentDepartmentId != null) {
            Department parentDepartment = getDepartmentById(parentDepartmentId);
            
            // Prevent circular reference
            if (isCircularReference(department, parentDepartment)) {
                throw new IllegalArgumentException("Setting this parent would create a circular reference");
            }
            
            department.setParentDepartment(parentDepartment);
        } else {
            department.setParentDepartment(null);
        }
        
        return departmentRepository.save(department);
    }
    
    /**
     * Get root departments (departments with no parent)
     */
    @Transactional(readOnly = true)
    public List<Department> getRootDepartments() {
        return departmentRepository.findByParentDepartmentIsNull();
    }
    
    /**
     * Get sub-departments of a department
     */
    @Transactional(readOnly = true)
    public List<Department> getSubDepartments(Long parentDepartmentId) {
        Department parentDepartment = getDepartmentById(parentDepartmentId);
        return departmentRepository.findByParentDepartment(parentDepartment);
    }
    
    /**
     * Get departments by status
     */
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByStatus(DepartmentStatus status) {
        return departmentRepository.findByStatus(status);
    }
    
    /**
     * Search departments by name
     */
    @Transactional(readOnly = true)
    public List<Department> searchDepartmentsByName(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Get departments by location
     */
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByLocation(String location) {
        return departmentRepository.findByLocationContainingIgnoreCase(location);
    }
    
    /**
     * Get departments by budget range
     */
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByBudgetRange(BigDecimal minBudget, BigDecimal maxBudget) {
        return departmentRepository.findByBudgetBetween(minBudget, maxBudget);
    }
    
    /**
     * Get departments without manager
     */
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsWithoutManager() {
        return departmentRepository.findActiveDepartmentsWithoutManager();
    }
    
    /**
     * Transfer employee to department
     */
    public void transferEmployeeToDepartment(Long employeeId, Long departmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        Department department = getDepartmentById(departmentId);
        
        employee.setDepartment(department);
        employeeRepository.save(employee);
    }
    
    /**
     * Get employee count for department
     */
    @Transactional(readOnly = true)
    public Long getEmployeeCount(Long departmentId) {
        return departmentRepository.countEmployeesByDepartmentId(departmentId);
    }
    
    /**
     * Check if department exists by ID
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return departmentRepository.existsById(id);
    }
    
    /**
     * Check if department code exists
     */
    @Transactional(readOnly = true)
    public boolean existsByDepartmentCode(String departmentCode) {
        return departmentRepository.existsByDepartmentCode(departmentCode);
    }
    
    // Helper method to check for circular references in department hierarchy
    private boolean isCircularReference(Department department, Department potentialParent) {
        if (department.getId().equals(potentialParent.getId())) {
            return true;
        }
        
        Department currentParent = potentialParent.getParentDepartment();
        while (currentParent != null) {
            if (currentParent.getId().equals(department.getId())) {
                return true;
            }
            currentParent = currentParent.getParentDepartment();
        }
        
        return false;
    }
}