package com.example.employee_api.service;

import com.example.employee_api.dto.AuditLogFilterDto;
import com.example.employee_api.dto.AuditLogRequestDto;
import com.example.employee_api.dto.AuditLogResponseDto;
import com.example.employee_api.model.AuditLog;
import com.example.employee_api.model.User;
import com.example.employee_api.repository.AuditLogRepository;
import com.example.employee_api.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Action Types Constants
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_READ = "READ";
    public static final String ACTION_ACCESS = "ACCESS";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_IMPORT = "IMPORT";
    public static final String ACTION_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String ACTION_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ACTION_PERMISSION_DENIED = "PERMISSION_DENIED";
    
    /**
     * Create audit log entry
     */
    public AuditLogResponseDto createAuditLog(AuditLogRequestDto requestDto) {
        User currentUser = getCurrentUser();
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(currentUser);
        auditLog.setUsername(currentUser != null ? currentUser.getUsername() : "SYSTEM");
        auditLog.setActionType(requestDto.getActionType());
        auditLog.setEntityType(requestDto.getEntityType());
        auditLog.setEntityId(requestDto.getEntityId());
        auditLog.setDescription(requestDto.getDescription());
        auditLog.setSecurityEvent(requestDto.getSecurityEvent() != null ? requestDto.getSecurityEvent() : false);
        auditLog.setOldValues(requestDto.getOldValues());
        auditLog.setNewValues(requestDto.getNewValues());
        auditLog.setSuccess(requestDto.getSuccess() != null ? requestDto.getSuccess() : true);
        auditLog.setErrorMessage(requestDto.getErrorMessage());
        
        // Capture request details
        captureRequestDetails(auditLog);
        
        AuditLog savedAuditLog = auditLogRepository.save(auditLog);
        return convertToResponseDto(savedAuditLog);
    }
    
    /**
     * Log user action automatically
     */
    public void logUserAction(String actionType, String entityType, Long entityId, String description) {
        logUserAction(actionType, entityType, entityId, description, true, null, null, null);
    }
    
    /**
     * Log user action with success status
     */
    public void logUserAction(String actionType, String entityType, Long entityId, String description, boolean success, String errorMessage) {
        logUserAction(actionType, entityType, entityId, description, success, errorMessage, null, null);
    }
    
    /**
     * Log user action with old and new values
     */
    public void logUserAction(String actionType, String entityType, Long entityId, String description, 
                             boolean success, String errorMessage, Object oldValues, Object newValues) {
        try {
            User currentUser = getCurrentUser();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(currentUser);
            auditLog.setUsername(currentUser != null ? currentUser.getUsername() : "SYSTEM");
            auditLog.setActionType(actionType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            
            // Convert objects to JSON strings
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
            
            // Mark as security event for certain actions
            auditLog.setSecurityEvent(isSecurityEvent(actionType));
            
            // Capture request details
            captureRequestDetails(auditLog);
            
            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            // Log the error but don't fail the main operation
            System.err.println("Failed to serialize audit log values: " + e.getMessage());
            // Create a simpler audit log without the problematic values
            logSimpleUserAction(actionType, entityType, entityId, description, success, errorMessage);
        } catch (Exception e) {
            // Log the error but don't fail the main operation
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String actionType, String description, boolean success, String errorMessage) {
        try {
            User currentUser = getCurrentUser();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(currentUser);
            auditLog.setUsername(currentUser != null ? currentUser.getUsername() : "ANONYMOUS");
            auditLog.setActionType(actionType);
            auditLog.setDescription(description);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSecurityEvent(true);
            
            // Capture request details
            captureRequestDetails(auditLog);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log the error but don't fail the main operation
            System.err.println("Failed to create security audit log: " + e.getMessage());
        }
    }
    
    /**
     * Log authentication event
     */
    public void logAuthenticationEvent(String username, String actionType, boolean success, String errorMessage, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            
            // Try to find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                auditLog.setUser(userOpt.get());
            }
            
            auditLog.setUsername(username);
            auditLog.setActionType(actionType);
            auditLog.setDescription(success ? actionType + " successful" : actionType + " failed: " + errorMessage);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSecurityEvent(true);
            auditLog.setIpAddress(ipAddress);
            
            // Capture additional request details if available
            captureRequestDetails(auditLog);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log the error but don't fail the main operation
            System.err.println("Failed to create authentication audit log: " + e.getMessage());
        }
    }
    
    /**
     * Get all audit logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);
        return auditLogs.map(this::convertToResponseDto);
    }
    
    /**
     * Get audit logs with filters
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getAuditLogsWithFilters(AuditLogFilterDto filterDto, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(
            filterDto.getUserId(),
            filterDto.getUsername(),
            filterDto.getActionType(),
            filterDto.getEntityType(),
            filterDto.getEntityId(),
            filterDto.getStartDate(),
            filterDto.getEndDate(),
            filterDto.getIpAddress(),
            filterDto.getSuccess(),
            filterDto.getSecurityEvent(),
            filterDto.getHttpMethod(),
            pageable
        );
        return auditLogs.map(this::convertToResponseDto);
    }
    
    /**
     * Get audit logs for specific user
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getAuditLogsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return auditLogs.map(this::convertToResponseDto);
    }
    
    /**
     * Get security events
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getSecurityEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findBySecurityEventTrueOrderByTimestampDesc(pageable);
        return auditLogs.map(this::convertToResponseDto);
    }
    
    /**
     * Get failed operations
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getFailedOperations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findBySuccessFalseOrderByTimestampDesc(pageable);
        return auditLogs.map(this::convertToResponseDto);
    }
    
    /**
     * Get audit statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAuditStatistics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Total counts
        statistics.put("totalEvents", auditLogRepository.countByTimestampAfter(startDate));
        statistics.put("securityEvents", auditLogRepository.countSecurityEventsByTimestampAfter(startDate));
        statistics.put("failedOperations", auditLogRepository.countFailedOperationsByTimestampAfter(startDate));
        
        // Action type statistics
        List<Object[]> actionStats = auditLogRepository.getActionTypeStatistics(startDate);
        Map<String, Long> actionStatistics = actionStats.stream()
            .collect(Collectors.toMap(
                stat -> (String) stat[0],
                stat -> (Long) stat[1]
            ));
        statistics.put("actionTypeStatistics", actionStatistics);
        
        // Entity type statistics
        List<Object[]> entityStats = auditLogRepository.getEntityTypeStatistics(startDate);
        Map<String, Long> entityStatistics = entityStats.stream()
            .collect(Collectors.toMap(
                stat -> (String) stat[0],
                stat -> (Long) stat[1]
            ));
        statistics.put("entityTypeStatistics", entityStatistics);
        
        // User activity statistics
        List<Object[]> userStats = auditLogRepository.getUserActivityStatistics(startDate);
        Map<String, Long> userStatistics = userStats.stream()
            .limit(10) // Top 10 most active users
            .collect(Collectors.toMap(
                stat -> (String) stat[0],
                stat -> (Long) stat[1]
            ));
        statistics.put("topUserActivity", userStatistics);
        
        return statistics;
    }
    
    /**
     * Get recent activity for user
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getRecentUserActivity(Long userId) {
        List<AuditLog> recentActivity = auditLogRepository.findTop10ByUserIdOrderByTimestampDesc(userId);
        return recentActivity.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Delete old audit logs
     */
    @Transactional
    public void cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteByTimestampBefore(cutoffDate);
    }
    
    // Private helper methods
    
    private void logSimpleUserAction(String actionType, String entityType, Long entityId, String description, boolean success, String errorMessage) {
        try {
            User currentUser = getCurrentUser();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(currentUser);
            auditLog.setUsername(currentUser != null ? currentUser.getUsername() : "SYSTEM");
            auditLog.setActionType(actionType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSecurityEvent(isSecurityEvent(actionType));
            
            captureRequestDetails(auditLog);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Failed to create simple audit log: " + e.getMessage());
        }
    }
    
    private boolean isSecurityEvent(String actionType) {
        return ACTION_LOGIN.equals(actionType) ||
               ACTION_LOGOUT.equals(actionType) ||
               ACTION_LOGIN_FAILED.equals(actionType) ||
               ACTION_PASSWORD_RESET.equals(actionType) ||
               ACTION_ACCOUNT_LOCKED.equals(actionType) ||
               ACTION_PERMISSION_DENIED.equals(actionType);
    }
    
    private void captureRequestDetails(AuditLog auditLog) {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestUrl(request.getRequestURL().toString());
                auditLog.setHttpMethod(request.getMethod());
                auditLog.setSessionId(request.getSession(false) != null ? request.getSession().getId() : null);
            }
        } catch (Exception e) {
            // Ignore exceptions during request detail capture
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }
        
        return request.getRemoteAddr();
    }
    
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                return userRepository.findByUsername(username).orElse(null);
            }
        } catch (Exception e) {
            // Ignore exceptions during user lookup
        }
        return null;
    }
    
    private AuditLogResponseDto convertToResponseDto(AuditLog auditLog) {
        AuditLogResponseDto dto = new AuditLogResponseDto();
        dto.setId(auditLog.getId());
        dto.setUserId(auditLog.getUser() != null ? auditLog.getUser().getId() : null);
        dto.setUsername(auditLog.getUsername());
        dto.setActionType(auditLog.getActionType());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setRequestUrl(auditLog.getRequestUrl());
        dto.setHttpMethod(auditLog.getHttpMethod());
        dto.setSuccess(auditLog.getSuccess());
        dto.setErrorMessage(auditLog.getErrorMessage());
        dto.setSecurityEvent(auditLog.getSecurityEvent());
        dto.setOldValues(auditLog.getOldValues());
        dto.setNewValues(auditLog.getNewValues());
        dto.setDescription(auditLog.getDescription());
        dto.setSessionId(auditLog.getSessionId());
        dto.setDurationMs(auditLog.getDurationMs());
        
        return dto;
    }
}