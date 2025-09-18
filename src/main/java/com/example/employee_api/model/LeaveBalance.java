package com.example.employee_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * Entity representing employee leave balances
 */
@Entity
@Table(name = "leave_balances", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "`year`"}))
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Year is required")
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @NotNull(message = "Allocated days is required")
    @Min(value = 0, message = "Allocated days must be non-negative")
    @Column(name = "allocated_days", nullable = false)
    private Double allocatedDays;

    @Min(value = 0, message = "Used days must be non-negative")
    @Column(name = "used_days")
    private Double usedDays = 0.0;

    @Min(value = 0, message = "Pending days must be non-negative")
    @Column(name = "pending_days")
    private Double pendingDays = 0.0;

    @Min(value = 0, message = "Carry forward days must be non-negative")
    @Column(name = "carry_forward_days")
    private Double carryForwardDays = 0.0;

    @Column(name = "remaining_days")
    private Double remainingDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public LeaveBalance() {}

    public LeaveBalance(Employee employee, LeaveType leaveType, Integer year, Double allocatedDays) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.year = year;
        this.allocatedDays = allocatedDays;
        calculateRemainingDays();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateRemainingDays();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateRemainingDays();
    }

    public void calculateRemainingDays() {
        this.remainingDays = (allocatedDays != null ? allocatedDays : 0.0) 
                           + (carryForwardDays != null ? carryForwardDays : 0.0)
                           - (usedDays != null ? usedDays : 0.0)
                           - (pendingDays != null ? pendingDays : 0.0);
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

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getAllocatedDays() {
        return allocatedDays;
    }

    public void setAllocatedDays(Double allocatedDays) {
        this.allocatedDays = allocatedDays;
        calculateRemainingDays();
    }

    public Double getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(Double usedDays) {
        this.usedDays = usedDays;
        calculateRemainingDays();
    }

    public Double getPendingDays() {
        return pendingDays;
    }

    public void setPendingDays(Double pendingDays) {
        this.pendingDays = pendingDays;
        calculateRemainingDays();
    }

    public Double getCarryForwardDays() {
        return carryForwardDays;
    }

    public void setCarryForwardDays(Double carryForwardDays) {
        this.carryForwardDays = carryForwardDays;
        calculateRemainingDays();
    }

    public Double getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(Double remainingDays) {
        this.remainingDays = remainingDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}