package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.BonusStatus;
import com.example.employee_api.model.enums.BonusType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing employee bonuses.
 * Tracks performance bonuses, signing bonuses, and other monetary rewards.
 */
@Entity
@Table(name = "bonuses", indexes = {
    @Index(name = "idx_bonus_employee", columnList = "employee_id"),
    @Index(name = "idx_bonus_type", columnList = "bonus_type"),
    @Index(name = "idx_bonus_status", columnList = "status"),
    @Index(name = "idx_bonus_award_date", columnList = "award_date"),
    @Index(name = "idx_bonus_payment_date", columnList = "payment_date")
})
public class Bonus extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-bonuses")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "bonus_type", nullable = false)
    @NotNull(message = "Bonus type is required")
    private BonusType bonusType;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "Bonus amount is required")
    @DecimalMin(value = "0.0", message = "Bonus amount must be positive")
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Column(name = "award_date", nullable = false)
    @NotNull(message = "Award date is required")
    private LocalDate awardDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    private BonusStatus status = BonusStatus.PENDING;

    @Column(name = "approved_by", length = 100)
    @Size(max = 100, message = "Approved by must be at most 100 characters")
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "performance_period_start")
    private LocalDate performancePeriodStart;

    @Column(name = "performance_period_end")
    private LocalDate performancePeriodEnd;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;

    @Column(name = "tax_withheld", precision = 12, scale = 2)
    private BigDecimal taxWithheld;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    // Default constructor
    public Bonus() {
    }

    // Constructor with essential fields
    public Bonus(Employee employee, BonusType bonusType, BigDecimal amount, LocalDate awardDate) {
        this.employee = employee;
        this.bonusType = bonusType;
        this.amount = amount;
        this.awardDate = awardDate;
        calculateNetAmount();
    }

    // Calculate net amount after tax withholding
    @PrePersist
    @PreUpdate
    private void calculateNetAmount() {
        if (amount != null) {
            if (taxWithheld != null) {
                this.netAmount = amount.subtract(taxWithheld);
            } else {
                this.netAmount = amount;
            }
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

    public BonusType getBonusType() {
        return bonusType;
    }

    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(LocalDate awardDate) {
        this.awardDate = awardDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BonusStatus getStatus() {
        return status;
    }

    public void setStatus(BonusStatus status) {
        this.status = status;
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

    public LocalDate getPerformancePeriodStart() {
        return performancePeriodStart;
    }

    public void setPerformancePeriodStart(LocalDate performancePeriodStart) {
        this.performancePeriodStart = performancePeriodStart;
    }

    public LocalDate getPerformancePeriodEnd() {
        return performancePeriodEnd;
    }

    public void setPerformancePeriodEnd(LocalDate performancePeriodEnd) {
        this.performancePeriodEnd = performancePeriodEnd;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getTaxWithheld() {
        return taxWithheld;
    }

    public void setTaxWithheld(BigDecimal taxWithheld) {
        this.taxWithheld = taxWithheld;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    // Helper methods
    public boolean isPaid() {
        return status == BonusStatus.PAID;
    }

    public boolean isPending() {
        return status == BonusStatus.PENDING;
    }

    public boolean isApproved() {
        return status == BonusStatus.APPROVED || status == BonusStatus.PAID;
    }

    public void approve(String approvedBy) {
        this.status = BonusStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvalDate = LocalDate.now();
    }

    public void markAsPaid(LocalDate paymentDate) {
        this.status = BonusStatus.PAID;
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Bonus{" +
                "id=" + id +
                ", employeeId=" + (employee != null ? employee.getId() : null) +
                ", bonusType=" + bonusType +
                ", amount=" + amount +
                ", awardDate=" + awardDate +
                ", status=" + status +
                ", netAmount=" + netAmount +
                '}';
    }
}