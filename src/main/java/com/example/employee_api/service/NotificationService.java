package com.example.employee_api.service;

import com.example.employee_api.dto.notification.*;
import com.example.employee_api.exception.ResourceNotFoundException;
import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.NotificationStatus;
import com.example.employee_api.model.enums.Priority;
import com.example.employee_api.repository.UserRepository;
import com.example.employee_api.repository.notification.NotificationRepository;
import com.example.employee_api.repository.notification.NotificationPreferencesRepository;
import com.example.employee_api.repository.notification.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing notifications
 */
@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private NotificationPreferencesRepository preferencesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Notification CRUD operations

    /**
     * Create a new notification
     */
    public NotificationResponse createNotification(NotificationRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with id: " + request.getRecipientId()));

        User sender = null;
        if (request.getSenderId() != null) {
            sender = userRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + request.getSenderId()));
        }

        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setPriority(request.getPriority());
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setRelatedEntityType(request.getRelatedEntityType());
        notification.setRelatedEntityId(request.getRelatedEntityId());
        notification.setActionUrl(request.getActionUrl());
        notification.setScheduledFor(request.getScheduledFor());
        notification.setExpiresAt(request.getExpiresAt());
        notification.setStatus(NotificationStatus.UNREAD);

        notification = notificationRepository.save(notification);

        // Send email if requested and user preferences allow
        if (request.getSendEmail() != null && request.getSendEmail()) {
            sendEmailNotification(notification);
        }

        return convertToResponse(notification);
    }

    /**
     * Create notification using template
     */
    public NotificationResponse createNotificationFromTemplate(String templateName, Long recipientId, Object... variables) {
        NotificationTemplate template = templateRepository.findByName(templateName)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateName));

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with id: " + recipientId));

        String processedTitle = template.processSubject(variables);
        String processedMessage = template.processMessage(variables);

        Notification notification = new Notification();
        notification.setTitle(processedTitle);
        notification.setMessage(processedMessage);
        notification.setType(template.getType());
        notification.setPriority(Priority.MEDIUM); // Default priority for template notifications
        notification.setRecipient(recipient);
        notification.setStatus(NotificationStatus.UNREAD);

        notification = notificationRepository.save(notification);

        // Check user preferences and send email if enabled
        Optional<NotificationPreferences> preferences = preferencesRepository
                .findByUserAndNotificationType(recipient, template.getType());
        
        if (preferences.isPresent() && preferences.get().getEmailEnabled() && preferences.get().shouldSendNotification()) {
            sendEmailNotification(notification, template, variables);
        }

        return convertToResponse(notification);
    }

    /**
     * Get notifications for current user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsForCurrentUser(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser, pageable);
        return notifications.map(this::convertToResponse);
    }

    /**
     * Get notifications by status for current user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Notification> notifications = notificationRepository.findByRecipientAndStatusOrderByCreatedAtDesc(currentUser, status, pageable);
        return notifications.map(this::convertToResponse);
    }

    /**
     * Get unread notifications count for current user
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationsCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByRecipientAndStatus(currentUser, NotificationStatus.UNREAD);
    }

    /**
     * Mark notification as read
     */
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = getNotificationForCurrentUser(notificationId);
        notification.markAsRead();
        notification = notificationRepository.save(notification);
        return convertToResponse(notification);
    }

    /**
     * Mark all notifications as read for current user
     */
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        notificationRepository.markAllAsReadForUser(currentUser, LocalDateTime.now());
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        Notification notification = getNotificationForCurrentUser(notificationId);
        notificationRepository.delete(notification);
    }

    /**
     * Search notifications for current user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> searchNotifications(String searchTerm, Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Notification> notifications = notificationRepository.searchNotifications(currentUser, searchTerm, pageable);
        return notifications.map(this::convertToResponse);
    }

    // Template management

    /**
     * Create notification template
     */
    public NotificationTemplateResponse createTemplate(NotificationTemplateRequest request) {
        if (templateRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Template with name '" + request.getName() + "' already exists");
        }

        NotificationTemplate template = new NotificationTemplate();
        template.setName(request.getName());
        template.setType(request.getType());
        template.setSubjectTemplate(request.getSubjectTemplate());
        template.setMessageTemplate(request.getMessageTemplate());
        template.setEmailTemplate(request.getEmailTemplate());
        template.setIsActive(request.getIsActive());
        template.setDescription(request.getDescription());
        template.setVariables(request.getVariables());
        template.setCreatedBy(getCurrentUser());

        template = templateRepository.save(template);
        return convertToTemplateResponse(template);
    }

    /**
     * Update notification template
     */
    public NotificationTemplateResponse updateTemplate(Long templateId, NotificationTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        if (templateRepository.existsByNameAndIdNot(request.getName(), templateId)) {
            throw new IllegalArgumentException("Template with name '" + request.getName() + "' already exists");
        }

        template.setName(request.getName());
        template.setType(request.getType());
        template.setSubjectTemplate(request.getSubjectTemplate());
        template.setMessageTemplate(request.getMessageTemplate());
        template.setEmailTemplate(request.getEmailTemplate());
        template.setIsActive(request.getIsActive());
        template.setDescription(request.getDescription());
        template.setVariables(request.getVariables());
        template.setUpdatedBy(getCurrentUser());

        template = templateRepository.save(template);
        return convertToTemplateResponse(template);
    }

    /**
     * Get all templates
     */
    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> getAllTemplates(Pageable pageable) {
        Page<NotificationTemplate> templates = templateRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return templates.map(this::convertToTemplateResponse);
    }

    /**
     * Get template by ID
     */
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(Long templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));
        return convertToTemplateResponse(template);
    }

    /**
     * Delete template
     */
    public void deleteTemplate(Long templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));
        
        if (template.getIsSystemTemplate()) {
            throw new IllegalArgumentException("Cannot delete system template");
        }
        
        templateRepository.delete(template);
    }

    // Preferences management

    /**
     * Get preferences for current user
     */
    @Transactional(readOnly = true)
    public List<NotificationPreferencesResponse> getPreferencesForCurrentUser() {
        User currentUser = getCurrentUser();
        List<NotificationPreferences> preferences = preferencesRepository.findByUser(currentUser);
        return preferences.stream()
                .map(this::convertToPreferencesResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update preferences for current user
     */
    public NotificationPreferencesResponse updatePreferences(NotificationPreferencesRequest request) {
        User currentUser = getCurrentUser();
        
        NotificationPreferences preferences = preferencesRepository
                .findByUserAndNotificationType(currentUser, request.getNotificationType())
                .orElse(new NotificationPreferences(currentUser, request.getNotificationType()));

        preferences.setInAppEnabled(request.getInAppEnabled());
        preferences.setEmailEnabled(request.getEmailEnabled());
        preferences.setSmsEnabled(request.getSmsEnabled());
        preferences.setPushEnabled(request.getPushEnabled());
        preferences.setQuietHoursStart(request.getQuietHoursStart());
        preferences.setQuietHoursEnd(request.getQuietHoursEnd());
        preferences.setWeekendEnabled(request.getWeekendEnabled());
        preferences.setFrequencyLimit(request.getFrequencyLimit());

        preferences = preferencesRepository.save(preferences);
        return convertToPreferencesResponse(preferences);
    }

    // Utility methods for workflow integration

    /**
     * Send leave request notification
     */
    public void sendLeaveRequestNotification(Long leaveRequestId, Long managerId, String employeeName) {
        createNotificationFromTemplate("leave_request_submitted", managerId,
                "employeeName", employeeName,
                "leaveRequestId", leaveRequestId,
                "actionUrl", "/leave-requests/" + leaveRequestId);
    }

    /**
     * Send leave approval notification
     */
    public void sendLeaveApprovalNotification(Long leaveRequestId, Long employeeId, String managerName) {
        createNotificationFromTemplate("leave_request_approved", employeeId,
                "managerName", managerName,
                "leaveRequestId", leaveRequestId,
                "actionUrl", "/leave-requests/" + leaveRequestId);
    }

    /**
     * Send performance review notification
     */
    public void sendPerformanceReviewNotification(Long reviewId, Long employeeId, String reviewerName) {
        createNotificationFromTemplate("performance_review_assigned", employeeId,
                "reviewerName", reviewerName,
                "reviewId", reviewId,
                "actionUrl", "/performance/reviews/" + reviewId);
    }

    /**
     * Send document approval notification
     */
    public void sendDocumentApprovalNotification(Long documentId, Long approverId, String employeeName, String documentName) {
        createNotificationFromTemplate("document_approval_required", approverId,
                "employeeName", employeeName,
                "documentName", documentName,
                "documentId", documentId,
                "actionUrl", "/documents/" + documentId);
    }

    // Private helper methods

    private Notification getNotificationForCurrentUser(Long notificationId) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access denied: notification belongs to different user");
        }
        
        return notification;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private void sendEmailNotification(Notification notification) {
        if (notification.getRecipient() != null && notification.getRecipient().getEmail() != null) {
            try {
                emailService.sendNotificationEmail(
                        notification.getRecipient().getEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                );
                notification.setEmailSent(true);
                notificationRepository.save(notification);
            } catch (Exception e) {
                // Log error but don't fail the notification creation
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
    }

    private void sendEmailNotification(Notification notification, NotificationTemplate template, Object... variables) {
        if (notification.getRecipient() != null && notification.getRecipient().getEmail() != null) {
            try {
                String emailContent = template.processEmail(variables);
                emailService.sendNotificationEmail(
                        notification.getRecipient().getEmail(),
                        notification.getTitle(),
                        emailContent
                );
                notification.setEmailSent(true);
                notificationRepository.save(notification);
            } catch (Exception e) {
                // Log error but don't fail the notification creation
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
    }

    // Conversion methods

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setPriority(notification.getPriority());
        response.setStatus(notification.getStatus());
        response.setRecipientUsername(notification.getRecipient().getUsername());
        response.setSenderUsername(notification.getSender() != null ? notification.getSender().getUsername() : null);
        response.setRelatedEntityType(notification.getRelatedEntityType());
        response.setRelatedEntityId(notification.getRelatedEntityId());
        response.setActionUrl(notification.getActionUrl());
        response.setEmailSent(notification.getEmailSent());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(notification.getReadAt());
        response.setScheduledFor(notification.getScheduledFor());
        response.setExpiresAt(notification.getExpiresAt());
        response.setIsExpired(notification.isExpired());
        return response;
    }

    private NotificationTemplateResponse convertToTemplateResponse(NotificationTemplate template) {
        NotificationTemplateResponse response = new NotificationTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setType(template.getType());
        response.setSubjectTemplate(template.getSubjectTemplate());
        response.setMessageTemplate(template.getMessageTemplate());
        response.setEmailTemplate(template.getEmailTemplate());
        response.setIsActive(template.getIsActive());
        response.setIsSystemTemplate(template.getIsSystemTemplate());
        response.setDescription(template.getDescription());
        response.setVariables(template.getVariables());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        response.setCreatedByUsername(template.getCreatedBy() != null ? template.getCreatedBy().getUsername() : null);
        response.setUpdatedByUsername(template.getUpdatedBy() != null ? template.getUpdatedBy().getUsername() : null);
        return response;
    }

    private NotificationPreferencesResponse convertToPreferencesResponse(NotificationPreferences preferences) {
        NotificationPreferencesResponse response = new NotificationPreferencesResponse();
        response.setId(preferences.getId());
        response.setNotificationType(preferences.getNotificationType());
        response.setInAppEnabled(preferences.getInAppEnabled());
        response.setEmailEnabled(preferences.getEmailEnabled());
        response.setSmsEnabled(preferences.getSmsEnabled());
        response.setPushEnabled(preferences.getPushEnabled());
        response.setQuietHoursStart(preferences.getQuietHoursStart());
        response.setQuietHoursEnd(preferences.getQuietHoursEnd());
        response.setWeekendEnabled(preferences.getWeekendEnabled());
        response.setFrequencyLimit(preferences.getFrequencyLimit());
        response.setCreatedAt(preferences.getCreatedAt());
        response.setUpdatedAt(preferences.getUpdatedAt());
        return response;
    }
}