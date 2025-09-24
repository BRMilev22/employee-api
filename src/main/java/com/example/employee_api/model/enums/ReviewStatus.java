package com.example.employee_api.model.enums;

/**
 * Performance review status enumeration
 */
public enum ReviewStatus {
    DRAFT,           // Review is being created
    PENDING,         // Waiting for employee input
    IN_PROGRESS,     // Under review by manager
    COMPLETED,       // Review completed
    APPROVED,        // Review approved by higher management
    CANCELLED        // Review cancelled
}