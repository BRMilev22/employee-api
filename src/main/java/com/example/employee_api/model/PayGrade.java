package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.PayGradeStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing the pay grade structure for compensation management.
 * Defines salary ranges and benefits for different job levels.
 * 
 * @author Employee API
 * @since 1.0
 */
@Entity
@Table(name = "pay_grades", indexes = {
    @Index(name = "idx_pay_grade_status", columnList = "status"),
    @Index(name = "idx_pay_grade_level", columnList = "level")
})
public class PayGrade extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_code", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Grade code is required")
    @Size(max = 20, message = "Grade code must not exceed 20 characters")
    private String gradeCode;

    @Column(name = "grade_name", nullable = false, length = 100)
    @NotBlank(message = "Grade name is required")
    @Size(max = 100, message = "Grade name must not exceed 100 characters")
    private String gradeName;

    @Column(name = "level", nullable = false, unique = true)
    @NotNull(message = "Pay grade level is required")
    private Integer level;

    @Column(name = "grade_level", nullable = false)
    @NotNull(message = "Grade level is required")
    private Integer gradeLevel;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Pay grade title is required")
    @Size(max = 100, message = "Pay grade title must not exceed 100 characters")
    private String title;

    @Column(name = "description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "min_salary", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Minimum salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum salary must be greater than 0")
    private BigDecimal minSalary;

    @Column(name = "max_salary", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Maximum salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum salary must be greater than 0")
    private BigDecimal maxSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Pay grade status is required")
    private PayGradeStatus status = PayGradeStatus.ACTIVE;

    @OneToMany(mappedBy = "payGrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("paygrade-employees")
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "payGrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("paygrade-salary-history")
    private List<SalaryHistory> salaryHistories = new ArrayList<>();

    // Default constructor
    public PayGrade() {}

    // Constructor with required fields
    public PayGrade(Integer level, String title, BigDecimal minSalary, BigDecimal maxSalary) {
        this.level = level;
        this.title = title;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.status = PayGradeStatus.ACTIVE;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGradeCode() {
        return gradeCode;
    }

    public void setGradeCode(String gradeCode) {
        this.gradeCode = gradeCode;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public PayGradeStatus getStatus() {
        return status;
    }

    public void setStatus(PayGradeStatus status) {
        this.status = status;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<SalaryHistory> getSalaryHistories() {
        return salaryHistories;
    }

    public void setSalaryHistories(List<SalaryHistory> salaryHistories) {
        this.salaryHistories = salaryHistories;
    }

    // Business methods
    public boolean isWithinRange(BigDecimal salary) {
        if (salary == null || minSalary == null || maxSalary == null) {
            return false;
        }
        return salary.compareTo(minSalary) >= 0 && salary.compareTo(maxSalary) <= 0;
    }

    public BigDecimal getSalaryRange() {
        if (maxSalary == null || minSalary == null) {
            return BigDecimal.ZERO;
        }
        return maxSalary.subtract(minSalary);
    }

    public BigDecimal getMidPointSalary() {
        if (maxSalary == null || minSalary == null) {
            return BigDecimal.ZERO;
        }
        return minSalary.add(maxSalary).divide(BigDecimal.valueOf(2));
    }

    public boolean isActive() {
        return status == PayGradeStatus.ACTIVE;
    }

    public int getEmployeeCount() {
        return employees != null ? employees.size() : 0;
    }

    // Utility methods
    @Override
    public String toString() {
        return "PayGrade{" +
                "id=" + id +
                ", level=" + level +
                ", title='" + title + '\'' +
                ", minSalary=" + minSalary +
                ", maxSalary=" + maxSalary +
                ", status=" + status +
                ", employeeCount=" + getEmployeeCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayGrade)) return false;
        PayGrade payGrade = (PayGrade) o;
        return getId() != null && getId().equals(payGrade.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Validates if this pay grade configuration is valid.
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return level != null && level > 0 &&
               title != null && !title.trim().isEmpty() &&
               minSalary != null && minSalary.compareTo(BigDecimal.ZERO) > 0 &&
               maxSalary != null && maxSalary.compareTo(BigDecimal.ZERO) > 0 &&
               maxSalary.compareTo(minSalary) >= 0 &&
               status != null;
    }

    /**
     * Creates a new salary history entry for this pay grade.
     * @param employee the employee
     * @param previousSalary the previous salary
     * @param newSalary the new salary
     * @param notes the notes for change
     * @return the created salary history entry
     */
    public SalaryHistory createSalaryHistory(Employee employee, BigDecimal previousSalary, 
                                           BigDecimal newSalary, String notes) {
        SalaryHistory history = new SalaryHistory();
        history.setEmployee(employee);
        history.setPayGrade(this);
        history.setPreviousSalary(previousSalary);
        history.setNewSalary(newSalary);
        history.setNotes(notes);
        
        if (salaryHistories == null) {
            salaryHistories = new ArrayList<>();
        }
        salaryHistories.add(history);
        
        return history;
    }
}