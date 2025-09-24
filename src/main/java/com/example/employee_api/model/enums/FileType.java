package com.example.employee_api.model.enums;

/**
 * Enum representing different types of files in the system
 */
public enum FileType {
    EMPLOYEE_PHOTO("Employee Profile Photo"),
    DOCUMENT("General Document"),
    RESUME("Resume/CV"),
    CONTRACT("Employment Contract"),
    ID_DOCUMENT("Identification Document"),
    CERTIFICATE("Certificate/Qualification"),
    TRAINING_MATERIAL("Training Material"),
    REPORT("Report"),
    BACKUP("System Backup"),
    TEMP("Temporary File");

    private final String description;

    FileType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}