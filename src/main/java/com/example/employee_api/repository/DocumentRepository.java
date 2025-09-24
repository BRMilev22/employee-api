package com.example.employee_api.repository;

import com.example.employee_api.model.Document;
import com.example.employee_api.model.DocumentCategory;
import com.example.employee_api.model.DocumentType;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.DocumentApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Document entity
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    /**
     * Find documents by employee
     */
    List<Document> findByEmployee(Employee employee);

    /**
     * Find documents by employee ID
     */
    @Query("SELECT d FROM Document d WHERE d.employee.id = :employeeId")
    List<Document> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find active documents by employee ID
     */
    @Query("SELECT d FROM Document d WHERE d.employee.id = :employeeId AND d.active = true")
    List<Document> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find documents by employee ID with pagination
     */
    @Query("SELECT d FROM Document d WHERE d.employee.id = :employeeId")
    Page<Document> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    /**
     * Find documents by document type
     */
    List<Document> findByDocumentType(DocumentType documentType);

    /**
     * Find documents by document type ID
     */
    @Query("SELECT d FROM Document d WHERE d.documentType.id = :documentTypeId")
    List<Document> findByDocumentTypeId(@Param("documentTypeId") Long documentTypeId);

    /**
     * Find documents by document category
     */
    List<Document> findByDocumentCategory(DocumentCategory documentCategory);

    /**
     * Find documents by document category ID
     */
    @Query("SELECT d FROM Document d WHERE d.documentCategory.id = :categoryId")
    List<Document> findByDocumentCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find document by file path
     */
    Optional<Document> findByFilePath(String filePath);

    /**
     * Find documents by file type
     */
    List<Document> findByFileType(String fileType);

    /**
     * Find documents by approval status
     */
    List<Document> findByApprovalStatus(DocumentApprovalStatus approvalStatus);

    /**
     * Find documents by active status
     */
    List<Document> findByActive(Boolean active);

    /**
     * Find active documents
     */
    List<Document> findByActiveTrue();

    /**
     * Find inactive documents
     */
    List<Document> findByActiveFalse();

    /**
     * Find confidential documents
     */
    List<Document> findByIsConfidentialTrue();

    /**
     * Find non-confidential documents
     */
    List<Document> findByIsConfidentialFalse();

    /**
     * Find documents by document name (case-insensitive)
     */
    List<Document> findByDocumentNameContainingIgnoreCase(String documentName);

    /**
     * Find documents uploaded by user
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedBy = :userId")
    List<Document> findByUploadedBy(@Param("userId") Long userId);

    /**
     * Find documents approved by user
     */
    @Query("SELECT d FROM Document d WHERE d.approvedBy = :userId")
    List<Document> findByApprovedBy(@Param("userId") Long userId);

    /**
     * Find documents created between dates
     */
    @Query("SELECT d FROM Document d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Document> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find documents updated between dates
     */
    @Query("SELECT d FROM Document d WHERE d.updatedAt BETWEEN :startDate AND :endDate")
    List<Document> findByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find documents approved between dates
     */
    @Query("SELECT d FROM Document d WHERE d.approvedAt BETWEEN :startDate AND :endDate")
    List<Document> findByApprovedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find documents expiring on specific date
     */
    List<Document> findByExpiryDate(LocalDate expiryDate);

    /**
     * Find documents expiring before date (expired documents)
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate < :date AND d.active = true")
    List<Document> findExpiredDocuments(@Param("date") LocalDate date);

    /**
     * Find documents expiring within days
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate BETWEEN :today AND :futureDate AND d.active = true")
    List<Document> findExpiringWithinDays(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);

    /**
     * Find documents without expiry date
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NULL")
    List<Document> findWithoutExpiryDate();

    /**
     * Search documents by tags containing
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Document> findByTagsContaining(@Param("tag") String tag);

    /**
     * Global text search across document fields
     */
    @Query("SELECT d FROM Document d WHERE " +
           "LOWER(d.documentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Document> globalTextSearch(@Param("searchTerm") String searchTerm);

    /**
     * Find documents by employee and document type
     */
    @Query("SELECT d FROM Document d WHERE d.employee.id = :employeeId AND d.documentType.id = :documentTypeId")
    List<Document> findByEmployeeIdAndDocumentTypeId(@Param("employeeId") Long employeeId, @Param("documentTypeId") Long documentTypeId);

    /**
     * Find documents by employee and approval status
     */
    @Query("SELECT d FROM Document d WHERE d.employee.id = :employeeId AND d.approvalStatus = :status")
    List<Document> findByEmployeeIdAndApprovalStatus(@Param("employeeId") Long employeeId, @Param("status") DocumentApprovalStatus status);

    /**
     * Find pending approval documents
     */
    @Query("SELECT d FROM Document d WHERE d.approvalStatus = 'PENDING' AND d.active = true")
    List<Document> findPendingApprovalDocuments();

    /**
     * Find approved documents
     */
    @Query("SELECT d FROM Document d WHERE d.approvalStatus = 'APPROVED' AND d.active = true")
    List<Document> findApprovedDocuments();

    /**
     * Find rejected documents
     */
    @Query("SELECT d FROM Document d WHERE d.approvalStatus = 'REJECTED' AND d.active = true")
    List<Document> findRejectedDocuments();

    /**
     * Count documents by employee
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.employee.id = :employeeId AND d.active = true")
    long countByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Count documents by document type
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.documentType.id = :documentTypeId AND d.active = true")
    long countByDocumentTypeId(@Param("documentTypeId") Long documentTypeId);

    /**
     * Count documents by document category
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.documentCategory.id = :categoryId AND d.active = true")
    long countByDocumentCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Count documents by approval status
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.approvalStatus = :status AND d.active = true")
    long countByApprovalStatus(@Param("status") DocumentApprovalStatus status);

    /**
     * Find latest version of document by employee and document type
     */
    Optional<Document> findFirstByEmployeeIdAndDocumentTypeIdAndActiveTrueOrderByVersionDesc(Long employeeId, Long documentTypeId);

    /**
     * Find all versions of document by employee and document type
     */
    @Query("SELECT d FROM Document d WHERE d.employee.id = :employeeId AND d.documentType.id = :documentTypeId " +
           "ORDER BY d.version DESC")
    List<Document> findAllVersionsByEmployeeIdAndDocumentTypeId(@Param("employeeId") Long employeeId, @Param("documentTypeId") Long documentTypeId);

    /**
     * Calculate total file size by employee
     */
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.employee.id = :employeeId AND d.active = true")
    Long getTotalFileSizeByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find documents requiring approval
     */
    @Query("SELECT d FROM Document d JOIN d.documentType dt WHERE dt.requiresApproval = true AND d.active = true")
    List<Document> findDocumentsRequiringApproval();

    /**
     * Find documents not requiring approval
     */
    @Query("SELECT d FROM Document d JOIN d.documentType dt WHERE dt.requiresApproval = false AND d.active = true")
    List<Document> findDocumentsNotRequiringApproval();

    /**
     * Check if employee has document of specific type
     */
    @Query("SELECT COUNT(d) > 0 FROM Document d WHERE d.employee.id = :employeeId AND d.documentType.id = :documentTypeId AND d.active = true")
    boolean hasDocumentOfType(@Param("employeeId") Long employeeId, @Param("documentTypeId") Long documentTypeId);

    /**
     * Find orphaned documents (employee is inactive/deleted)
     */
    @Query("SELECT d FROM Document d JOIN d.employee e WHERE e.status != 'ACTIVE' AND d.active = true")
    List<Document> findOrphanedDocuments();
}