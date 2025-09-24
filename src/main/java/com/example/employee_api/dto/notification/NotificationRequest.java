package com.example.employee_api.dto.notification;

import com.example.employee_api.model.enums.NotificationType;
import com.example.employee_api.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for creating a new notification
 */
public class NotificationRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    private Long senderId;

    private String relatedEntityType;

    private Long relatedEntityId;

    private String actionUrl;

    private LocalDateTime scheduledFor;

    private LocalDateTime expiresAt;

    private Boolean sendEmail = false;

    // Constructors
    public NotificationRequest() {}

    public NotificationRequest(String title, String message, NotificationType type, Priority priority, Long recipientId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.recipientId = recipientId;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", recipientId=" + recipientId +
                '}';
    }
}