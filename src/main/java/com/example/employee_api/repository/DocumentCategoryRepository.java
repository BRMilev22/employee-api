package com.example.employee_api.repository;

import com.example.employee_api.model.DocumentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DocumentCategory entity
 */
@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long>, JpaSpecificationExecutor<DocumentCategory> {

    /**
     * Find document category by name (case-insensitive)
     */
    Optional<DocumentCategory> findByNameIgnoreCase(String name);

    /**
     * Find document category by name (exact match)
     */
    Optional<DocumentCategory> findByName(String name);

    /**
     * Check if document category exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all active document categories
     */
    List<DocumentCategory> findByActiveTrue();

    /**
     * Find all inactive document categories
     */
    List<DocumentCategory> findByActiveFalse();

    /**
     * Find active document categories with pagination
     */
    Page<DocumentCategory> findByActiveTrue(Pageable pageable);

    /**
     * Find document categories by active status
     */
    List<DocumentCategory> findByActive(Boolean active);

    /**
     * Search document categories by name containing (case-insensitive)
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE LOWER(dc.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<DocumentCategory> searchByNameContaining(@Param("name") String name);

    /**
     * Search document categories by name or description containing (case-insensitive)
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE " +
           "LOWER(dc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(dc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentCategory> searchByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Find document categories by color
     */
    List<DocumentCategory> findByColor(String color);

    /**
     * Find document categories by icon
     */
    List<DocumentCategory> findByIcon(String icon);

    /**
     * Find document categories with specific color or icon
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE dc.color = :color OR dc.icon = :icon")
    List<DocumentCategory> findByColorOrIcon(@Param("color") String color, @Param("icon") String icon);

    /**
     * Count active document categories
     */
    @Query("SELECT COUNT(dc) FROM DocumentCategory dc WHERE dc.active = true")
    long countActiveDocumentCategories();

    /**
     * Count inactive document categories
     */
    @Query("SELECT COUNT(dc) FROM DocumentCategory dc WHERE dc.active = false")
    long countInactiveDocumentCategories();

    /**
     * Find document categories with null or empty color
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE dc.color IS NULL OR TRIM(dc.color) = ''")
    List<DocumentCategory> findWithNoColor();

    /**
     * Find document categories with assigned color
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE dc.color IS NOT NULL AND TRIM(dc.color) != ''")
    List<DocumentCategory> findWithColor();

    /**
     * Find document categories with null or empty icon
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE dc.icon IS NULL OR TRIM(dc.icon) = ''")
    List<DocumentCategory> findWithNoIcon();

    /**
     * Find document categories with assigned icon
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE dc.icon IS NOT NULL AND TRIM(dc.icon) != ''")
    List<DocumentCategory> findWithIcon();

    /**
     * Check if a document category with name exists excluding a specific ID
     */
    @Query("SELECT COUNT(dc) > 0 FROM DocumentCategory dc WHERE LOWER(dc.name) = LOWER(:name) AND dc.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * Find document categories ordered by name
     */
    @Query("SELECT dc FROM DocumentCategory dc WHERE dc.active = true ORDER BY dc.name ASC")
    List<DocumentCategory> findAllActiveOrderedByName();

    /**
     * Find all document categories ordered by name
     */
    @Query("SELECT dc FROM DocumentCategory dc ORDER BY dc.name ASC")
    List<DocumentCategory> findAllOrderedByName();
}