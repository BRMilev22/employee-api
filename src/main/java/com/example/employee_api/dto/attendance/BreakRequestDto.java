package com.example.employee_api.dto.attendance;

import com.example.employee_api.model.enums.BreakType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class BreakRequestDto {
    
    @NotNull(message = "Break type is required")
    private BreakType breakType;
    
    @Size(max = 200, message = "Notes must not exceed 200 characters")
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    // Constructors
    public BreakRequestDto() {}
    
    public BreakRequestDto(BreakType breakType, String notes) {
        this.breakType = breakType;
        this.notes = notes;
    }
    
    // Getters and Setters
    public BreakType getBreakType() {
        return breakType;
    }
    
    public void setBreakType(BreakType breakType) {
        this.breakType = breakType;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}