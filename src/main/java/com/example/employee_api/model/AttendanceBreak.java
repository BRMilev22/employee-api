package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.BreakType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "attendance_breaks", indexes = {
    @Index(name = "idx_break_attendance", columnList = "time_attendance_id"),
    @Index(name = "idx_break_start_time", columnList = "start_time")
})
public class AttendanceBreak extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_attendance_id", nullable = false)
    @JsonBackReference
    private TimeAttendance timeAttendance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "break_type", nullable = false)
    private BreakType breakType;
    
    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Size(max = 200, message = "Notes must not exceed 200 characters")
    @Column(name = "notes", length = 200)
    private String notes;
    
    // Constructors
    public AttendanceBreak() {}
    
    public AttendanceBreak(TimeAttendance timeAttendance, BreakType breakType, LocalDateTime startTime) {
        this.timeAttendance = timeAttendance;
        this.breakType = breakType;
        this.startTime = startTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TimeAttendance getTimeAttendance() {
        return timeAttendance;
    }
    
    public void setTimeAttendance(TimeAttendance timeAttendance) {
        this.timeAttendance = timeAttendance;
    }
    
    public BreakType getBreakType() {
        return breakType;
    }
    
    public void setBreakType(BreakType breakType) {
        this.breakType = breakType;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Business methods
    public boolean isActive() {
        return endTime == null;
    }
    
    public void endBreak(LocalDateTime endTime) {
        this.endTime = endTime;
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceBreak that = (AttendanceBreak) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(timeAttendance, that.timeAttendance) &&
               Objects.equals(startTime, that.startTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, timeAttendance, startTime);
    }
    
    @Override
    public String toString() {
        return "AttendanceBreak{" +
               "id=" + id +
               ", breakType=" + breakType +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", durationMinutes=" + durationMinutes +
               '}';
    }
}