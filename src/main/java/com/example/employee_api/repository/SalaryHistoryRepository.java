package com.example.employee_api.repository;

import com.example.employee_api.model.SalaryHistory;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.PayGrade;
import com.example.employee_api.model.enums.SalaryChangeReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SalaryHistory entity
 */
@Repository
public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {

    /**
     * Find salary history by employee ordered by effective date descending
     */
    List<SalaryHistory> findByEmployeeOrderByEffectiveDateDesc(Employee employee);

    /**
     * Find salary history by employee ID ordered by effective date descending
     */
    List<SalaryHistory> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    /**
     * Find current salary for employee (most recent entry)
     */
    @Query("SELECT sh FROM SalaryHistory sh WHERE sh.employee.id = :employeeId " +
           "AND sh.effectiveDate <= CURRENT_DATE " +
           "ORDER BY sh.effectiveDate DESC")
    List<SalaryHistory> findCurrentSalaryByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find most recent salary history entry for employee
     */
    Optional<SalaryHistory> findFirstByEmployeeOrderByEffectiveDateDesc(Employee employee);

    /**
     * Find salary history by employee and date range
     */
    List<SalaryHistory> findByEmployeeAndEffectiveDateBetweenOrderByEffectiveDateDesc(
            Employee employee, LocalDate startDate, LocalDate endDate);

    /**
     * Find salary history by employee ID and date range
     */
    @Query("SELECT sh FROM SalaryHistory sh WHERE sh.employee.id = :employeeId " +
           "AND sh.effectiveDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sh.effectiveDate DESC")
    List<SalaryHistory> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Find salary history by change reason
     */
    List<SalaryHistory> findByChangeReasonOrderByEffectiveDateDesc(SalaryChangeReason changeReason);

    /**
     * Find salary history by pay grade
     */
    List<SalaryHistory> findByPayGradeOrderByEffectiveDateDesc(PayGrade payGrade);

    /**
     * Find salary history with salary above threshold
     */
    List<SalaryHistory> findByNewSalaryGreaterThanOrderByEffectiveDateDesc(BigDecimal threshold);

    /**
     * Find salary history with salary below threshold
     */
    List<SalaryHistory> findByNewSalaryLessThanOrderByEffectiveDateDesc(BigDecimal threshold);

    /**
     * Find salary history with salary between range
     */
    List<SalaryHistory> findByNewSalaryBetweenOrderByEffectiveDateDesc(BigDecimal minSalary, BigDecimal maxSalary);

    /**
     * Find salary increases
     */
    @Query("SELECT sh FROM SalaryHistory sh WHERE sh.previousSalary IS NOT NULL " +
           "AND sh.newSalary > sh.previousSalary " +
           "ORDER BY sh.effectiveDate DESC")
    List<SalaryHistory> findSalaryIncreases();

    /**
     * Find salary decreases
     */
    @Query("SELECT sh FROM SalaryHistory sh WHERE sh.previousSalary IS NOT NULL " +
           "AND sh.newSalary < sh.previousSalary " +
           "ORDER BY sh.effectiveDate DESC")
    List<SalaryHistory> findSalaryDecreases();

    /**
     * Find salary history by effective date
     */
    List<SalaryHistory> findByEffectiveDateOrderByEmployeeIdAsc(LocalDate effectiveDate);

    /**
     * Find salary history by date range
     */
    List<SalaryHistory> findByEffectiveDateBetweenOrderByEffectiveDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Count salary changes for employee
     */
    long countByEmployee(Employee employee);

    /**
     * Count salary changes by change reason
     */
    long countByChangeReason(SalaryChangeReason changeReason);

    /**
     * Find salary history with pagination
     */
    Page<SalaryHistory> findByEmployeeOrderByEffectiveDateDesc(Employee employee, Pageable pageable);

    /**
     * Get average salary for employee over time
     */
    @Query("SELECT AVG(sh.newSalary) FROM SalaryHistory sh WHERE sh.employee.id = :employeeId")
    BigDecimal getAverageSalaryForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Get salary history with percentage increases above threshold
     */
    @Query("SELECT sh FROM SalaryHistory sh WHERE sh.percentageIncrease IS NOT NULL " +
           "AND sh.percentageIncrease > :threshold " +
           "ORDER BY sh.percentageIncrease DESC")
    List<SalaryHistory> findByPercentageIncreaseGreaterThan(@Param("threshold") BigDecimal threshold);

    /**
     * Find employees with recent salary changes
     */
    @Query("SELECT DISTINCT sh.employee FROM SalaryHistory sh " +
           "WHERE sh.effectiveDate >= :sinceDate")
    List<Employee> findEmployeesWithRecentSalaryChanges(@Param("sinceDate") LocalDate sinceDate);

    /**
     * Get salary trend for employee
     */
    @Query("SELECT sh.effectiveDate, sh.newSalary FROM SalaryHistory sh " +
           "WHERE sh.employee.id = :employeeId " +
           "ORDER BY sh.effectiveDate ASC")
    List<Object[]> getSalaryTrendForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Find salary history by approved by
     */
    List<SalaryHistory> findByApprovedByContainingIgnoreCaseOrderByEffectiveDateDesc(String approvedBy);

    /**
     * Get salary statistics for date range
     */
    @Query("SELECT " +
           "COUNT(sh) as totalChanges, " +
           "AVG(sh.newSalary) as avgSalary, " +
           "MIN(sh.newSalary) as minSalary, " +
           "MAX(sh.newSalary) as maxSalary, " +
           "AVG(sh.percentageIncrease) as avgIncrease " +
           "FROM SalaryHistory sh " +
           "WHERE sh.effectiveDate BETWEEN :startDate AND :endDate")
    Object[] getSalaryStatistics(@Param("startDate") LocalDate startDate, 
                                @Param("endDate") LocalDate endDate);

    /**
     * Search salary history by notes
     */
    List<SalaryHistory> findByNotesContainingIgnoreCaseOrderByEffectiveDateDesc(String searchTerm);
}