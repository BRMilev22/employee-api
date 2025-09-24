package com.example.employee_api.model;

import com.example.employee_api.model.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * User notification preferences entity
 */
@Entity
@Table(name = "notification_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_type"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "quiet_hours_start")
    private String quietHoursStart; // Format: "HH:mm"

    @Column(name = "quiet_hours_end")
    private String quietHoursEnd; // Format: "HH:mm"

    @Column(name = "weekend_enabled", nullable = false)
    private Boolean weekendEnabled = false;

    @Column(name = "frequency_limit")
    private Integer frequencyLimit; // Max notifications per day for this type

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public NotificationPreferences() {}

    public NotificationPreferences(User user, NotificationType notificationType) {
        this.user = user;
        this.notificationType = notificationType;
        this.inAppEnabled = true;
        this.emailEnabled = true;
        this.smsEnabled = false;
        this.pushEnabled = true;
        this.weekendEnabled = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (inAppEnabled == null) {
            inAppEnabled = true;
        }
        if (emailEnabled == null) {
            emailEnabled = true;
        }
        if (smsEnabled == null) {
            smsEnabled = false;
        }
        if (pushEnabled == null) {
            pushEnabled = true;
        }
        if (weekendEnabled == null) {
            weekendEnabled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isNotificationAllowed() {
        return inAppEnabled || emailEnabled || smsEnabled || pushEnabled;
    }

    public boolean isQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        try {
            String currentTime = String.format("%02d:%02d", 
                LocalDateTime.now().getHour(), 
                LocalDateTime.now().getMinute());
            
            return currentTime.compareTo(quietHoursStart) >= 0 && 
                   currentTime.compareTo(quietHoursEnd) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isWeekend() {
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }

    public boolean shouldSendNotification() {
        if (!isNotificationAllowed()) {
            return false;
        }
        
        if (isWeekend() && !weekendEnabled) {
            return false;
        }
        
        if (isQuietHours()) {
            return false;
        }
        
        return true;
    }

    // Getters and setters
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
        return "NotificationPreferences{" +
                "id=" + id +
                ", notificationType=" + notificationType +
                ", inAppEnabled=" + inAppEnabled +
                ", emailEnabled=" + emailEnabled +
                ", smsEnabled=" + smsEnabled +
                ", pushEnabled=" + pushEnabled +
                '}';
    }
}