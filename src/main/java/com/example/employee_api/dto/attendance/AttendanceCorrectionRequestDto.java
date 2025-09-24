package com.example.employee_api.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AttendanceCorrectionRequestDto {
    
    @NotNull(message = "Time attendance ID is required")
    private Long timeAttendanceId;
    
    @NotBlank(message = "Correction type is required")
    @Size(max = 50, message = "Correction type must not exceed 50 characters")
    private String correctionType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedClockIn;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedClockOut;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    // Constructors
    public AttendanceCorrectionRequestDto() {}
    
    public AttendanceCorrectionRequestDto(Long timeAttendanceId, String correctionType, String reason) {
        this.timeAttendanceId = timeAttendanceId;
        this.correctionType = correctionType;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getTimeAttendanceId() {
        return timeAttendanceId;
    }
    
    public void setTimeAttendanceId(Long timeAttendanceId) {
        this.timeAttendanceId = timeAttendanceId;
    }
    
    public String getCorrectionType() {
        return correctionType;
    }
    
    public void setCorrectionType(String correctionType) {
        this.correctionType = correctionType;
    }
    
    public LocalDateTime getRequestedClockIn() {
        return requestedClockIn;
    }
    
    public void setRequestedClockIn(LocalDateTime requestedClockIn) {
        this.requestedClockIn = requestedClockIn;
    }
    
    public LocalDateTime getRequestedClockOut() {
        return requestedClockOut;
    }
    
    public void setRequestedClockOut(LocalDateTime requestedClockOut) {
        this.requestedClockOut = requestedClockOut;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}