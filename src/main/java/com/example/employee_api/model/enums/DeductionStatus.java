package com.example.employee_api.model.enums;

/**
 * Enumeration representing the status of a deduction
 */
public enum DeductionStatus {
    ACTIVE("Active", "Deduction is currently active"),
    INACTIVE("Inactive", "Deduction is temporarily inactive"),
    SUSPENDED("Suspended", "Deduction has been suspended"),
    TERMINATED("Terminated", "Deduction has been permanently terminated"),
    PENDING("Pending", "Deduction is pending approval");

    private final String displayName;
    private final String description;

    DeductionStatus(String displayName, String description) {
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