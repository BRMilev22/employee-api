package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Employee;
import com.example.employee_api.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing employee hierarchy relationships
 */
@Service
@Transactional
public class EmployeeHierarchyService {

    private final EmployeeRepository employeeRepository;

    public EmployeeHierarchyService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Assign a manager to an employee
     */
    public Employee assignManager(Long employeeId, Long managerId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new EmployeeNotFoundException("Manager not found with id: " + managerId));

        // Validate that this assignment won't create a circular reference
        if (wouldCreateCircularReference(managerId, employeeId)) {
            throw new IllegalArgumentException("Cannot assign manager: This would create a circular reference in the hierarchy");
        }

        // Validate that an employee cannot be their own manager
        if (employeeId.equals(managerId)) {
            throw new IllegalArgumentException("An employee cannot be their own manager");
        }

        employee.setManager(manager);
        Employee savedEmployee = employeeRepository.save(employee);
        return savedEmployee;
    }

    /**
     * Remove the manager assignment from an employee
     */
    public Employee removeManager(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        employee.setManager(null);
        Employee savedEmployee = employeeRepository.save(employee);
        return savedEmployee;
    }

    /**
     * Get all direct subordinates of a manager
     */
    @Transactional(readOnly = true)
    public List<Employee> getSubordinates(Long managerId) {
        // Validate that the manager exists
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new EmployeeNotFoundException("Manager not found with id: " + managerId));

        return employeeRepository.findByManagerId(managerId);
    }

    /**
     * Get the complete reporting chain for an employee (all managers up to top level)
     */
    @Transactional(readOnly = true)
    public List<Employee> getReportingChain(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        List<Employee> reportingChain = new ArrayList<>();
        Employee currentManager = employee.getManager();
        
        // Traverse up the hierarchy to build reporting chain
        while (currentManager != null) {
            reportingChain.add(currentManager);
            currentManager = currentManager.getManager();
        }

        return reportingChain;
    }

    /**
     * Get the organizational chart starting from top-level employees
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrganizationalChart() {
        List<Employee> topLevelEmployees = employeeRepository.findTopLevelEmployees();
        
        Map<String, Object> orgChart = new HashMap<>();
        orgChart.put("topLevel", topLevelEmployees.stream()
                .map(this::buildEmployeeHierarchy)
                .collect(Collectors.toList()));
        
        return orgChart;
    }

    /**
     * Get all managers in the organization
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllManagers() {
        return employeeRepository.findAllManagers();
    }

    /**
     * Get organizational statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHierarchyStatistics() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> managers = employeeRepository.findAllManagers();
        List<Employee> topLevel = employeeRepository.findTopLevelEmployees();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", allEmployees.size());
        stats.put("totalManagers", managers.size());
        stats.put("topLevelEmployees", topLevel.size());
        stats.put("employeesWithManagers", allEmployees.size() - topLevel.size());
        
        // Calculate average span of control
        if (!managers.isEmpty()) {
            double avgSpanOfControl = managers.stream()
                    .mapToInt(manager -> employeeRepository.findByManagerId(manager.getId()).size())
                    .average()
                    .orElse(0.0);
            stats.put("averageSpanOfControl", Math.round(avgSpanOfControl * 100.0) / 100.0);
        } else {
            stats.put("averageSpanOfControl", 0.0);
        }

        return stats;
    }

    /**
     * Check if assigning a manager would create a circular reference
     * This uses a programmatic approach that's database-agnostic
     */
    private boolean wouldCreateCircularReference(Long managerId, Long subordinateId) {
        // If manager and subordinate are the same, it's circular
        if (managerId.equals(subordinateId)) {
            return true;
        }
        
        // Check if subordinateId is in the reporting chain of managerId
        // If the potential subordinate is already above the potential manager, it would create a cycle
        return isInReportingChain(managerId, subordinateId);
    }
    
    /**
     * Check if targetId is in the reporting chain of employeeId
     */
    private boolean isInReportingChain(Long employeeId, Long targetId) {
        Employee current = employeeRepository.findById(employeeId).orElse(null);
        
        // Track visited employees to prevent infinite loops in case of existing cycles
        java.util.Set<Long> visited = new java.util.HashSet<>();
        
        while (current != null && current.getManager() != null) {
            Long currentManagerId = current.getManager().getId();
            
            // Prevent infinite loops
            if (visited.contains(currentManagerId)) {
                break;
            }
            visited.add(currentManagerId);
            
            // If we found the target in the reporting chain, return true
            if (currentManagerId.equals(targetId)) {
                return true;
            }
            
            current = current.getManager();
        }
        
        return false;
    }

    /**
     * Recursively build employee hierarchy for organizational chart
     */
    private Map<String, Object> buildEmployeeHierarchy(Employee employee) {
        Map<String, Object> node = new HashMap<>();
        node.put("employee", employee);
        
        List<Employee> subordinates = employeeRepository.findByManagerId(employee.getId());
        if (!subordinates.isEmpty()) {
            node.put("subordinates", subordinates.stream()
                    .map(this::buildEmployeeHierarchy)
                    .collect(Collectors.toList()));
        }
        
        return node;
    }
}