package com.example.employee_api.repository;

import com.example.employee_api.model.LeaveBalance;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LeaveBalance entity
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long>, JpaSpecificationExecutor<LeaveBalance> {

    /**
     * Find leave balance by employee, leave type, and year
     */
    Optional<LeaveBalance> findByEmployeeAndLeaveTypeAndYear(Employee employee, LeaveType leaveType, Integer year);

    /**
     * Find leave balance by employee ID, leave type ID, and year
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.leaveType.id = :leaveTypeId AND lb.year = :year")
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(@Param("employeeId") Long employeeId, 
                                                                @Param("leaveTypeId") Long leaveTypeId, 
                                                                @Param("year") Integer year);

    /**
     * Find all leave balances for an employee in a specific year
     */
    List<LeaveBalance> findByEmployeeAndYear(Employee employee, Integer year);

    /**
     * Find all leave balances for an employee ID in a specific year
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    List<LeaveBalance> findByEmployeeIdAndYear(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Find all leave balances for an employee
     */
    List<LeaveBalance> findByEmployee(Employee employee);

    /**
     * Find all leave balances for an employee ID
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId")
    List<LeaveBalance> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find all leave balances for a leave type in a specific year
     */
    List<LeaveBalance> findByLeaveTypeAndYear(LeaveType leaveType, Integer year);

    /**
     * Find all leave balances for a leave type ID in a specific year
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.leaveType.id = :leaveTypeId AND lb.year = :year")
    List<LeaveBalance> findByLeaveTypeIdAndYear(@Param("leaveTypeId") Long leaveTypeId, @Param("year") Integer year);

    /**
     * Find leave balances with remaining days greater than zero
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year AND lb.remainingDays > 0")
    List<LeaveBalance> findByEmployeeIdAndYearWithRemainingDays(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Find leave balances with insufficient balance
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year AND lb.remainingDays < :requiredDays")
    List<LeaveBalance> findByEmployeeIdAndYearWithInsufficientBalance(@Param("employeeId") Long employeeId, 
                                                                     @Param("year") Integer year, 
                                                                     @Param("requiredDays") Double requiredDays);

    /**
     * Find employees with leave balances expiring
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.year = :year AND lb.remainingDays > 0 AND lb.leaveType.carryForward = false")
    List<LeaveBalance> findExpiringBalances(@Param("year") Integer year);

    /**
     * Find employees eligible for carry forward
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.year = :year AND lb.remainingDays > 0 AND lb.leaveType.carryForward = true")
    List<LeaveBalance> findCarryForwardEligibleBalances(@Param("year") Integer year);

    /**
     * Get total allocated days for an employee in a year
     */
    @Query("SELECT COALESCE(SUM(lb.allocatedDays), 0) FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    Double getTotalAllocatedDaysByEmployeeAndYear(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Get total used days for an employee in a year
     */
    @Query("SELECT COALESCE(SUM(lb.usedDays), 0) FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    Double getTotalUsedDaysByEmployeeAndYear(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Get total remaining days for an employee in a year
     */
    @Query("SELECT COALESCE(SUM(lb.remainingDays), 0) FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    Double getTotalRemainingDaysByEmployeeAndYear(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Find employees with zero balance for a specific leave type
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.leaveType.id = :leaveTypeId AND lb.year = :year AND lb.remainingDays <= 0")
    List<LeaveBalance> findEmployeesWithZeroBalance(@Param("leaveTypeId") Long leaveTypeId, @Param("year") Integer year);

    /**
     * Find all unique years in leave balances
     */
    @Query("SELECT DISTINCT lb.year FROM LeaveBalance lb ORDER BY lb.year DESC")
    List<Integer> findDistinctYears();

    /**
     * Get leave balance statistics for a year
     */
    @Query("SELECT " +
           "COUNT(DISTINCT lb.employee.id) as employeeCount, " +
           "COALESCE(SUM(lb.allocatedDays), 0) as totalAllocated, " +
           "COALESCE(SUM(lb.usedDays), 0) as totalUsed, " +
           "COALESCE(SUM(lb.remainingDays), 0) as totalRemaining, " +
           "COALESCE(AVG(lb.allocatedDays), 0) as avgAllocated, " +
           "COALESCE(AVG(lb.usedDays), 0) as avgUsed " +
           "FROM LeaveBalance lb WHERE lb.year = :year")
    Object[] getLeaveBalanceStatsByYear(@Param("year") Integer year);

    /**
     * Get leave balance statistics by leave type for a year
     */
    @Query("SELECT " +
           "lb.leaveType.name as leaveTypeName, " +
           "COUNT(lb) as balanceCount, " +
           "COALESCE(SUM(lb.allocatedDays), 0) as totalAllocated, " +
           "COALESCE(SUM(lb.usedDays), 0) as totalUsed, " +
           "COALESCE(SUM(lb.remainingDays), 0) as totalRemaining " +
           "FROM LeaveBalance lb WHERE lb.year = :year " +
           "GROUP BY lb.leaveType.id, lb.leaveType.name " +
           "ORDER BY lb.leaveType.name")
    List<Object[]> getLeaveBalanceStatsByLeaveTypeAndYear(@Param("year") Integer year);

    /**
     * Search leave balances by employee name or leave type
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE " +
           "(LOWER(CONCAT(lb.employee.firstName, ' ', lb.employee.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lb.employee.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lb.leaveType.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "lb.year = :year")
    Page<LeaveBalance> searchLeaveBalances(@Param("searchTerm") String searchTerm, 
                                          @Param("year") Integer year, 
                                          Pageable pageable);

    /**
     * Find balances that need carry forward processing
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE " +
           "lb.year = :previousYear AND " +
           "lb.remainingDays > 0 AND " +
           "lb.leaveType.carryForward = true AND " +
           "NOT EXISTS (SELECT lb2 FROM LeaveBalance lb2 WHERE " +
           "lb2.employee = lb.employee AND lb2.leaveType = lb.leaveType AND lb2.year = :currentYear)")
    List<LeaveBalance> findBalancesNeedingCarryForward(@Param("previousYear") Integer previousYear, 
                                                      @Param("currentYear") Integer currentYear);

    /**
     * Update used and pending days for leave balance
     */
    @Query("UPDATE LeaveBalance lb SET " +
           "lb.usedDays = :usedDays, " +
           "lb.pendingDays = :pendingDays, " +
           "lb.remainingDays = lb.allocatedDays + COALESCE(lb.carryForwardDays, 0) - :usedDays - :pendingDays, " +
           "lb.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE lb.id = :id")
    void updateBalanceDays(@Param("id") Long id, 
                          @Param("usedDays") Double usedDays, 
                          @Param("pendingDays") Double pendingDays);
}