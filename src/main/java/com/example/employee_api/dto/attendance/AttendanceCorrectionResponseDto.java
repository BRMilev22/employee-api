package com.example.employee_api.dto.attendance;

import com.example.employee_api.model.enums.CorrectionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AttendanceCorrectionResponseDto {
    
    private Long id;
    private Long requestedByUserId;
    private String requestedByUserName;
    private Long approvedByUserId;
    private String approvedByUserName;
    private CorrectionStatus status;
    private String correctionType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime originalClockIn;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedClockIn;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime originalClockOut;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedClockOut;
    
    private String reason;
    private String managerComments;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime responseDate;
    
    // Constructors
    public AttendanceCorrectionResponseDto() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRequestedByUserId() {
        return requestedByUserId;
    }
    
    public void setRequestedByUserId(Long requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }
    
    public String getRequestedByUserName() {
        return requestedByUserName;
    }
    
    public void setRequestedByUserName(String requestedByUserName) {
        this.requestedByUserName = requestedByUserName;
    }
    
    public Long getApprovedByUserId() {
        return approvedByUserId;
    }
    
    public void setApprovedByUserId(Long approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }
    
    public String getApprovedByUserName() {
        return approvedByUserName;
    }
    
    public void setApprovedByUserName(String approvedByUserName) {
        this.approvedByUserName = approvedByUserName;
    }
    
    public CorrectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(CorrectionStatus status) {
        this.status = status;
    }
    
    public String getCorrectionType() {
        return correctionType;
    }
    
    public void setCorrectionType(String correctionType) {
        this.correctionType = correctionType;
    }
    
    public LocalDateTime getOriginalClockIn() {
        return originalClockIn;
    }
    
    public void setOriginalClockIn(LocalDateTime originalClockIn) {
        this.originalClockIn = originalClockIn;
    }
    
    public LocalDateTime getRequestedClockIn() {
        return requestedClockIn;
    }
    
    public void setRequestedClockIn(LocalDateTime requestedClockIn) {
        this.requestedClockIn = requestedClockIn;
    }
    
    public LocalDateTime getOriginalClockOut() {
        return originalClockOut;
    }
    
    public void setOriginalClockOut(LocalDateTime originalClockOut) {
        this.originalClockOut = originalClockOut;
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
    
    public String getManagerComments() {
        return managerComments;
    }
    
    public void setManagerComments(String managerComments) {
        this.managerComments = managerComments;
    }
    
    public LocalDateTime getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public LocalDateTime getResponseDate() {
        return responseDate;
    }
    
    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
    }
}