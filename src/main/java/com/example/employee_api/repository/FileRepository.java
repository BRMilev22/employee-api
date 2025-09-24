package com.example.employee_api.repository;

import com.example.employee_api.model.File;
import com.example.employee_api.model.enums.FileStatus;
import com.example.employee_api.model.enums.FileType;
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

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    // Basic queries
    List<File> findByStatus(FileStatus status);
    List<File> findByFileType(FileType fileType);
    List<File> findByEmployeeId(Long employeeId);

    // Paginated queries
    Page<File> findByStatus(FileStatus status, Pageable pageable);
    Page<File> findByFileType(FileType fileType, Pageable pageable);
    Page<File> findByEmployeeId(Long employeeId, Pageable pageable);

    // Employee-specific queries
    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.status = :status")
    List<File> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") FileStatus status);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.fileType = :fileType")
    List<File> findByEmployeeIdAndFileType(@Param("employeeId") Long employeeId, @Param("fileType") FileType fileType);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.fileType = :fileType AND f.status = :status")
    Optional<File> findByEmployeeIdAndFileTypeAndStatus(@Param("employeeId") Long employeeId,
                                                        @Param("fileType") FileType fileType,
                                                        @Param("status") FileStatus status);

    // Photo-specific queries
    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.fileType = 'EMPLOYEE_PHOTO' AND f.status = 'ACTIVE'")
    Optional<File> findActiveEmployeePhoto(@Param("employeeId") Long employeeId);

    @Query("SELECT f FROM File f WHERE f.fileType = 'EMPLOYEE_PHOTO' AND f.status = 'ACTIVE'")
    List<File> findAllActiveEmployeePhotos();

    // Public file queries
    @Query("SELECT f FROM File f WHERE f.isPublic = true AND f.status = 'ACTIVE'")
    Page<File> findPublicFiles(Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.isPublic = true AND f.status = 'ACTIVE' AND f.fileType = :fileType")
    List<File> findPublicFilesByType(@Param("fileType") FileType fileType);

    // Search queries
    @Query("SELECT f FROM File f WHERE " +
           "(LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.tags) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "f.status = 'ACTIVE'")
    Page<File> searchFiles(@Param("query") String query, Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND " +
           "(LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.tags) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "f.status = 'ACTIVE'")
    List<File> searchEmployeeFiles(@Param("employeeId") Long employeeId, @Param("query") String query);

    // Size and statistics queries
    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.employee.id = :employeeId AND f.status = 'ACTIVE'")
    Long getTotalFileSizeByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT COUNT(f) FROM File f WHERE f.employee.id = :employeeId AND f.status = 'ACTIVE'")
    Long getFileCountByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.status = 'ACTIVE'")
    Long getTotalActiveFileSize();

    @Query("SELECT COUNT(f) FROM File f WHERE f.status = 'ACTIVE'")
    Long getTotalActiveFileCount();

    @Query("SELECT f.fileType, COUNT(f), SUM(f.fileSize) FROM File f WHERE f.status = 'ACTIVE' GROUP BY f.fileType")
    List<Object[]> getFileStatisticsByType();

    // Date range queries
    @Query("SELECT f FROM File f WHERE f.createdAt BETWEEN :startDate AND :endDate AND f.status = 'ACTIVE'")
    List<File> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND " +
           "f.createdAt BETWEEN :startDate AND :endDate AND f.status = 'ACTIVE'")
    List<File> findByEmployeeIdAndCreatedAtBetween(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Expiration queries
    @Query("SELECT f FROM File f WHERE f.expiresAt IS NOT NULL AND f.expiresAt <= :currentTime AND f.status = 'ACTIVE'")
    List<File> findExpiredFiles(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT f FROM File f WHERE f.expiresAt IS NOT NULL AND " +
           "f.expiresAt BETWEEN :startTime AND :endTime AND f.status = 'ACTIVE'")
    List<File> findFilesExpiringBetween(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    // Download tracking
    @Query("SELECT f FROM File f WHERE f.status = 'ACTIVE' ORDER BY f.downloadCount DESC")
    Page<File> findMostDownloadedFiles(Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.status = 'ACTIVE' ORDER BY f.downloadCount DESC")
    List<File> findMostDownloadedFilesByEmployee(@Param("employeeId") Long employeeId, Pageable pageable);

    // Recent files
    @Query("SELECT f FROM File f WHERE f.status = 'ACTIVE' ORDER BY f.createdAt DESC")
    Page<File> findRecentFiles(Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.status = 'ACTIVE' ORDER BY f.createdAt DESC")
    List<File> findRecentFilesByEmployee(@Param("employeeId") Long employeeId, Pageable pageable);

    // MIME type queries
    @Query("SELECT f FROM File f WHERE f.mimeType LIKE :mimeTypePrefix AND f.status = 'ACTIVE'")
    List<File> findByMimeTypeStartingWith(@Param("mimeTypePrefix") String mimeTypePrefix);

    @Query("SELECT DISTINCT f.mimeType FROM File f WHERE f.status = 'ACTIVE' ORDER BY f.mimeType")
    List<String> findDistinctActiveMimeTypes();

    // Large files
    @Query("SELECT f FROM File f WHERE f.fileSize > :minSize AND f.status = 'ACTIVE' ORDER BY f.fileSize DESC")
    List<File> findLargeFiles(@Param("minSize") Long minSize);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.fileSize > :minSize AND " +
           "f.status = 'ACTIVE' ORDER BY f.fileSize DESC")
    List<File> findLargeFilesByEmployee(@Param("employeeId") Long employeeId, @Param("minSize") Long minSize);

    // Department-based queries (through employee relationship)
    @Query("SELECT f FROM File f WHERE f.employee.department.id = :departmentId AND f.status = 'ACTIVE'")
    List<File> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT f FROM File f WHERE f.employee.department.id = :departmentId AND " +
           "f.fileType = :fileType AND f.status = 'ACTIVE'")
    List<File> findByDepartmentIdAndFileType(@Param("departmentId") Long departmentId,
                                             @Param("fileType") FileType fileType);

    // Update operations
    @Modifying
    @Query("UPDATE File f SET f.downloadCount = f.downloadCount + 1, f.lastAccessedAt = :accessTime WHERE f.id = :fileId")
    void incrementDownloadCount(@Param("fileId") Long fileId, @Param("accessTime") LocalDateTime accessTime);

    @Modifying
    @Query("UPDATE File f SET f.status = :newStatus WHERE f.id = :fileId")
    void updateFileStatus(@Param("fileId") Long fileId, @Param("newStatus") FileStatus newStatus);

    @Modifying
    @Query("UPDATE File f SET f.status = 'EXPIRED' WHERE f.expiresAt <= :currentTime AND f.status = 'ACTIVE'")
    int markExpiredFiles(@Param("currentTime") LocalDateTime currentTime);

    // Bulk operations
    @Modifying
    @Query("UPDATE File f SET f.status = 'DELETED' WHERE f.employee.id = :employeeId AND f.status = 'ACTIVE'")
    int softDeleteEmployeeFiles(@Param("employeeId") Long employeeId);

    @Modifying
    @Query("DELETE FROM File f WHERE f.status = 'DELETED' AND f.updatedAt < :cutoffDate")
    int hardDeleteOldDeletedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Existence checks
    boolean existsByEmployeeIdAndFileTypeAndStatus(Long employeeId, FileType fileType, FileStatus status);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM File f " +
           "WHERE f.employee.id = :employeeId AND f.originalFilename = :filename AND f.status = 'ACTIVE'")
    boolean existsByEmployeeIdAndFilename(@Param("employeeId") Long employeeId, @Param("filename") String filename);

    // Orphaned files (files without employee)
    @Query("SELECT f FROM File f WHERE f.employee IS NULL AND f.status = 'ACTIVE'")
    List<File> findOrphanedFiles();

    // Duplicate detection
    @Query("SELECT f FROM File f WHERE f.checksum = :checksum AND f.status = 'ACTIVE'")
    List<File> findByChecksum(@Param("checksum") String checksum);

    @Query("SELECT f FROM File f WHERE f.employee.id = :employeeId AND f.checksum = :checksum AND " +
           "f.status = 'ACTIVE'")
    List<File> findByEmployeeIdAndChecksum(@Param("employeeId") Long employeeId, @Param("checksum") String checksum);
}