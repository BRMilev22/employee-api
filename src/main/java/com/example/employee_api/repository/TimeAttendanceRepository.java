package com.example.employee_api.repository;

import com.example.employee_api.model.TimeAttendance;
import com.example.employee_api.model.enums.AttendanceStatus;
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
public interface TimeAttendanceRepository extends JpaRepository<TimeAttendance, Long>, JpaSpecificationExecutor<TimeAttendance> {

    // Basic queries
    List<TimeAttendance> findByEmployeeId(Long employeeId);
    Optional<TimeAttendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
    List<TimeAttendance> findByWorkDate(LocalDate workDate);
    List<TimeAttendance> findByAttendanceStatus(AttendanceStatus status);

    // Paginated queries
    Page<TimeAttendance> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<TimeAttendance> findByAttendanceStatus(AttendanceStatus status, Pageable pageable);

    // Date range queries
    List<TimeAttendance> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findByWorkDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.workDate BETWEEN :startDate AND :endDate ORDER BY ta.workDate DESC")
    Page<TimeAttendance> findByEmployeeIdAndWorkDateBetween(@Param("employeeId") Long employeeId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate,
                                                            Pageable pageable);

    // Status-based queries
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.attendanceStatus = :status")
    List<TimeAttendance> findByEmployeeIdAndAttendanceStatus(@Param("employeeId") Long employeeId, @Param("status") AttendanceStatus status);

    // Current status queries
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.clockInTime IS NOT NULL AND ta.clockOutTime IS NULL")
    Optional<TimeAttendance> findCurrentlyActiveClock(@Param("employeeId") Long employeeId);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.clockInTime IS NOT NULL AND ta.clockOutTime IS NULL")
    List<TimeAttendance> findAllCurrentlyActiveClocks();

    // Late arrivals and early departures
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.lateMinutes > 0 AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findLateArrivals(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.earlyDepartureMinutes > 0 AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findEarlyDepartures(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Overtime queries
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.overtimeHours > 0 AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findOvertimeRecords(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.overtimeHours > 0 AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findEmployeeOvertimeRecords(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // Remote work queries
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.isRemoteWork = true AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findRemoteWorkRecords(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.isRemoteWork = true")
    List<TimeAttendance> findEmployeeRemoteWorkRecords(@Param("employeeId") Long employeeId);

    // Department-based queries
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.department.id = :departmentId AND ta.workDate = :workDate")
    List<TimeAttendance> findByDepartmentAndWorkDate(@Param("departmentId") Long departmentId, @Param("workDate") LocalDate workDate);

    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.department.id = :departmentId AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // Statistics queries
    @Query("SELECT COUNT(ta) FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.attendanceStatus = :status AND ta.workDate BETWEEN :startDate AND :endDate")
    Long countByEmployeeIdAndStatusAndDateRange(@Param("employeeId") Long employeeId,
                                               @Param("status") AttendanceStatus status,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(ta.totalHoursWorked) FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.workDate BETWEEN :startDate AND :endDate")
    Double getAverageHoursWorked(@Param("employeeId") Long employeeId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ta.totalHoursWorked) FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.workDate BETWEEN :startDate AND :endDate")
    Double getTotalHoursWorked(@Param("employeeId") Long employeeId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ta.overtimeHours) FROM TimeAttendance ta WHERE ta.employee.id = :employeeId AND ta.workDate BETWEEN :startDate AND :endDate")
    Double getTotalOvertimeHours(@Param("employeeId") Long employeeId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    // Existence checks
    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END FROM TimeAttendance ta " +
           "WHERE ta.employee.id = :employeeId AND ta.clockInTime IS NOT NULL AND ta.clockOutTime IS NULL")
    boolean isEmployeeCurrentlyClockedIn(@Param("employeeId") Long employeeId);

    // Recent records
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id = :employeeId ORDER BY ta.workDate DESC")
    Page<TimeAttendance> findRecentByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    @Query("SELECT ta FROM TimeAttendance ta ORDER BY ta.workDate DESC, ta.clockInTime DESC")
    Page<TimeAttendance> findAllRecent(Pageable pageable);

    // Bulk operations support
    @Query("SELECT ta FROM TimeAttendance ta WHERE ta.employee.id IN :employeeIds AND ta.workDate BETWEEN :startDate AND :endDate")
    List<TimeAttendance> findByEmployeeIdsAndDateRange(@Param("employeeIds") List<Long> employeeIds,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}