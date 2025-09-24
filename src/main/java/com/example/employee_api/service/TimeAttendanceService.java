package com.example.employee_api.service;

import com.example.employee_api.dto.attendance.*;
import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.AttendanceStatus;
import com.example.employee_api.model.enums.BreakType;
import com.example.employee_api.model.enums.CorrectionStatus;
import com.example.employee_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimeAttendanceService {

    private final TimeAttendanceRepository timeAttendanceRepository;
    private final AttendanceBreakRepository attendanceBreakRepository;
    private final AttendanceCorrectionRepository attendanceCorrectionRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    // Business constants
    private static final int STANDARD_WORK_HOURS = 8;
    private static final int MINUTES_PER_HOUR = 60;
    private static final BigDecimal OVERTIME_THRESHOLD = new BigDecimal("8.0");

    @Autowired
    public TimeAttendanceService(
            TimeAttendanceRepository timeAttendanceRepository,
            AttendanceBreakRepository attendanceBreakRepository,
            AttendanceCorrectionRepository attendanceCorrectionRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {
        this.timeAttendanceRepository = timeAttendanceRepository;
        this.attendanceBreakRepository = attendanceBreakRepository;
        this.attendanceCorrectionRepository = attendanceCorrectionRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // Clock In/Out Operations

    public TimeAttendanceResponseDto clockIn(Long employeeId, ClockInRequestDto request) {
        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Check if already clocked in today
        LocalDate today = LocalDate.now();
        Optional<TimeAttendance> existingAttendance = timeAttendanceRepository
                .findByEmployeeIdAndWorkDate(employeeId, today);

        TimeAttendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
            if (attendance.isClockedIn()) {
                throw new RuntimeException("Employee is already clocked in for today");
            }
        } else {
            attendance = new TimeAttendance(employee, today);
        }

        // Set clock in details
        LocalDateTime clockInTime = request.getClockInTime() != null ?
                request.getClockInTime() : LocalDateTime.now();

        attendance.setClockInTime(clockInTime);
        attendance.setWorkLocation(request.getWorkLocation());
        attendance.setIsRemoteWork(request.getIsRemoteWork() != null ? request.getIsRemoteWork() : false);
        attendance.setNotes(request.getNotes());
        attendance.setAttendanceStatus(AttendanceStatus.PRESENT);

        // Calculate if late
        if (attendance.getScheduledStartTime() != null) {
            calculateLateMinutes(attendance);
        }

        attendance = timeAttendanceRepository.save(attendance);
        return convertToResponseDto(attendance);
    }

    public TimeAttendanceResponseDto clockOut(Long employeeId, ClockOutRequestDto request) {
        // Find today's attendance record
        LocalDate today = LocalDate.now();
        TimeAttendance attendance = timeAttendanceRepository
                .findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No clock-in record found for today"));

        if (attendance.isClockedOut()) {
            throw new RuntimeException("Employee is already clocked out for today");
        }

        // End any active break
        Optional<AttendanceBreak> activeBreak = attendanceBreakRepository
                .findActiveBreakByEmployeeId(employeeId);
        if (activeBreak.isPresent()) {
            endBreak(employeeId, activeBreak.get().getId());
        }

        // Set clock out time
        LocalDateTime clockOutTime = request.getClockOutTime() != null ?
                request.getClockOutTime() : LocalDateTime.now();
        attendance.setClockOutTime(clockOutTime);

        // Update notes if provided
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String existingNotes = attendance.getNotes();
            String newNotes = existingNotes != null ?
                    existingNotes + " | Clock Out: " + request.getNotes() :
                    "Clock Out: " + request.getNotes();
            attendance.setNotes(newNotes);
        }

        // Calculate work hours and early departure
        calculateWorkHours(attendance);
        if (attendance.getScheduledEndTime() != null) {
            calculateEarlyDepartureMinutes(attendance);
        }

        attendance = timeAttendanceRepository.save(attendance);
        return convertToResponseDto(attendance);
    }

    // Break Management

    public AttendanceBreakResponseDto startBreak(Long employeeId, BreakRequestDto request) {
        // Verify employee is clocked in
        TimeAttendance attendance = timeAttendanceRepository
                .findCurrentlyActiveClock(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee is not currently clocked in"));

        // Check if already on break
        if (attendanceBreakRepository.isEmployeeOnBreak(employeeId)) {
            throw new RuntimeException("Employee is already on a break");
        }

        AttendanceBreak attendanceBreak = new AttendanceBreak();
        attendanceBreak.setTimeAttendance(attendance);
        attendanceBreak.setBreakType(request.getBreakType());
        attendanceBreak.setStartTime(request.getStartTime() != null ?
                request.getStartTime() : LocalDateTime.now());
        attendanceBreak.setNotes(request.getNotes());

        attendanceBreak = attendanceBreakRepository.save(attendanceBreak);
        return convertToBreakResponseDto(attendanceBreak);
    }

    public AttendanceBreakResponseDto endBreak(Long employeeId, Long breakId) {
        AttendanceBreak attendanceBreak = attendanceBreakRepository.findById(breakId)
                .orElseThrow(() -> new RuntimeException("Break not found with id: " + breakId));

        // Verify the break belongs to the employee
        if (!attendanceBreak.getTimeAttendance().getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Break does not belong to the specified employee");
        }

        if (attendanceBreak.getEndTime() != null) {
            throw new RuntimeException("Break is already ended");
        }

        LocalDateTime endTime = LocalDateTime.now();
        attendanceBreak.setEndTime(endTime);

        // Calculate duration
        Duration duration = Duration.between(attendanceBreak.getStartTime(), endTime);
        attendanceBreak.setDurationMinutes((int) duration.toMinutes());

        // Update total break duration in attendance record
        updateAttendanceBreakDuration(attendanceBreak.getTimeAttendance());

        attendanceBreak = attendanceBreakRepository.save(attendanceBreak);
        return convertToBreakResponseDto(attendanceBreak);
    }

    // Attendance Correction

    public AttendanceCorrectionResponseDto submitCorrection(Long employeeId, AttendanceCorrectionRequestDto request) {
        // Validate employee and attendance record
        TimeAttendance attendance = timeAttendanceRepository.findById(request.getTimeAttendanceId())
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        if (!attendance.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Attendance record does not belong to the specified employee");
        }

        // Get the employee's user account as the requester
        Employee employee = attendance.getEmployee();
        User requestedBy = userRepository.findByEmail(employee.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found for employee"));

        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setTimeAttendance(attendance);
        correction.setRequestedBy(requestedBy);
        correction.setCorrectionType(request.getCorrectionType());
        // Store the original and requested correction times
        correction.setOriginalClockIn(attendance.getClockInTime());
        correction.setOriginalClockOut(attendance.getClockOutTime());
        correction.setRequestedClockIn(request.getRequestedClockIn());
        correction.setRequestedClockOut(request.getRequestedClockOut());
        correction.setReason(request.getReason());
        correction.setStatus(CorrectionStatus.PENDING);

        correction = attendanceCorrectionRepository.save(correction);
        return convertToCorrectionResponseDto(correction);
    }

    public AttendanceCorrectionResponseDto approveCorrection(Long correctionId, Long approverId) {
        AttendanceCorrection correction = attendanceCorrectionRepository.findById(correctionId)
                .orElseThrow(() -> new RuntimeException("Correction not found with id: " + correctionId));

        if (correction.getStatus() != CorrectionStatus.PENDING) {
            throw new RuntimeException("Correction is not in pending status");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        correction.setApprovedBy(approver);
        correction.setStatus(CorrectionStatus.APPROVED);

        // Apply the correction to the attendance record
        applyCorrection(correction);

        correction = attendanceCorrectionRepository.save(correction);
        return convertToCorrectionResponseDto(correction);
    }

    public AttendanceCorrectionResponseDto rejectCorrection(Long correctionId, Long approverId, String rejectionReason) {
        AttendanceCorrection correction = attendanceCorrectionRepository.findById(correctionId)
                .orElseThrow(() -> new RuntimeException("Correction not found with id: " + correctionId));

        if (correction.getStatus() != CorrectionStatus.PENDING) {
            throw new RuntimeException("Correction is not in pending status");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        correction.setApprovedBy(approver);
        correction.setStatus(CorrectionStatus.REJECTED);
        // Store rejection reason in the reason field or add notes
        String existingReason = correction.getReason();
        correction.setReason(existingReason + " | Rejection: " + rejectionReason);

        correction = attendanceCorrectionRepository.save(correction);
        return convertToCorrectionResponseDto(correction);
    }

    // Query Methods

    public PagedResponse<TimeAttendanceResponseDto> getEmployeeAttendance(Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "workDate"));
        Page<TimeAttendance> attendancePage = timeAttendanceRepository.findByEmployeeId(employeeId, pageable);

        List<TimeAttendanceResponseDto> content = attendancePage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return PagedResponse.of(attendancePage, "workDate", "desc", content);
    }

    public List<TimeAttendanceResponseDto> getEmployeeAttendanceByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<TimeAttendance> attendanceList = timeAttendanceRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);

        return attendanceList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public AttendanceSummaryDto getEmployeeAttendanceSummary(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<TimeAttendance> attendanceList = timeAttendanceRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);

        AttendanceSummaryDto summary = new AttendanceSummaryDto();
        summary.setEmployeeId(employeeId);
        summary.setPeriodStart(startDate);
        summary.setPeriodEnd(endDate);

        // Calculate statistics
        summary.setTotalWorkDays(attendanceList.size());
        summary.setDaysPresent((int) attendanceList.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.PRESENT ||
                           a.getAttendanceStatus() == AttendanceStatus.REMOTE_WORK)
                .count());
        summary.setDaysAbsent((int) attendanceList.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.ABSENT)
                .count());
        summary.setDaysLate((int) attendanceList.stream()
                .filter(a -> a.getLateMinutes() > 0)
                .count());
        summary.setRemoteDays((int) attendanceList.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsRemoteWork()))
                .count());

        // Calculate hours
        BigDecimal totalHours = attendanceList.stream()
                .map(TimeAttendance::getTotalHoursWorked)
                .filter(hours -> hours != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalHoursWorked(totalHours);

        BigDecimal overtimeHours = attendanceList.stream()
                .map(TimeAttendance::getOvertimeHours)
                .filter(hours -> hours != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalOvertimeHours(overtimeHours);

        if (summary.getTotalWorkDays() > 0) {
            summary.setAverageHoursPerDay(totalHours.divide(
                    new BigDecimal(summary.getTotalWorkDays()), 2, RoundingMode.HALF_UP));
        }

        summary.setTotalOvertimeHours(overtimeHours);
        summary.setTotalRegularHours(totalHours.subtract(overtimeHours));

        return summary;
    }

    // Business Logic Helper Methods

    private void calculateLateMinutes(TimeAttendance attendance) {
        if (attendance.getClockInTime() != null && attendance.getScheduledStartTime() != null) {
            if (attendance.getClockInTime().isAfter(attendance.getScheduledStartTime())) {
                Duration lateDuration = Duration.between(attendance.getScheduledStartTime(),
                        attendance.getClockInTime());
                attendance.setLateMinutes((int) lateDuration.toMinutes());

                if (attendance.getLateMinutes() > 0) {
                    attendance.setAttendanceStatus(AttendanceStatus.LATE);
                }
            }
        }
    }

    private void calculateEarlyDepartureMinutes(TimeAttendance attendance) {
        if (attendance.getClockOutTime() != null && attendance.getScheduledEndTime() != null) {
            if (attendance.getClockOutTime().isBefore(attendance.getScheduledEndTime())) {
                Duration earlyDuration = Duration.between(attendance.getClockOutTime(),
                        attendance.getScheduledEndTime());
                attendance.setEarlyDepartureMinutes((int) earlyDuration.toMinutes());

                if (attendance.getEarlyDepartureMinutes() > 0 &&
                    attendance.getAttendanceStatus() == AttendanceStatus.PRESENT) {
                    attendance.setAttendanceStatus(AttendanceStatus.LEFT_EARLY);
                }
            }
        }
    }

    private void calculateWorkHours(TimeAttendance attendance) {
        if (attendance.getClockInTime() != null && attendance.getClockOutTime() != null) {
            Duration workDuration = Duration.between(attendance.getClockInTime(),
                    attendance.getClockOutTime());

            // Subtract break time
            Long totalBreakMinutes = attendanceBreakRepository
                    .getTotalBreakDurationForAttendance(attendance.getId());
            if (totalBreakMinutes == null) {
                totalBreakMinutes = 0L;
            }

            long workMinutes = workDuration.toMinutes() - totalBreakMinutes;
            BigDecimal totalHours = new BigDecimal(workMinutes)
                    .divide(new BigDecimal(MINUTES_PER_HOUR), 2, RoundingMode.HALF_UP);

            attendance.setTotalHoursWorked(totalHours);

            // Calculate regular and overtime hours
            if (totalHours.compareTo(OVERTIME_THRESHOLD) <= 0) {
                attendance.setRegularHours(totalHours);
                attendance.setOvertimeHours(BigDecimal.ZERO);
            } else {
                attendance.setRegularHours(OVERTIME_THRESHOLD);
                attendance.setOvertimeHours(totalHours.subtract(OVERTIME_THRESHOLD));
            }

            attendance.setBreakDurationMinutes(totalBreakMinutes.intValue());
        }
    }

    private void updateAttendanceBreakDuration(TimeAttendance attendance) {
        Long totalBreakMinutes = attendanceBreakRepository
                .getTotalBreakDurationForAttendance(attendance.getId());
        attendance.setBreakDurationMinutes(totalBreakMinutes != null ? totalBreakMinutes.intValue() : 0);

        // Recalculate work hours if clocked out
        if (attendance.isClockedOut()) {
            calculateWorkHours(attendance);
        }

        timeAttendanceRepository.save(attendance);
    }

    private void applyCorrection(AttendanceCorrection correction) {
        TimeAttendance attendance = correction.getTimeAttendance();

        if (correction.getRequestedClockIn() != null) {
            attendance.setClockInTime(correction.getRequestedClockIn());
        }

        if (correction.getRequestedClockOut() != null) {
            attendance.setClockOutTime(correction.getRequestedClockOut());
        }

        // Recalculate hours and status
        if (attendance.isClockedOut()) {
            calculateWorkHours(attendance);
        }

        if (attendance.getScheduledStartTime() != null) {
            calculateLateMinutes(attendance);
        }

        if (attendance.getScheduledEndTime() != null) {
            calculateEarlyDepartureMinutes(attendance);
        }

        timeAttendanceRepository.save(attendance);
    }

    // DTO Conversion Methods

    private TimeAttendanceResponseDto convertToResponseDto(TimeAttendance attendance) {
        TimeAttendanceResponseDto dto = new TimeAttendanceResponseDto();
        dto.setId(attendance.getId());
        dto.setEmployeeId(attendance.getEmployee().getId());
        dto.setEmployeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        dto.setWorkDate(attendance.getWorkDate());
        dto.setClockInTime(attendance.getClockInTime());
        dto.setClockOutTime(attendance.getClockOutTime());
        dto.setScheduledStartTime(attendance.getScheduledStartTime());
        dto.setScheduledEndTime(attendance.getScheduledEndTime());
        dto.setAttendanceStatus(attendance.getAttendanceStatus());
        dto.setTotalHoursWorked(attendance.getTotalHoursWorked());
        dto.setRegularHours(attendance.getRegularHours());
        dto.setOvertimeHours(attendance.getOvertimeHours());
        dto.setBreakDurationMinutes(attendance.getBreakDurationMinutes());
        dto.setLateMinutes(attendance.getLateMinutes());
        dto.setEarlyDepartureMinutes(attendance.getEarlyDepartureMinutes());
        dto.setNotes(attendance.getNotes());
        dto.setWorkLocation(attendance.getWorkLocation());
        dto.setIsRemoteWork(attendance.getIsRemoteWork());
        dto.setIpAddress(attendance.getIpAddress());

        // Convert breaks and corrections
        List<AttendanceBreakResponseDto> breaks = attendance.getBreaks().stream()
                .map(this::convertToBreakResponseDto)
                .collect(Collectors.toList());
        dto.setBreaks(breaks);

        List<AttendanceCorrectionResponseDto> corrections = attendance.getCorrections().stream()
                .map(this::convertToCorrectionResponseDto)
                .collect(Collectors.toList());
        dto.setCorrections(corrections);

        return dto;
    }

    private AttendanceBreakResponseDto convertToBreakResponseDto(AttendanceBreak attendanceBreak) {
        AttendanceBreakResponseDto dto = new AttendanceBreakResponseDto();
        dto.setId(attendanceBreak.getId());
        // Note: AttendanceBreakResponseDto doesn't have timeAttendanceId field based on the model
        dto.setBreakType(attendanceBreak.getBreakType());
        dto.setStartTime(attendanceBreak.getStartTime());
        dto.setEndTime(attendanceBreak.getEndTime());
        dto.setDurationMinutes(attendanceBreak.getDurationMinutes());
        dto.setNotes(attendanceBreak.getNotes());
        return dto;
    }

    private AttendanceCorrectionResponseDto convertToCorrectionResponseDto(AttendanceCorrection correction) {
        AttendanceCorrectionResponseDto dto = new AttendanceCorrectionResponseDto();
        dto.setId(correction.getId());
        dto.setRequestedByUserId(correction.getRequestedBy().getId());
        dto.setRequestedByUserName(correction.getRequestedBy().getUsername());
        dto.setApprovedByUserId(correction.getApprovedBy() != null ? correction.getApprovedBy().getId() : null);
        dto.setApprovedByUserName(correction.getApprovedBy() != null ? correction.getApprovedBy().getUsername() : null);
        dto.setStatus(correction.getStatus());
        dto.setCorrectionType(correction.getCorrectionType());
        dto.setOriginalClockIn(correction.getOriginalClockIn());
        dto.setRequestedClockIn(correction.getRequestedClockIn());
        dto.setOriginalClockOut(correction.getOriginalClockOut());
        dto.setRequestedClockOut(correction.getRequestedClockOut());
        dto.setReason(correction.getReason());
        dto.setManagerComments(correction.getManagerComments());
        dto.setRequestDate(correction.getRequestDate());
        dto.setResponseDate(correction.getResponseDate());
        return dto;
    }
}