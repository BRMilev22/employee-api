package com.example.employee_api.repository;

import com.example.employee_api.model.EmployeePositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EmployeePositionHistory entity operations
 * Provides data access methods for managing employee position history
 */
@Repository
public interface EmployeePositionHistoryRepository extends JpaRepository<EmployeePositionHistory, Long> {

    /**
     * Find position history by employee ID ordered by start date descending
     */
    List<EmployeePositionHistory> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    /**
     * Find position history by position ID ordered by start date descending
     */
    List<EmployeePositionHistory> findByPositionIdOrderByStartDateDesc(Long positionId);

    /**
     * Find current position for an employee
     */
    Optional<EmployeePositionHistory> findByEmployeeIdAndIsCurrentTrue(Long employeeId);

    /**
     * Find all current position assignments
     */
    List<EmployeePositionHistory> findByIsCurrentTrueOrderByStartDateDesc();

    /**
     * Find position history within a date range
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE " +
           "eph.startDate <= :endDate AND (eph.endDate IS NULL OR eph.endDate >= :startDate)")
    List<EmployeePositionHistory> findPositionHistoryInDateRange(@Param("startDate") LocalDate startDate, 
                                                                @Param("endDate") LocalDate endDate);

    /**
     * Find employees who held a specific position
     */
    List<EmployeePositionHistory> findByPositionIdAndEndDateIsNotNull(Long positionId);

    /**
     * Find employees currently in a specific position
     */
    List<EmployeePositionHistory> findByPositionIdAndIsCurrentTrue(Long positionId);

    /**
     * Find position changes for an employee within a date range
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE eph.employee.id = :employeeId AND " +
           "eph.startDate BETWEEN :startDate AND :endDate ORDER BY eph.startDate DESC")
    List<EmployeePositionHistory> findEmployeePositionChangesInDateRange(@Param("employeeId") Long employeeId,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);

    /**
     * Find the most recent position for an employee
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE eph.employee.id = :employeeId " +
           "ORDER BY eph.startDate DESC, eph.id DESC")
    Optional<EmployeePositionHistory> findMostRecentPositionForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Count position changes for an employee
     */
    Long countByEmployeeId(Long employeeId);

    /**
     * Count total employees who have held a position
     */
    @Query("SELECT COUNT(DISTINCT eph.employee.id) FROM EmployeePositionHistory eph WHERE eph.position.id = :positionId")
    Long countDistinctEmployeesByPosition(@Param("positionId") Long positionId);

    /**
     * Find longest tenure in a position
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE eph.position.id = :positionId AND eph.endDate IS NOT NULL " +
           "ORDER BY (eph.endDate - eph.startDate) DESC")
    List<EmployeePositionHistory> findLongestTenureInPosition(@Param("positionId") Long positionId);

    /**
     * Find position assignments that started in a specific year
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE YEAR(eph.startDate) = :year")
    List<EmployeePositionHistory> findPositionAssignmentsByYear(@Param("year") Integer year);

    /**
     * Find active position assignments (no end date)
     */
    List<EmployeePositionHistory> findByEndDateIsNullOrderByStartDateDesc();

    /**
     * Find position assignments by change reason
     */
    List<EmployeePositionHistory> findByChangeReasonContainingIgnoreCase(String reason);

    /**
     * Get average tenure for a position
     */
    @Query("SELECT AVG(CAST((COALESCE(eph.endDate, CURRENT_DATE) - eph.startDate) AS DOUBLE)) " +
           "FROM EmployeePositionHistory eph WHERE eph.position.id = :positionId")
    Double getAverageTenureForPosition(@Param("positionId") Long positionId);

    /**
     * Find overlapping position assignments (potential data issues)
     */
    @Query("SELECT eph1 FROM EmployeePositionHistory eph1, EmployeePositionHistory eph2 WHERE " +
           "eph1.employee.id = eph2.employee.id AND eph1.id != eph2.id AND " +
           "eph1.startDate <= COALESCE(eph2.endDate, CURRENT_DATE) AND " +
           "COALESCE(eph1.endDate, CURRENT_DATE) >= eph2.startDate")
    List<EmployeePositionHistory> findOverlappingAssignments();

    /**
     * Find promotion history (position level increases)
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE eph.employee.id = :employeeId AND " +
           "eph.changeReason LIKE '%promotion%' OR eph.changeReason LIKE '%advance%' " +
           "ORDER BY eph.startDate DESC")
    List<EmployeePositionHistory> findPromotionHistoryForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Find employees with multiple position changes
     */
    @Query("SELECT eph.employee.id, COUNT(eph) FROM EmployeePositionHistory eph " +
           "GROUP BY eph.employee.id HAVING COUNT(eph) > :minChanges")
    List<Object[]> findEmployeesWithMultiplePositionChanges(@Param("minChanges") Long minChanges);

    /**
     * Find position assignments with salary information
     */
    @Query("SELECT eph FROM EmployeePositionHistory eph WHERE " +
           "eph.startingSalary IS NOT NULL OR eph.endingSalary IS NOT NULL")
    List<EmployeePositionHistory> findAssignmentsWithSalaryData();

    /**
     * Get position tenure statistics
     */
    @Query("SELECT " +
           "eph.position.id, " +
           "eph.position.title, " +
           "COUNT(eph), " +
           "AVG(CAST((COALESCE(eph.endDate, CURRENT_DATE) - eph.startDate) AS DOUBLE)), " +
           "MIN(CAST((COALESCE(eph.endDate, CURRENT_DATE) - eph.startDate) AS DOUBLE)), " +
           "MAX(CAST((COALESCE(eph.endDate, CURRENT_DATE) - eph.startDate) AS DOUBLE)) " +
           "FROM EmployeePositionHistory eph " +
           "GROUP BY eph.position.id, eph.position.title")
    List<Object[]> getPositionTenureStatistics();
}