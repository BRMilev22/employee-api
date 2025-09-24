package com.example.employee_api.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AttendanceSummaryDto {
    
    private Long employeeId;
    private String employeeName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;
    
    private Integer totalWorkDays;
    private Integer daysPresent;
    private Integer daysAbsent;
    private Integer daysLate;
    private Integer daysLeftEarly;
    private Integer remoteDays;
    
    private BigDecimal totalHoursWorked;
    private BigDecimal totalRegularHours;
    private BigDecimal totalOvertimeHours;
    private Integer totalBreakMinutes;
    private Integer totalLateMinutes;
    private Integer totalEarlyDepartureMinutes;
    
    private BigDecimal averageHoursPerDay;
    private Double attendanceRate;
    private Double punctualityRate;
    
    // Constructors
    public AttendanceSummaryDto() {}
    
    // Getters and Setters
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
    
    public LocalDate getPeriodStart() {
        return periodStart;
    }
    
    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }
    
    public LocalDate getPeriodEnd() {
        return periodEnd;
    }
    
    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }
    
    public Integer getTotalWorkDays() {
        return totalWorkDays;
    }
    
    public void setTotalWorkDays(Integer totalWorkDays) {
        this.totalWorkDays = totalWorkDays;
    }
    
    public Integer getDaysPresent() {
        return daysPresent;
    }
    
    public void setDaysPresent(Integer daysPresent) {
        this.daysPresent = daysPresent;
    }
    
    public Integer getDaysAbsent() {
        return daysAbsent;
    }
    
    public void setDaysAbsent(Integer daysAbsent) {
        this.daysAbsent = daysAbsent;
    }
    
    public Integer getDaysLate() {
        return daysLate;
    }
    
    public void setDaysLate(Integer daysLate) {
        this.daysLate = daysLate;
    }
    
    public Integer getDaysLeftEarly() {
        return daysLeftEarly;
    }
    
    public void setDaysLeftEarly(Integer daysLeftEarly) {
        this.daysLeftEarly = daysLeftEarly;
    }
    
    public Integer getRemoteDays() {
        return remoteDays;
    }
    
    public void setRemoteDays(Integer remoteDays) {
        this.remoteDays = remoteDays;
    }
    
    public BigDecimal getTotalHoursWorked() {
        return totalHoursWorked;
    }
    
    public void setTotalHoursWorked(BigDecimal totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }
    
    public BigDecimal getTotalRegularHours() {
        return totalRegularHours;
    }
    
    public void setTotalRegularHours(BigDecimal totalRegularHours) {
        this.totalRegularHours = totalRegularHours;
    }
    
    public BigDecimal getTotalOvertimeHours() {
        return totalOvertimeHours;
    }
    
    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }
    
    public Integer getTotalBreakMinutes() {
        return totalBreakMinutes;
    }
    
    public void setTotalBreakMinutes(Integer totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }
    
    public Integer getTotalLateMinutes() {
        return totalLateMinutes;
    }
    
    public void setTotalLateMinutes(Integer totalLateMinutes) {
        this.totalLateMinutes = totalLateMinutes;
    }
    
    public Integer getTotalEarlyDepartureMinutes() {
        return totalEarlyDepartureMinutes;
    }
    
    public void setTotalEarlyDepartureMinutes(Integer totalEarlyDepartureMinutes) {
        this.totalEarlyDepartureMinutes = totalEarlyDepartureMinutes;
    }
    
    public BigDecimal getAverageHoursPerDay() {
        return averageHoursPerDay;
    }
    
    public void setAverageHoursPerDay(BigDecimal averageHoursPerDay) {
        this.averageHoursPerDay = averageHoursPerDay;
    }
    
    public Double getAttendanceRate() {
        return attendanceRate;
    }
    
    public void setAttendanceRate(Double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }
    
    public Double getPunctualityRate() {
        return punctualityRate;
    }
    
    public void setPunctualityRate(Double punctualityRate) {
        this.punctualityRate = punctualityRate;
    }
}