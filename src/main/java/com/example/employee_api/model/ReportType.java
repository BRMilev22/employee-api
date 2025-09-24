package com.example.employee_api.model;

public enum ReportType {
    EMPLOYEES("Employee Report"),
    DEPARTMENTS("Department Report"),
    ATTENDANCE("Attendance Report"),
    PERFORMANCE("Performance Report"),
    PAYROLL("Payroll Report"),
    TURNOVER("Turnover Analysis"),
    DEMOGRAPHICS("Demographics Analysis"),
    CUSTOM("Custom Report");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}