package com.example.employee_api.dto.attendance;

import com.example.employee_api.model.enums.BreakType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AttendanceBreakResponseDto {
    
    private Long id;
    private BreakType breakType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private Integer durationMinutes;
    private String notes;
    
    // Constructors
    public AttendanceBreakResponseDto() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
}