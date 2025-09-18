package com.example.employee_api.repository;

import com.example.employee_api.model.LeaveDocument;
import com.example.employee_api.model.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LeaveDocument entity
 */
@Repository
public interface LeaveDocumentRepository extends JpaRepository<LeaveDocument, Long>, JpaSpecificationExecutor<LeaveDocument> {

    /**
     * Find documents by leave request
     */
    List<LeaveDocument> findByLeaveRequest(LeaveRequest leaveRequest);

    /**
     * Find documents by leave request ID
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE ld.leaveRequest.id = :leaveRequestId")
    List<LeaveDocument> findByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);

    /**
     * Find document by file path
     */
    Optional<LeaveDocument> findByFilePath(String filePath);

    /**
     * Find documents by file type
     */
    List<LeaveDocument> findByFileType(String fileType);

    /**
     * Find documents by document name (case-insensitive)
     */
    List<LeaveDocument> findByDocumentNameContainingIgnoreCase(String documentName);

    /**
     * Find documents uploaded by user
     */
    List<LeaveDocument> findByUploadedBy(Long uploadedBy);

    /**
     * Find documents by size range
     */
    List<LeaveDocument> findByFileSizeBetween(Long minSize, Long maxSize);

    /**
     * Find large documents (over specified size)
     */
    List<LeaveDocument> findByFileSizeGreaterThan(Long fileSize);

    /**
     * Find documents uploaded in date range
     */
    List<LeaveDocument> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count documents by leave request
     */
    Long countByLeaveRequest(LeaveRequest leaveRequest);

    /**
     * Count documents by leave request ID
     */
    @Query("SELECT COUNT(ld) FROM LeaveDocument ld WHERE ld.leaveRequest.id = :leaveRequestId")
    Long countByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);

    /**
     * Count documents by file type
     */
    Long countByFileType(String fileType);

    /**
     * Get total file size by leave request
     */
    @Query("SELECT COALESCE(SUM(ld.fileSize), 0) FROM LeaveDocument ld WHERE ld.leaveRequest.id = :leaveRequestId")
    Long getTotalFileSizeByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);

    /**
     * Get total file size by employee
     */
    @Query("SELECT COALESCE(SUM(ld.fileSize), 0) FROM LeaveDocument ld WHERE ld.leaveRequest.employee.id = :employeeId")
    Long getTotalFileSizeByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find documents by employee
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE ld.leaveRequest.employee.id = :employeeId")
    List<LeaveDocument> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find documents by employee and file type
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE ld.leaveRequest.employee.id = :employeeId AND ld.fileType = :fileType")
    List<LeaveDocument> findByEmployeeIdAndFileType(@Param("employeeId") Long employeeId, @Param("fileType") String fileType);

    /**
     * Search documents by name or description
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE " +
           "LOWER(ld.documentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ld.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<LeaveDocument> searchDocuments(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find documents by leave request status
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE ld.leaveRequest.status = :status")
    List<LeaveDocument> findByLeaveRequestStatus(@Param("status") LeaveRequest.LeaveStatus status);

    /**
     * Find orphaned documents (leave request deleted)
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE ld.leaveRequest IS NULL")
    List<LeaveDocument> findOrphanedDocuments();

    /**
     * Get file type statistics
     */
    @Query("SELECT " +
           "ld.fileType as fileType, " +
           "COUNT(ld) as documentCount, " +
           "COALESCE(SUM(ld.fileSize), 0) as totalSize, " +
           "COALESCE(AVG(ld.fileSize), 0) as avgSize " +
           "FROM LeaveDocument ld " +
           "GROUP BY ld.fileType " +
           "ORDER BY COUNT(ld) DESC")
    List<Object[]> getFileTypeStatistics();

    /**
     * Get upload statistics by date range
     */
    @Query("SELECT " +
           "DATE(ld.createdAt) as uploadDate, " +
           "COUNT(ld) as documentCount, " +
           "COALESCE(SUM(ld.fileSize), 0) as totalSize " +
           "FROM LeaveDocument ld " +
           "WHERE ld.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(ld.createdAt) " +
           "ORDER BY DATE(ld.createdAt)")
    List<Object[]> getUploadStatistics(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find documents needing cleanup (old rejected/cancelled requests)
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE " +
           "ld.leaveRequest.status IN ('REJECTED', 'CANCELLED') AND " +
           "ld.createdAt < :cutoffDate")
    List<LeaveDocument> findDocumentsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if file path exists
     */
    boolean existsByFilePath(String filePath);

    /**
     * Delete documents by leave request ID
     */
    @Query("DELETE FROM LeaveDocument ld WHERE ld.leaveRequest.id = :leaveRequestId")
    void deleteByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);

    /**
     * Get recent documents by employee
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE " +
           "ld.leaveRequest.employee.id = :employeeId " +
           "ORDER BY ld.createdAt DESC")
    Page<LeaveDocument> findRecentDocumentsByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    /**
     * Find duplicate documents by name and size
     */
    @Query("SELECT ld FROM LeaveDocument ld WHERE " +
           "ld.documentName = :documentName AND " +
           "ld.fileSize = :fileSize AND " +
           "ld.leaveRequest.employee.id = :employeeId")
    List<LeaveDocument> findPotentialDuplicates(@Param("documentName") String documentName,
                                               @Param("fileSize") Long fileSize,
                                               @Param("employeeId") Long employeeId);

    /**
     * Get storage summary
     */
    @Query("SELECT " +
           "COUNT(ld) as totalDocuments, " +
           "COALESCE(SUM(ld.fileSize), 0) as totalStorageUsed, " +
           "COALESCE(AVG(ld.fileSize), 0) as avgFileSize, " +
           "COALESCE(MAX(ld.fileSize), 0) as maxFileSize " +
           "FROM LeaveDocument ld")
    Object[] getStorageSummary();
}