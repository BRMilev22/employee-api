package com.example.employee_api.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ClockOutRequestDto {
    
    @Size(max = 300, message = "Notes must not exceed 300 characters")
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockOutTime;
    
    // Constructors
    public ClockOutRequestDto() {}
    
    public ClockOutRequestDto(String notes) {
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }
    
    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }
}