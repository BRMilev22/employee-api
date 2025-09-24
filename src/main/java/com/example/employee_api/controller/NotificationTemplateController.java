package com.example.employee_api.controller;

import com.example.employee_api.dto.notification.NotificationTemplateRequest;
import com.example.employee_api.dto.notification.NotificationTemplateResponse;
import com.example.employee_api.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/templates")
@CrossOrigin(origins = "*")
public class NotificationTemplateController {

    private final NotificationService notificationService;
    
    @Autowired
    public NotificationTemplateController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications/templates - Get all notification templates
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Page<NotificationTemplateResponse>> getAllTemplates(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationTemplateResponse> templates = notificationService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/notifications/templates/{id} - Get template by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<NotificationTemplateResponse> getTemplateById(@PathVariable Long id) {
        NotificationTemplateResponse template = notificationService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }

    /**
     * POST /api/notifications/templates - Create notification template
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody NotificationTemplateRequest request) {
        NotificationTemplateResponse template = notificationService.createTemplate(request);
        return ResponseEntity.ok(template);
    }

    /**
     * PUT /api/notifications/templates/{id} - Update notification template
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateRequest request) {
        NotificationTemplateResponse template = notificationService.updateTemplate(id, request);
        return ResponseEntity.ok(template);
    }

    /**
     * DELETE /api/notifications/templates/{id} - Delete notification template
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        notificationService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }
}