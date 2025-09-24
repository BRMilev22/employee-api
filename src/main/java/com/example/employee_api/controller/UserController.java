package com.example.employee_api.controller;

import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.dto.UserResponse;
import com.example.employee_api.dto.UserRolesUpdateRequest;
import com.example.employee_api.dto.UserSearchCriteria;
import com.example.employee_api.dto.UserUpdateRequest;
import com.example.employee_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST Controller for User Management operations
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users with pagination and sorting
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        PagedResponse<UserResponse> response = userService.getAllUsers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user information
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user (soft delete - disable account)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user roles
     * GET /api/users/{id}/roles
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long id) {
        Set<String> roles = userService.getUserRoles(id);
        return ResponseEntity.ok(roles);
    }

    /**
     * Update user roles
     * PUT /api/users/{id}/roles
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRolesUpdateRequest request) {
        
        UserResponse updatedUser = userService.updateUserRoles(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Activate user account
     * POST /api/users/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        UserResponse user = userService.activateUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivate user account
     * POST /api/users/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        UserResponse user = userService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Search users with criteria
     * GET /api/users/search
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserResponse>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setUsername(username);
        criteria.setEmail(email);
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setEnabled(enabled);
        criteria.setEmailVerified(emailVerified);
        criteria.setRole(role);
        
        PagedResponse<UserResponse> response = userService.searchUsers(criteria, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
}