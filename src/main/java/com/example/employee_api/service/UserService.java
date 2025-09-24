package com.example.employee_api.service;

import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.dto.UserResponse;
import com.example.employee_api.dto.UserRolesUpdateRequest;
import com.example.employee_api.dto.UserSearchCriteria;
import com.example.employee_api.dto.UserUpdateRequest;
import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Role;
import com.example.employee_api.model.User;
import com.example.employee_api.repository.RoleRepository;
import com.example.employee_api.repository.UserRepository;
import com.example.employee_api.specifications.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Get all users with pagination and sorting
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserResponse> userResponses = userPage.getContent()
            .stream()
            .map(UserResponse::new)
            .collect(Collectors.toList());

        return PagedResponse.of(userPage, sortBy, sortDirection, userResponses);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));
        return new UserResponse(user);
    }

    /**
     * Update user information
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));

        // Check if email is already taken by another user
        if (!user.getEmail().equals(request.getEmail())) {
            userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new IllegalArgumentException("Email is already taken by another user");
                    }
                });
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    /**
     * Soft delete user (disable account)
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));
        
        user.setEnabled(false);
        userRepository.save(user);
    }

    /**
     * Get user roles
     */
    @Transactional(readOnly = true)
    public Set<String> getUserRoles(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));
        
        return user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Update user roles
     */
    public UserResponse updateUserRoles(Long id, UserRolesUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));

        Set<Role> roles = new HashSet<>();
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
            roles.add(role);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    /**
     * Activate user account
     */
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));
        
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    /**
     * Deactivate user account
     */
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("User not found with id: " + id));
        
        user.setEnabled(false);
        
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    /**
     * Search users with criteria
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> searchUsers(UserSearchCriteria criteria, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<User> specification = UserSpecifications.withCriteria(criteria);
        Page<User> userPage = userRepository.findAll(specification, pageable);

        List<UserResponse> userResponses = userPage.getContent()
            .stream()
            .map(UserResponse::new)
            .collect(Collectors.toList());

        return PagedResponse.of(userPage, sortBy, sortDirection, userResponses);
    }
}