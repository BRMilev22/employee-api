package com.example.employee_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing leave types in the system
 */
@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Leave type name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @NotNull(message = "Days allowed is required")
    @Min(value = 0, message = "Days allowed must be non-negative")
    @Column(name = "days_allowed", nullable = false)
    private Integer daysAllowed;

    @Column(name = "carry_forward")
    private Boolean carryForward = false;

    @Column(name = "max_carry_forward_days")
    private Integer maxCarryForwardDays = 0;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = true;

    @Column(name = "requires_documents")
    private Boolean requiresDocuments = false;

    @Column(name = "minimum_notice_days")
    private Integer minimumNoticeDays = 0;

    @Column(name = "maximum_consecutive_days")
    private Integer maximumConsecutiveDays;

    @Column(name = "applies_to_probation")
    private Boolean appliesToProbation = false;

    @Column(name = "pro_rated")
    private Boolean proRated = true;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "color_code", length = 7)
    private String colorCode = "#007bff";

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "leaveType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "leaveType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveBalance> leaveBalances;

    // Constructors
    public LeaveType() {}

    public LeaveType(String name, String description, Integer daysAllowed) {
        this.name = name;
        this.description = description;
        this.daysAllowed = daysAllowed;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getDaysAllowed() {
        return daysAllowed;
    }

    public void setDaysAllowed(Integer daysAllowed) {
        this.daysAllowed = daysAllowed;
    }

    public Boolean getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(Boolean carryForward) {
        this.carryForward = carryForward;
    }

    public Integer getMaxCarryForwardDays() {
        return maxCarryForwardDays;
    }

    public void setMaxCarryForwardDays(Integer maxCarryForwardDays) {
        this.maxCarryForwardDays = maxCarryForwardDays;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Boolean getRequiresDocuments() {
        return requiresDocuments;
    }

    public void setRequiresDocuments(Boolean requiresDocuments) {
        this.requiresDocuments = requiresDocuments;
    }

    public Integer getMinimumNoticeDays() {
        return minimumNoticeDays;
    }

    public void setMinimumNoticeDays(Integer minimumNoticeDays) {
        this.minimumNoticeDays = minimumNoticeDays;
    }

    public Integer getMaximumConsecutiveDays() {
        return maximumConsecutiveDays;
    }

    public void setMaximumConsecutiveDays(Integer maximumConsecutiveDays) {
        this.maximumConsecutiveDays = maximumConsecutiveDays;
    }

    public Boolean getAppliesToProbation() {
        return appliesToProbation;
    }

    public void setAppliesToProbation(Boolean appliesToProbation) {
        this.appliesToProbation = appliesToProbation;
    }

    public Boolean getProRated() {
        return proRated;
    }

    public void setProRated(Boolean proRated) {
        this.proRated = proRated;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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

    public List<LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<LeaveRequest> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    public List<LeaveBalance> getLeaveBalances() {
        return leaveBalances;
    }

    public void setLeaveBalances(List<LeaveBalance> leaveBalances) {
        this.leaveBalances = leaveBalances;
    }
}