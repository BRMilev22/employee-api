package com.example.employee_api.repository.notification;

import com.example.employee_api.model.Notification;
import com.example.employee_api.model.User;
import com.example.employee_api.model.enums.NotificationStatus;
import com.example.employee_api.model.enums.NotificationType;
import com.example.employee_api.model.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by recipient
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    // Find notifications by recipient and status
    Page<Notification> findByRecipientAndStatusOrderByCreatedAtDesc(User recipient, NotificationStatus status, Pageable pageable);

    // Find unread notifications for a user
    Page<Notification> findByRecipientAndStatusInOrderByCreatedAtDesc(User recipient, List<NotificationStatus> statuses, Pageable pageable);

    // Count unread notifications for a user
    long countByRecipientAndStatus(User recipient, NotificationStatus status);

    // Find notifications by type for a user
    Page<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(User recipient, NotificationType type, Pageable pageable);

    // Find notifications by priority for a user
    Page<Notification> findByRecipientAndPriorityOrderByCreatedAtDesc(User recipient, Priority priority, Pageable pageable);

    // Find notifications sent by a user
    Page<Notification> findBySenderOrderByCreatedAtDesc(User sender, Pageable pageable);

    // Find notifications by related entity
    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);

    // Find expired notifications
    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :currentTime")
    List<Notification> findExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);

    // Find scheduled notifications ready to be sent
    @Query("SELECT n FROM Notification n WHERE n.scheduledFor <= :currentTime AND n.status = 'UNREAD'")
    List<Notification> findScheduledNotifications(@Param("currentTime") LocalDateTime currentTime);

    // Find notifications where email needs to be sent
    @Query("SELECT n FROM Notification n WHERE n.emailSent = false AND n.status = 'UNREAD'")
    List<Notification> findNotificationsNeedingEmail();

    // Find notifications created in date range
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientAndCreatedAtBetween(@Param("recipient") User recipient, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate, 
                                                         Pageable pageable);

    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.id = :id")
    int markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.recipient = :recipient AND n.status = 'UNREAD'")
    int markAllAsReadForUser(@Param("recipient") User recipient, @Param("readAt") LocalDateTime readAt);

    // Mark email as sent
    @Modifying
    @Query("UPDATE Notification n SET n.emailSent = true WHERE n.id = :id")
    int markEmailAsSent(@Param("id") Long id);

    // Delete old notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Get notification statistics for a user
    @Query("SELECT " +
           "COUNT(n), " +
           "SUM(CASE WHEN n.status = 'UNREAD' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.status = 'READ' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.priority = 'HIGH' OR n.priority = 'URGENT' THEN 1 ELSE 0 END) " +
           "FROM Notification n WHERE n.recipient = :recipient")
    Object[] getNotificationStatistics(@Param("recipient") User recipient);

    // Find notifications by multiple criteria
    @Query("SELECT n FROM Notification n WHERE " +
           "(:recipient IS NULL OR n.recipient = :recipient) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:priority IS NULL OR n.priority = :priority) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByMultipleCriteria(@Param("recipient") User recipient,
                                            @Param("type") NotificationType type,
                                            @Param("status") NotificationStatus status,
                                            @Param("priority") Priority priority,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    // Count notifications by type for a user in a date range
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.type = :type AND n.createdAt BETWEEN :startDate AND :endDate")
    long countByRecipientAndTypeAndCreatedAtBetween(@Param("recipient") User recipient, 
                                                   @Param("type") NotificationType type, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    // Find latest notification for a specific entity
    Optional<Notification> findTopByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    // Check if notification exists for specific entity and type
    boolean existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndType(User recipient, String entityType, Long entityId, NotificationType type);

    // Search notifications by title or message content
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotifications(@Param("recipient") User recipient, 
                                         @Param("searchTerm") String searchTerm, 
                                         Pageable pageable);
}