package com.example.employee_api.model;

import com.example.employee_api.model.enums.DepartmentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Entity
@Table(name = "departments")
@EntityListeners(AuditingEntityListener.class)
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @NotBlank(message = "Department code is required")
    @Size(max = 10, message = "Department code must not exceed 10 characters")
    @Column(name = "department_code", nullable = false, unique = true, length = 10)
    private String departmentCode;
    
    @NotNull
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Column(name = "location", length = 200)
    private String location;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;
    
    @NotNull(message = "Department status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
    
    // Department hierarchy - self-referencing relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    @JsonBackReference("parent-subdepartments")
    private Department parentDepartment;
    
    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("parent-subdepartments")
    private List<Department> subDepartments = new ArrayList<>();
    
    // Department manager (Employee)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonBackReference("department-manager")
    private Employee manager;
    
    // Employees in this department
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("department-employees")
    private List<Employee> employees = new ArrayList<>();
    
    // Cost center for accounting
    @Size(max = 20, message = "Cost center must not exceed 20 characters")
    @Column(name = "cost_center", length = 20)
    private String costCenter;
    
    // Contact information
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Phone number format is invalid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    // Constructors
    public Department() {}
    
    public Department(String departmentCode, String name) {
        this.departmentCode = departmentCode;
        this.name = name;
        this.status = DepartmentStatus.ACTIVE;
    }
    
    public Department(String departmentCode, String name, String description, String location) {
        this.departmentCode = departmentCode;
        this.name = name;
        this.description = description;
        this.location = location;
        this.status = DepartmentStatus.ACTIVE;
    }
    
    // Business methods
    public boolean isActive() {
        return status == DepartmentStatus.ACTIVE;
    }
    
    public boolean hasSubDepartments() {
        return subDepartments != null && !subDepartments.isEmpty();
    }
    
    public boolean hasEmployees() {
        return employees != null && !employees.isEmpty();
    }
    
    public int getEmployeeCount() {
        return employees != null ? employees.size() : 0;
    }
    
    public int getTotalEmployeeCount() {
        int total = getEmployeeCount();
        if (hasSubDepartments()) {
            for (Department subDept : subDepartments) {
                total += subDept.getTotalEmployeeCount();
            }
        }
        return total;
    }
    
    public void addSubDepartment(Department subDepartment) {
        if (subDepartments == null) {
            subDepartments = new ArrayList<>();
        }
        subDepartments.add(subDepartment);
        subDepartment.setParentDepartment(this);
    }
    
    public void removeSubDepartment(Department subDepartment) {
        if (subDepartments != null) {
            subDepartments.remove(subDepartment);
            subDepartment.setParentDepartment(null);
        }
    }
    
    public void addEmployee(Employee employee) {
        if (employees == null) {
            employees = new ArrayList<>();
        }
        employees.add(employee);
        employee.setDepartment(this);
    }
    
    public void removeEmployee(Employee employee) {
        if (employees != null) {
            employees.remove(employee);
            employee.setDepartment(null);
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public BigDecimal getBudget() {
        return budget;
    }
    
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    
    public DepartmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(DepartmentStatus status) {
        this.status = status;
    }
    
    public Department getParentDepartment() {
        return parentDepartment;
    }
    
    public void setParentDepartment(Department parentDepartment) {
        this.parentDepartment = parentDepartment;
    }
    
    public List<Department> getSubDepartments() {
        return subDepartments;
    }
    
    public void setSubDepartments(List<Department> subDepartments) {
        this.subDepartments = subDepartments;
    }
    
    public Employee getManager() {
        return manager;
    }
    
    public void setManager(Employee manager) {
        this.manager = manager;
    }
    
    public List<Employee> getEmployees() {
        return employees;
    }
    
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
    
    public String getCostCenter() {
        return costCenter;
    }
    
    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
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
    
    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", departmentCode='" + departmentCode + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", status=" + status +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department)) return false;
        Department that = (Department) o;
        return departmentCode != null ? departmentCode.equals(that.departmentCode) : that.departmentCode == null;
    }
    
    @Override
    public int hashCode() {
        return departmentCode != null ? departmentCode.hashCode() : 0;
    }
}