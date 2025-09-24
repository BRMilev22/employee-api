package com.example.employee_api.dto.attendance;

import com.example.employee_api.model.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TimeAttendanceResponseDto {
    
    private Long id;
    private Long employeeId;
    private String employeeName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockOutTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEndTime;
    
    private AttendanceStatus attendanceStatus;
    private BigDecimal totalHoursWorked;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private Integer breakDurationMinutes;
    private Integer lateMinutes;
    private Integer earlyDepartureMinutes;
    private String notes;
    private String workLocation;
    private Boolean isRemoteWork;
    private String ipAddress;
    
    private List<AttendanceBreakResponseDto> breaks;
    private List<AttendanceCorrectionResponseDto> corrections;
    
    // Constructors
    public TimeAttendanceResponseDto() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getEmployeeName() {
        return employeeName;
    }
    
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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
    
    public List<AttendanceBreakResponseDto> getBreaks() {
        return breaks;
    }
    
    public void setBreaks(List<AttendanceBreakResponseDto> breaks) {
        this.breaks = breaks;
    }
    
    public List<AttendanceCorrectionResponseDto> getCorrections() {
        return corrections;
    }
    
    public void setCorrections(List<AttendanceCorrectionResponseDto> corrections) {
        this.corrections = corrections;
    }
}