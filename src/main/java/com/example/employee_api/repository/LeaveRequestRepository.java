package com.example.employee_api.repository;

import com.example.employee_api.model.LeaveRequest;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    /**
     * Find leave requests by employee
     */
    List<LeaveRequest> findByEmployee(Employee employee);

    /**
     * Find leave requests by employee ID
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId")
    List<LeaveRequest> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find leave requests by employee and status
     */
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveRequest.LeaveStatus status);

    /**
     * Find leave requests by employee ID and status
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = :status")
    List<LeaveRequest> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") LeaveRequest.LeaveStatus status);

    /**
     * Find leave requests by status
     */
    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);

    /**
     * Find pending leave requests
     */
    List<LeaveRequest> findByStatusOrderByAppliedDateAsc(LeaveRequest.LeaveStatus status);

    /**
     * Find leave requests by leave type
     */
    List<LeaveRequest> findByLeaveType(LeaveType leaveType);

    /**
     * Find leave requests by leave type ID
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.leaveType.id = :leaveTypeId")
    List<LeaveRequest> findByLeaveTypeId(@Param("leaveTypeId") Long leaveTypeId);

    /**
     * Find leave requests by date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(lr.startDate BETWEEN :startDate AND :endDate) OR " +
           "(lr.endDate BETWEEN :startDate AND :endDate) OR " +
           "(lr.startDate <= :startDate AND lr.endDate >= :endDate)")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find overlapping leave requests for an employee
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employee.id = :employeeId AND " +
           "lr.status IN (:statuses) AND " +
           "((lr.startDate BETWEEN :startDate AND :endDate) OR " +
           "(lr.endDate BETWEEN :startDate AND :endDate) OR " +
           "(lr.startDate <= :startDate AND lr.endDate >= :endDate))")
    List<LeaveRequest> findOverlappingRequests(@Param("employeeId") Long employeeId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("statuses") List<LeaveRequest.LeaveStatus> statuses);

    /**
     * Find leave requests approved by a specific employee
     */
    List<LeaveRequest> findByApprovedBy(Employee approvedBy);

    /**
     * Find leave requests approved by employee ID
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.approvedBy.id = :approvedById")
    List<LeaveRequest> findByApprovedById(@Param("approvedById") Long approvedById);

    /**
     * Find leave requests applied in date range
     */
    List<LeaveRequest> findByAppliedDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find leave requests created in date range
     */
    List<LeaveRequest> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * Find leave requests by employee and year
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employee.id = :employeeId AND " +
           "YEAR(lr.startDate) = :year")
    List<LeaveRequest> findByEmployeeIdAndYear(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Find leave requests by employee, leave type and year
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employee.id = :employeeId AND " +
           "lr.leaveType.id = :leaveTypeId AND " +
           "YEAR(lr.startDate) = :year")
    List<LeaveRequest> findByEmployeeIdAndLeaveTypeIdAndYear(@Param("employeeId") Long employeeId,
                                                           @Param("leaveTypeId") Long leaveTypeId,
                                                           @Param("year") Integer year);

    /**
     * Get approved leave days for employee by leave type and year
     */
    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr WHERE " +
           "lr.employee.id = :employeeId AND " +
           "lr.leaveType.id = :leaveTypeId AND " +
           "lr.status = 'APPROVED' AND " +
           "YEAR(lr.startDate) = :year")
    Double getApprovedLeaveDaysByEmployeeAndLeaveTypeAndYear(@Param("employeeId") Long employeeId,
                                                           @Param("leaveTypeId") Long leaveTypeId,
                                                           @Param("year") Integer year);

    /**
     * Get pending leave days for employee by leave type and year
     */
    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr WHERE " +
           "lr.employee.id = :employeeId AND " +
           "lr.leaveType.id = :leaveTypeId AND " +
           "lr.status = 'PENDING' AND " +
           "YEAR(lr.startDate) = :year")
    Double getPendingLeaveDaysByEmployeeAndLeaveTypeAndYear(@Param("employeeId") Long employeeId,
                                                          @Param("leaveTypeId") Long leaveTypeId,
                                                          @Param("year") Integer year);

    /**
     * Find team leave requests for a manager
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employee.manager.id = :managerId AND " +
           "lr.status = :status")
    List<LeaveRequest> findTeamLeaveRequestsByManagerAndStatus(@Param("managerId") Long managerId, 
                                                              @Param("status") LeaveRequest.LeaveStatus status);

    /**
     * Find upcoming leave requests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.startDate >= :fromDate AND " +
           "lr.status = 'APPROVED' " +
           "ORDER BY lr.startDate ASC")
    List<LeaveRequest> findUpcomingLeaveRequests(@Param("fromDate") LocalDate fromDate);

    /**
     * Find current leave requests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.startDate <= :currentDate AND " +
           "lr.endDate >= :currentDate AND " +
           "lr.status = 'APPROVED'")
    List<LeaveRequest> findCurrentLeaveRequests(@Param("currentDate") LocalDate currentDate);

    /**
     * Find leave requests requiring approval
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.status = 'PENDING' AND " +
           "lr.leaveType.requiresApproval = true " +
           "ORDER BY lr.appliedDate ASC")
    List<LeaveRequest> findRequestsRequiringApproval();

    /**
     * Count leave requests by status
     */
    Long countByStatus(LeaveRequest.LeaveStatus status);

    /**
     * Count leave requests by employee and status
     */
    Long countByEmployeeAndStatus(Employee employee, LeaveRequest.LeaveStatus status);

    /**
     * Count leave requests by employee ID and status
     */
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = :status")
    Long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") LeaveRequest.LeaveStatus status);

    /**
     * Get leave request statistics for a year
     */
    @Query("SELECT " +
           "COUNT(lr) as totalRequests, " +
           "COUNT(CASE WHEN lr.status = 'APPROVED' THEN 1 END) as approvedRequests, " +
           "COUNT(CASE WHEN lr.status = 'PENDING' THEN 1 END) as pendingRequests, " +
           "COUNT(CASE WHEN lr.status = 'REJECTED' THEN 1 END) as rejectedRequests, " +
           "COALESCE(SUM(CASE WHEN lr.status = 'APPROVED' THEN lr.totalDays ELSE 0 END), 0) as totalApprovedDays " +
           "FROM LeaveRequest lr WHERE YEAR(lr.startDate) = :year")
    Object[] getLeaveRequestStatsByYear(@Param("year") Integer year);

    /**
     * Search leave requests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(LOWER(CONCAT(lr.employee.firstName, ' ', lr.employee.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lr.employee.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lr.leaveType.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lr.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<LeaveRequest> searchLeaveRequests(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find leave requests by department
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :departmentId")
    List<LeaveRequest> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * Find leave requests by department and status
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :departmentId AND lr.status = :status")
    List<LeaveRequest> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") LeaveRequest.LeaveStatus status);

    /**
     * Find long pending requests (older than specified days)
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.status = 'PENDING' AND " +
           "lr.appliedDate <= :cutoffDate " +
           "ORDER BY lr.appliedDate ASC")
    List<LeaveRequest> findLongPendingRequests(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find holiday overlapping leave requests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.status = 'APPROVED' AND " +
           "((lr.startDate <= :holidayDate AND lr.endDate >= :holidayDate))")
    List<LeaveRequest> findLeaveRequestsOnHoliday(@Param("holidayDate") LocalDate holidayDate);

    /**
     * Get monthly leave statistics
     */
    @Query("SELECT " +
           "MONTH(lr.startDate) as month, " +
           "COUNT(lr) as requestCount, " +
           "COALESCE(SUM(lr.totalDays), 0) as totalDays " +
           "FROM LeaveRequest lr WHERE " +
           "YEAR(lr.startDate) = :year AND " +
           "lr.status = 'APPROVED' " +
           "GROUP BY MONTH(lr.startDate) " +
           "ORDER BY MONTH(lr.startDate)")
    List<Object[]> getMonthlyLeaveStatistics(@Param("year") Integer year);
}