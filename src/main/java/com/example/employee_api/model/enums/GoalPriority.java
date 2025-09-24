package com.example.employee_api.model.enums;

/**
 * Goal priority enumeration
 */
public enum GoalPriority {
    LOW(1, "Low Priority"),
    MEDIUM(2, "Medium Priority"),
    HIGH(3, "High Priority"),
    CRITICAL(4, "Critical Priority");

    private final int level;
    private final String description;

    GoalPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}