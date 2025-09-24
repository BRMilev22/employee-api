package com.example.employee_api.repository.notification;

import com.example.employee_api.model.NotificationPreferences;
import com.example.employee_api.model.User;
import com.example.employee_api.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NotificationPreferences entity
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {

    // Find preferences by user
    List<NotificationPreferences> findByUser(User user);

    // Find preferences by user and notification type
    Optional<NotificationPreferences> findByUserAndNotificationType(User user, NotificationType notificationType);

    // Find preferences by user with specific settings enabled
    List<NotificationPreferences> findByUserAndInAppEnabledTrue(User user);
    List<NotificationPreferences> findByUserAndEmailEnabledTrue(User user);
    List<NotificationPreferences> findByUserAndSmsEnabledTrue(User user);
    List<NotificationPreferences> findByUserAndPushEnabledTrue(User user);

    // Find users who have email notifications enabled for a specific type
    @Query("SELECT np.user FROM NotificationPreferences np WHERE np.notificationType = :type AND np.emailEnabled = true")
    List<User> findUsersWithEmailEnabledForType(@Param("type") NotificationType type);

    // Find users who have in-app notifications enabled for a specific type
    @Query("SELECT np.user FROM NotificationPreferences np WHERE np.notificationType = :type AND np.inAppEnabled = true")
    List<User> findUsersWithInAppEnabledForType(@Param("type") NotificationType type);

    // Find users who have SMS notifications enabled for a specific type
    @Query("SELECT np.user FROM NotificationPreferences np WHERE np.notificationType = :type AND np.smsEnabled = true")
    List<User> findUsersWithSmsEnabledForType(@Param("type") NotificationType type);

    // Find users who have push notifications enabled for a specific type
    @Query("SELECT np.user FROM NotificationPreferences np WHERE np.notificationType = :type AND np.pushEnabled = true")
    List<User> findUsersWithPushEnabledForType(@Param("type") NotificationType type);

    // Check if user has any notification enabled for a type
    @Query("SELECT np FROM NotificationPreferences np WHERE np.user = :user AND np.notificationType = :type AND " +
           "(np.inAppEnabled = true OR np.emailEnabled = true OR np.smsEnabled = true OR np.pushEnabled = true)")
    Optional<NotificationPreferences> findEnabledPreferencesByUserAndType(@Param("user") User user, @Param("type") NotificationType type);

    // Find preferences with weekend notifications disabled
    List<NotificationPreferences> findByUserAndWeekendEnabledFalse(User user);

    // Find preferences with quiet hours set
    @Query("SELECT np FROM NotificationPreferences np WHERE np.user = :user AND np.quietHoursStart IS NOT NULL AND np.quietHoursEnd IS NOT NULL")
    List<NotificationPreferences> findByUserWithQuietHours(@Param("user") User user);

    // Count preferences by user
    long countByUser(User user);

    // Delete preferences by user and type
    void deleteByUserAndNotificationType(User user, NotificationType notificationType);

    // Check if preferences exist for user and type
    boolean existsByUserAndNotificationType(User user, NotificationType notificationType);

    // Find all distinct notification types that user has preferences for
    @Query("SELECT DISTINCT np.notificationType FROM NotificationPreferences np WHERE np.user = :user")
    List<NotificationType> findNotificationTypesByUser(@Param("user") User user);

    // Find preferences with frequency limits
    @Query("SELECT np FROM NotificationPreferences np WHERE np.user = :user AND np.frequencyLimit IS NOT NULL")
    List<NotificationPreferences> findByUserWithFrequencyLimits(@Param("user") User user);

    // Get notification method preferences for user and type
    @Query("SELECT np.inAppEnabled, np.emailEnabled, np.smsEnabled, np.pushEnabled FROM NotificationPreferences np WHERE np.user = :user AND np.notificationType = :type")
    Object[] getNotificationMethods(@Param("user") User user, @Param("type") NotificationType type);
}