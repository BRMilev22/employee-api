package com.example.employee_api.controller;

import com.example.employee_api.model.LeaveType;
import com.example.employee_api.repository.LeaveTypeRepository;
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
 * REST Controller for managing leave types
 */
@RestController
@RequestMapping("/api/leave-types")
@CrossOrigin(origins = "*")
public class LeaveTypeController {

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    /**
     * Get all leave types with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<Page<LeaveType>> getAllLeaveTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveType> leaveTypes = leaveTypeRepository.findAll(pageable);
        
        return ResponseEntity.ok(leaveTypes);
    }

    /**
     * Get leave type by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveType> getLeaveTypeById(@PathVariable Long id) {
        Optional<LeaveType> leaveType = leaveTypeRepository.findById(id);
        
        if (leaveType.isPresent()) {
            return ResponseEntity.ok(leaveType.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get leave type by name
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveType> getLeaveTypeByName(@PathVariable String name) {
        Optional<LeaveType> leaveType = leaveTypeRepository.findByNameIgnoreCase(name);
        
        if (leaveType.isPresent()) {
            return ResponseEntity.ok(leaveType.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get active leave types
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveType>> getActiveLeaveTypes() {
        List<LeaveType> activeLeaveTypes = leaveTypeRepository.findByActiveTrue();
        return ResponseEntity.ok(activeLeaveTypes);
    }

    /**
     * Get inactive leave types
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveType>> getInactiveLeaveTypes() {
        List<LeaveType> inactiveLeaveTypes = leaveTypeRepository.findByActiveFalse();
        return ResponseEntity.ok(inactiveLeaveTypes);
    }

    /**
     * Create a new leave type
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveType> createLeaveType(@Valid @RequestBody LeaveType leaveType) {
        // Check if leave type with same name already exists
        if (leaveTypeRepository.existsByNameIgnoreCase(leaveType.getName())) {
            return ResponseEntity.badRequest().build();
        }
        
        LeaveType savedLeaveType = leaveTypeRepository.save(leaveType);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLeaveType);
    }

    /**
     * Update an existing leave type
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveType> updateLeaveType(
            @PathVariable Long id, 
            @Valid @RequestBody LeaveType leaveType) {
        
        Optional<LeaveType> existingLeaveType = leaveTypeRepository.findById(id);
        if (existingLeaveType.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if another leave type with same name exists (excluding current one)
        Optional<LeaveType> duplicateLeaveType = leaveTypeRepository.findByNameIgnoreCase(leaveType.getName());
        if (duplicateLeaveType.isPresent() && !duplicateLeaveType.get().getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        
        leaveType.setId(id);
        LeaveType updatedLeaveType = leaveTypeRepository.save(leaveType);
        return ResponseEntity.ok(updatedLeaveType);
    }

    /**
     * Activate a leave type
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveType> activateLeaveType(@PathVariable Long id) {
        Optional<LeaveType> existingLeaveType = leaveTypeRepository.findById(id);
        if (existingLeaveType.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        LeaveType leaveType = existingLeaveType.get();
        leaveType.setActive(true);
        LeaveType updatedLeaveType = leaveTypeRepository.save(leaveType);
        return ResponseEntity.ok(updatedLeaveType);
    }

    /**
     * Deactivate a leave type
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveType> deactivateLeaveType(@PathVariable Long id) {
        Optional<LeaveType> existingLeaveType = leaveTypeRepository.findById(id);
        if (existingLeaveType.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        LeaveType leaveType = existingLeaveType.get();
        leaveType.setActive(false);
        LeaveType updatedLeaveType = leaveTypeRepository.save(leaveType);
        return ResponseEntity.ok(updatedLeaveType);
    }

    /**
     * Delete a leave type (soft delete by deactivating)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        Optional<LeaveType> existingLeaveType = leaveTypeRepository.findById(id);
        if (existingLeaveType.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Soft delete by deactivating
        LeaveType leaveType = existingLeaveType.get();
        leaveType.setActive(false);
        leaveTypeRepository.save(leaveType);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Search leave types by name pattern
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveType>> searchLeaveTypes(@RequestParam String name) {
        List<LeaveType> leaveTypes = leaveTypeRepository.searchActiveLeaveTypes(name);
        return ResponseEntity.ok(leaveTypes);
    }

    /**
     * Check if leave type name is available
     */
    @GetMapping("/check-name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<NameCheckResponse> checkLeaveTypeName(@RequestParam String name) {
        boolean exists = leaveTypeRepository.existsByNameIgnoreCase(name);
        return ResponseEntity.ok(new NameCheckResponse(!exists, name));
    }

    /**
     * Response class for name availability check
     */
    public static class NameCheckResponse {
        private boolean available;
        private String name;

        public NameCheckResponse(boolean available, String name) {
            this.available = available;
            this.name = name;
        }

        // Getters and setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}