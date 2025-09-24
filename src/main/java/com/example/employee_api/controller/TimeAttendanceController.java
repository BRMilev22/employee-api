package com.example.employee_api.controller;

import com.example.employee_api.dto.attendance.*;
import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.service.TimeAttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class TimeAttendanceController {

    private final TimeAttendanceService timeAttendanceService;

    @Autowired
    public TimeAttendanceController(TimeAttendanceService timeAttendanceService) {
        this.timeAttendanceService = timeAttendanceService;
    }

    /**
     * POST /api/attendance/employees/{employeeId}/clock-in - Clock in employee
     */
    @PostMapping("/employees/{employeeId}/clock-in")
    public ResponseEntity<TimeAttendanceResponseDto> clockIn(
            @PathVariable Long employeeId,
            @Valid @RequestBody ClockInRequestDto request) {
        TimeAttendanceResponseDto response = timeAttendanceService.clockIn(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/attendance/employees/{employeeId}/clock-out - Clock out employee
     */
    @PostMapping("/employees/{employeeId}/clock-out")
    public ResponseEntity<TimeAttendanceResponseDto> clockOut(
            @PathVariable Long employeeId,
            @Valid @RequestBody ClockOutRequestDto request) {
        TimeAttendanceResponseDto response = timeAttendanceService.clockOut(employeeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/attendance/employees/{employeeId}/breaks/start - Start break
     */
    @PostMapping("/employees/{employeeId}/breaks/start")
    public ResponseEntity<AttendanceBreakResponseDto> startBreak(
            @PathVariable Long employeeId,
            @Valid @RequestBody BreakRequestDto request) {
        AttendanceBreakResponseDto response = timeAttendanceService.startBreak(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/attendance/employees/{employeeId}/breaks/{breakId}/end - End break
     */
    @PostMapping("/employees/{employeeId}/breaks/{breakId}/end")
    public ResponseEntity<AttendanceBreakResponseDto> endBreak(
            @PathVariable Long employeeId,
            @PathVariable Long breakId) {
        AttendanceBreakResponseDto response = timeAttendanceService.endBreak(employeeId, breakId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/attendance/employees/{employeeId} - Get employee attendance records (paginated)
     */
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<PagedResponse<TimeAttendanceResponseDto>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<TimeAttendanceResponseDto> response = timeAttendanceService.getEmployeeAttendance(employeeId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/attendance/employees/{employeeId}/date-range - Get employee attendance by date range
     */
    @GetMapping("/employees/{employeeId}/date-range")
    public ResponseEntity<List<TimeAttendanceResponseDto>> getEmployeeAttendanceByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TimeAttendanceResponseDto> response = timeAttendanceService.getEmployeeAttendanceByDateRange(employeeId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/attendance/employees/{employeeId}/summary - Get employee attendance summary
     */
    @GetMapping("/employees/{employeeId}/summary")
    public ResponseEntity<AttendanceSummaryDto> getEmployeeAttendanceSummary(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AttendanceSummaryDto response = timeAttendanceService.getEmployeeAttendanceSummary(employeeId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/attendance/corrections - Submit attendance correction
     */
    @PostMapping("/corrections")
    public ResponseEntity<AttendanceCorrectionResponseDto> submitCorrection(
            @RequestParam Long employeeId,
            @Valid @RequestBody AttendanceCorrectionRequestDto request) {
        AttendanceCorrectionResponseDto response = timeAttendanceService.submitCorrection(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/attendance/corrections/{correctionId}/approve - Approve attendance correction
     */
    @PostMapping("/corrections/{correctionId}/approve")
    public ResponseEntity<AttendanceCorrectionResponseDto> approveCorrection(
            @PathVariable Long correctionId,
            @RequestParam Long approverId) {
        AttendanceCorrectionResponseDto response = timeAttendanceService.approveCorrection(correctionId, approverId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/attendance/corrections/{correctionId}/reject - Reject attendance correction
     */
    @PostMapping("/corrections/{correctionId}/reject")
    public ResponseEntity<AttendanceCorrectionResponseDto> rejectCorrection(
            @PathVariable Long correctionId,
            @RequestParam Long approverId,
            @RequestParam String rejectionReason) {
        AttendanceCorrectionResponseDto response = timeAttendanceService.rejectCorrection(correctionId, approverId, rejectionReason);
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for this controller
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}