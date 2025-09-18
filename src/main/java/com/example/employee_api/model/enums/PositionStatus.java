package com.example.employee_api.model.enums;

/**
 * Enum representing the status of a position within the organization
 */
public enum PositionStatus {
    ACTIVE,      // Position is active and can be filled
    INACTIVE,    // Position is temporarily inactive
    FROZEN,      // Position is frozen (hiring freeze)
    OBSOLETE,    // Position is no longer needed
    PENDING_APPROVAL  // Position is pending approval to be created
}