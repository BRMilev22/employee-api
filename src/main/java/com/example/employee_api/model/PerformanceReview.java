package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.PerformanceRating;
import com.example.employee_api.model.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "performance_reviews", indexes = {
    @Index(name = "idx_performance_review_employee", columnList = "employee_id"),
    @Index(name = "idx_performance_review_reviewer", columnList = "reviewer_id"),
    @Index(name = "idx_performance_review_status", columnList = "status"),
    @Index(name = "idx_performance_review_period", columnList = "review_period_start, review_period_end"),
    @Index(name = "idx_performance_review_due_date", columnList = "due_date")
})
public class PerformanceReview extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-reviews")
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonBackReference("reviewer-reviews")
    private Employee reviewer;

    @NotNull
    @NotBlank(message = "Review title is required")
    @Size(max = 200, message = "Review title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 1000, message = "Review description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Column(name = "review_period_start", nullable = false)
    private LocalDate reviewPeriodStart;

    @NotNull
    @Column(name = "review_period_end", nullable = false)
    private LocalDate reviewPeriodEnd;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_rating", length = 30)
    private PerformanceRating overallRating;

    @DecimalMin(value = "0.0", message = "Overall score must be non-negative")
    @DecimalMax(value = "10.0", message = "Overall score must not exceed 10.0")
    @Column(name = "overall_score", precision = 3, scale = 1)
    private BigDecimal overallScore;

    @Size(max = 2000, message = "Employee comments must not exceed 2000 characters")
    @Column(name = "employee_comments", length = 2000)
    private String employeeComments;

    @Size(max = 2000, message = "Manager comments must not exceed 2000 characters")
    @Column(name = "manager_comments", length = 2000)
    private String managerComments;

    @Size(max = 2000, message = "HR comments must not exceed 2000 characters")
    @Column(name = "hr_comments", length = 2000)
    private String hrComments;

    @Size(max = 2000, message = "Strengths must not exceed 2000 characters")
    @Column(name = "strengths", length = 2000)
    private String strengths;

    @Size(max = 2000, message = "Areas for improvement must not exceed 2000 characters")
    @Column(name = "areas_for_improvement", length = 2000)
    private String areasForImprovement;

    @Size(max = 2000, message = "Development goals must not exceed 2000 characters")
    @Column(name = "development_goals", length = 2000)
    private String developmentGoals;

    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("review-goals")
    private List<Goal> goals = new ArrayList<>();

    // Constructors
    public PerformanceReview() {}

    public PerformanceReview(Employee employee, Employee reviewer, String title, 
                           LocalDate reviewPeriodStart, LocalDate reviewPeriodEnd, LocalDate dueDate) {
        this.employee = employee;
        this.reviewer = reviewer;
        this.title = title;
        this.reviewPeriodStart = reviewPeriodStart;
        this.reviewPeriodEnd = reviewPeriodEnd;
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

    public Employee getReviewer() {
        return reviewer;
    }

    public void setReviewer(Employee reviewer) {
        this.reviewer = reviewer;
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

    public LocalDate getReviewPeriodStart() {
        return reviewPeriodStart;
    }

    public void setReviewPeriodStart(LocalDate reviewPeriodStart) {
        this.reviewPeriodStart = reviewPeriodStart;
    }

    public LocalDate getReviewPeriodEnd() {
        return reviewPeriodEnd;
    }

    public void setReviewPeriodEnd(LocalDate reviewPeriodEnd) {
        this.reviewPeriodEnd = reviewPeriodEnd;
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

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public PerformanceRating getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(PerformanceRating overallRating) {
        this.overallRating = overallRating;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public String getEmployeeComments() {
        return employeeComments;
    }

    public void setEmployeeComments(String employeeComments) {
        this.employeeComments = employeeComments;
    }

    public String getManagerComments() {
        return managerComments;
    }

    public void setManagerComments(String managerComments) {
        this.managerComments = managerComments;
    }

    public String getHrComments() {
        return hrComments;
    }

    public void setHrComments(String hrComments) {
        this.hrComments = hrComments;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getAreasForImprovement() {
        return areasForImprovement;
    }

    public void setAreasForImprovement(String areasForImprovement) {
        this.areasForImprovement = areasForImprovement;
    }

    public String getDevelopmentGoals() {
        return developmentGoals;
    }

    public void setDevelopmentGoals(String developmentGoals) {
        this.developmentGoals = developmentGoals;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    // Helper methods
    public void addGoal(Goal goal) {
        goals.add(goal);
        goal.setPerformanceReview(this);
    }

    public void removeGoal(Goal goal) {
        goals.remove(goal);
        goal.setPerformanceReview(null);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && status != ReviewStatus.COMPLETED;
    }

    public boolean isCompleted() {
        return status == ReviewStatus.COMPLETED || status == ReviewStatus.APPROVED;
    }

    // equals, hashCode and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceReview that = (PerformanceReview) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PerformanceReview{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", reviewPeriodStart=" + reviewPeriodStart +
                ", reviewPeriodEnd=" + reviewPeriodEnd +
                ", dueDate=" + dueDate +
                ", overallRating=" + overallRating +
                '}';
    }
}