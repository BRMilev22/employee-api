package com.example.employee_api.model;

import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a position/job title within the organization
 * This entity tracks job titles, pay grades, requirements, and position history
 */
@Entity
@Table(name = "positions", indexes = {
        @Index(name = "idx_position_title", columnList = "title"),
        @Index(name = "idx_position_level", columnList = "level"),
        @Index(name = "idx_position_department", columnList = "department_id"),
        @Index(name = "idx_position_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Position title is required")
    @Size(max = 100, message = "Position title must not exceed 100 characters")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 1000, message = "Position description must not exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Position level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private PositionLevel level;

    @NotNull(message = "Position status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PositionStatus status;

    // Department this position belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference("department-positions")
    private Department department;

    // Salary range for this position
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Minimum salary format is invalid")
    @Column(name = "min_salary", precision = 12, scale = 2)
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Maximum salary format is invalid")
    @Column(name = "max_salary", precision = 12, scale = 2)
    private BigDecimal maxSalary;

    // Pay grade
    @Size(max = 10, message = "Pay grade must not exceed 10 characters")
    @Column(name = "pay_grade", length = 10)
    private String payGrade;

    // Required qualifications and skills
    @Column(name = "required_qualifications", columnDefinition = "TEXT")
    private String requiredQualifications;

    @Column(name = "preferred_qualifications", columnDefinition = "TEXT")
    private String preferredQualifications;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills;

    // Experience requirements
    @Min(value = 0, message = "Minimum experience years cannot be negative")
    @Column(name = "min_experience_years")
    private Integer minExperienceYears;

    @Min(value = 0, message = "Maximum experience years cannot be negative")
    @Column(name = "max_experience_years")
    private Integer maxExperienceYears;

    // Reports to (another position)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_to_position_id")
    @JsonBackReference("position-subordinates")
    private Position reportsTo;

    @OneToMany(mappedBy = "reportsTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("position-subordinates")
    private List<Position> subordinatePositions = new ArrayList<>();

    // Employees who have held this position (history tracking)
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("position-assignments")
    private List<EmployeePositionHistory> employeePositionHistories = new ArrayList<>();

    // Current active employees in this position
    @OneToMany(mappedBy = "currentPosition", fetch = FetchType.LAZY)
    @JsonManagedReference("position-current-employees")
    private List<Employee> currentEmployees = new ArrayList<>();

    // Number of open positions
    @Min(value = 0, message = "Number of openings cannot be negative")
    @Column(name = "number_of_openings")
    private Integer numberOfOpenings = 0;

    // Total headcount for this position
    @Min(value = 0, message = "Total headcount cannot be negative")
    @Column(name = "total_headcount")
    private Integer totalHeadcount = 1;

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
    public Position() {}

    public Position(String title, PositionLevel level, Department department) {
        this.title = title;
        this.level = level;
        this.department = department;
        this.status = PositionStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public PositionLevel getLevel() {
        return level;
    }

    public void setLevel(PositionLevel level) {
        this.level = level;
    }

    public PositionStatus getStatus() {
        return status;
    }

    public void setStatus(PositionStatus status) {
        this.status = status;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(String payGrade) {
        this.payGrade = payGrade;
    }

    public String getRequiredQualifications() {
        return requiredQualifications;
    }

    public void setRequiredQualifications(String requiredQualifications) {
        this.requiredQualifications = requiredQualifications;
    }

    public String getPreferredQualifications() {
        return preferredQualifications;
    }

    public void setPreferredQualifications(String preferredQualifications) {
        this.preferredQualifications = preferredQualifications;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(String preferredSkills) {
        this.preferredSkills = preferredSkills;
    }

    public Integer getMinExperienceYears() {
        return minExperienceYears;
    }

    public void setMinExperienceYears(Integer minExperienceYears) {
        this.minExperienceYears = minExperienceYears;
    }

    public Integer getMaxExperienceYears() {
        return maxExperienceYears;
    }

    public void setMaxExperienceYears(Integer maxExperienceYears) {
        this.maxExperienceYears = maxExperienceYears;
    }

    public Position getReportsTo() {
        return reportsTo;
    }

    public void setReportsTo(Position reportsTo) {
        this.reportsTo = reportsTo;
    }

    public List<Position> getSubordinatePositions() {
        return subordinatePositions;
    }

    public void setSubordinatePositions(List<Position> subordinatePositions) {
        this.subordinatePositions = subordinatePositions;
    }

    public List<EmployeePositionHistory> getEmployeePositionHistories() {
        return employeePositionHistories;
    }

    public void setEmployeePositionHistories(List<EmployeePositionHistory> employeePositionHistories) {
        this.employeePositionHistories = employeePositionHistories;
    }

    public List<Employee> getCurrentEmployees() {
        return currentEmployees;
    }

    public void setCurrentEmployees(List<Employee> currentEmployees) {
        this.currentEmployees = currentEmployees;
    }

    public Integer getNumberOfOpenings() {
        return numberOfOpenings;
    }

    public void setNumberOfOpenings(Integer numberOfOpenings) {
        this.numberOfOpenings = numberOfOpenings;
    }

    public Integer getTotalHeadcount() {
        return totalHeadcount;
    }

    public void setTotalHeadcount(Integer totalHeadcount) {
        this.totalHeadcount = totalHeadcount;
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
    public boolean hasOpenings() {
        return numberOfOpenings != null && numberOfOpenings > 0;
    }

    public boolean isActive() {
        return status == PositionStatus.ACTIVE;
    }

    public boolean canAcceptNewEmployees() {
        return isActive() && hasOpenings();
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", level=" + level +
                ", status=" + status +
                ", department=" + (department != null ? department.getName() : null) +
                '}';
    }
}