package com.example.employee_api.controller;

import com.example.employee_api.dto.notification.*;
import com.example.employee_api.model.enums.NotificationStatus;
import com.example.employee_api.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications - Get notifications for current user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getNotificationsForCurrentUser(pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/by-status - Get notifications by status
     */
    @GetMapping("/by-status")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByStatus(
            @RequestParam NotificationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getNotificationsByStatus(status, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * POST /api/notifications - Create notification
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse notification = notificationService.createNotification(request);
        return ResponseEntity.ok(notification);
    }

    /**
     * POST /api/notifications/from-template - Create notification from template
     */
    @PostMapping("/from-template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<NotificationResponse> createNotificationFromTemplate(
            @RequestParam String templateName,
            @RequestParam Long recipientId,
            @RequestParam(required = false) String[] variables) {
        Object[] vars = variables != null ? variables : new Object[0];
        NotificationResponse notification = notificationService.createNotificationFromTemplate(templateName, recipientId, vars);
        return ResponseEntity.ok(notification);
    }

    /**
     * PATCH /api/notifications/{id}/read - Mark notification as read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * PATCH /api/notifications/mark-all-read - Mark all notifications as read
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/notifications/{id} - Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/notifications/unread-count - Get unread notifications count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadNotificationsCount();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * GET /api/notifications/search - Search notifications
     */
    @GetMapping("/search")
    public ResponseEntity<Page<NotificationResponse>> searchNotifications(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.searchNotifications(query, pageable);
        return ResponseEntity.ok(notifications);
    }
}