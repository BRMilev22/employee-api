package com.example.employee_api.dto.notification;

import com.example.employee_api.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for notification template request
 */
public class NotificationTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Subject template is required")
    @Size(max = 200, message = "Subject template cannot exceed 200 characters")
    private String subjectTemplate;

    @NotBlank(message = "Message template is required")
    @Size(max = 2000, message = "Message template cannot exceed 2000 characters")
    private String messageTemplate;

    @Size(max = 5000, message = "Email template cannot exceed 5000 characters")
    private String emailTemplate;

    private Boolean isActive = true;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 1000, message = "Variables cannot exceed 1000 characters")
    private String variables;

    // Constructors
    public NotificationTemplateRequest() {}

    public NotificationTemplateRequest(String name, NotificationType type, String subjectTemplate, String messageTemplate) {
        this.name = name;
        this.type = type;
        this.subjectTemplate = subjectTemplate;
        this.messageTemplate = messageTemplate;
    }

    // Getters and setters
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

    @Override
    public String toString() {
        return "NotificationTemplateRequest{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", isActive=" + isActive +
                '}';
    }
}