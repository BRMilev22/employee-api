package com.example.employee_api.controller;

import com.example.employee_api.dto.AuditLogFilterDto;
import com.example.employee_api.dto.AuditLogRequestDto;
import com.example.employee_api.dto.AuditLogResponseDto;
import com.example.employee_api.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public class AuditLogController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * Create audit log entry manually
     * POST /api/audit
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogResponseDto> createAuditLog(@Valid @RequestBody AuditLogRequestDto requestDto) {
        AuditLogResponseDto response = auditLogService.createAuditLog(requestDto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all audit logs with pagination
     * GET /api/audit?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponseDto>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponseDto> auditLogs = auditLogService.getAllAuditLogs(page, size);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs with comprehensive filtering
     * GET /api/audit/filter
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogsWithFilters(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) Boolean securityEvent,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        AuditLogFilterDto filterDto = new AuditLogFilterDto();
        filterDto.setUserId(userId);
        filterDto.setUsername(username);
        filterDto.setActionType(actionType);
        filterDto.setEntityType(entityType);
        filterDto.setEntityId(entityId);
        filterDto.setStartDate(startDate);
        filterDto.setEndDate(endDate);
        filterDto.setIpAddress(ipAddress);
        filterDto.setSuccess(success);
        filterDto.setSecurityEvent(securityEvent);
        filterDto.setHttpMethod(httpMethod);
        
        Page<AuditLogResponseDto> auditLogs = auditLogService.getAuditLogsWithFilters(filterDto, page, size);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs for specific user
     * GET /api/audit/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponseDto> auditLogs = auditLogService.getAuditLogsByUser(userId, page, size);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get recent activity for specific user
     * GET /api/audit/user/{userId}/recent
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<AuditLogResponseDto>> getRecentUserActivity(@PathVariable Long userId) {
        List<AuditLogResponseDto> recentActivity = auditLogService.getRecentUserActivity(userId);
        return ResponseEntity.ok(recentActivity);
    }
    
    /**
     * Get security events
     * GET /api/audit/security-events
     */
    @GetMapping("/security-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getSecurityEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponseDto> securityEvents = auditLogService.getSecurityEvents(page, size);
        return ResponseEntity.ok(securityEvents);
    }
    
    /**
     * Get failed operations
     * GET /api/audit/failures
     */
    @GetMapping("/failures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getFailedOperations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponseDto> failedOperations = auditLogService.getFailedOperations(page, size);
        return ResponseEntity.ok(failedOperations);
    }
    
    /**
     * Get audit statistics
     * GET /api/audit/statistics?days=30
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAuditStatistics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> statistics = auditLogService.getAuditStatistics(days);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get audit logs by action type
     * GET /api/audit/action/{actionType}
     */
    @GetMapping("/action/{actionType}")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogsByActionType(
            @PathVariable String actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        AuditLogFilterDto filterDto = new AuditLogFilterDto();
        filterDto.setActionType(actionType);
        
        Page<AuditLogResponseDto> auditLogs = auditLogService.getAuditLogsWithFilters(filterDto, page, size);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs by entity type and ID
     * GET /api/audit/entity/{entityType}/{entityId}
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        AuditLogFilterDto filterDto = new AuditLogFilterDto();
        filterDto.setEntityType(entityType);
        filterDto.setEntityId(entityId);
        
        Page<AuditLogResponseDto> auditLogs = auditLogService.getAuditLogsWithFilters(filterDto, page, size);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit logs by date range
     * GET /api/audit/date-range?startDate=2023-01-01T00:00:00&endDate=2023-12-31T23:59:59
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        AuditLogFilterDto filterDto = new AuditLogFilterDto();
        filterDto.setStartDate(startDate);
        filterDto.setEndDate(endDate);
        
        Page<AuditLogResponseDto> auditLogs = auditLogService.getAuditLogsWithFilters(filterDto, page, size);
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Cleanup old audit logs (Admin only)
     * DELETE /api/audit/cleanup?daysToKeep=365
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupOldAuditLogs(
            @RequestParam(defaultValue = "365") int daysToKeep) {
        
        auditLogService.cleanupOldAuditLogs(daysToKeep);
        
        // Log the cleanup action
        auditLogService.logUserAction(
            AuditLogService.ACTION_DELETE,
            "AuditLog",
            null,
            "Cleaned up audit logs older than " + daysToKeep + " days"
        );
        
        return ResponseEntity.ok(Map.of(
            "message", "Audit logs cleanup completed",
            "daysToKeep", String.valueOf(daysToKeep)
        ));
    }
}