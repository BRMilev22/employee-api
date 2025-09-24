package com.example.employee_api.model.enums;

/**
 * Performance rating enumeration
 */
public enum PerformanceRating {
    EXCEEDS_EXPECTATIONS(5, "Exceeds Expectations"),
    MEETS_EXPECTATIONS(4, "Meets Expectations"), 
    PARTIALLY_MEETS_EXPECTATIONS(3, "Partially Meets Expectations"),
    BELOW_EXPECTATIONS(2, "Below Expectations"),
    UNSATISFACTORY(1, "Unsatisfactory");

    private final int numericValue;
    private final String description;

    PerformanceRating(int numericValue, String description) {
        this.numericValue = numericValue;
        this.description = description;
    }

    public int getNumericValue() {
        return numericValue;
    }

    public String getDescription() {
        return description;
    }
}