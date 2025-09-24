package com.example.employee_api.model.enums;

/**
 * Enumeration representing the status of a pay grade
 */
public enum PayGradeStatus {
    ACTIVE("Active", "Pay grade is currently active and can be assigned to employees"),
    INACTIVE("Inactive", "Pay grade is inactive and cannot be assigned to new employees"),
    DEPRECATED("Deprecated", "Pay grade has been deprecated and should not be used");

    private final String displayName;
    private final String description;

    PayGradeStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}