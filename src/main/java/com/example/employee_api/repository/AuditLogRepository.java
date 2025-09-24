package com.example.employee_api.repository;

import com.example.employee_api.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find by user
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);
    
    // Find by action type
    Page<AuditLog> findByActionTypeOrderByTimestampDesc(String actionType, Pageable pageable);
    
    // Find by entity
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId, Pageable pageable);
    
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);
    
    // Find by date range
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find security events
    Page<AuditLog> findBySecurityEventTrueOrderByTimestampDesc(Pageable pageable);
    
    // Find failed operations
    Page<AuditLog> findBySuccessFalseOrderByTimestampDesc(Pageable pageable);
    
    // Find by IP address
    Page<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);
    
    // Find recent activity for a user
    List<AuditLog> findTop10ByUserIdOrderByTimestampDesc(Long userId);
    
    // Complex filter query
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:username IS NULL OR a.username = :username) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:entityId IS NULL OR a.entityId = :entityId) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) AND " +
           "(:ipAddress IS NULL OR a.ipAddress = :ipAddress) AND " +
           "(:success IS NULL OR a.success = :success) AND " +
           "(:securityEvent IS NULL OR a.securityEvent = :securityEvent) AND " +
           "(:httpMethod IS NULL OR a.httpMethod = :httpMethod) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findWithFilters(
        @Param("userId") Long userId,
        @Param("username") String username,
        @Param("actionType") String actionType,
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("ipAddress") String ipAddress,
        @Param("success") Boolean success,
        @Param("securityEvent") Boolean securityEvent,
        @Param("httpMethod") String httpMethod,
        Pageable pageable
    );
    
    // Count queries for statistics
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate")
    Long countByTimestampAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.securityEvent = true AND a.timestamp >= :startDate")
    Long countSecurityEventsByTimestampAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.success = false AND a.timestamp >= :startDate")
    Long countFailedOperationsByTimestampAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId AND a.timestamp >= :startDate")
    Long countByUserIdAndTimestampAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    // Statistics queries
    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate GROUP BY a.actionType ORDER BY COUNT(a) DESC")
    List<Object[]> getActionTypeStatistics(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate AND a.entityType IS NOT NULL GROUP BY a.entityType ORDER BY COUNT(a) DESC")
    List<Object[]> getEntityTypeStatistics(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a.ipAddress, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate AND a.ipAddress IS NOT NULL GROUP BY a.ipAddress ORDER BY COUNT(a) DESC")
    List<Object[]> getIpAddressStatistics(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a.username, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate AND a.username IS NOT NULL GROUP BY a.username ORDER BY COUNT(a) DESC")
    List<Object[]> getUserActivityStatistics(@Param("startDate") LocalDateTime startDate);
    
    // Delete old audit logs (for cleanup)
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    void deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find suspicious activity
    @Query("SELECT a FROM AuditLog a WHERE a.success = false AND a.actionType IN ('LOGIN', 'ACCESS') AND a.timestamp >= :startDate AND a.ipAddress = :ipAddress ORDER BY a.timestamp DESC")
    List<AuditLog> findSuspiciousActivity(@Param("ipAddress") String ipAddress, @Param("startDate") LocalDateTime startDate);
    
    // Find login/logout events for a user
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.actionType IN ('LOGIN', 'LOGOUT') AND a.timestamp >= :startDate ORDER BY a.timestamp DESC")
    List<AuditLog> findUserLoginLogoutEvents(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}