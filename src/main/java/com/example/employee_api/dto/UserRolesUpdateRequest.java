package com.example.employee_api.dto;

import java.util.Set;

/**
 * DTO for updating user roles
 */
public class UserRolesUpdateRequest {
    
    private Set<Long> roleIds;
    
    // Constructors
    public UserRolesUpdateRequest() {}
    
    public UserRolesUpdateRequest(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
    
    // Getters and Setters
    public Set<Long> getRoleIds() {
        return roleIds;
    }
    
    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}