package com.example.employee_api.model.enums;

/**
 * Enumeration representing the different types of deductions
 */
public enum DeductionType {
    // Tax Deductions
    FEDERAL_TAX("Federal Tax", "Federal income tax withholding"),
    STATE_TAX("State Tax", "State income tax withholding"),
    LOCAL_TAX("Local Tax", "Local income tax withholding"),
    SOCIAL_SECURITY("Social Security", "Social Security tax (FICA)"),
    MEDICARE("Medicare", "Medicare tax"),
    
    // Insurance Deductions
    HEALTH_INSURANCE("Health Insurance", "Health insurance premiums"),
    DENTAL_INSURANCE("Dental Insurance", "Dental insurance premiums"),
    VISION_INSURANCE("Vision Insurance", "Vision insurance premiums"),
    LIFE_INSURANCE("Life Insurance", "Life insurance premiums"),
    DISABILITY_INSURANCE("Disability Insurance", "Disability insurance premiums"),
    
    // Retirement Deductions
    RETIREMENT_401K("401(k)", "401(k) retirement plan contribution"),
    RETIREMENT_403B("403(b)", "403(b) retirement plan contribution"),
    PENSION("Pension", "Pension plan contribution"),
    ROTH_IRA("Roth IRA", "Roth IRA contribution"),
    
    // Other Benefit Deductions
    FLEXIBLE_SPENDING("Flexible Spending Account", "FSA contribution"),
    HEALTH_SAVINGS("Health Savings Account", "HSA contribution"),
    DEPENDENT_CARE("Dependent Care", "Dependent care assistance"),
    COMMUTER_BENEFITS("Commuter Benefits", "Transit and parking benefits"),
    
    // Voluntary Deductions
    UNION_DUES("Union Dues", "Labor union dues"),
    CHARITABLE_CONTRIBUTION("Charitable Contribution", "Charitable donations"),
    EMPLOYEE_LOAN("Employee Loan", "Loan repayment"),
    GARNISHMENT("Garnishment", "Court-ordered wage garnishment"),
    CHILD_SUPPORT("Child Support", "Child support payments"),
    
    // Company Deductions
    UNIFORM("Uniform", "Company uniform costs"),
    PARKING("Parking", "Parking fees"),
    CAFETERIA("Cafeteria", "Meal plan deductions"),
    EQUIPMENT("Equipment", "Company equipment costs"),
    TRAINING("Training", "Training and certification costs"),
    
    OTHER("Other", "Other type of deduction");

    private final String displayName;
    private final String description;

    DeductionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTaxDeduction() {
        return this == FEDERAL_TAX || this == STATE_TAX || this == LOCAL_TAX || 
               this == SOCIAL_SECURITY || this == MEDICARE;
    }

    public boolean isInsuranceDeduction() {
        return this == HEALTH_INSURANCE || this == DENTAL_INSURANCE || 
               this == VISION_INSURANCE || this == LIFE_INSURANCE || 
               this == DISABILITY_INSURANCE;
    }

    public boolean isRetirementDeduction() {
        return this == RETIREMENT_401K || this == RETIREMENT_403B || 
               this == PENSION || this == ROTH_IRA;
    }

    @Override
    public String toString() {
        return displayName;
    }
}