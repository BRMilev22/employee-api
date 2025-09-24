package com.example.employee_api.model.enums;

/**
 * Goal status enumeration
 */
public enum GoalStatus {
    NOT_STARTED,     // Goal not yet started
    IN_PROGRESS,     // Goal is being worked on
    COMPLETED,       // Goal completed successfully
    OVERDUE,         // Goal past due date
    CANCELLED,       // Goal cancelled
    ON_HOLD          // Goal temporarily paused
}