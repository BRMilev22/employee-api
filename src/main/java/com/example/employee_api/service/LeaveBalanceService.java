package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.LeaveBalance;
import com.example.employee_api.model.LeaveType;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.LeaveBalanceRepository;
import com.example.employee_api.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing employee leave balances
 */
@Service
@Transactional
public class LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    /**
     * Get all leave balances with pagination
     */
    @Transactional(readOnly = true)
    public Page<LeaveBalance> getAllLeaveBalances(Pageable pageable) {
        return leaveBalanceRepository.findAll(pageable);
    }

    /**
     * Get leave balance by ID
     */
    @Transactional(readOnly = true)
    public Optional<LeaveBalance> getLeaveBalanceById(Long id) {
        return leaveBalanceRepository.findById(id);
    }

    /**
     * Get leave balances for a specific employee
     */
    @Transactional(readOnly = true)
    public List<LeaveBalance> getLeaveBalancesByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        return leaveBalanceRepository.findByEmployee(employee);
    }

    /**
     * Get leave balances for a specific employee and year
     */
    @Transactional(readOnly = true)
    public List<LeaveBalance> getLeaveBalancesByEmployeeAndYear(Long employeeId, Integer year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        return leaveBalanceRepository.findByEmployeeAndYear(employee, year);
    }

    /**
     * Get leave balance for a specific employee, leave type, and year
     */
    @Transactional(readOnly = true)
    public Optional<LeaveBalance> getLeaveBalance(Long employeeId, Long leaveTypeId, Integer year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + leaveTypeId));
        return leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(employee, leaveType, year);
    }

    /**
     * Get current year leave balances for an employee
     */
    @Transactional(readOnly = true)
    public List<LeaveBalance> getCurrentYearLeaveBalances(Long employeeId) {
        int currentYear = LocalDate.now().getYear();
        return getLeaveBalancesByEmployeeAndYear(employeeId, currentYear);
    }

    /**
     * Create or update leave balance
     */
    public LeaveBalance saveLeaveBalance(LeaveBalance leaveBalance) {
        leaveBalance.calculateRemainingDays();
        return leaveBalanceRepository.save(leaveBalance);
    }

    /**
     * Initialize leave balances for an employee for a specific year
     * This creates balances for all active leave types based on their default allocations
     */
    public List<LeaveBalance> initializeLeaveBalancesForYear(Long employeeId, Integer year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        
        List<LeaveType> activeLeaveTypes = leaveTypeRepository.findByActiveTrue();
        
        return activeLeaveTypes.stream()
                .map(leaveType -> {
                    // Check if balance already exists
                    Optional<LeaveBalance> existingBalance = leaveBalanceRepository
                            .findByEmployeeAndLeaveTypeAndYear(employee, leaveType, year);
                    
                    if (existingBalance.isPresent()) {
                        return existingBalance.get();
                    }
                    
                    // Create new balance
                    LeaveBalance newBalance = new LeaveBalance(employee, leaveType, year, 
                            leaveType.getDaysAllowed().doubleValue());
                    
                    // Add carry forward days if applicable
                    if (leaveType.getCarryForward() && year > employee.getHireDate().getYear()) {
                        Double carryForwardDays = calculateCarryForwardDays(employee, leaveType, year - 1);
                        newBalance.setCarryForwardDays(carryForwardDays);
                        newBalance.calculateRemainingDays();
                    }
                    
                    return leaveBalanceRepository.save(newBalance);
                })
                .toList();
    }

    /**
     * Calculate carry forward days for an employee from previous year
     */
    public Double calculateCarryForwardDays(Employee employee, LeaveType leaveType, Integer previousYear) {
        if (!leaveType.getCarryForward()) {
            return 0.0;
        }
        
        Optional<LeaveBalance> previousBalance = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYear(employee, leaveType, previousYear);
        
        if (previousBalance.isEmpty()) {
            return 0.0;
        }
        
        Double remainingDays = previousBalance.get().getRemainingDays();
        if (remainingDays == null || remainingDays <= 0) {
            return 0.0;
        }
        
        // Apply maximum carry forward limit if set
        Integer maxCarryForward = leaveType.getMaxCarryForwardDays();
        if (maxCarryForward != null && maxCarryForward > 0) {
            return Math.min(remainingDays, maxCarryForward.doubleValue());
        }
        
        return remainingDays;
    }

    /**
     * Update leave balance when a leave request is approved
     */
    public LeaveBalance updateBalanceForApprovedLeave(Long employeeId, Long leaveTypeId, 
                                                     Integer year, Double daysToDeduct) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + leaveTypeId));
        
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(employee, leaveType, year)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for employee, leave type, and year"));
        
        // Deduct from pending days and add to used days
        balance.setPendingDays(balance.getPendingDays() - daysToDeduct);
        balance.setUsedDays(balance.getUsedDays() + daysToDeduct);
        balance.calculateRemainingDays();
        
        return leaveBalanceRepository.save(balance);
    }

    /**
     * Update leave balance when a leave request is pending approval
     */
    public LeaveBalance updateBalanceForPendingLeave(Long employeeId, Long leaveTypeId, 
                                                    Integer year, Double daysToReserve) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + leaveTypeId));
        
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(employee, leaveType, year)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for employee, leave type, and year"));
        
        // Add to pending days
        balance.setPendingDays(balance.getPendingDays() + daysToReserve);
        balance.calculateRemainingDays();
        
        return leaveBalanceRepository.save(balance);
    }

    /**
     * Update leave balance when a leave request is rejected or cancelled
     */
    public LeaveBalance updateBalanceForRejectedOrCancelledLeave(Long employeeId, Long leaveTypeId, 
                                                               Integer year, Double daysToRestore) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + leaveTypeId));
        
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(employee, leaveType, year)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for employee, leave type, and year"));
        
        // Remove from pending days
        balance.setPendingDays(balance.getPendingDays() - daysToRestore);
        balance.calculateRemainingDays();
        
        return leaveBalanceRepository.save(balance);
    }

    /**
     * Check if employee has sufficient leave balance for a request
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(Long employeeId, Long leaveTypeId, Integer year, Double daysRequested) {
        Optional<LeaveBalance> balanceOpt = getLeaveBalance(employeeId, leaveTypeId, year);
        
        if (balanceOpt.isEmpty()) {
            return false;
        }
        
        LeaveBalance balance = balanceOpt.get();
        Double availableDays = balance.getAllocatedDays() + balance.getCarryForwardDays() 
                             - balance.getUsedDays() - balance.getPendingDays();
        
        return availableDays >= daysRequested;
    }

    /**
     * Get leave balance statistics for an employee
     */
    @Transactional(readOnly = true)
    public LeaveBalanceStatistics getLeaveBalanceStatistics(Long employeeId, Integer year) {
        List<LeaveBalance> balances = getLeaveBalancesByEmployeeAndYear(employeeId, year);
        
        Double totalAllocated = balances.stream()
                .mapToDouble(balance -> balance.getAllocatedDays() + balance.getCarryForwardDays())
                .sum();
        
        Double totalUsed = balances.stream()
                .mapToDouble(LeaveBalance::getUsedDays)
                .sum();
        
        Double totalPending = balances.stream()
                .mapToDouble(LeaveBalance::getPendingDays)
                .sum();
        
        Double totalRemaining = balances.stream()
                .mapToDouble(LeaveBalance::getRemainingDays)
                .sum();
        
        return new LeaveBalanceStatistics(totalAllocated, totalUsed, totalPending, totalRemaining, balances.size());
    }

    /**
     * Delete a leave balance
     */
    public void deleteLeaveBalance(Long id) {
        leaveBalanceRepository.deleteById(id);
    }

    /**
     * Get leave balances that are expiring soon (for notifications)
     */
    @Transactional(readOnly = true)
    public List<LeaveBalance> getExpiringLeaveBalances(Integer year) {
        return leaveBalanceRepository.findExpiringBalances(year);
    }

    /**
     * Inner class for leave balance statistics
     */
    public static class LeaveBalanceStatistics {
        private final Double totalAllocated;
        private final Double totalUsed;
        private final Double totalPending;
        private final Double totalRemaining;
        private final Integer totalLeaveTypes;

        public LeaveBalanceStatistics(Double totalAllocated, Double totalUsed, Double totalPending, 
                                    Double totalRemaining, Integer totalLeaveTypes) {
            this.totalAllocated = totalAllocated;
            this.totalUsed = totalUsed;
            this.totalPending = totalPending;
            this.totalRemaining = totalRemaining;
            this.totalLeaveTypes = totalLeaveTypes;
        }

        // Getters
        public Double getTotalAllocated() { return totalAllocated; }
        public Double getTotalUsed() { return totalUsed; }
        public Double getTotalPending() { return totalPending; }
        public Double getTotalRemaining() { return totalRemaining; }
        public Integer getTotalLeaveTypes() { return totalLeaveTypes; }
    }
}