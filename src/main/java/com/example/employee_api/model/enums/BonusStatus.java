package com.example.employee_api.model.enums;

/**
 * Enumeration representing the status of a bonus
 */
public enum BonusStatus {
    PENDING("Pending", "Bonus is pending approval"),
    APPROVED("Approved", "Bonus has been approved but not yet paid"),
    PAID("Paid", "Bonus has been paid to the employee"),
    CANCELLED("Cancelled", "Bonus has been cancelled"),
    REJECTED("Rejected", "Bonus has been rejected");

    private final String displayName;
    private final String description;

    BonusStatus(String displayName, String description) {
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