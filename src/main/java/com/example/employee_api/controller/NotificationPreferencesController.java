package com.example.employee_api.controller;

import com.example.employee_api.dto.notification.NotificationPreferencesRequest;
import com.example.employee_api.dto.notification.NotificationPreferencesResponse;
import com.example.employee_api.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/preferences")
@CrossOrigin(origins = "*")
public class NotificationPreferencesController {

    private final NotificationService notificationService;
    
    @Autowired
    public NotificationPreferencesController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications/preferences - Get current user's notification preferences
     */
    @GetMapping
    public ResponseEntity<List<NotificationPreferencesResponse>> getPreferences() {
        List<NotificationPreferencesResponse> preferences = notificationService.getPreferencesForCurrentUser();
        return ResponseEntity.ok(preferences);
    }

    /**
     * PUT /api/notifications/preferences - Update notification preferences
     */
    @PutMapping
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @Valid @RequestBody NotificationPreferencesRequest request) {
        NotificationPreferencesResponse preferences = notificationService.updatePreferences(request);
        return ResponseEntity.ok(preferences);
    }
}