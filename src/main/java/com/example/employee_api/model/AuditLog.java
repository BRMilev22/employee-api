package com.example.employee_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_action_type", columnList = "action_type"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_security_event", columnList = "security_event")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "username")
    private String username; // For cases where user might be deleted
    
    @NotBlank(message = "Action type is required")
    @Size(max = 100, message = "Action type must not exceed 100 characters")
    @Column(name = "action_type", nullable = false)
    private String actionType; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, ACCESS, etc.
    
    @Size(max = 100, message = "Entity type must not exceed 100 characters")
    @Column(name = "entity_type")
    private String entityType; // Employee, Department, Position, etc.
    
    @Column(name = "entity_id")
    private Long entityId; // ID of the affected entity
    
    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Size(max = 500, message = "User agent must not exceed 500 characters")
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "request_url")
    private String requestUrl;
    
    @Size(max = 10, message = "HTTP method must not exceed 10 characters")
    @Column(name = "http_method")
    private String httpMethod; // GET, POST, PUT, DELETE
    
    @Column(name = "success")
    private Boolean success; // True for successful operations, false for failures
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // For failed operations
    
    @Column(name = "security_event")
    private Boolean securityEvent; // True for security-related events
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON representation of old values for updates
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON representation of new values for updates
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description")
    private String description; // Human-readable description of the action
    
    @Size(max = 50, message = "Session ID must not exceed 50 characters")
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "duration_ms")
    private Long durationMs; // Duration of the operation in milliseconds
    
    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditLog(String actionType, String description) {
        this();
        this.actionType = actionType;
        this.description = description;
    }
    
    public AuditLog(User user, String actionType, String description) {
        this();
        this.user = user;
        this.username = user != null ? user.getUsername() : null;
        this.actionType = actionType;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        this.username = user != null ? user.getUsername() : null;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
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
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", actionType='" + actionType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", timestamp=" + timestamp +
                ", success=" + success +
                ", securityEvent=" + securityEvent +
                ", description='" + description + '\'' +
                '}';
    }
}