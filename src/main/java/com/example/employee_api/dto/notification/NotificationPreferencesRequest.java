package com.example.employee_api.dto.notification;

import com.example.employee_api.model.enums.NotificationType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for notification preferences request
 */
public class NotificationPreferencesRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    private Boolean inAppEnabled = true;
    private Boolean emailEnabled = true;
    private Boolean smsEnabled = false;
    private Boolean pushEnabled = true;
    private String quietHoursStart;
    private String quietHoursEnd;
    private Boolean weekendEnabled = false;
    private Integer frequencyLimit;

    // Constructors
    public NotificationPreferencesRequest() {}

    public NotificationPreferencesRequest(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    // Getters and setters
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

    @Override
    public String toString() {
        return "NotificationPreferencesRequest{" +
                "notificationType=" + notificationType +
                ", inAppEnabled=" + inAppEnabled +
                ", emailEnabled=" + emailEnabled +
                '}';
    }
}