package com.example.employee_api.repository;

import com.example.employee_api.model.PerformanceReview;
import com.example.employee_api.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long>, JpaSpecificationExecutor<PerformanceReview> {

    // Find by employee
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.id = :employeeId")
    List<PerformanceReview> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.id = :employeeId ORDER BY pr.reviewPeriodStart DESC")
    Page<PerformanceReview> findByEmployeeIdOrderByReviewPeriodStartDesc(@Param("employeeId") Long employeeId, Pageable pageable);

    // Find by reviewer
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.reviewer.id = :reviewerId")
    List<PerformanceReview> findByReviewerId(@Param("reviewerId") Long reviewerId);

    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.reviewer.id = :reviewerId ORDER BY pr.dueDate ASC")
    Page<PerformanceReview> findByReviewerIdOrderByDueDateAsc(@Param("reviewerId") Long reviewerId, Pageable pageable);

    // Find by status
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.status = :status ORDER BY pr.dueDate ASC")
    List<PerformanceReview> findByStatus(@Param("status") ReviewStatus status);

    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.status = :status ORDER BY pr.dueDate ASC")
    Page<PerformanceReview> findByStatusOrderByDueDateAsc(@Param("status") ReviewStatus status, Pageable pageable);

    // Find pending reviews for a manager
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.reviewer.id = :reviewerId AND pr.status IN ('PENDING', 'IN_PROGRESS') ORDER BY pr.dueDate ASC")
    List<PerformanceReview> findPendingReviewsByReviewer(@Param("reviewerId") Long reviewerId);

    // Find overdue reviews
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.dueDate < :currentDate AND pr.status NOT IN ('COMPLETED', 'APPROVED', 'CANCELLED') ORDER BY pr.dueDate ASC")
    List<PerformanceReview> findOverdueReviews(@Param("currentDate") LocalDate currentDate);

    // Find reviews by date range
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.reviewPeriodStart >= :startDate AND pr.reviewPeriodEnd <= :endDate")
    List<PerformanceReview> findByReviewPeriodBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find reviews due in next X days
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.dueDate BETWEEN :startDate AND :endDate AND pr.status NOT IN ('COMPLETED', 'APPROVED', 'CANCELLED') ORDER BY pr.dueDate ASC")
    List<PerformanceReview> findReviewsDueInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find completed reviews by employee
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.id = :employeeId AND pr.status IN ('COMPLETED', 'APPROVED') ORDER BY pr.completedDate DESC")
    List<PerformanceReview> findCompletedReviewsByEmployee(@Param("employeeId") Long employeeId);

    // Find most recent review for employee
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.id = :employeeId ORDER BY pr.reviewPeriodStart DESC")
    Optional<PerformanceReview> findMostRecentReviewByEmployee(@Param("employeeId") Long employeeId);

    // Count reviews by status
    @Query("SELECT COUNT(pr) FROM PerformanceReview pr WHERE pr.status = :status")
    long countByStatus(@Param("status") ReviewStatus status);

    // Find reviews by department (through employee)
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.department.id = :departmentId")
    List<PerformanceReview> findByEmployeeDepartmentId(@Param("departmentId") Long departmentId);

    // Find reviews created in date range
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.createdAt >= :startDate AND pr.createdAt <= :endDate")
    List<PerformanceReview> findCreatedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Check if employee has review in period
    @Query("SELECT COUNT(pr) > 0 FROM PerformanceReview pr WHERE pr.employee.id = :employeeId AND pr.reviewPeriodStart <= :endDate AND pr.reviewPeriodEnd >= :startDate")
    boolean hasReviewInPeriod(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find reviews by title (partial match)
    @Query("SELECT pr FROM PerformanceReview pr WHERE LOWER(pr.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<PerformanceReview> findByTitleContainingIgnoreCase(@Param("title") String title);

    // Statistics queries
    @Query("SELECT AVG(pr.overallScore) FROM PerformanceReview pr WHERE pr.overallScore IS NOT NULL AND pr.status IN ('COMPLETED', 'APPROVED')")
    Double getAverageOverallScore();

    @Query("SELECT AVG(pr.overallScore) FROM PerformanceReview pr WHERE pr.employee.department.id = :departmentId AND pr.overallScore IS NOT NULL AND pr.status IN ('COMPLETED', 'APPROVED')")
    Double getAverageOverallScoreByDepartment(@Param("departmentId") Long departmentId);
}