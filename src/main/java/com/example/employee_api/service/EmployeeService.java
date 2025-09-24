package com.example.employee_api.service;

import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.dto.search.EmployeeSearchCriteria;
import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.specification.EmployeeSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    /**
     * Get all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    /**
     * Get employee by ID
     */
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
    }
    
    /**
     * Create a new employee
     */
    public Employee createEmployee(Employee employee) {
        // Validate input
        if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee first name cannot be empty");
        }
        if (employee.getLastName() == null || employee.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee last name cannot be empty");
        }
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee email cannot be empty");
        }
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Update an existing employee
     */
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        
        // Update fields if provided
        if (employeeDetails.getFirstName() != null && !employeeDetails.getFirstName().trim().isEmpty()) {
            employee.setFirstName(employeeDetails.getFirstName());
        }
        if (employeeDetails.getLastName() != null && !employeeDetails.getLastName().trim().isEmpty()) {
            employee.setLastName(employeeDetails.getLastName());
        }
        if (employeeDetails.getEmail() != null && !employeeDetails.getEmail().trim().isEmpty()) {
            employee.setEmail(employeeDetails.getEmail());
        }
        if (employeeDetails.getJobTitle() != null && !employeeDetails.getJobTitle().trim().isEmpty()) {
            employee.setJobTitle(employeeDetails.getJobTitle());
        }
        if (employeeDetails.getPhone() != null) {
            employee.setPhone(employeeDetails.getPhone());
        }
        if (employeeDetails.getAddress() != null) {
            employee.setAddress(employeeDetails.getAddress());
        }
        if (employeeDetails.getSalary() != null) {
            employee.setSalary(employeeDetails.getSalary());
        }
        if (employeeDetails.getHireDate() != null) {
            employee.setHireDate(employeeDetails.getHireDate());
        }
        if (employeeDetails.getStatus() != null) {
            employee.setStatus(employeeDetails.getStatus());
        }
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Delete an employee
     */
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
    
    /**
     * Check if employee exists by ID
     */
    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }
    
    /**
     * Get employees by job title
     */
    public List<Employee> getEmployeesByJobTitle(String jobTitle) {
        return employeeRepository.findByJobTitle(jobTitle);
    }
    
    /**
     * Get employees by status
     */
    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status);
    }
    
    /**
     * Advanced search with comprehensive filtering, pagination, and sorting
     */
    @Transactional(readOnly = true)
    public PagedResponse<Employee> searchEmployees(EmployeeSearchCriteria criteria) {
        // Build specifications
        Specification<Employee> spec = buildSpecification(criteria);
        
        // Create Pageable with sorting
        Sort sort = createSort(criteria.getSortBy(), criteria.getSortDirection());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        // Execute search
        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        
        // Return wrapped response
        return PagedResponse.of(page, criteria.getSortBy(), criteria.getSortDirection());
    }
    
    /**
     * Global search across multiple fields
     */
    @Transactional(readOnly = true)
    public PagedResponse<Employee> globalSearch(String searchTerm, int page, int size, 
                                               String sortBy, String sortDirection) {
        Specification<Employee> spec = EmployeeSpecifications.globalSearch(searchTerm);
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Employee> resultPage = employeeRepository.findAll(spec, pageable);
        return PagedResponse.of(resultPage, sortBy, sortDirection);
    }
    
    /**
     * Quick search with basic filters
     */
    @Transactional(readOnly = true)
    public PagedResponse<Employee> quickSearch(String name, String email, Long departmentId, 
                                             EmployeeStatus status, int page, int size) {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setFullName(name);
        criteria.setEmail(email);
        criteria.setDepartmentId(departmentId);
        criteria.setStatus(status);
        criteria.setPage(page);
        criteria.setSize(size);
        
        return searchEmployees(criteria);
    }
    
    /**
     * Get employees by department with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<Employee> getEmployeesByDepartment(Long departmentId, int page, int size, 
                                                           String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Employee> resultPage = employeeRepository.findByDepartmentId(departmentId, pageable);
        return PagedResponse.of(resultPage, sortBy, sortDirection);
    }
    
    /**
     * Get employees by salary range
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        return employeeRepository.findBySalaryRange(minSalary, maxSalary);
    }
    
    /**
     * Get employees hired within date range
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesHiredBetween(LocalDate startDate, LocalDate endDate) {
        return employeeRepository.findByHireDateBetween(startDate, endDate);
    }
    
    /**
     * Get employee statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("activeEmployees", employeeRepository.countByStatus(EmployeeStatus.ACTIVE));
        stats.put("inactiveEmployees", employeeRepository.countByStatus(EmployeeStatus.INACTIVE));
        
        // Department distribution
        List<Object[]> departmentStats = employeeRepository.countEmployeesByDepartment();
        Map<String, Long> departmentDistribution = new java.util.HashMap<>();
        for (Object[] stat : departmentStats) {
            departmentDistribution.put((String) stat[0], (Long) stat[1]);
        }
        stats.put("departmentDistribution", departmentDistribution);
        
        // Employment type distribution
        List<Object[]> employmentStats = employeeRepository.countEmployeesByEmploymentType();
        Map<String, Long> employmentTypeDistribution = new java.util.HashMap<>();
        for (Object[] stat : employmentStats) {
            employmentTypeDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("employmentTypeDistribution", employmentTypeDistribution);
        
        return stats;
    }
    
    /**
     * Build dynamic specification from search criteria
     */
    private Specification<Employee> buildSpecification(EmployeeSearchCriteria criteria) {
        Specification<Employee> spec = Specification.where(null);
        
        // Global search takes precedence
        if (criteria.getGlobalSearch() != null && !criteria.getGlobalSearch().trim().isEmpty()) {
            return EmployeeSpecifications.globalSearch(criteria.getGlobalSearch());
        }
        
        // Individual field searches
        spec = spec.and(EmployeeSpecifications.hasFirstName(criteria.getFirstName()));
        spec = spec.and(EmployeeSpecifications.hasLastName(criteria.getLastName()));
        spec = spec.and(EmployeeSpecifications.hasFullName(criteria.getFullName()));
        spec = spec.and(EmployeeSpecifications.hasEmail(criteria.getEmail()));
        spec = spec.and(EmployeeSpecifications.hasEmployeeId(criteria.getEmployeeId()));
        spec = spec.and(EmployeeSpecifications.hasJobTitle(criteria.getJobTitle()));
        spec = spec.and(EmployeeSpecifications.hasPhone(criteria.getPhone()));
        spec = spec.and(EmployeeSpecifications.hasGender(criteria.getGender()));
        
        // Status and employment type
        spec = spec.and(EmployeeSpecifications.hasStatus(criteria.getStatus()));
        spec = spec.and(EmployeeSpecifications.hasEmploymentType(criteria.getEmploymentType()));
        
        // Department and position
        spec = spec.and(EmployeeSpecifications.belongsToDepartment(criteria.getDepartmentId()));
        spec = spec.and(EmployeeSpecifications.belongsToDepartmentName(criteria.getDepartmentName()));
        spec = spec.and(EmployeeSpecifications.hasCurrentPosition(criteria.getPositionId()));
        spec = spec.and(EmployeeSpecifications.hasCurrentPositionTitle(criteria.getPositionTitle()));
        
        // Manager relationships
        if (criteria.getHasManager() != null) {
            if (criteria.getHasManager()) {
                spec = spec.and(EmployeeSpecifications.hasManager(criteria.getManagerId()));
            } else {
                spec = spec.and(EmployeeSpecifications.hasNoManager());
            }
        } else if (criteria.getManagerId() != null) {
            spec = spec.and(EmployeeSpecifications.hasManager(criteria.getManagerId()));
        }
        
        // Salary range
        spec = spec.and(EmployeeSpecifications.hasSalaryBetween(criteria.getMinSalary(), criteria.getMaxSalary()));
        
        // Date ranges
        spec = spec.and(EmployeeSpecifications.hasHireDateBetween(criteria.getHireDateFrom(), criteria.getHireDateTo()));
        spec = spec.and(EmployeeSpecifications.hasBirthDateBetween(criteria.getBirthDateFrom(), criteria.getBirthDateTo()));
        
        // Age and years of service
        spec = spec.and(EmployeeSpecifications.hasAgeBetween(criteria.getMinAge(), criteria.getMaxAge()));
        spec = spec.and(EmployeeSpecifications.hasYearsOfServiceBetween(criteria.getMinYearsOfService(), criteria.getMaxYearsOfService()));
        
        // Location
        spec = spec.and(EmployeeSpecifications.livesInCity(criteria.getCity()));
        spec = spec.and(EmployeeSpecifications.livesInState(criteria.getState()));
        spec = spec.and(EmployeeSpecifications.hasPostalCode(criteria.getPostalCode()));
        
        return spec;
    }
    
    /**
     * Create Sort object from sort parameters
     */
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Validate sortBy field
        String validatedSortBy = validateSortField(sortBy);
        
        return Sort.by(direction, validatedSortBy);
    }
    
    /**
     * Validate and sanitize sort field name
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "lastName";
        }
        
        // List of allowed sort fields
        List<String> allowedFields = List.of(
            "id", "employeeId", "firstName", "lastName", "email", "jobTitle", 
            "salary", "hireDate", "birthDate", "status", "employmentType",
            "createdAt", "updatedAt", "phone", "city", "state", "postalCode"
        );
        
        if (allowedFields.contains(sortBy)) {
            return sortBy;
        }
        
        // Default to lastName if invalid field
        return "lastName";
    }
    
    // ========== NEW LOCATION AND MANAGER FILTERING METHODS ==========
    
    /**
     * Get employees by location (city/state)
     */
    public List<Employee> getEmployeesByLocation(String city, String state, String postalCode) {
        // Build specification for combined filtering
        Specification<Employee> spec = Specification.where(null);
        
        if (city != null && !city.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("city")),
                    city.trim().toLowerCase()
                )
            );
        }
        
        if (state != null && !state.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("state")),
                    state.trim().toLowerCase()
                )
            );
        }
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("postalCode"), postalCode.trim())
            );
        }
        
        return employeeRepository.findAll(spec);
    }
    
    /**
     * Get employees without manager
     */
    public List<Employee> getEmployeesWithoutManager(EmployeeStatus status) {
        if (status != null) {
            // Filter by status as well using Specification
            Specification<Employee> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                    criteriaBuilder.isNull(root.get("manager")),
                    criteriaBuilder.equal(root.get("status"), status)
                );
            return employeeRepository.findAll(spec);
        }
        return employeeRepository.findEmployeesWithoutManager();
    }
    
    /**
     * Export employees data in CSV format
     */
    public String exportEmployeesToCSV(String name, Long departmentId, EmployeeStatus status) {
        // Get employees based on filter criteria
        List<Employee> employees = getFilteredEmployeesForExport(name, departmentId, status);
        
        StringBuilder csvBuilder = new StringBuilder();
        
        // CSV Header
        csvBuilder.append("Employee ID,First Name,Last Name,Email,Job Title,Department,Status,Salary,Hire Date,Phone,City,State\n");
        
        // CSV Data
        for (Employee employee : employees) {
            csvBuilder.append(formatCSVRow(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getJobTitle(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : "",
                employee.getStatus().toString(),
                employee.getSalary() != null ? employee.getSalary().toString() : "",
                employee.getHireDate() != null ? employee.getHireDate().toString() : "",
                employee.getPhone() != null ? employee.getPhone() : "",
                employee.getCity() != null ? employee.getCity() : "",
                employee.getState() != null ? employee.getState() : ""
            )).append("\n");
        }
        
        return csvBuilder.toString();
    }
    
    /**
     * Get filtered employees for export
     */
    private List<Employee> getFilteredEmployeesForExport(String name, Long departmentId, EmployeeStatus status) {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        
        if (name != null && !name.trim().isEmpty()) {
            criteria.setFirstName(name.trim());
        }
        if (departmentId != null) {
            criteria.setDepartmentId(departmentId);
        }
        if (status != null) {
            criteria.setStatus(status);
        }
        
        // Use existing search functionality if we have criteria
        if (name != null || departmentId != null || status != null) {
            Specification<Employee> spec = buildSpecification(criteria);
            return employeeRepository.findAll(spec);
        }
        
        // Otherwise return all employees
        return employeeRepository.findAll();
    }
    
    /**
     * Format CSV row with proper escaping
     */
    private String formatCSVRow(String... values) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                row.append(",");
            }
            String value = values[i] != null ? values[i] : "";
            // Escape quotes and wrap in quotes if contains comma or quote
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            row.append(value);
        }
        return row.toString();
    }
}