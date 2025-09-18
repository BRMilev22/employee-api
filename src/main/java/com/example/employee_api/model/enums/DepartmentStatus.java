package com.example.employee_api.model.enums;

public enum DepartmentStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"), 
    SUSPENDED("Suspended"),
    UNDER_REVIEW("Under Review"),
    MERGED("Merged"),
    DISSOLVED("Dissolved");
    
    private final String displayName;
    
    DepartmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}