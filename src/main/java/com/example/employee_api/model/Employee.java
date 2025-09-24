package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.model.enums.EmploymentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_email", columnList = "email", unique = true),
    @Index(name = "idx_employee_employee_id", columnList = "employee_id", unique = true),
    @Index(name = "idx_employee_status", columnList = "status"),
    @Index(name = "idx_employee_department", columnList = "department_id"),
    @Index(name = "idx_employee_manager", columnList = "manager_id"),
    @Index(name = "idx_employee_current_position", columnList = "current_position_id")
})
public class Employee extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    @Column(name = "employee_id", nullable = false, unique = true, length = 20)
    private String employeeId;
    
    @NotNull
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @NotNull
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @NotNull
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Phone number format is invalid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Birth date must be in the past")
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @NotNull(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "state", length = 100)
    private String state;
    
    @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$", message = "Postal code format is invalid")
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "country", length = 100)
    private String country;
    
    @NotNull
    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;
    
    // Department relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference("department-employees")
    private Department department;
    
    // Current position relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_position_id")
    @JsonBackReference("position-current-employees")
    private Position currentPosition;
    
    // Position history
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("employee-position-history")
    private List<EmployeePositionHistory> positionHistory = new ArrayList<>();
    
    // Manager-Subordinate relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonBackReference("manager-subordinates")
    private Employee manager;
    
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonManagedReference("manager-subordinates")
    private List<Employee> subordinates = new ArrayList<>();
    
    // Performance Management relationships
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("employee-reviews")
    private List<PerformanceReview> performanceReviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "reviewer", fetch = FetchType.LAZY)
    @JsonManagedReference("reviewer-reviews")
    private List<PerformanceReview> reviewsAsReviewer = new ArrayList<>();
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("employee-goals")
    private List<Goal> goals = new ArrayList<>();

    // Payroll relationships
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("employee-salary-history")
    private List<SalaryHistory> salaryHistory = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("employee-bonuses")
    private List<Bonus> bonuses = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("employee-deductions")
    private List<Deduction> deductions = new ArrayList<>();

    // Document relationships
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Document> documents = new ArrayList<>();
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    // Pay grade relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_grade_id")
    @JsonBackReference("paygrade-employees")
    private PayGrade payGrade;
    
    @NotNull(message = "Employment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;
    
    @NotNull(message = "Employee status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;
    
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Emergency contact phone format is invalid")
    @Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
    
    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;
    
    @Pattern(regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{4}$", message = "SSN format should be XXX-XX-XXXX")
    @Column(name = "ssn", length = 11)
    private String ssn;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;
    
    // Default constructor
    public Employee() {
        this.status = EmployeeStatus.ACTIVE;
        this.employmentType = EmploymentType.FULL_TIME;
    }
    
    // Constructor with required parameters
    public Employee(String employeeId, String firstName, String lastName, String email, String jobTitle) {
        this();
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.jobTitle = jobTitle;
    }
    
    // Computed properties
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }
    
    public boolean isTerminated() {
        return status == EmployeeStatus.TERMINATED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
    }

    public List<EmployeePositionHistory> getPositionHistory() {
        return positionHistory;
    }

    public void setPositionHistory(List<EmployeePositionHistory> positionHistory) {
        this.positionHistory = positionHistory;
    }
    
    public Employee getManager() {
        return manager;
    }
    
    public void setManager(Employee manager) {
        this.manager = manager;
    }
    
    public Long getManagerId() {
        return manager != null ? manager.getId() : null;
    }
    
    public List<Employee> getSubordinates() {
        return subordinates;
    }
    
    public void setSubordinates(List<Employee> subordinates) {
        this.subordinates = subordinates;
    }
    
    public List<PerformanceReview> getPerformanceReviews() {
        return performanceReviews;
    }
    
    public void setPerformanceReviews(List<PerformanceReview> performanceReviews) {
        this.performanceReviews = performanceReviews;
    }
    
    public List<PerformanceReview> getReviewsAsReviewer() {
        return reviewsAsReviewer;
    }
    
    public void setReviewsAsReviewer(List<PerformanceReview> reviewsAsReviewer) {
        this.reviewsAsReviewer = reviewsAsReviewer;
    }
    
    public List<Goal> getGoals() {
        return goals;
    }
    
    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    public List<SalaryHistory> getSalaryHistory() {
        return salaryHistory;
    }

    public void setSalaryHistory(List<SalaryHistory> salaryHistory) {
        this.salaryHistory = salaryHistory;
    }

    public List<Bonus> getBonuses() {
        return bonuses;
    }

    public void setBonuses(List<Bonus> bonuses) {
        this.bonuses = bonuses;
    }

    public List<Deduction> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<Deduction> deductions) {
        this.deductions = deductions;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    
    public LocalDate getTerminationDate() {
        return terminationDate;
    }
    
    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }
    
    public BigDecimal getSalary() {
        return salary;
    }
    
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public PayGrade getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(PayGrade payGrade) {
        this.payGrade = payGrade;
    }
    
    public EmploymentType getEmploymentType() {
        return employmentType;
    }
    
    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }
    
    public EmployeeStatus getStatus() {
        return status;
    }
    
    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }
    
    public String getEmergencyContactName() {
        return emergencyContactName;
    }
    
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }
    
    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }
    
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }
    
    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }
    
    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }
    
    public String getSsn() {
        return ssn;
    }
    
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    // Hierarchy utility methods
    
    /**
     * Add a subordinate to this employee (manager)
     */
    public void addSubordinate(Employee subordinate) {
        if (subordinate != null) {
            subordinates.add(subordinate);
            subordinate.setManager(this);
        }
    }
    
    /**
     * Remove a subordinate from this employee (manager)
     */
    public void removeSubordinate(Employee subordinate) {
        if (subordinate != null) {
            subordinates.remove(subordinate);
            subordinate.setManager(null);
        }
    }
    
    /**
     * Check if this employee is a manager (has subordinates)
     */
    public boolean isManager() {
        return subordinates != null && !subordinates.isEmpty();
    }
    
    /**
     * Check if this employee has a manager
     */
    public boolean hasManager() {
        return manager != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) &&
               Objects.equals(employeeId, employee.employeeId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId);
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeId='" + employeeId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", status=" + status +
                ", employmentType=" + employmentType +
                '}';
    }
}