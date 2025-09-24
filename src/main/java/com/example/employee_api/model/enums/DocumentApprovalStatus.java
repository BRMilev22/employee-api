package com.example.employee_api.model.enums;

/**
 * Enum representing the approval status of a document
 */
public enum DocumentApprovalStatus {
    PENDING("Pending", "Document is awaiting approval"),
    APPROVED("Approved", "Document has been approved"),
    REJECTED("Rejected", "Document has been rejected");

    private final String displayName;
    private final String description;

    DocumentApprovalStatus(String displayName, String description) {
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