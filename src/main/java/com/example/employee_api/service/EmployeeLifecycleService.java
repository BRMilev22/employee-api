package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.EmployeeStatusHistory;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.EmployeeStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing employee lifecycle and status changes
 */
@Service
@Transactional
public class EmployeeLifecycleService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusHistoryRepository statusHistoryRepository;

    public EmployeeLifecycleService(EmployeeRepository employeeRepository,
                                  EmployeeStatusHistoryRepository statusHistoryRepository) {
        this.employeeRepository = employeeRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    /**
     * Activate an employee
     */
    public Employee activateEmployee(Long employeeId, String reason) {
        Employee employee = findEmployeeById(employeeId);
        
        if (employee.getStatus() == EmployeeStatus.ACTIVE) {
            throw new IllegalArgumentException("Employee is already active");
        }

        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException("Cannot activate a terminated employee. Use rehire process instead.");
        }

        return changeEmployeeStatus(employee, EmployeeStatus.ACTIVE, reason);
    }

    /**
     * Deactivate an employee
     */
    public Employee deactivateEmployee(Long employeeId, String reason) {
        Employee employee = findEmployeeById(employeeId);
        
        if (employee.getStatus() == EmployeeStatus.INACTIVE) {
            throw new IllegalArgumentException("Employee is already inactive");
        }

        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException("Cannot deactivate a terminated employee");
        }

        return changeEmployeeStatus(employee, EmployeeStatus.INACTIVE, reason);
    }

    /**
     * Terminate an employee
     */
    public Employee terminateEmployee(Long employeeId, String reason, LocalDate terminationDate) {
        Employee employee = findEmployeeById(employeeId);
        
        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException("Employee is already terminated");
        }

        // Set termination date if provided
        if (terminationDate != null) {
            employee.setTerminationDate(terminationDate);
        } else {
            employee.setTerminationDate(LocalDate.now());
        }

        return changeEmployeeStatus(employee, EmployeeStatus.TERMINATED, reason);
    }

    /**
     * Start onboarding process for a new employee
     */
    public Employee onboardEmployee(Long employeeId, String reason) {
        Employee employee = findEmployeeById(employeeId);
        
        // Onboarding typically puts employee in PROBATION status first
        if (employee.getStatus() == EmployeeStatus.PROBATION) {
            throw new IllegalArgumentException("Employee is already in onboarding (probation) status");
        }

        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException("Cannot onboard a terminated employee");
        }

        // Set hire date if not already set
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }

        return changeEmployeeStatus(employee, EmployeeStatus.PROBATION, reason != null ? reason : "Employee onboarding started");
    }

    /**
     * Start offboarding process for an employee
     */
    public Employee offboardEmployee(Long employeeId, String reason, LocalDate lastWorkingDay) {
        Employee employee = findEmployeeById(employeeId);
        
        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException("Employee is already terminated");
        }

        // Set termination date for tracking
        if (lastWorkingDay != null) {
            employee.setTerminationDate(lastWorkingDay);
        }

        // First deactivate, then will terminate later
        return changeEmployeeStatus(employee, EmployeeStatus.INACTIVE, reason != null ? reason : "Employee offboarding started");
    }

    /**
     * Get status history for an employee
     */
    @Transactional(readOnly = true)
    public List<EmployeeStatusHistory> getEmployeeStatusHistory(Long employeeId) {
        Employee employee = findEmployeeById(employeeId);
        return statusHistoryRepository.findByEmployeeOrderByChangedAtDesc(employee);
    }

    /**
     * Get status history for an employee with pagination
     */
    @Transactional(readOnly = true)
    public Page<EmployeeStatusHistory> getEmployeeStatusHistory(Long employeeId, Pageable pageable) {
        Employee employee = findEmployeeById(employeeId);
        return statusHistoryRepository.findByEmployeeOrderByChangedAtDesc(employee, pageable);
    }

    /**
     * Get employees activated in a date range
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesActivatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return statusHistoryRepository.findEmployeesActivatedBetween(startDate, endDate);
    }

    /**
     * Get employees terminated in a date range
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesTerminatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return statusHistoryRepository.findEmployeesTerminatedBetween(startDate, endDate);
    }

    /**
     * Get status change statistics
     */
    @Transactional(readOnly = true)
    public StatusChangeStatistics getStatusChangeStatistics() {
        long totalChanges = statusHistoryRepository.count();
        long activations = statusHistoryRepository.countByNewStatus(EmployeeStatus.ACTIVE);
        long deactivations = statusHistoryRepository.countByNewStatus(EmployeeStatus.INACTIVE);
        long terminations = statusHistoryRepository.countByNewStatus(EmployeeStatus.TERMINATED);
        long onboardings = statusHistoryRepository.countByNewStatus(EmployeeStatus.PROBATION);

        return new StatusChangeStatistics(totalChanges, activations, deactivations, terminations, onboardings);
    }

    /**
     * Change employee status and record the change
     */
    private Employee changeEmployeeStatus(Employee employee, EmployeeStatus newStatus, String reason) {
        EmployeeStatus previousStatus = employee.getStatus();
        
        // Update employee status
        employee.setStatus(newStatus);
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Record status change in history
        String changedBy = getCurrentUser();
        EmployeeStatusHistory statusHistory = new EmployeeStatusHistory(
            employee, previousStatus, newStatus, reason, changedBy
        );
        statusHistoryRepository.save(statusHistory);
        
        return savedEmployee;
    }

    /**
     * Find employee by ID or throw exception
     */
    private Employee findEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
    }

    /**
     * Get current authenticated user
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    /**
     * Inner class for status change statistics
     */
    public static class StatusChangeStatistics {
        private final long totalChanges;
        private final long activations;
        private final long deactivations;
        private final long terminations;
        private final long onboardings;

        public StatusChangeStatistics(long totalChanges, long activations, long deactivations, 
                                    long terminations, long onboardings) {
            this.totalChanges = totalChanges;
            this.activations = activations;
            this.deactivations = deactivations;
            this.terminations = terminations;
            this.onboardings = onboardings;
        }

        // Getters
        public long getTotalChanges() { return totalChanges; }
        public long getActivations() { return activations; }
        public long getDeactivations() { return deactivations; }
        public long getTerminations() { return terminations; }
        public long getOnboardings() { return onboardings; }
    }
}