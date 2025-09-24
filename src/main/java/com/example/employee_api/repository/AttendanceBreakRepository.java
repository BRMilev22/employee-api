package com.example.employee_api.repository;

import com.example.employee_api.model.AttendanceBreak;
import com.example.employee_api.model.enums.BreakType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceBreakRepository extends JpaRepository<AttendanceBreak, Long> {

    // Basic queries
    List<AttendanceBreak> findByTimeAttendanceId(Long timeAttendanceId);
    List<AttendanceBreak> findByBreakType(BreakType breakType);

    // Active break queries
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.id = :attendanceId AND ab.endTime IS NULL")
    Optional<AttendanceBreak> findActiveBreakByAttendanceId(@Param("attendanceId") Long attendanceId);

    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.endTime IS NULL")
    List<AttendanceBreak> findAllActiveBreaks();

    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.endTime IS NULL")
    Optional<AttendanceBreak> findActiveBreakByEmployeeId(@Param("employeeId") Long employeeId);

    // Date range queries
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.id = :attendanceId AND ab.startTime BETWEEN :startTime AND :endTime")
    List<AttendanceBreak> findByAttendanceIdAndDateRange(@Param("attendanceId") Long attendanceId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.startTime BETWEEN :startTime AND :endTime")
    List<AttendanceBreak> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    // Break type and employee queries
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.breakType = :breakType")
    List<AttendanceBreak> findByEmployeeIdAndBreakType(@Param("employeeId") Long employeeId, @Param("breakType") BreakType breakType);

    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.id = :attendanceId AND ab.breakType = :breakType")
    List<AttendanceBreak> findByAttendanceIdAndBreakType(@Param("attendanceId") Long attendanceId, @Param("breakType") BreakType breakType);

    // Statistics queries
    @Query("SELECT COUNT(ab) FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.breakType = :breakType")
    Long countByEmployeeIdAndBreakType(@Param("employeeId") Long employeeId, @Param("breakType") BreakType breakType);

    @Query("SELECT SUM(ab.durationMinutes) FROM AttendanceBreak ab WHERE ab.timeAttendance.id = :attendanceId")
    Long getTotalBreakDurationForAttendance(@Param("attendanceId") Long attendanceId);

    @Query("SELECT SUM(ab.durationMinutes) FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.startTime BETWEEN :startTime AND :endTime")
    Long getTotalBreakDurationForEmployee(@Param("employeeId") Long employeeId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT AVG(ab.durationMinutes) FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.breakType = :breakType")
    Double getAverageBreakDuration(@Param("employeeId") Long employeeId, @Param("breakType") BreakType breakType);

    // Long break detection
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.durationMinutes > :maxMinutes")
    List<AttendanceBreak> findLongBreaks(@Param("maxMinutes") Integer maxMinutes);

    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId AND ab.durationMinutes > :maxMinutes")
    List<AttendanceBreak> findEmployeeLongBreaks(@Param("employeeId") Long employeeId, @Param("maxMinutes") Integer maxMinutes);

    // Department-based queries
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.department.id = :departmentId AND ab.startTime BETWEEN :startTime AND :endTime")
    List<AttendanceBreak> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    // Recent breaks
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.employee.id = :employeeId ORDER BY ab.startTime DESC")
    Page<AttendanceBreak> findRecentByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    @Query("SELECT ab FROM AttendanceBreak ab ORDER BY ab.startTime DESC")
    Page<AttendanceBreak> findAllRecent(Pageable pageable);

    // Overlap detection
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.timeAttendance.id = :attendanceId AND " +
           "((ab.startTime BETWEEN :startTime AND :endTime) OR (ab.endTime BETWEEN :startTime AND :endTime) OR " +
           "(ab.startTime <= :startTime AND ab.endTime >= :endTime))")
    List<AttendanceBreak> findOverlappingBreaks(@Param("attendanceId") Long attendanceId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    // Existence checks
    @Query("SELECT CASE WHEN COUNT(ab) > 0 THEN true ELSE false END FROM AttendanceBreak ab " +
           "WHERE ab.timeAttendance.employee.id = :employeeId AND ab.endTime IS NULL")
    boolean isEmployeeOnBreak(@Param("employeeId") Long employeeId);

    boolean existsByTimeAttendanceIdAndBreakTypeAndEndTimeIsNull(Long timeAttendanceId, BreakType breakType);

    // Incomplete breaks (started but not ended)
    @Query("SELECT ab FROM AttendanceBreak ab WHERE ab.endTime IS NULL AND ab.startTime < :cutoffTime")
    List<AttendanceBreak> findIncompleteBreaksOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}