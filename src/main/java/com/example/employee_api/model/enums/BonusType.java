package com.example.employee_api.model.enums;

/**
 * Enumeration representing the different types of bonuses
 */
public enum BonusType {
    PERFORMANCE("Performance Bonus", "Bonus based on individual or team performance"),
    SIGNING("Signing Bonus", "One-time bonus for new hires"),
    RETENTION("Retention Bonus", "Bonus to retain valuable employees"),
    ANNUAL("Annual Bonus", "Yearly bonus based on company performance"),
    QUARTERLY("Quarterly Bonus", "Quarterly bonus based on performance"),
    PROJECT("Project Bonus", "Bonus for successful project completion"),
    SPOT("Spot Bonus", "Immediate recognition bonus for exceptional work"),
    REFERRAL("Referral Bonus", "Bonus for successful employee referrals"),
    SALES("Sales Bonus", "Commission or incentive for sales targets"),
    PROFIT_SHARING("Profit Sharing", "Share of company profits distributed to employees"),
    HOLIDAY("Holiday Bonus", "Seasonal or holiday bonus"),
    MILESTONE("Milestone Bonus", "Bonus for reaching career or tenure milestones"),
    SAFETY("Safety Bonus", "Bonus for safety achievements"),
    ATTENDANCE("Attendance Bonus", "Bonus for perfect attendance"),
    OTHER("Other", "Other type of bonus");

    private final String displayName;
    private final String description;

    BonusType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}