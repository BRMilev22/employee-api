package com.example.employee_api.model.enums;

/**
 * Enum representing the status of files in the system
 */
public enum FileStatus {
    UPLOADING("File upload in progress"),
    ACTIVE("File is available and accessible"),
    ARCHIVED("File is archived but accessible"),
    DELETED("File is marked for deletion"),
    CORRUPTED("File is corrupted or invalid"),
    QUARANTINED("File is quarantined due to security concerns"),
    EXPIRED("File has expired and is no longer valid");

    private final String description;

    FileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}