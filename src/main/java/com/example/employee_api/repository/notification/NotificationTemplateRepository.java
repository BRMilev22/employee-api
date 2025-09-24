package com.example.employee_api.repository.notification;

import com.example.employee_api.model.NotificationTemplate;
import com.example.employee_api.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NotificationTemplate entity
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    // Find template by name
    Optional<NotificationTemplate> findByName(String name);

    // Find templates by type
    List<NotificationTemplate> findByType(NotificationType type);

    // Find active templates
    List<NotificationTemplate> findByIsActiveTrue();

    // Find active templates by type
    List<NotificationTemplate> findByTypeAndIsActiveTrue(NotificationType type);

    // Find system templates
    List<NotificationTemplate> findByIsSystemTemplateTrue();

    // Find user-created templates
    List<NotificationTemplate> findByIsSystemTemplateFalse();

    // Find templates with pagination
    Page<NotificationTemplate> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // Find templates by type with pagination
    Page<NotificationTemplate> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);

    // Search templates by name
    @Query("SELECT t FROM NotificationTemplate t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.createdAt DESC")
    Page<NotificationTemplate> searchTemplates(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find templates by multiple criteria
    @Query("SELECT t FROM NotificationTemplate t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive) AND " +
           "(:isSystemTemplate IS NULL OR t.isSystemTemplate = :isSystemTemplate) " +
           "ORDER BY t.createdAt DESC")
    Page<NotificationTemplate> findByMultipleCriteria(@Param("type") NotificationType type,
                                                     @Param("isActive") Boolean isActive,
                                                     @Param("isSystemTemplate") Boolean isSystemTemplate,
                                                     Pageable pageable);

    // Check if template name exists (for uniqueness validation)
    boolean existsByName(String name);

    // Check if template name exists excluding specific ID (for updates)
    boolean existsByNameAndIdNot(String name, Long id);

    // Count templates by type
    long countByType(NotificationType type);

    // Count active templates
    long countByIsActiveTrue();

    // Find default template for a notification type
    @Query("SELECT t FROM NotificationTemplate t WHERE t.type = :type AND t.isActive = true AND t.isSystemTemplate = true ORDER BY t.createdAt ASC")
    Optional<NotificationTemplate> findDefaultTemplateByType(@Param("type") NotificationType type);
}