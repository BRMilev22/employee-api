package com.example.employee_api.repository;

import com.example.employee_api.model.Deduction;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.DeductionStatus;
import com.example.employee_api.model.enums.DeductionType;
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
 * Repository interface for Deduction entity
 */
@Repository
public interface DeductionRepository extends JpaRepository<Deduction, Long> {

    /**
     * Find deductions by employee ordered by effective date descending
     */
    List<Deduction> findByEmployeeOrderByEffectiveDateDesc(Employee employee);

    /**
     * Find deductions by employee ID ordered by effective date descending
     */
    List<Deduction> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    /**
     * Find active deductions for employee
     */
    @Query("SELECT d FROM Deduction d WHERE d.employee.id = :employeeId " +
           "AND d.status = 'ACTIVE' " +
           "AND d.effectiveDate <= CURRENT_DATE " +
           "AND (d.endDate IS NULL OR d.endDate >= CURRENT_DATE)")
    List<Deduction> findActiveDeductionsForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Find deductions by status
     */
    List<Deduction> findByStatusOrderByEffectiveDateDesc(DeductionStatus status);

    /**
     * Find deductions by type
     */
    List<Deduction> findByDeductionTypeOrderByEffectiveDateDesc(DeductionType deductionType);

    /**
     * Find deductions by employee and status
     */
    List<Deduction> findByEmployeeAndStatusOrderByEffectiveDateDesc(Employee employee, DeductionStatus status);

    /**
     * Find deductions by employee and type
     */
    List<Deduction> findByEmployeeAndDeductionTypeOrderByEffectiveDateDesc(Employee employee, DeductionType deductionType);

    /**
     * Find mandatory deductions
     */
    List<Deduction> findByIsMandatoryTrueOrderByEffectiveDateDesc();

    /**
     * Find pre-tax deductions
     */
    List<Deduction> findByIsPreTaxTrueOrderByEffectiveDateDesc();

    /**
     * Find deductions by effective date range
     */
    List<Deduction> findByEffectiveDateBetweenOrderByEffectiveDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find deductions by end date range
     */
    List<Deduction> findByEndDateBetweenOrderByEndDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find deductions by frequency
     */
    List<Deduction> findByFrequencyOrderByEffectiveDateDesc(String frequency);

    /**
     * Find deductions with amount greater than threshold
     */
    List<Deduction> findByAmountGreaterThanOrderByAmountDesc(BigDecimal threshold);

    /**
     * Find deductions with percentage greater than threshold
     */
    List<Deduction> findByPercentageGreaterThanOrderByPercentageDesc(BigDecimal threshold);

    /**
     * Find deductions by vendor name
     */
    List<Deduction> findByVendorNameContainingIgnoreCaseOrderByEffectiveDateDesc(String vendorName);

    /**
     * Find deductions by policy number
     */
    List<Deduction> findByPolicyNumberContainingIgnoreCaseOrderByEffectiveDateDesc(String policyNumber);

    /**
     * Find deductions that have reached annual limit
     */
    @Query("SELECT d FROM Deduction d WHERE d.annualLimit IS NOT NULL " +
           "AND d.yearToDateAmount >= d.annualLimit")
    List<Deduction> findDeductionsAtAnnualLimit();

    /**
     * Find deductions close to annual limit
     */
    @Query("SELECT d FROM Deduction d WHERE d.annualLimit IS NOT NULL " +
           "AND d.yearToDateAmount >= (d.annualLimit * :percentage / 100)")
    List<Deduction> findDeductionsCloseToAnnualLimit(@Param("percentage") BigDecimal percentage);

    /**
     * Count deductions by employee
     */
    long countByEmployee(Employee employee);

    /**
     * Count deductions by status
     */
    long countByStatus(DeductionStatus status);

    /**
     * Count deductions by type
     */
    long countByDeductionType(DeductionType deductionType);

