package com.example.employee_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuditLogRequestDto {
    
    @NotBlank(message = "Action type is required")
    @Size(max = 100, message = "Action type must not exceed 100 characters")
    private String actionType;
    
    @Size(max = 100, message = "Entity type must not exceed 100 characters")
    private String entityType;
    
    private Long entityId;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Boolean securityEvent;
    
    private String oldValues; // JSON string
    
    private String newValues; // JSON string
    
    private Boolean success;
    
    private String errorMessage;
    
    // Constructors
    public AuditLogRequestDto() {}
    
    public AuditLogRequestDto(String actionType, String description) {
        this.actionType = actionType;
        this.description = description;
    }
    
    // Getters and Setters
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getSecurityEvent() {
        return securityEvent;
    }
    
    public void setSecurityEvent(Boolean securityEvent) {
        this.securityEvent = securityEvent;
    }
    
    public String getOldValues() {
        return oldValues;
    }
    
    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }
    
    public String getNewValues() {
        return newValues;
    }
    
    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}