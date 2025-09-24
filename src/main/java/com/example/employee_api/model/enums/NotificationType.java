package com.example.employee_api.model.enums;

/**
 * Enumeration for notification types
 */
public enum NotificationType {
    LEAVE_REQUEST("Leave Request"),
    LEAVE_APPROVAL("Leave Approval"),
    LEAVE_REJECTION("Leave Rejection"),
    PERFORMANCE_REVIEW("Performance Review"),
    GOAL_ASSIGNED("Goal Assigned"),
    GOAL_DUE("Goal Due"),
    DOCUMENT_APPROVAL("Document Approval"),
    DOCUMENT_REJECTION("Document Rejection"),
    PAYROLL_UPDATE("Payroll Update"),
    EMPLOYEE_ONBOARDING("Employee Onboarding"),
    EMPLOYEE_OFFBOARDING("Employee Offboarding"),
    SYSTEM_ANNOUNCEMENT("System Announcement"),
    REMINDER("Reminder"),
    ALERT("Alert"),
    INFO("Information");

    private final String displayName;

    NotificationType(String displayName) {
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