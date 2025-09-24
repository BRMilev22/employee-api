package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.DeductionStatus;
import com.example.employee_api.model.enums.DeductionType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing employee deductions.
 * Tracks various deductions from employee salary such as taxes, insurance, etc.
 */
@Entity
@Table(name = "deductions", indexes = {
    @Index(name = "idx_deduction_employee", columnList = "employee_id"),
    @Index(name = "idx_deduction_type", columnList = "deduction_type"),
    @Index(name = "idx_deduction_status", columnList = "status"),
    @Index(name = "idx_deduction_effective_date", columnList = "effective_date"),
    @Index(name = "idx_deduction_end_date", columnList = "end_date")
})
public class Deduction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-deductions")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "deduction_type", nullable = false)
    @NotNull(message = "Deduction type is required")
    private DeductionType deductionType;

    @Column(name = "amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Deduction amount must be positive")
    private BigDecimal amount;

    @Column(name = "percentage", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Deduction percentage must be positive")
    private BigDecimal percentage;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Column(name = "effective_date", nullable = false)
    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    private DeductionStatus status = DeductionStatus.ACTIVE;

    @Column(name = "is_pre_tax", nullable = false)
    private Boolean isPreTax = false;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @Column(name = "frequency", length = 20)
    @Size(max = 20, message = "Frequency must be at most 20 characters")
    private String frequency = "MONTHLY"; // WEEKLY, BIWEEKLY, MONTHLY, ANNUALLY

    @Column(name = "employer_contribution", precision = 12, scale = 2)
    private BigDecimal employerContribution;

    @Column(name = "annual_limit", precision = 12, scale = 2)
    private BigDecimal annualLimit;

    @Column(name = "year_to_date_amount", precision = 12, scale = 2)
    private BigDecimal yearToDateAmount = BigDecimal.ZERO;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;

    @Column(name = "vendor_name", length = 100)
    @Size(max = 100, message = "Vendor name must be at most 100 characters")
    private String vendorName;

    @Column(name = "policy_number", length = 50)
    @Size(max = 50, message = "Policy number must be at most 50 characters")
    private String policyNumber;

    // Default constructor
    public Deduction() {
    }

    // Constructor with essential fields
    public Deduction(Employee employee, DeductionType deductionType, LocalDate effectiveDate) {
        this.employee = employee;
        this.deductionType = deductionType;
        this.effectiveDate = effectiveDate;
    }

    // Constructor with amount
    public Deduction(Employee employee, DeductionType deductionType, BigDecimal amount, LocalDate effectiveDate) {
        this.employee = employee;
        this.deductionType = deductionType;
        this.amount = amount;
        this.effectiveDate = effectiveDate;
    }

    // Constructor with percentage
    public Deduction(Employee employee, DeductionType deductionType, BigDecimal percentage, LocalDate effectiveDate, boolean isPercentage) {
        this.employee = employee;
        this.deductionType = deductionType;
        if (isPercentage) {
            this.percentage = percentage;
        } else {
            this.amount = percentage;
        }
        this.effectiveDate = effectiveDate;
    }

    // Business logic methods
    public BigDecimal calculateDeduction(BigDecimal grossSalary) {
        if (percentage != null) {
            return grossSalary.multiply(percentage).divide(BigDecimal.valueOf(100));
        } else if (amount != null) {
            return amount;
        }
        return BigDecimal.ZERO;
    }

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return status == DeductionStatus.ACTIVE && 
               !effectiveDate.isAfter(now) && 
               (endDate == null || !endDate.isBefore(now));
    }

    public boolean hasReachedAnnualLimit() {
        return annualLimit != null && yearToDateAmount != null && 
               yearToDateAmount.compareTo(annualLimit) >= 0;
    }

    public void addToYearToDate(BigDecimal deductionAmount) {
        if (yearToDateAmount == null) {
            yearToDateAmount = BigDecimal.ZERO;
        }
        this.yearToDateAmount = yearToDateAmount.add(deductionAmount);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public DeductionType getDeductionType() {
        return deductionType;
    }

    public void setDeductionType(DeductionType deductionType) {
        this.deductionType = deductionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public DeductionStatus getStatus() {
        return status;
    }

    public void setStatus(DeductionStatus status) {
        this.status = status;
    }

    public Boolean getIsPreTax() {
        return isPreTax;
    }

    public void setIsPreTax(Boolean isPreTax) {
        this.isPreTax = isPreTax;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public BigDecimal getEmployerContribution() {
        return employerContribution;
    }

    public void setEmployerContribution(BigDecimal employerContribution) {
        this.employerContribution = employerContribution;
    }

    public BigDecimal getAnnualLimit() {
        return annualLimit;
    }

    public void setAnnualLimit(BigDecimal annualLimit) {
        this.annualLimit = annualLimit;
    }

    public BigDecimal getYearToDateAmount() {
        return yearToDateAmount;
    }

    public void setYearToDateAmount(BigDecimal yearToDateAmount) {
        this.yearToDateAmount = yearToDateAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    @Override
    public String toString() {
        return "Deduction{" +
                "id=" + id +
                ", employeeId=" + (employee != null ? employee.getId() : null) +
                ", deductionType=" + deductionType +
                ", amount=" + amount +
                ", percentage=" + percentage +
                ", effectiveDate=" + effectiveDate +
                ", status=" + status +
                ", isPreTax=" + isPreTax +
                ", frequency='" + frequency + '\'' +
                '}';
    }
}