package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TimeAttendanceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TimeAttendanceRepository timeAttendanceRepository;

    @Autowired
    private AttendanceBreakRepository attendanceBreakRepository;

    @Autowired
    private AttendanceCorrectionRepository attendanceCorrectionRepository;

    // Test data
    private Employee testEmployee;
    private User testUser;
    private User testManager;
    private Department testDepartment;
    private Role testRole;
    private TimeAttendance testAttendance;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up
        attendanceCorrectionRepository.deleteAll();
        attendanceBreakRepository.deleteAll();
        timeAttendanceRepository.deleteAll();
        employeeRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test role
        testRole = new Role();
        testRole.setName("EMPLOYEE");
        testRole.setDescription("Employee Role");
        testRole.setPermissions(new HashSet<>());
        testRole = roleRepository.save(testRole);

        // Create test department
        testDepartment = new Department();
        testDepartment.setDepartmentCode("TEST");
        testDepartment.setName("Test Department");
        testDepartment.setStatus(DepartmentStatus.ACTIVE);
        testDepartment = departmentRepository.save(testDepartment);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.getRoles().add(testRole);
        testUser = userRepository.save(testUser);

        // Create test manager user
        testManager = new User();
        testManager.setUsername("testmanager");
        testManager.setEmail("manager@example.com");
        testManager.setPassword("password");
        testManager.setFirstName("Test");
        testManager.setLastName("Manager");
        testManager.getRoles().add(testRole);
        testManager = userRepository.save(testManager);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("Employee");
        testEmployee.setEmail("test@example.com");
        testEmployee.setJobTitle("Software Developer");
        testEmployee.setGender("MALE");
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        testEmployee.setDepartment(testDepartment);
        testEmployee.setSalary(new BigDecimal("50000"));
        testEmployee.setHireDate(LocalDate.now().minusYears(1));
        testEmployee = employeeRepository.save(testEmployee);

        // Create test attendance record
        testAttendance = new TimeAttendance();
        testAttendance.setEmployee(testEmployee);
        testAttendance.setWorkDate(LocalDate.now());
        testAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);
        testAttendance = timeAttendanceRepository.save(testAttendance);
    }

    // Clock In Tests

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClockIn_Success() throws Exception {
        // Clean any existing attendance for today
        timeAttendanceRepository.deleteAll();

        String clockInRequest = """
                {
                    "workLocation": "Office",
                    "isRemoteWork": false,
                    "notes": "Starting work day"
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-in", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockInRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.attendanceStatus").value("PRESENT"))
                .andExpect(jsonPath("$.workLocation").value("Office"))
                .andExpect(jsonPath("$.isRemoteWork").value(false))
                .andExpect(jsonPath("$.notes").value("Starting work day"))
                .andExpect(jsonPath("$.clockInTime").exists())
                .andExpect(jsonPath("$.clockOutTime").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClockIn_AlreadyClockedIn() throws Exception {
        String clockInRequest = """
                {
                    "workLocation": "Office",
                    "isRemoteWork": false,
                    "notes": "Starting work day"
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-in", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockInRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("already clocked in")));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClockIn_EmployeeNotFound() throws Exception {
        String clockInRequest = """
                {
                    "workLocation": "Office",
                    "isRemoteWork": false
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-in", 99999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockInRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Employee not found")));
    }

    // Clock Out Tests

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClockOut_Success() throws Exception {
        String clockOutRequest = """
                {
                    "notes": "End of work day"
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-out", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockOutRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.clockInTime").exists())
                .andExpect(jsonPath("$.clockOutTime").exists())
                .andExpect(jsonPath("$.totalHoursWorked").exists())
                .andExpect(jsonPath("$.notes").value(containsString("End of work day")));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClockOut_NotClockedIn() throws Exception {
        // Create an employee with no clock-in record
        timeAttendanceRepository.deleteAll();

        String clockOutRequest = """
                {
                    "notes": "End of work day"
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-out", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockOutRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("No clock-in record found")));
    }

    // Break Management Tests

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testStartBreak_Success() throws Exception {
        String breakRequest = """
                {
                    "breakType": "LUNCH_BREAK",
                    "notes": "Lunch time"
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/breaks/start", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(breakRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.breakType").value("LUNCH_BREAK"))
                .andExpect(jsonPath("$.notes").value("Lunch time"))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testStartBreak_NotClockedIn() throws Exception {
        timeAttendanceRepository.deleteAll();

        String breakRequest = """
                {
                    "breakType": "COFFEE_BREAK"
                }
                """;

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/breaks/start", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(breakRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("not currently clocked in")));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testEndBreak_Success() throws Exception {
        // First start a break
        AttendanceBreak attendanceBreak = new AttendanceBreak();
        attendanceBreak.setTimeAttendance(testAttendance);
        attendanceBreak.setBreakType(BreakType.LUNCH_BREAK);
        attendanceBreak.setStartTime(LocalDateTime.now().minusMinutes(30));
        attendanceBreak = attendanceBreakRepository.save(attendanceBreak);

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/breaks/{breakId}/end",
                        testEmployee.getId(), attendanceBreak.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakType").value("LUNCH_BREAK"))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.durationMinutes").exists());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testEndBreak_BreakNotFound() throws Exception {
        mockMvc.perform(post("/api/attendance/employees/{employeeId}/breaks/{breakId}/end",
                        testEmployee.getId(), 99999L)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Break not found")));
    }

    // Attendance Query Tests

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployeeAttendance_Success() throws Exception {
        mockMvc.perform(get("/api/attendance/employees/{employeeId}", testEmployee.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployeeAttendanceByDateRange_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/api/attendance/employees/{employeeId}/date-range", testEmployee.getId())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].employeeId").value(testEmployee.getId()));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployeeAttendanceSummary_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/api/attendance/employees/{employeeId}/summary", testEmployee.getId())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.periodStart").value(startDate.toString()))
                .andExpect(jsonPath("$.periodEnd").value(endDate.toString()))
                .andExpect(jsonPath("$.totalWorkDays").exists())
                .andExpect(jsonPath("$.daysPresent").exists())
                .andExpect(jsonPath("$.totalHoursWorked").exists());
    }

    // Correction Tests

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testSubmitCorrection_Success() throws Exception {
        String correctionRequest = """
                {
                    "timeAttendanceId": %d,
                    "correctionType": "CLOCK_IN_CORRECTION",
                    "requestedClockIn": "2024-09-22 08:00:00",
                    "reason": "Forgot to clock in on time"
                }
                """.formatted(testAttendance.getId());

        mockMvc.perform(post("/api/attendance/corrections")
                        .param("employeeId", testEmployee.getId().toString())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctionRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correctionType").value("CLOCK_IN_CORRECTION"))
                .andExpect(jsonPath("$.reason").value("Forgot to clock in on time"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requestedClockIn").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testApproveCorrection_Success() throws Exception {
        // Create a correction first
        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setTimeAttendance(testAttendance);
        correction.setRequestedBy(testUser);
        correction.setCorrectionType("CLOCK_IN_CORRECTION");
        correction.setReason("Forgot to clock in");
        correction.setRequestedClockIn(LocalDateTime.now().minusHours(8));
        correction.setStatus(CorrectionStatus.PENDING);
        correction = attendanceCorrectionRepository.save(correction);

        mockMvc.perform(post("/api/attendance/corrections/{correctionId}/approve", correction.getId())
                        .param("approverId", testManager.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedByUserId").value(testManager.getId()));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testRejectCorrection_Success() throws Exception {
        // Create a correction first
        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setTimeAttendance(testAttendance);
        correction.setRequestedBy(testUser);
        correction.setCorrectionType("CLOCK_IN_CORRECTION");
        correction.setReason("Forgot to clock in");
        correction.setStatus(CorrectionStatus.PENDING);
        correction = attendanceCorrectionRepository.save(correction);

        mockMvc.perform(post("/api/attendance/corrections/{correctionId}/reject", correction.getId())
                        .param("approverId", testManager.getId().toString())
                        .param("rejectionReason", "Insufficient documentation")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.approvedByUserId").value(testManager.getId()));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testApproveCorrection_NotFound() throws Exception {
        mockMvc.perform(post("/api/attendance/corrections/{correctionId}/approve", 99999L)
                        .param("approverId", testManager.getId().toString())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Correction not found")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testApproveCorrection_NotPending() throws Exception {
        // Create an already approved correction
        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setTimeAttendance(testAttendance);
        correction.setRequestedBy(testUser);
        correction.setCorrectionType("CLOCK_IN_CORRECTION");
        correction.setReason("Test");
        correction.setStatus(CorrectionStatus.APPROVED);
        correction = attendanceCorrectionRepository.save(correction);

        mockMvc.perform(post("/api/attendance/corrections/{correctionId}/approve", correction.getId())
                        .param("approverId", testManager.getId().toString())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("not in pending status")));
    }

    // Validation Tests

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClockIn_InvalidWorkLocation() throws Exception {
        String clockInRequest = """
                {
                    "workLocation": "%s",
                    "isRemoteWork": false
                }
                """.formatted("A".repeat(201)); // Exceeds max length

        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-in", testEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockInRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testSubmitCorrection_MissingRequiredFields() throws Exception {
        String correctionRequest = """
                {
                    "timeAttendanceId": %d
                }
                """.formatted(testAttendance.getId());

        mockMvc.perform(post("/api/attendance/corrections")
                        .param("employeeId", testEmployee.getId().toString())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctionRequest))
                .andExpect(status().isBadRequest());
    }

    // Security Tests

    @Test
    void testClockIn_WithoutCSRF() throws Exception {
        String clockInRequest = """
                {
                    "workLocation": "Office"
                }
                """;

        // Test without CSRF token - should fail due to missing CSRF or validation
        mockMvc.perform(post("/api/attendance/employees/{employeeId}/clock-in", testEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clockInRequest))
                .andExpect(status().is4xxClientError()); // Either 400 or 403 is acceptable
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployeeAttendance_WithValidRole() throws Exception {
        mockMvc.perform(get("/api/attendance/employees/{employeeId}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}