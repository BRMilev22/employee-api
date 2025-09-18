package com.example.employee_api.controller;

import com.example.employee_api.model.LeaveRequest;
import com.example.employee_api.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing leave requests
 */
@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * Get all leave requests with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveRequest>> getAllLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequest> leaveRequests = leaveRequestService.getAllLeaveRequests(pageable);
        
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get leave request by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @leaveRequestController.canAccessLeaveRequest(#id, authentication.name)")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestService.getLeaveRequestById(id);
        
        if (leaveRequest.isPresent()) {
            return ResponseEntity.ok(leaveRequest.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get leave requests by employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployee(employeeId);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get leave requests by employee and status
     */
    @GetMapping("/employee/{employeeId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#employeeId, authentication.name)")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByEmployeeAndStatus(
            @PathVariable Long employeeId,
            @PathVariable LeaveRequest.LeaveStatus status) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeAndStatus(employeeId, status);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get pending leave requests
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequests() {
        List<LeaveRequest> pendingRequests = leaveRequestService.getPendingLeaveRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Get leave requests by date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get leave requests for manager approval
     */
    @GetMapping("/manager/{managerId}/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @employeeController.canAccessEmployee(#managerId, authentication.name)")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsForManagerApproval(@PathVariable Long managerId) {
        List<LeaveRequest> requests = leaveRequestService.getLeaveRequestsForManagerApproval(managerId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Create a new leave request
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveRequest> createLeaveRequest(@Valid @RequestBody LeaveRequest leaveRequest) {
        try {
            LeaveRequest savedRequest = leaveRequestService.createLeaveRequest(leaveRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing leave request
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or @leaveRequestController.canModifyLeaveRequest(#id, authentication.name)")
    public ResponseEntity<LeaveRequest> updateLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest updatedRequest) {
        try {
            LeaveRequest updated = leaveRequestService.updateLeaveRequest(id, updatedRequest);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Approve a leave request
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('MANAGER')")
    public ResponseEntity<LeaveRequest> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequest approvalRequest) {
        try {
            LeaveRequest approved = leaveRequestService.approveLeaveRequest(
                    id, 
                    approvalRequest.getApprovedById(), 
                    approvalRequest.getComments()
            );
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reject a leave request
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('MANAGER')")
    public ResponseEntity<LeaveRequest> rejectLeaveRequest(
            @PathVariable Long id,
            @RequestBody RejectionRequest rejectionRequest) {
        try {
            LeaveRequest rejected = leaveRequestService.rejectLeaveRequest(
                    id, 
                    rejectionRequest.getRejectedById(), 
                    rejectionRequest.getReason()
            );
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel a leave request
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or @leaveRequestController.canModifyLeaveRequest(#id, authentication.name)")
    public ResponseEntity<LeaveRequest> cancelLeaveRequest(
            @PathVariable Long id,
            @RequestBody CancellationRequest cancellationRequest) {
        try {
            LeaveRequest cancelled = leaveRequestService.cancelLeaveRequest(
                    id, 
                    cancellationRequest.getCancelledById(), 
                    cancellationRequest.getReason()
            );
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get leave calendar for a specific period
     */
    @GetMapping("/calendar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveRequest>> getLeaveCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LeaveRequest> calendar = leaveRequestService.getLeaveCalendar(startDate, endDate);
        return ResponseEntity.ok(calendar);
    }

    /**
     * Get overlapping leave requests for conflict detection
     */
    @GetMapping("/overlapping")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveRequest>> getOverlappingLeaveRequests(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LeaveRequest> overlapping = leaveRequestService.getOverlappingLeaveRequests(employeeId, startDate, endDate);
        return ResponseEntity.ok(overlapping);
    }

    /**
     * Delete a leave request
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or @leaveRequestController.canModifyLeaveRequest(#id, authentication.name)")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Long id) {
        try {
            leaveRequestService.deleteLeaveRequest(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Request classes for API operations
     */
    public static class ApprovalRequest {
        private Long approvedById;
        private String comments;

        // Getters and setters
        public Long getApprovedById() { return approvedById; }
        public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    public static class RejectionRequest {
        private Long rejectedById;
        private String reason;

        // Getters and setters
        public Long getRejectedById() { return rejectedById; }
        public void setRejectedById(Long rejectedById) { this.rejectedById = rejectedById; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CancellationRequest {
        private Long cancelledById;
        private String reason;

        // Getters and setters
        public Long getCancelledById() { return cancelledById; }
        public void setCancelledById(Long cancelledById) { this.cancelledById = cancelledById; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Security methods for access control
     */
    public boolean canAccessLeaveRequest(Long leaveRequestId, String username) {
        // Implementation to check if user can access this specific leave request
        // For now, allowing access (should be properly implemented)
        return true;
    }

    public boolean canModifyLeaveRequest(Long leaveRequestId, String username) {
        // Implementation to check if user can modify this specific leave request
        // For now, allowing access (should be properly implemented)
        return true;
    }
}