package com.example.employee_api.model.enums;

/**
 * Enumeration representing the reasons for salary changes
 */
public enum SalaryChangeReason {
    PROMOTION("Promotion", "Salary increase due to promotion"),
    ANNUAL_REVIEW("Annual Review", "Salary adjustment based on annual performance review"),
    MARKET_ADJUSTMENT("Market Adjustment", "Salary adjustment to match market rates"),
    MERIT_INCREASE("Merit Increase", "Salary increase based on exceptional performance"),
    COST_OF_LIVING("Cost of Living", "Salary adjustment for cost of living changes"),
    ROLE_CHANGE("Role Change", "Salary change due to change in role or responsibilities"),
    DEMOTION("Demotion", "Salary decrease due to demotion"),
    DISCIPLINARY("Disciplinary", "Salary reduction due to disciplinary action"),
    INITIAL_SALARY("Initial Salary", "Initial salary upon hiring"),
    CONTRACT_RENEWAL("Contract Renewal", "Salary change upon contract renewal"),
    RECLASSIFICATION("Reclassification", "Salary change due to job reclassification"),
    UNION_AGREEMENT("Union Agreement", "Salary change based on union agreement"),
    COMPANY_RESTRUCTURE("Company Restructure", "Salary change due to company restructuring"),
    OTHER("Other", "Other reason for salary change");

    private final String displayName;
    private final String description;

    SalaryChangeReason(String displayName, String description) {
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