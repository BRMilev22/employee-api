package com.example.employee_api.controller;

import com.example.employee_api.model.LeaveBalance;
import com.example.employee_api.service.LeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing leave balances
 */
@RestController
@RequestMapping("/api/leave-balances")
@CrossOrigin(origins = "*")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    /**
     * Get all leave balances with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveBalance>> getAllLeaveBalances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveBalance> leaveBalances = leaveBalanceService.getAllLeaveBalances(pageable);
        
        return ResponseEntity.ok(leaveBalances);
    }

    /**
     * Get leave balance by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @leaveBalanceController.canAccessLeaveBalance(#id, authentication.name)")
    public ResponseEntity<LeaveBalance> getLeaveBalanceById(@PathVariable Long id) {
        Optional<LeaveBalance> leaveBalance = leaveBalanceService.getLeaveBalanceById(id);
        
        if (leaveBalance.isPresent()) {
            return ResponseEntity.ok(leaveBalance.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get leave balances for a specific employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<List<LeaveBalance>> getLeaveBalancesByEmployee(@PathVariable Long employeeId) {
        List<LeaveBalance> leaveBalances = leaveBalanceService.getLeaveBalancesByEmployee(employeeId);
        return ResponseEntity.ok(leaveBalances);
    }

    /**
     * Get leave balances for a specific employee and year
     */
    @GetMapping("/employee/{employeeId}/year/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<List<LeaveBalance>> getLeaveBalancesByEmployeeAndYear(
            @PathVariable Long employeeId, 
            @PathVariable Integer year) {
        List<LeaveBalance> leaveBalances = leaveBalanceService.getLeaveBalancesByEmployeeAndYear(employeeId, year);
        return ResponseEntity.ok(leaveBalances);
    }

    /**
     * Get current year leave balances for an employee
     */
    @GetMapping("/employee/{employeeId}/current-year")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<List<LeaveBalance>> getCurrentYearLeaveBalances(@PathVariable Long employeeId) {
        List<LeaveBalance> leaveBalances = leaveBalanceService.getCurrentYearLeaveBalances(employeeId);
        return ResponseEntity.ok(leaveBalances);
    }

    /**
     * Get specific leave balance for employee, leave type, and year
     */
    @GetMapping("/employee/{employeeId}/leave-type/{leaveTypeId}/year/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<LeaveBalance> getLeaveBalance(
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId,
            @PathVariable Integer year) {
        Optional<LeaveBalance> leaveBalance = leaveBalanceService.getLeaveBalance(employeeId, leaveTypeId, year);
        
        if (leaveBalance.isPresent()) {
            return ResponseEntity.ok(leaveBalance.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create or update leave balance
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveBalance> createLeaveBalance(@Valid @RequestBody LeaveBalance leaveBalance) {
        LeaveBalance savedLeaveBalance = leaveBalanceService.saveLeaveBalance(leaveBalance);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLeaveBalance);
    }

    /**
     * Update leave balance
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveBalance> updateLeaveBalance(
            @PathVariable Long id, 
            @Valid @RequestBody LeaveBalance leaveBalance) {
        
        Optional<LeaveBalance> existingBalance = leaveBalanceService.getLeaveBalanceById(id);
        if (existingBalance.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        leaveBalance.setId(id);
        LeaveBalance updatedBalance = leaveBalanceService.saveLeaveBalance(leaveBalance);
        return ResponseEntity.ok(updatedBalance);
    }

    /**
     * Initialize leave balances for an employee for a specific year
     */
    @PostMapping("/employee/{employeeId}/year/{year}/initialize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<List<LeaveBalance>> initializeLeaveBalancesForYear(
            @PathVariable Long employeeId,
            @PathVariable Integer year) {
        List<LeaveBalance> balances = leaveBalanceService.initializeLeaveBalancesForYear(employeeId, year);
        return ResponseEntity.status(HttpStatus.CREATED).body(balances);
    }

    /**
     * Check if employee has sufficient balance for a request
     */
    @GetMapping("/employee/{employeeId}/leave-type/{leaveTypeId}/year/{year}/check-balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<BalanceCheckResponse> checkSufficientBalance(
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId,
            @PathVariable Integer year,
            @RequestParam Double daysRequested) {
        
        boolean hasSufficientBalance = leaveBalanceService.hasSufficientBalance(employeeId, leaveTypeId, year, daysRequested);
        
        Optional<LeaveBalance> balanceOpt = leaveBalanceService.getLeaveBalance(employeeId, leaveTypeId, year);
        Double remainingDays = balanceOpt.map(LeaveBalance::getRemainingDays).orElse(0.0);
        
        BalanceCheckResponse response = new BalanceCheckResponse(hasSufficientBalance, remainingDays, daysRequested);
        return ResponseEntity.ok(response);
    }

    /**
     * Get leave balance statistics for an employee
     */
    @GetMapping("/employee/{employeeId}/year/{year}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<LeaveBalanceService.LeaveBalanceStatistics> getLeaveBalanceStatistics(
            @PathVariable Long employeeId,
            @PathVariable Integer year) {
        LeaveBalanceService.LeaveBalanceStatistics stats = leaveBalanceService.getLeaveBalanceStatistics(employeeId, year);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get leave balances that are expiring soon
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveBalance>> getExpiringLeaveBalances(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year) {
        List<LeaveBalance> expiringBalances = leaveBalanceService.getExpiringLeaveBalances(year);
        return ResponseEntity.ok(expiringBalances);
    }

    /**
     * Delete a leave balance
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<Void> deleteLeaveBalance(@PathVariable Long id) {
        Optional<LeaveBalance> existingBalance = leaveBalanceService.getLeaveBalanceById(id);
        if (existingBalance.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        leaveBalanceService.deleteLeaveBalance(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Response class for balance check
     */
    public static class BalanceCheckResponse {
        private boolean hasSufficientBalance;
        private Double remainingDays;
        private Double requestedDays;

        public BalanceCheckResponse(boolean hasSufficientBalance, Double remainingDays, Double requestedDays) {
            this.hasSufficientBalance = hasSufficientBalance;
            this.remainingDays = remainingDays;
            this.requestedDays = requestedDays;
        }

        // Getters and setters
        public boolean isHasSufficientBalance() { return hasSufficientBalance; }
        public void setHasSufficientBalance(boolean hasSufficientBalance) { this.hasSufficientBalance = hasSufficientBalance; }
        
        public Double getRemainingDays() { return remainingDays; }
        public void setRemainingDays(Double remainingDays) { this.remainingDays = remainingDays; }
        
        public Double getRequestedDays() { return requestedDays; }
        public void setRequestedDays(Double requestedDays) { this.requestedDays = requestedDays; }
    }

    /**
     * Security method to check if user can access specific leave balance
     */
    public boolean canAccessLeaveBalance(Long leaveBalanceId, String username) {
        // This would be implemented to check if the user has access to this specific leave balance
        // For now, we'll allow access (this should be properly implemented based on business rules)
        return true;
    }
}