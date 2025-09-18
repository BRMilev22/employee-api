package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Permission;
import com.example.employee_api.model.Role;
import com.example.employee_api.repository.PermissionRepository;
import com.example.employee_api.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for role management operations
 */
@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Get all roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Get role by ID
     */
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Role not found with id: " + id));
    }

    /**
     * Get role by name
     */
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EmployeeNotFoundException("Role not found with name: " + name));
    }

    /**
     * Create new role
     */
    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role already exists with name: " + name);
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);

        return roleRepository.save(role);
    }

    /**
     * Update role
     */
    public Role updateRole(Long id, String name, String description) {
        Role role = getRoleById(id);
        
        // Check if name is being changed and if the new name already exists
        if (name != null && !name.equals(role.getName()) && roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role already exists with name: " + name);
        }

        if (name != null) {
            role.setName(name);
        }
        if (description != null) {
            role.setDescription(description);
        }

        return roleRepository.save(role);
    }

    /**
     * Delete role
     */
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        
        // Check if role is being used by any users
        if (!role.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete role that is assigned to users");
        }

        roleRepository.delete(role);
    }

    /**
     * Check if role exists by name
     */
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
    
    /**
     * Assign permission to role
     */
    public Role assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = getRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EmployeeNotFoundException("Permission not found with id: " + permissionId));
        
        // Create a new HashSet to ensure we have a mutable collection
        Set<Permission> permissions = new HashSet<>(role.getPermissions());
        permissions.add(permission);
        role.setPermissions(permissions);
        
        return roleRepository.save(role);
    }
    
    /**
     * Remove permission from role
     */
    public Role removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = getRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EmployeeNotFoundException("Permission not found with id: " + permissionId));
        
        // Create a new HashSet to ensure we have a mutable collection
        Set<Permission> permissions = new HashSet<>(role.getPermissions());
        permissions.remove(permission);
        role.setPermissions(permissions);
        
        return roleRepository.save(role);
    }
    
    /**
     * Get all permissions for a role
     */
    public Set<Permission> getRolePermissions(Long roleId) {
        Role role = getRoleById(roleId);
        return role.getPermissions();
    }
}