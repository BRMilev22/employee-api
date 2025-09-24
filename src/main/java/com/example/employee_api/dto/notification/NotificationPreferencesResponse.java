package com.example.employee_api.dto.notification;

import com.example.employee_api.model.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * DTO for notification preferences response
 */
public class NotificationPreferencesResponse {

    private Long id;
    private NotificationType notificationType;
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
    private Boolean weekendEnabled;
    private Integer frequencyLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public NotificationPreferencesResponse() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Boolean getInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(Boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public Boolean getSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(Boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public Boolean getPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(Boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public String getQuietHoursStart() {
        return quietHoursStart;
    }

    public void setQuietHoursStart(String quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }

    public String getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public void setQuietHoursEnd(String quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }

    public Boolean getWeekendEnabled() {
        return weekendEnabled;
    }

    public void setWeekendEnabled(Boolean weekendEnabled) {
        this.weekendEnabled = weekendEnabled;
    }

    public Integer getFrequencyLimit() {
        return frequencyLimit;
    }

    public void setFrequencyLimit(Integer frequencyLimit) {
        this.frequencyLimit = frequencyLimit;
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

    @Override
    public String toString() {
        return "NotificationPreferencesResponse{" +
                "id=" + id +
                ", notificationType=" + notificationType +
                ", inAppEnabled=" + inAppEnabled +
                ", emailEnabled=" + emailEnabled +
                '}';
    }
}