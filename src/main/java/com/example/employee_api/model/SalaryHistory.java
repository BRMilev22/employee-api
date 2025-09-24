package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.SalaryChangeReason;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing the salary history of employees.
 * Tracks all salary changes over time for audit and historical purposes.
 */
@Entity
@Table(name = "salary_history", indexes = {
    @Index(name = "idx_salary_history_employee", columnList = "employee_id"),
    @Index(name = "idx_salary_history_effective_date", columnList = "effective_date"),
    @Index(name = "idx_salary_history_pay_grade", columnList = "pay_grade_id")
})
public class SalaryHistory extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-salary-history")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_grade_id")
    @JsonBackReference("paygrade-salary-history")
    private PayGrade payGrade;

    @Column(name = "previous_salary", precision = 12, scale = 2)
    private BigDecimal previousSalary;

    @Column(name = "new_salary", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "New salary is required")
    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private BigDecimal newSalary;

    @Column(name = "effective_date", nullable = false)
    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_reason", nullable = false)
    @NotNull(message = "Change reason is required")
    private SalaryChangeReason changeReason;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;

    @Column(name = "approved_by", length = 100)
    @Size(max = 100, message = "Approved by must be at most 100 characters")
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "percentage_increase", precision = 5, scale = 2)
    private BigDecimal percentageIncrease;

    // Default constructor
    public SalaryHistory() {
    }

    // Constructor with essential fields
    public SalaryHistory(Employee employee, BigDecimal newSalary, LocalDate effectiveDate, SalaryChangeReason changeReason) {
        this.employee = employee;
        this.newSalary = newSalary;
        this.effectiveDate = effectiveDate;
        this.changeReason = changeReason;
        calculatePercentageIncrease();
    }

    // Calculate percentage increase automatically
    @PrePersist
    @PreUpdate
    private void calculatePercentageIncrease() {
        if (previousSalary != null && newSalary != null && previousSalary.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal increase = newSalary.subtract(previousSalary);
            this.percentageIncrease = increase.divide(previousSalary, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
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

    public PayGrade getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(PayGrade payGrade) {
        this.payGrade = payGrade;
    }

    public BigDecimal getPreviousSalary() {
        return previousSalary;
    }

    public void setPreviousSalary(BigDecimal previousSalary) {
        this.previousSalary = previousSalary;
    }

    public BigDecimal getNewSalary() {
        return newSalary;
    }

    public void setNewSalary(BigDecimal newSalary) {
        this.newSalary = newSalary;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public SalaryChangeReason getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(SalaryChangeReason changeReason) {
        this.changeReason = changeReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public BigDecimal getPercentageIncrease() {
        return percentageIncrease;
    }

    public void setPercentageIncrease(BigDecimal percentageIncrease) {
        this.percentageIncrease = percentageIncrease;
    }

    // Helper methods
    public boolean isIncrease() {
        return previousSalary != null && newSalary.compareTo(previousSalary) > 0;
    }

    public boolean isDecrease() {
        return previousSalary != null && newSalary.compareTo(previousSalary) < 0;
    }

    public BigDecimal getSalaryDifference() {
        return previousSalary != null ? newSalary.subtract(previousSalary) : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "SalaryHistory{" +
                "id=" + id +
                ", employeeId=" + (employee != null ? employee.getId() : null) +
                ", previousSalary=" + previousSalary +
                ", newSalary=" + newSalary +
                ", effectiveDate=" + effectiveDate +
                ", changeReason=" + changeReason +
                ", percentageIncrease=" + percentageIncrease +
                '}';
    }
}