package com.example.employee_api.repository;

import com.example.employee_api.model.Report;
import com.example.employee_api.model.ReportStatus;
import com.example.employee_api.model.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Find reports by type
    Page<Report> findByType(ReportType type, Pageable pageable);
    
    // Find reports by status
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    
    // Find reports by creator
    Page<Report> findByCreatedBy(String createdBy, Pageable pageable);
    
    // Find reports by type and status
    Page<Report> findByTypeAndStatus(ReportType type, ReportStatus status, Pageable pageable);
    
    // Find reports by creator and status
    Page<Report> findByCreatedByAndStatus(String createdBy, ReportStatus status, Pageable pageable);
    
    // Find reports created within date range
    Page<Report> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find completed reports
    List<Report> findByStatusOrderByCompletedAtDesc(ReportStatus status);
    
    // Find scheduled reports
    List<Report> findByScheduledTrueAndNextRunTimeBefore(LocalDateTime currentTime);
    
    // Find reports by creator and type
    Page<Report> findByCreatedByAndType(String createdBy, ReportType type, Pageable pageable);
    
    // Custom query to find reports with error
    @Query("SELECT r FROM Report r WHERE r.status = 'FAILED' AND r.errorMessage IS NOT NULL")
    List<Report> findFailedReports();
    
    // Custom query to find recent reports
    @Query("SELECT r FROM Report r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Report> findRecentReports(@Param("since") LocalDateTime since);
    
    // Count reports by status
    long countByStatus(ReportStatus status);
    
    // Count reports by type
    long countByType(ReportType type);
    
    // Count reports by creator
    long countByCreatedBy(String createdBy);
    
    // Find reports that need cleanup (old completed reports)
    @Query("SELECT r FROM Report r WHERE r.status = 'COMPLETED' AND r.completedAt < :cutoffDate")
    List<Report> findOldCompletedReports(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find large reports (by file size)
    @Query("SELECT r FROM Report r WHERE r.fileSizeBytes > :minSize ORDER BY r.fileSizeBytes DESC")
    List<Report> findLargeReports(@Param("minSize") Long minSize);
    
    // Search reports by title containing text
    Page<Report> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Find reports by multiple criteria
    @Query("SELECT r FROM Report r WHERE " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:createdBy IS NULL OR r.createdBy = :createdBy) AND " +
           "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.createdAt <= :endDate)")
    Page<Report> findByMultipleCriteria(
        @Param("type") ReportType type,
        @Param("status") ReportStatus status,
        @Param("createdBy") String createdBy,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}