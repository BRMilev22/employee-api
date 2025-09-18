package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.*;
import com.example.employee_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing leave requests
 */
@Service
@Transactional
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    /**
     * Get all leave requests with pagination
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequest> getAllLeaveRequests(Pageable pageable) {
        return leaveRequestRepository.findAll(pageable);
    }

    /**
     * Get leave request by ID
     */
    @Transactional(readOnly = true)
    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    /**
     * Get leave requests by employee
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        return leaveRequestRepository.findByEmployee(employee);
    }

    /**
     * Get leave requests by employee and status
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByEmployeeAndStatus(Long employeeId, LeaveRequest.LeaveStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        return leaveRequestRepository.findByEmployeeAndStatus(employee, status);
    }

    /**
     * Get pending leave requests for approval
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING);
    }

    /**
     * Get leave requests by date range
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get leave requests for a specific manager to approve
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsForManagerApproval(Long managerId) {
        return leaveRequestRepository.findTeamLeaveRequestsByManagerAndStatus(managerId, LeaveRequest.LeaveStatus.PENDING);
    }

    /**
     * Create a new leave request
     */
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        // Validate the leave request
        validateLeaveRequest(leaveRequest);
        
        // Calculate total days
        Double totalDays = calculateLeaveDays(leaveRequest.getStartDate(), leaveRequest.getEndDate(), 
                                            leaveRequest.getHalfDay());
        leaveRequest.setTotalDays(totalDays);
        
        // Set applied date
        leaveRequest.setAppliedDate(LocalDate.now());
        
        // Set initial status
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        
        // Check if leave balance is sufficient
        int year = leaveRequest.getStartDate().getYear();
        if (!leaveBalanceService.hasSufficientBalance(leaveRequest.getEmployee().getId(), 
                leaveRequest.getLeaveType().getId(), year, totalDays)) {
            throw new IllegalArgumentException("Insufficient leave balance for this request");
        }
        
        // Save the leave request
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        // Update leave balance for pending request
        leaveBalanceService.updateBalanceForPendingLeave(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year,
                totalDays
        );
        
        return savedRequest;
    }

    /**
     * Update an existing leave request (only if it's still pending)
     */
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest updatedRequest) {
        LeaveRequest existingRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        
        // Only allow updates if the request is still pending
        if (existingRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Cannot update leave request that is not in pending status");
        }
        
        // Restore previous balance allocation
        int previousYear = existingRequest.getStartDate().getYear();
        leaveBalanceService.updateBalanceForRejectedOrCancelledLeave(
                existingRequest.getEmployee().getId(),
                existingRequest.getLeaveType().getId(),
                previousYear,
                existingRequest.getTotalDays()
        );
        
        // Update the request details
        existingRequest.setStartDate(updatedRequest.getStartDate());
        existingRequest.setEndDate(updatedRequest.getEndDate());
        existingRequest.setReason(updatedRequest.getReason());
        existingRequest.setEmergencyContact(updatedRequest.getEmergencyContact());
        existingRequest.setWorkHandover(updatedRequest.getWorkHandover());
        existingRequest.setHalfDay(updatedRequest.getHalfDay());
        existingRequest.setHalfDayPeriod(updatedRequest.getHalfDayPeriod());
        
        // Recalculate total days
        Double totalDays = calculateLeaveDays(existingRequest.getStartDate(), existingRequest.getEndDate(), 
                                            existingRequest.getHalfDay());
        existingRequest.setTotalDays(totalDays);
        
        // Validate updated request (excluding current request from overlap check)
        validateLeaveRequestForUpdate(existingRequest, id);
        
        // Check if updated leave balance is sufficient
        int newYear = existingRequest.getStartDate().getYear();
        if (!leaveBalanceService.hasSufficientBalance(existingRequest.getEmployee().getId(), 
                existingRequest.getLeaveType().getId(), newYear, totalDays)) {
            throw new IllegalArgumentException("Insufficient leave balance for updated request");
        }
        
        // Save updated request
        LeaveRequest savedRequest = leaveRequestRepository.save(existingRequest);
        
        // Update leave balance for new pending request
        leaveBalanceService.updateBalanceForPendingLeave(
                existingRequest.getEmployee().getId(),
                existingRequest.getLeaveType().getId(),
                newYear,
                totalDays
        );
        
        return savedRequest;
    }

    /**
     * Approve a leave request
     */
    public LeaveRequest approveLeaveRequest(Long id, Long approvedById, String approvalComments) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        
        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only pending leave requests can be approved");
        }
        
        Employee approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new EmployeeNotFoundException("Approver not found with id: " + approvedById));
        
        // Update request status
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setApprovalComments(approvalComments);
        
        // Update leave balance (move from pending to used)
        int year = leaveRequest.getStartDate().getYear();
        leaveBalanceService.updateBalanceForApprovedLeave(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year,
                leaveRequest.getTotalDays()
        );
        
        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * Reject a leave request
     */
    public LeaveRequest rejectLeaveRequest(Long id, Long rejectedById, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        
        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only pending leave requests can be rejected");
        }
        
        Employee rejector = employeeRepository.findById(rejectedById)
                .orElseThrow(() -> new EmployeeNotFoundException("Rejector not found with id: " + rejectedById));
        
        // Update request status
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(rejector);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setRejectionReason(rejectionReason);
        
        // Restore leave balance (remove from pending)
        int year = leaveRequest.getStartDate().getYear();
        leaveBalanceService.updateBalanceForRejectedOrCancelledLeave(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year,
                leaveRequest.getTotalDays()
        );
        
        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * Cancel a leave request
     */
    public LeaveRequest cancelLeaveRequest(Long id, Long cancelledById, String cancelReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        
        if (leaveRequest.getStatus() == LeaveRequest.LeaveStatus.CANCELLED) {
            throw new IllegalArgumentException("Leave request is already cancelled");
        }
        
        // Can only cancel pending or approved requests
        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING && 
            leaveRequest.getStatus() != LeaveRequest.LeaveStatus.APPROVED) {
            throw new IllegalArgumentException("Only pending or approved leave requests can be cancelled");
        }
        
        // Update request status
        LeaveRequest.LeaveStatus previousStatus = leaveRequest.getStatus();
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        leaveRequest.setIsCancelled(true);
        leaveRequest.setCancelledAt(LocalDateTime.now());
        leaveRequest.setCancelledBy(cancelledById);
        leaveRequest.setCancelReason(cancelReason);
        
        // Restore leave balance based on previous status
        int year = leaveRequest.getStartDate().getYear();
        if (previousStatus == LeaveRequest.LeaveStatus.PENDING) {
            // Restore from pending days
            leaveBalanceService.updateBalanceForRejectedOrCancelledLeave(
                    leaveRequest.getEmployee().getId(),
                    leaveRequest.getLeaveType().getId(),
                    year,
                    leaveRequest.getTotalDays()
            );
        } else if (previousStatus == LeaveRequest.LeaveStatus.APPROVED) {
            // If already approved, we need to restore used days back to available
            // This is more complex and might require a different method
            restoreUsedLeaveDays(leaveRequest);
        }
        
        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * Get leave calendar for a specific period
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveCalendar(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findByDateRange(startDate, endDate).stream()
                .filter(request -> request.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
                .toList();
    }

    /**
     * Get overlapping leave requests for conflict detection
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getOverlappingLeaveRequests(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest.LeaveStatus> statuses = List.of(
                LeaveRequest.LeaveStatus.PENDING, 
                LeaveRequest.LeaveStatus.APPROVED
        );
        return leaveRequestRepository.findOverlappingRequests(employeeId, startDate, endDate, statuses);
    }

    /**
     * Delete a leave request (only if pending)
     */
    public void deleteLeaveRequest(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        
        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only pending leave requests can be deleted");
        }
        
        // Restore leave balance
        int year = leaveRequest.getStartDate().getYear();
        leaveBalanceService.updateBalanceForRejectedOrCancelledLeave(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year,
                leaveRequest.getTotalDays()
        );
        
        leaveRequestRepository.deleteById(id);
    }

    /**
     * Validate leave request business rules
     */
    private void validateLeaveRequest(LeaveRequest leaveRequest) {
        // Check if start date is before end date
        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Check minimum notice period
        LeaveType leaveType = leaveRequest.getLeaveType();
        if (leaveType.getMinimumNoticeDays() != null && leaveType.getMinimumNoticeDays() > 0) {
            LocalDate minimumNoticeDate = LocalDate.now().plusDays(leaveType.getMinimumNoticeDays());
            if (leaveRequest.getStartDate().isBefore(minimumNoticeDate)) {
                throw new IllegalArgumentException("Leave request must be submitted at least " + 
                        leaveType.getMinimumNoticeDays() + " days in advance");
            }
        }
        
        // Check maximum consecutive days
        if (leaveType.getMaximumConsecutiveDays() != null && leaveType.getMaximumConsecutiveDays() > 0) {
            long requestedDays = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            if (requestedDays > leaveType.getMaximumConsecutiveDays()) {
                throw new IllegalArgumentException("Leave request exceeds maximum consecutive days limit of " + 
                        leaveType.getMaximumConsecutiveDays());
            }
        }
        
        // Check for overlapping requests
        List<LeaveRequest> overlappingRequests = getOverlappingLeaveRequests(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        );
        
        if (!overlappingRequests.isEmpty()) {
            throw new IllegalArgumentException("Leave request conflicts with existing leave requests");
        }
    }

    /**
     * Validate leave request for updates (excludes current request from overlap check)
     */
    private void validateLeaveRequestForUpdate(LeaveRequest leaveRequest, Long currentRequestId) {
        // Check if start date is before end date
        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Check minimum notice period
        LeaveType leaveType = leaveRequest.getLeaveType();
        if (leaveType.getMinimumNoticeDays() != null && leaveType.getMinimumNoticeDays() > 0) {
            LocalDate minimumNoticeDate = LocalDate.now().plusDays(leaveType.getMinimumNoticeDays());
            if (leaveRequest.getStartDate().isBefore(minimumNoticeDate)) {
                throw new IllegalArgumentException("Leave request must be submitted at least " + 
                        leaveType.getMinimumNoticeDays() + " days in advance");
            }
        }
        
        // Check maximum consecutive days
        if (leaveType.getMaximumConsecutiveDays() != null && leaveType.getMaximumConsecutiveDays() > 0) {
            long requestedDays = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            if (requestedDays > leaveType.getMaximumConsecutiveDays()) {
                throw new IllegalArgumentException("Leave request exceeds maximum consecutive days limit of " + 
                        leaveType.getMaximumConsecutiveDays());
            }
        }
        
        // Check for overlapping requests (excluding current request)
        List<LeaveRequest> overlappingRequests = getOverlappingLeaveRequests(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        );
        
        // Filter out the current request being updated
        overlappingRequests = overlappingRequests.stream()
                .filter(request -> !request.getId().equals(currentRequestId))
                .toList();
        
        if (!overlappingRequests.isEmpty()) {
            throw new IllegalArgumentException("Leave request conflicts with existing leave requests");
        }
    }

    /**
     * Calculate leave days excluding weekends and holidays
     */
    private Double calculateLeaveDays(LocalDate startDate, LocalDate endDate, Boolean halfDay) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        // For now, simple calculation. In future, can add weekend/holiday exclusion
        Double leaveDays = (double) totalDays;
        
        // If it's a half-day leave, adjust the calculation
        if (halfDay != null && halfDay) {
            leaveDays = 0.5;
        }
        
        return leaveDays;
    }

    /**
     * Restore used leave days when an approved leave is cancelled
     */
    private void restoreUsedLeaveDays(LeaveRequest leaveRequest) {
        int year = leaveRequest.getStartDate().getYear();
        
        // Get the current balance
        Optional<LeaveBalance> balanceOpt = leaveBalanceService.getLeaveBalance(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year
        );
        
        if (balanceOpt.isPresent()) {
            LeaveBalance balance = balanceOpt.get();
            // Move days from used back to available
            balance.setUsedDays(balance.getUsedDays() - leaveRequest.getTotalDays());
            balance.calculateRemainingDays();
            leaveBalanceService.saveLeaveBalance(balance);
        }
    }
}