package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.CorrectionStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "attendance_corrections", indexes = {
    @Index(name = "idx_correction_attendance", columnList = "time_attendance_id"),
    @Index(name = "idx_correction_status", columnList = "status"),
    @Index(name = "idx_correction_requested_by", columnList = "requested_by_user_id")
})
public class AttendanceCorrection extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_attendance_id", nullable = false)
    @JsonBackReference
    private TimeAttendance timeAttendance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CorrectionStatus status = CorrectionStatus.PENDING;
    
    @NotBlank(message = "Correction type is required")
    @Size(max = 50, message = "Correction type must not exceed 50 characters")
    @Column(name = "correction_type", nullable = false, length = 50)
    private String correctionType;
    
    @Column(name = "original_clock_in")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime originalClockIn;
    
    @Column(name = "requested_clock_in")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedClockIn;
    
    @Column(name = "original_clock_out")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime originalClockOut;
    
    @Column(name = "requested_clock_out")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedClockOut;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;
    
    @Size(max = 500, message = "Manager comments must not exceed 500 characters")
    @Column(name = "manager_comments", length = 500)
    private String managerComments;
    
    @Column(name = "request_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;
    
    @Column(name = "response_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime responseDate;
    
    // Constructors
    public AttendanceCorrection() {
        this.requestDate = LocalDateTime.now();
    }
    
    public AttendanceCorrection(TimeAttendance timeAttendance, User requestedBy, String correctionType, String reason) {
        this();
        this.timeAttendance = timeAttendance;
        this.requestedBy = requestedBy;
        this.correctionType = correctionType;
        this.reason = reason;
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
    
    public User getRequestedBy() {
        return requestedBy;
    }
    
    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }
    
    public User getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
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
    
    // Business methods
    public void approve(User approver, String comments) {
        this.status = CorrectionStatus.APPROVED;
        this.approvedBy = approver;
        this.managerComments = comments;
        this.responseDate = LocalDateTime.now();
    }
    
    public void reject(User approver, String comments) {
        this.status = CorrectionStatus.REJECTED;
        this.approvedBy = approver;
        this.managerComments = comments;
        this.responseDate = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return status == CorrectionStatus.PENDING;
    }
    
    public boolean isApproved() {
        return status == CorrectionStatus.APPROVED;
    }
    
    public boolean isRejected() {
        return status == CorrectionStatus.REJECTED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceCorrection that = (AttendanceCorrection) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(timeAttendance, that.timeAttendance) &&
               Objects.equals(requestDate, that.requestDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, timeAttendance, requestDate);
    }
    
    @Override
    public String toString() {
        return "AttendanceCorrection{" +
               "id=" + id +
               ", correctionType='" + correctionType + '\'' +
               ", status=" + status +
               ", requestDate=" + requestDate +
               '}';
    }
}