    /**
     * Get total deduction amount for employee
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN d.amount IS NOT NULL THEN d.amount ELSE 0 END), 0) " +
           "FROM Deduction d WHERE d.employee.id = :employeeId AND d.status = 'ACTIVE'")
    BigDecimal getTotalDeductionAmountForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Get total deduction percentage for employee
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN d.percentage IS NOT NULL THEN d.percentage ELSE 0 END), 0) " +
           "FROM Deduction d WHERE d.employee.id = :employeeId AND d.status = 'ACTIVE'")
    BigDecimal getTotalDeductionPercentageForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Get year-to-date deduction amount for employee
     */
    @Query("SELECT COALESCE(SUM(d.yearToDateAmount), 0) FROM Deduction d " +
           "WHERE d.employee.id = :employeeId")
    BigDecimal getYearToDateDeductionAmountForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Find deductions with pagination
     */
    Page<Deduction> findByEmployeeOrderByEffectiveDateDesc(Employee employee, Pageable pageable);

    /**
     * Search deductions by description
     */
    List<Deduction> findByDescriptionContainingIgnoreCaseOrderByEffectiveDateDesc(String searchTerm);

    /**
     * Find expiring deductions
     */
    @Query("SELECT d FROM Deduction d WHERE d.endDate IS NOT NULL " +
           "AND d.endDate BETWEEN CURRENT_DATE AND :futureDate " +
           "AND d.status = 'ACTIVE'")
    List<Deduction> findExpiringDeductions(@Param("futureDate") LocalDate futureDate);

    /**
     * Find expired deductions
     */
    @Query("SELECT d FROM Deduction d WHERE d.endDate IS NOT NULL " +
           "AND d.endDate < CURRENT_DATE " +
           "AND d.status = 'ACTIVE'")
    List<Deduction> findExpiredDeductions();

    /**
     * Get deduction statistics by type
     */
    @Query("SELECT d.deductionType, COUNT(d), AVG(d.amount), SUM(d.amount) " +
           "FROM Deduction d " +
           "WHERE d.status = 'ACTIVE' AND d.amount IS NOT NULL " +
           "GROUP BY d.deductionType " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> getDeductionStatisticsByType();

    /**
     * Get employee deduction summary
     */
    @Query("SELECT d.employee, COUNT(d), SUM(CASE WHEN d.amount IS NOT NULL THEN d.amount ELSE 0 END) " +
           "FROM Deduction d " +
           "WHERE d.status = :status " +
           "GROUP BY d.employee " +
           "ORDER BY SUM(CASE WHEN d.amount IS NOT NULL THEN d.amount ELSE 0 END) DESC")
    List<Object[]> getEmployeeDeductionSummary(@Param("status") DeductionStatus status);

    /**
     * Find employees with specific deduction type
     */
    @Query("SELECT DISTINCT d.employee FROM Deduction d " +
           "WHERE d.deductionType = :deductionType AND d.status = 'ACTIVE'")
    List<Employee> findEmployeesWithDeductionType(@Param("deductionType") DeductionType deductionType);

    /**
     * Find deductions by employee and active status
     */
    @Query("SELECT d FROM Deduction d WHERE d.employee.id = :employeeId " +
           "AND d.status = 'ACTIVE' " +
           "AND d.effectiveDate <= CURRENT_DATE " +
           "AND (d.endDate IS NULL OR d.endDate >= CURRENT_DATE) " +
           "ORDER BY d.effectiveDate DESC")
    List<Deduction> findCurrentActiveDeductionsForEmployee(@Param("employeeId") Long employeeId);

    /**
     * Get monthly deduction total for employee
     */
    @Query("SELECT COALESCE(SUM(" +
           "CASE " +
           "WHEN d.frequency = 'MONTHLY' AND d.amount IS NOT NULL THEN d.amount " +
           "WHEN d.frequency = 'BIWEEKLY' AND d.amount IS NOT NULL THEN d.amount * 2.17 " +
           "WHEN d.frequency = 'WEEKLY' AND d.amount IS NOT NULL THEN d.amount * 4.33 " +
           "WHEN d.frequency = 'ANNUALLY' AND d.amount IS NOT NULL THEN d.amount / 12 " +
           "ELSE 0 END), 0) " +
           "FROM Deduction d WHERE d.employee.id = :employeeId AND d.status = 'ACTIVE'")
    BigDecimal getMonthlyDeductionTotalForEmployee(@Param("employeeId") Long employeeId);
}