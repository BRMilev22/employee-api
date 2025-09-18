package com.example.employee_api.dto.search;

import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.model.enums.EmploymentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for employee search criteria with comprehensive filtering options
 */
public class EmployeeSearchCriteria {
    
    // Basic search fields
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String employeeId;
    private String jobTitle;
    private String phone;
    private String gender;
    
    // Status and employment
    private EmployeeStatus status;
    private EmploymentType employmentType;
    
    // Department and position
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionTitle;
    
    // Manager relationships
    private Long managerId;
    private Boolean hasManager;
    
    // Salary range
    @Min(value = 0, message = "Minimum salary must be non-negative")
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    
    // Date ranges
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDateFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDateTo;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDateFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDateTo;
    
    // Age range
    @Min(value = 0, message = "Minimum age must be non-negative")
    @Max(value = 150, message = "Maximum age must be reasonable")
    private Integer minAge;
    
    @Min(value = 0, message = "Maximum age must be non-negative")
    @Max(value = 150, message = "Maximum age must be reasonable")
    private Integer maxAge;
    
    // Years of service
    @Min(value = 0, message = "Minimum years of service must be non-negative")
    private Integer minYearsOfService;
    private Integer maxYearsOfService;
    
    // Location
    private String city;
    private String state;
    private String postalCode;
    private String address;
    
    // Global search
    private String globalSearch;
    
    // Pagination and sorting
    private int page = 0;
    private int size = 20;
    private String sortBy = "lastName";
    private String sortDirection = "asc";
    
    // Constructors
    public EmployeeSearchCriteria() {}
    
    // Getters and Setters
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
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public EmployeeStatus getStatus() {
        return status;
    }
    
    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }
    
    public EmploymentType getEmploymentType() {
        return employmentType;
    }
    
    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public Long getPositionId() {
        return positionId;
    }
    
    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }
    
    public String getPositionTitle() {
        return positionTitle;
    }
    
    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }
    
    public Long getManagerId() {
        return managerId;
    }
    
    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
    
    public Boolean getHasManager() {
        return hasManager;
    }
    
    public void setHasManager(Boolean hasManager) {
        this.hasManager = hasManager;
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
    
    public LocalDate getHireDateFrom() {
        return hireDateFrom;
    }
    
    public void setHireDateFrom(LocalDate hireDateFrom) {
        this.hireDateFrom = hireDateFrom;
    }
    
    public LocalDate getHireDateTo() {
        return hireDateTo;
    }
    
    public void setHireDateTo(LocalDate hireDateTo) {
        this.hireDateTo = hireDateTo;
    }
    
    public LocalDate getBirthDateFrom() {
        return birthDateFrom;
    }
    
    public void setBirthDateFrom(LocalDate birthDateFrom) {
        this.birthDateFrom = birthDateFrom;
    }
    
    public LocalDate getBirthDateTo() {
        return birthDateTo;
    }
    
    public void setBirthDateTo(LocalDate birthDateTo) {
        this.birthDateTo = birthDateTo;
    }
    
    public Integer getMinAge() {
        return minAge;
    }
    
    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }
    
    public Integer getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
    
    public Integer getMinYearsOfService() {
        return minYearsOfService;
    }
    
    public void setMinYearsOfService(Integer minYearsOfService) {
        this.minYearsOfService = minYearsOfService;
    }
    
    public Integer getMaxYearsOfService() {
        return maxYearsOfService;
    }
    
    public void setMaxYearsOfService(Integer maxYearsOfService) {
        this.maxYearsOfService = maxYearsOfService;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getGlobalSearch() {
        return globalSearch;
    }
    
    public void setGlobalSearch(String globalSearch) {
        this.globalSearch = globalSearch;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = Math.max(0, page);
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = Math.min(Math.max(1, size), 100); // Limit to 100 per page
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    // Helper methods
    public boolean hasAnySearchCriteria() {
        return firstName != null || lastName != null || fullName != null || email != null ||
               employeeId != null || jobTitle != null || phone != null || gender != null ||
               status != null || employmentType != null || departmentId != null ||
               departmentName != null || positionId != null || positionTitle != null ||
               managerId != null || hasManager != null || minSalary != null || maxSalary != null ||
               hireDateFrom != null || hireDateTo != null || birthDateFrom != null ||
               birthDateTo != null || minAge != null || maxAge != null ||
               minYearsOfService != null || maxYearsOfService != null ||
               city != null || state != null || postalCode != null || address != null ||
               globalSearch != null;
    }
    
    @Override
    public String toString() {
        return "EmployeeSearchCriteria{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", status=" + status +
                ", employmentType=" + employmentType +
                ", departmentId=" + departmentId +
                ", globalSearch='" + globalSearch + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}