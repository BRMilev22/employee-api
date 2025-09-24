package com.example.employee_api.model;

import com.example.employee_api.model.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Notification template entity for customizable notification messages
 */
@Entity
@Table(name = "notification_templates")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name cannot exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @NotBlank(message = "Subject template is required")
    @Size(max = 200, message = "Subject template cannot exceed 200 characters")
    @Column(name = "subject_template", nullable = false, length = 200)
    private String subjectTemplate;

    @NotBlank(message = "Message template is required")
    @Size(max = 2000, message = "Message template cannot exceed 2000 characters")
    @Column(name = "message_template", nullable = false, length = 2000)
    private String messageTemplate;

    @Column(name = "email_template", length = 5000)
    private String emailTemplate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_system_template", nullable = false)
    private Boolean isSystemTemplate = false;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "variables", length = 1000)
    private String variables; // JSON string of available variables

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    // Constructors
    public NotificationTemplate() {}

    public NotificationTemplate(String name, NotificationType type, String subjectTemplate, String messageTemplate) {
        this.name = name;
        this.type = type;
        this.subjectTemplate = subjectTemplate;
        this.messageTemplate = messageTemplate;
        this.isActive = true;
        this.isSystemTemplate = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isSystemTemplate == null) {
            isSystemTemplate = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String processTemplate(String template, Object... variables) {
        String processed = template;
        for (int i = 0; i < variables.length; i += 2) {
            if (i + 1 < variables.length) {
                String placeholder = "{" + variables[i] + "}";
                String value = variables[i + 1] != null ? variables[i + 1].toString() : "";
                processed = processed.replace(placeholder, value);
            }
        }
        return processed;
    }

    public String processSubject(Object... variables) {
        return processTemplate(subjectTemplate, variables);
    }

    public String processMessage(Object... variables) {
        return processTemplate(messageTemplate, variables);
    }

    public String processEmail(Object... variables) {
        if (emailTemplate == null) {
            return processMessage(variables);
        }
        return processTemplate(emailTemplate, variables);
    }

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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "NotificationTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isActive=" + isActive +
                ", isSystemTemplate=" + isSystemTemplate +
                '}';
    }
}