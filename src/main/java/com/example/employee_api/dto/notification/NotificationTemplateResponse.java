package com.example.employee_api.dto.notification;

import com.example.employee_api.model.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * DTO for notification template response
 */
public class NotificationTemplateResponse {

    private Long id;
    private String name;
    private NotificationType type;
    private String subjectTemplate;
    private String messageTemplate;
    private String emailTemplate;
    private Boolean isActive;
    private Boolean isSystemTemplate;
    private String description;
    private String variables;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByUsername;
    private String updatedByUsername;

    // Constructors
    public NotificationTemplateResponse() {}

    // Getters and setters
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

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsSystemTemplate() {
        return isSystemTemplate;
    }

    public void setIsSystemTemplate(Boolean systemTemplate) {
        isSystemTemplate = systemTemplate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
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

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }

    @Override
    public String toString() {
        return "NotificationTemplateResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isActive=" + isActive +
                ", isSystemTemplate=" + isSystemTemplate +
                '}';
    }
}