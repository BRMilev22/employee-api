package com.example.employee_api.model;

import com.example.employee_api.model.enums.EmployeeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing the history of employee status changes
 */
@Entity
@Table(name = "employee_status_history",
        indexes = {
                @Index(name = "idx_status_history_employee", columnList = "employee_id"),
                @Index(name = "idx_status_history_date", columnList = "changed_at"),
                @Index(name = "idx_status_history_status", columnList = "new_status")
        })
public class EmployeeStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private EmployeeStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    @NotNull(message = "New status is required")
    private EmployeeStatus newStatus;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime changedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Default constructor
    public EmployeeStatusHistory() {}

    // Constructor for status changes
    public EmployeeStatusHistory(Employee employee, EmployeeStatus previousStatus, 
                               EmployeeStatus newStatus, String reason, String changedBy) {
        this.employee = employee;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
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

    public EmployeeStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(EmployeeStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public EmployeeStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(EmployeeStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "EmployeeStatusHistory{" +
                "id=" + id +
                ", previousStatus=" + previousStatus +
                ", newStatus=" + newStatus +
                ", reason='" + reason + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}