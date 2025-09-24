package com.example.employee_api.repository;

import com.example.employee_api.model.Bonus;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.BonusStatus;
import com.example.employee_api.model.enums.BonusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Bonus entity
 */
@Repository
public interface BonusRepository extends JpaRepository<Bonus, Long> {

    /**
     * Find bonuses by employee ordered by award date descending
     */
    List<Bonus> findByEmployeeOrderByAwardDateDesc(Employee employee);

    /**
     * Find bonuses by employee ID ordered by award date descending
     */
    List<Bonus> findByEmployeeIdOrderByAwardDateDesc(Long employeeId);

    /**
     * Find bonuses by status
     */
    List<Bonus> findByStatusOrderByAwardDateDesc(BonusStatus status);

    /**
     * Find bonuses by type
     */
    List<Bonus> findByBonusTypeOrderByAwardDateDesc(BonusType bonusType);

    /**
     * Find bonuses by employee and status
     */
    List<Bonus> findByEmployeeAndStatusOrderByAwardDateDesc(Employee employee, BonusStatus status);

    /**
     * Find bonuses by employee and type
     */
    List<Bonus> findByEmployeeAndBonusTypeOrderByAwardDateDesc(Employee employee, BonusType bonusType);

    /**
     * Find bonuses by award date range
     */
    List<Bonus> findByAwardDateBetweenOrderByAwardDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find bonuses by payment date range
     */
    List<Bonus> findByPaymentDateBetweenOrderByPaymentDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find bonuses by employee and award date range
     */
    List<Bonus> findByEmployeeAndAwardDateBetweenOrderByAwardDateDesc(
            Employee employee, LocalDate startDate, LocalDate endDate);

    /**
     * Find bonuses with amount greater than threshold
     */
    List<Bonus> findByAmountGreaterThanOrderByAmountDesc(BigDecimal threshold);

    /**
     * Find bonuses with amount between range
     */
    List<Bonus> findByAmountBetweenOrderByAmountDesc(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find pending bonuses
     */
    List<Bonus> findByStatusOrderByAwardDateAsc(BonusStatus status);

    /**
     * Find bonuses approved by specific person
     */
    List<Bonus> findByApprovedByContainingIgnoreCaseOrderByAwardDateDesc(String approvedBy);

    /**
     * Find bonuses by approval date range
     */
    List<Bonus> findByApprovalDateBetweenOrderByApprovalDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find unpaid bonuses (approved but not paid)
     */
    @Query("SELECT b FROM Bonus b WHERE b.status = 'APPROVED' AND b.paymentDate IS NULL")
    List<Bonus> findUnpaidBonuses();

    /**
     * Find overdue bonuses (approved but not paid within timeframe)
     */
    @Query("SELECT b FROM Bonus b WHERE b.status = 'APPROVED' " +
           "AND b.approvalDate < :cutoffDate AND b.paymentDate IS NULL")
    List<Bonus> findOverdueBonuses(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Count bonuses by employee
     */
    long countByEmployee(Employee employee);

    /**
     * Count bonuses by status
     */
    long countByStatus(BonusStatus status);

    /**
     * Count bonuses by type
     */
    long countByBonusType(BonusType bonusType);

    /**
     * Get total bonus amount for employee
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bonus b WHERE b.employee.id = :employeeId")
    BigDecimal getTotalBonusAmountForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Get total bonus amount for employee by status
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bonus b " +
           "WHERE b.employee.id = :employeeId AND b.status = :status")
    BigDecimal getTotalBonusAmountForEmployeeByStatus(@Param("employeeId") Long employeeId, 
                                                      @Param("status") BonusStatus status);

    /**
     * Get total bonus amount by date range
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bonus b " +
           "WHERE b.awardDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBonusAmountByDateRange(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    /**
     * Find bonuses with pagination
     */
    Page<Bonus> findByEmployeeOrderByAwardDateDesc(Employee employee, Pageable pageable);

    /**
     * Search bonuses by description
     */
    List<Bonus> findByDescriptionContainingIgnoreCaseOrderByAwardDateDesc(String searchTerm);

    /**
     * Find bonuses by performance period
     */
    @Query("SELECT b FROM Bonus b WHERE b.performancePeriodStart >= :startDate " +
           "AND b.performancePeriodEnd <= :endDate " +
           "ORDER BY b.awardDate DESC")
    List<Bonus> findByPerformancePeriod(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);

    /**
     * Find recent bonuses for employee
     */
    @Query("SELECT b FROM Bonus b WHERE b.employee.id = :employeeId " +
           "AND b.awardDate >= :sinceDate " +
           "ORDER BY b.awardDate DESC")
    List<Bonus> findRecentBonusesForEmployee(@Param("employeeId") Long employeeId, 
                                            @Param("sinceDate") LocalDate sinceDate);

    /**
     * Get bonus statistics by type
     */
    @Query("SELECT b.bonusType, COUNT(b), AVG(b.amount), SUM(b.amount) " +
           "FROM Bonus b " +
           "GROUP BY b.bonusType " +
           "ORDER BY SUM(b.amount) DESC")
    List<Object[]> getBonusStatisticsByType();

    /**
     * Get employee bonus summary
     */
    @Query("SELECT b.employee, COUNT(b), SUM(b.amount) " +
           "FROM Bonus b " +
           "WHERE b.status = :status " +
           "GROUP BY b.employee " +
           "ORDER BY SUM(b.amount) DESC")
    List<Object[]> getEmployeeBonusSummary(@Param("status") BonusStatus status);

    /**
     * Find employees with bonuses above threshold
     */
    @Query("SELECT DISTINCT b.employee FROM Bonus b " +
           "WHERE b.amount > :threshold")
    List<Employee> findEmployeesWithBonusesAbove(@Param("threshold") BigDecimal threshold);

    /**
     * Get annual bonus summary for employee
     */
    @Query("SELECT EXTRACT(YEAR FROM b.awardDate), COUNT(b), SUM(b.amount) " +
           "FROM Bonus b " +
           "WHERE b.employee.id = :employeeId " +
           "GROUP BY EXTRACT(YEAR FROM b.awardDate) " +
           "ORDER BY EXTRACT(YEAR FROM b.awardDate) DESC")
    List<Object[]> getAnnualBonusSummaryForEmployee(@Param("employeeId") Long employeeId);
}