package com.example.employee_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing the history of employee position assignments
 * This tracks when employees held specific positions, including salary and status changes
 */
@Entity
@Table(name = "employee_position_history", indexes = {
        @Index(name = "idx_eph_employee", columnList = "employee_id"),
        @Index(name = "idx_eph_position", columnList = "position_id"),
        @Index(name = "idx_eph_start_date", columnList = "start_date"),
        @Index(name = "idx_eph_end_date", columnList = "end_date"),
        @Index(name = "idx_eph_is_current", columnList = "is_current")
})
@EntityListeners(AuditingEntityListener.class)
public class EmployeePositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee who held this position
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-position-history")
    private Employee employee;

    // Position that was held
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    @JsonBackReference("position-assignments")
    private Position position;

    // Date when employee started in this position
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Date when employee ended in this position (null if current)
    @Column(name = "end_date")
    private LocalDate endDate;

    // Whether this is the current position for the employee
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    // Salary when starting this position
    @DecimalMin(value = "0.0", inclusive = false, message = "Starting salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Starting salary format is invalid")
    @Column(name = "starting_salary", precision = 12, scale = 2)
    private BigDecimal startingSalary;

    // Salary when ending this position (or current salary if still in position)
    @DecimalMin(value = "0.0", inclusive = false, message = "Ending salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Ending salary format is invalid")
    @Column(name = "ending_salary", precision = 12, scale = 2)
    private BigDecimal endingSalary;

    // Reason for position change
    @Size(max = 500, message = "Change reason must not exceed 500 characters")
    @Column(name = "change_reason", length = 500)
    private String changeReason;

    // Notes about this position assignment
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Department at the time (may be different from current position department)
    @Size(max = 100, message = "Department at time must not exceed 100 characters")
    @Column(name = "department_at_time", length = 100)
    private String departmentAtTime;

    // Manager at the time
    @Size(max = 100, message = "Manager at time must not exceed 100 characters")
    @Column(name = "manager_at_time", length = 100)
    private String managerAtTime;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Size(max = 50, message = "Created by must not exceed 50 characters")
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @Size(max = 50, message = "Updated by must not exceed 50 characters")
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Constructors
    public EmployeePositionHistory() {}

    public EmployeePositionHistory(Employee employee, Position position, LocalDate startDate) {
        this.employee = employee;
        this.position = position;
        this.startDate = startDate;
        this.isCurrent = true;
    }

    public EmployeePositionHistory(Employee employee, Position position, LocalDate startDate, BigDecimal startingSalary) {
        this.employee = employee;
        this.position = position;
        this.startDate = startDate;
        this.startingSalary = startingSalary;
        this.isCurrent = true;
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public BigDecimal getStartingSalary() {
        return startingSalary;
    }

    public void setStartingSalary(BigDecimal startingSalary) {
        this.startingSalary = startingSalary;
    }

    public BigDecimal getEndingSalary() {
        return endingSalary;
    }

    public void setEndingSalary(BigDecimal endingSalary) {
        this.endingSalary = endingSalary;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDepartmentAtTime() {
        return departmentAtTime;
    }

    public void setDepartmentAtTime(String departmentAtTime) {
        this.departmentAtTime = departmentAtTime;
    }

    public String getManagerAtTime() {
        return managerAtTime;
    }

    public void setManagerAtTime(String managerAtTime) {
        this.managerAtTime = managerAtTime;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Business methods
    public boolean isCurrentPosition() {
        return isCurrent != null && isCurrent;
    }

    public boolean isActiveAssignment() {
        return endDate == null;
    }

    public long getDurationInDays() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, end);
    }

    public BigDecimal getSalaryIncrease() {
        if (startingSalary != null && endingSalary != null) {
            return endingSalary.subtract(startingSalary);
        }
        return BigDecimal.ZERO;
    }

    public void endAssignment(LocalDate endDate, String reason) {
        this.endDate = endDate;
        this.isCurrent = false;
        this.changeReason = reason;
    }

    @Override
    public String toString() {
        return "EmployeePositionHistory{" +
                "id=" + id +
                ", employee=" + (employee != null ? employee.getFirstName() + " " + employee.getLastName() : null) +
                ", position=" + (position != null ? position.getTitle() : null) +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isCurrent=" + isCurrent +
                '}';
    }
}