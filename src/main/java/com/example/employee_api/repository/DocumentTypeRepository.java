package com.example.employee_api.repository;

import com.example.employee_api.model.DocumentType;
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
 * Repository interface for DocumentType entity
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long>, JpaSpecificationExecutor<DocumentType> {

    /**
     * Find document type by name (case-insensitive)
     */
    Optional<DocumentType> findByNameIgnoreCase(String name);

    /**
     * Find document type by name (exact match)
     */
    Optional<DocumentType> findByName(String name);

    /**
     * Check if document type exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all active document types
     */
    List<DocumentType> findByActiveTrue();

    /**
     * Find all inactive document types
     */
    List<DocumentType> findByActiveFalse();

    /**
     * Find active document types with pagination
     */
    Page<DocumentType> findByActiveTrue(Pageable pageable);

    /**
     * Find document types by active status
     */
    List<DocumentType> findByActive(Boolean active);

    /**
     * Find document types that require approval
     */
    List<DocumentType> findByRequiresApprovalTrue();

    /**
     * Find document types that don't require approval
     */
    List<DocumentType> findByRequiresApprovalFalse();

    /**
     * Find document types by requires approval status
     */
    List<DocumentType> findByRequiresApproval(Boolean requiresApproval);

    /**
     * Search document types by name containing (case-insensitive)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE LOWER(dt.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<DocumentType> searchByNameContaining(@Param("name") String name);

    /**
     * Search document types by name or description containing (case-insensitive)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(dt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentType> searchByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Find document types with specific file size limit
     */
    List<DocumentType> findByMaxFileSizeMb(Integer maxFileSizeMb);

    /**
     * Find document types with file size limit greater than or equal to specified value
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.maxFileSizeMb >= :minSize")
    List<DocumentType> findByMaxFileSizeMbGreaterThanEqual(@Param("minSize") Integer minSize);

    /**
     * Find document types with file size limit less than or equal to specified value
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.maxFileSizeMb <= :maxSize")
    List<DocumentType> findByMaxFileSizeMbLessThanEqual(@Param("maxSize") Integer maxSize);

    /**
     * Find document types that allow specific file extension
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "dt.allowedFileTypes IS NULL OR " +
           "LOWER(dt.allowedFileTypes) LIKE LOWER(CONCAT('%', :fileExtension, '%'))")
    List<DocumentType> findByAllowedFileTypesContaining(@Param("fileExtension") String fileExtension);

    /**
     * Count active document types
     */
    @Query("SELECT COUNT(dt) FROM DocumentType dt WHERE dt.active = true")
    long countActiveDocumentTypes();

    /**
     * Count inactive document types
     */
    @Query("SELECT COUNT(dt) FROM DocumentType dt WHERE dt.active = false")
    long countInactiveDocumentTypes();

    /**
     * Find document types with null or empty allowed file types (no restrictions)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.allowedFileTypes IS NULL OR TRIM(dt.allowedFileTypes) = ''")
    List<DocumentType> findWithNoFileTypeRestrictions();

    /**
     * Find document types with file type restrictions
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.allowedFileTypes IS NOT NULL AND TRIM(dt.allowedFileTypes) != ''")
    List<DocumentType> findWithFileTypeRestrictions();

    /**
     * Check if a document type with name exists excluding a specific ID
     */
    @Query("SELECT COUNT(dt) > 0 FROM DocumentType dt WHERE LOWER(dt.name) = LOWER(:name) AND dt.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);
}