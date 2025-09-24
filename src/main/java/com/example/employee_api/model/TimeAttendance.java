package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "time_attendance", indexes = {
    @Index(name = "idx_attendance_employee", columnList = "employee_id"),
    @Index(name = "idx_attendance_date", columnList = "work_date"),
    @Index(name = "idx_attendance_status", columnList = "attendance_status"),
    @Index(name = "idx_attendance_employee_date", columnList = "employee_id, work_date", unique = true)
})
public class TimeAttendance extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference
    private Employee employee;
    
    @NotNull(message = "Work date is required")
    @Column(name = "work_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
    
    @Column(name = "clock_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockInTime;
    
    @Column(name = "clock_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockOutTime;
    
    @Column(name = "scheduled_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStartTime;
    
    @Column(name = "scheduled_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.ABSENT;
    
    @Column(name = "total_hours_worked", precision = 5, scale = 2)
    private BigDecimal totalHoursWorked = BigDecimal.ZERO;
    
    @Column(name = "regular_hours", precision = 5, scale = 2)
    private BigDecimal regularHours = BigDecimal.ZERO;
    
    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    
    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes = 0;
    
    @Column(name = "late_minutes")
    private Integer lateMinutes = 0;
    
    @Column(name = "early_departure_minutes")
    private Integer earlyDepartureMinutes = 0;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Column(name = "work_location", length = 200)
    private String workLocation;
    
    @Column(name = "is_remote_work")
    private Boolean isRemoteWork = false;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @OneToMany(mappedBy = "timeAttendance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceBreak> breaks = new ArrayList<>();
    
    @OneToMany(mappedBy = "timeAttendance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceCorrection> corrections = new ArrayList<>();
    
    // Constructors
    public TimeAttendance() {}
    
    public TimeAttendance(Employee employee, LocalDate workDate) {
        this.employee = employee;
        this.workDate = workDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    public LocalDate getWorkDate() {
        return workDate;
    }
    
    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }
    
    public LocalDateTime getClockInTime() {
        return clockInTime;
    }
    
    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }
    
    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }
    
    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }
    
    public LocalDateTime getScheduledStartTime() {
        return scheduledStartTime;
    }
    
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }
    
    public LocalDateTime getScheduledEndTime() {
        return scheduledEndTime;
    }
    
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }
    
    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }
    
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
    
    public BigDecimal getTotalHoursWorked() {
        return totalHoursWorked;
    }
    
    public void setTotalHoursWorked(BigDecimal totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }
    
    public BigDecimal getRegularHours() {
        return regularHours;
    }
    
    public void setRegularHours(BigDecimal regularHours) {
        this.regularHours = regularHours;
    }
    
    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }
    
    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }
    
    public Integer getBreakDurationMinutes() {
        return breakDurationMinutes;
    }
    
    public void setBreakDurationMinutes(Integer breakDurationMinutes) {
        this.breakDurationMinutes = breakDurationMinutes;
    }
    
    public Integer getLateMinutes() {
        return lateMinutes;
    }
    
    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }
    
    public Integer getEarlyDepartureMinutes() {
        return earlyDepartureMinutes;
    }
    
    public void setEarlyDepartureMinutes(Integer earlyDepartureMinutes) {
        this.earlyDepartureMinutes = earlyDepartureMinutes;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getWorkLocation() {
        return workLocation;
    }
    
    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }
    
    public Boolean getIsRemoteWork() {
        return isRemoteWork;
    }
    
    public void setIsRemoteWork(Boolean isRemoteWork) {
        this.isRemoteWork = isRemoteWork;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public List<AttendanceBreak> getBreaks() {
        return breaks;
    }
    
    public void setBreaks(List<AttendanceBreak> breaks) {
        this.breaks = breaks;
    }
    
    public List<AttendanceCorrection> getCorrections() {
        return corrections;
    }
    
    public void setCorrections(List<AttendanceCorrection> corrections) {
        this.corrections = corrections;
    }
    
    // Business methods
    public void addBreak(AttendanceBreak attendanceBreak) {
        breaks.add(attendanceBreak);
        attendanceBreak.setTimeAttendance(this);
    }
    
    public void removeBreak(AttendanceBreak attendanceBreak) {
        breaks.remove(attendanceBreak);
        attendanceBreak.setTimeAttendance(null);
    }
    
    public void addCorrection(AttendanceCorrection correction) {
        corrections.add(correction);
        correction.setTimeAttendance(this);
    }
    
    public void removeCorrection(AttendanceCorrection correction) {
        corrections.remove(correction);
        correction.setTimeAttendance(null);
    }
    
    public boolean isClockedIn() {
        return clockInTime != null && clockOutTime == null;
    }
    
    public boolean isClockedOut() {
        return clockInTime != null && clockOutTime != null;
    }
    
    public boolean isOnBreak() {
        return breaks.stream().anyMatch(b -> b.getEndTime() == null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeAttendance that = (TimeAttendance) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(employee, that.employee) &&
               Objects.equals(workDate, that.workDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, employee, workDate);
    }
    
    @Override
    public String toString() {
        return "TimeAttendance{" +
               "id=" + id +
               ", workDate=" + workDate +
               ", attendanceStatus=" + attendanceStatus +
               ", totalHoursWorked=" + totalHoursWorked +
               '}';
    }
}