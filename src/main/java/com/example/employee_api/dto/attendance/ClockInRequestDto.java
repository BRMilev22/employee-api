package com.example.employee_api.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ClockInRequestDto {
    
    @Size(max = 200, message = "Work location must not exceed 200 characters")
    private String workLocation;
    
    private Boolean isRemoteWork = false;
    
    @Size(max = 300, message = "Notes must not exceed 300 characters")
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockInTime;
    
    // Constructors
    public ClockInRequestDto() {}
    
    public ClockInRequestDto(String workLocation, Boolean isRemoteWork, String notes) {
        this.workLocation = workLocation;
        this.isRemoteWork = isRemoteWork;
        this.notes = notes;
    }
    
    // Getters and Setters
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getClockInTime() {
        return clockInTime;
    }
    
    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }
}