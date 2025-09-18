package com.example.employee_api.controller;

import com.example.employee_api.model.Permission;
import com.example.employee_api.model.Role;
import com.example.employee_api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controller for role management
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Get all roles
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Create new role
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role roleRequest) {
        Role role = roleService.createRole(roleRequest.getName(), roleRequest.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    /**
     * Update role
     */
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleRequest) {
        Role role = roleService.updateRole(id, roleRequest.getName(), roleRequest.getDescription());
        return ResponseEntity.ok(role);
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get role by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        Role role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }
    
    /**
     * Assign permission to role
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Role> assignPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        Role role = roleService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(role);
    }
    
    /**
     * Remove permission from role
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Role> removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        Role role = roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(role);
    }
    
    /**
     * Get all permissions for a role
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<Permission>> getRolePermissions(@PathVariable Long id) {
        Set<Permission> permissions = roleService.getRolePermissions(id);
        return ResponseEntity.ok(permissions);
    }
}