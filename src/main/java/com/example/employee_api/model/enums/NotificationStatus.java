package com.example.employee_api.model.enums;

/**
 * Enumeration for notification status
 */
public enum NotificationStatus {
    UNREAD("Unread"),
    READ("Read"),
    ARCHIVED("Archived"),
    DELETED("Deleted");

    private final String displayName;

    NotificationStatus(String displayName) {
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