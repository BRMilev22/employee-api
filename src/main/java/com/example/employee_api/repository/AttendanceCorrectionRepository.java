package com.example.employee_api.repository;

import com.example.employee_api.model.AttendanceCorrection;
import com.example.employee_api.model.enums.CorrectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, Long> {

    // Basic queries
    List<AttendanceCorrection> findByTimeAttendanceId(Long timeAttendanceId);
    List<AttendanceCorrection> findByStatus(CorrectionStatus status);
    List<AttendanceCorrection> findByRequestedById(Long requestedById);
    List<AttendanceCorrection> findByApprovedById(Long approvedById);

    // Paginated queries
    Page<AttendanceCorrection> findByStatus(CorrectionStatus status, Pageable pageable);
    Page<AttendanceCorrection> findByRequestedById(Long requestedById, Pageable pageable);

    // Employee-based queries
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId")
    List<AttendanceCorrection> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId AND ac.status = :status")
    List<AttendanceCorrection> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") CorrectionStatus status);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId ORDER BY ac.createdAt DESC")
    Page<AttendanceCorrection> findByEmployeeIdOrderByCreatedAtDesc(@Param("employeeId") Long employeeId, Pageable pageable);

    // Date range queries
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.createdAt BETWEEN :startDate AND :endDate")
    List<AttendanceCorrection> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId AND ac.createdAt BETWEEN :startDate AND :endDate")
    List<AttendanceCorrection> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    // Correction type queries
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.correctionType = :correctionType")
    List<AttendanceCorrection> findByCorrectionType(@Param("correctionType") String correctionType);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId AND ac.correctionType = :correctionType")
    List<AttendanceCorrection> findByEmployeeIdAndCorrectionType(@Param("employeeId") Long employeeId, @Param("correctionType") String correctionType);

    // Pending approvals
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.status = 'PENDING' ORDER BY ac.createdAt ASC")
    List<AttendanceCorrection> findPendingCorrections();

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.status = 'PENDING' ORDER BY ac.createdAt ASC")
    Page<AttendanceCorrection> findPendingCorrections(Pageable pageable);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.department.id = :departmentId AND ac.status = 'PENDING'")
    List<AttendanceCorrection> findPendingCorrectionsByDepartment(@Param("departmentId") Long departmentId);

    // Manager approval queries
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.manager.id = :managerId AND ac.status = 'PENDING'")
    List<AttendanceCorrection> findPendingCorrectionsForManager(@Param("managerId") Long managerId);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.manager.id = :managerId AND ac.status = 'PENDING'")
    Page<AttendanceCorrection> findPendingCorrectionsForManager(@Param("managerId") Long managerId, Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(ac) FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId AND ac.status = :status")
    Long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") CorrectionStatus status);

    @Query("SELECT COUNT(ac) FROM AttendanceCorrection ac WHERE ac.correctionType = :correctionType AND ac.status = :status")
    Long countByCorrectionTypeAndStatus(@Param("correctionType") String correctionType, @Param("status") CorrectionStatus status);

    @Query("SELECT ac.correctionType, COUNT(ac) FROM AttendanceCorrection ac WHERE ac.createdAt BETWEEN :startDate AND :endDate GROUP BY ac.correctionType")
    List<Object[]> getCorrectionTypeStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Department-based queries
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.department.id = :departmentId")
    List<AttendanceCorrection> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.department.id = :departmentId AND ac.status = :status")
    List<AttendanceCorrection> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") CorrectionStatus status);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.department.id = :departmentId AND ac.createdAt BETWEEN :startDate AND :endDate")
    List<AttendanceCorrection> findByDepartmentIdAndDateRange(@Param("departmentId") Long departmentId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);

    // Recent corrections
    @Query("SELECT ac FROM AttendanceCorrection ac ORDER BY ac.createdAt DESC")
    Page<AttendanceCorrection> findAllRecent(Pageable pageable);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id = :employeeId ORDER BY ac.createdAt DESC")
    List<AttendanceCorrection> findRecentByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    // Overdue corrections (pending for too long)
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.status = 'PENDING' AND ac.createdAt < :cutoffDate")
    List<AttendanceCorrection> findOverdueCorrections(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Approved/Rejected corrections with date range
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.status IN ('APPROVED', 'REJECTED') AND ac.updatedAt BETWEEN :startDate AND :endDate")
    List<AttendanceCorrection> findProcessedCorrections(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // User activity queries
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.requestedBy.id = :userId ORDER BY ac.createdAt DESC")
    Page<AttendanceCorrection> findCorrectionsRequestedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.approvedBy.id = :userId ORDER BY ac.updatedAt DESC")
    Page<AttendanceCorrection> findCorrectionsApprovedByUser(@Param("userId") Long userId, Pageable pageable);

    // Existence checks
    boolean existsByTimeAttendanceIdAndStatus(Long timeAttendanceId, CorrectionStatus status);

    @Query("SELECT CASE WHEN COUNT(ac) > 0 THEN true ELSE false END FROM AttendanceCorrection ac " +
           "WHERE ac.timeAttendance.employee.id = :employeeId AND ac.status = 'PENDING'")
    boolean hasEmployeePendingCorrections(@Param("employeeId") Long employeeId);

    // Bulk operations support
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE ac.timeAttendance.employee.id IN :employeeIds AND ac.status = :status")
    List<AttendanceCorrection> findByEmployeeIdsAndStatus(@Param("employeeIds") List<Long> employeeIds, @Param("status") CorrectionStatus status);
}