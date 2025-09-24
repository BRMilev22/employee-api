package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.GoalPriority;
import com.example.employee_api.model.enums.GoalStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "goals", indexes = {
    @Index(name = "idx_goal_employee", columnList = "employee_id"),
    @Index(name = "idx_goal_review", columnList = "performance_review_id"),
    @Index(name = "idx_goal_status", columnList = "status"),
    @Index(name = "idx_goal_priority", columnList = "priority"),
    @Index(name = "idx_goal_due_date", columnList = "due_date")
})
public class Goal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-goals")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id")
    @JsonBackReference("review-goals")
    private PerformanceReview performanceReview;

    @NotNull
    @NotBlank(message = "Goal title is required")
    @Size(max = 200, message = "Goal title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 1000, message = "Goal description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private GoalPriority priority = GoalPriority.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @DecimalMin(value = "0.0", message = "Progress percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Progress percentage must not exceed 100")
    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @Size(max = 2000, message = "Success criteria must not exceed 2000 characters")
    @Column(name = "success_criteria", length = 2000)
    private String successCriteria;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "weight", precision = 3, scale = 2)
    private BigDecimal weight;

    // Constructors
    public Goal() {}

    public Goal(Employee employee, String title, GoalPriority priority) {
        this.employee = employee;
        this.title = title;
        this.priority = priority;
    }

    public Goal(Employee employee, String title, String description, GoalPriority priority, LocalDate dueDate) {
        this.employee = employee;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
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

    public PerformanceReview getPerformanceReview() {
        return performanceReview;
    }

    public void setPerformanceReview(PerformanceReview performanceReview) {
        this.performanceReview = performanceReview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GoalPriority getPriority() {
        return priority;
    }

    public void setPriority(GoalPriority priority) {
        this.priority = priority;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getSuccessCriteria() {
        return successCriteria;
    }

    public void setSuccessCriteria(String successCriteria) {
        this.successCriteria = successCriteria;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    // Helper methods
    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && status != GoalStatus.COMPLETED;
    }

    public boolean isCompleted() {
        return status == GoalStatus.COMPLETED;
    }

    public void markAsCompleted() {
        this.status = GoalStatus.COMPLETED;
        this.completedDate = LocalDate.now();
        this.progressPercentage = BigDecimal.valueOf(100);
    }

    public void updateProgress(BigDecimal newProgress) {
        this.progressPercentage = newProgress;
        if (newProgress.compareTo(BigDecimal.valueOf(100)) >= 0 && status != GoalStatus.COMPLETED) {
            markAsCompleted();
        } else if (newProgress.compareTo(BigDecimal.ZERO) > 0 && status == GoalStatus.NOT_STARTED) {
            this.status = GoalStatus.IN_PROGRESS;
        }
    }

    // equals, hashCode and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return Objects.equals(id, goal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", progressPercentage=" + progressPercentage +
                '}';
    }
}