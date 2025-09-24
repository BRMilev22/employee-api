package com.example.employee_api.controller;

import com.example.employee_api.dto.ReportRequest;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class ReportControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    private User testUser;
    private Employee testEmployee;
    private Department testDepartment;
    private Position testPosition;

    @BeforeEach
    @Transactional
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up data
        reportRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$encrypted.password.hash");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

                // Create test department
        testDepartment = new Department();
        testDepartment.setDepartmentCode("TD001");
        testDepartment.setName("Test Department");
        testDepartment.setDescription("Test Department Description");
        testDepartment.setLocation("Test Location");
        testDepartment.setBudget(BigDecimal.valueOf(100000));
        testDepartment.setStatus(DepartmentStatus.ACTIVE);
        testDepartment = departmentRepository.save(testDepartment);

        // Create test position
        testPosition = new Position();
        testPosition.setTitle("Test Position");
        testPosition.setDescription("Test Position Description");
        testPosition.setDepartment(testDepartment);
        testPosition.setMinSalary(BigDecimal.valueOf(50000.0));
        testPosition.setMaxSalary(BigDecimal.valueOf(100000.0));
        testPosition.setLevel(PositionLevel.MID);
        testPosition.setRequiredSkills("Java, Spring Boot");
        testPosition.setStatus(PositionStatus.ACTIVE);
        testPosition = positionRepository.save(testPosition);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setGender("MALE");
        testEmployee.setPhone("123-456-7890");
        testEmployee.setHireDate(LocalDate.of(2022, 1, 15));
        testEmployee.setDepartment(testDepartment);
        testEmployee.setCurrentPosition(testPosition);
        testEmployee.setSalary(BigDecimal.valueOf(75000.0));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee = employeeRepository.save(testEmployee);
    }

    // ==================== REPORT GENERATION TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testGenerateReport_Success() throws Exception {
        ReportRequest request = new ReportRequest();
        request.setType(ReportType.EMPLOYEES);
        request.setTitle("Employee Report");
        request.setDescription("List of all employees");

        mockMvc.perform(post("/api/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Employee Report"))
                .andExpect(jsonPath("$.type").value("EMPLOYEES"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGenerateReport_WithInvalidType() throws Exception {
        mockMvc.perform(post("/api/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"INVALID_TYPE\",\"title\":\"Invalid Report\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== REPORT CRUD TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetAllReports_Success() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportById_Success() throws Exception {
        // Create a test report
        Report report = new Report(ReportType.EMPLOYEES, "Test Report", testUser.getUsername());
        report.setDescription("Test Description");
        report = reportRepository.save(report);

        mockMvc.perform(get("/api/reports/" + report.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(report.getId()))
                .andExpect(jsonPath("$.title").value("Test Report"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportById_NotFound() throws Exception {
        mockMvc.perform(get("/api/reports/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteReport_Success() throws Exception {
        // Create a test report
        Report report = new Report(ReportType.EMPLOYEES, "Test Report", testUser.getUsername());
        report = reportRepository.save(report);

        mockMvc.perform(delete("/api/reports/" + report.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDeleteReport_Forbidden() throws Exception {
        // Create a test report
        Report report = new Report(ReportType.EMPLOYEES, "Test Report", testUser.getUsername());
        report = reportRepository.save(report);

        mockMvc.perform(delete("/api/reports/" + report.getId()))
                .andExpect(status().isForbidden());
    }

    // ==================== DOWNLOAD TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testDownloadReport_Success() throws Exception {
        // Create a completed test report
        Report report = new Report(ReportType.EMPLOYEES, "Test Report", testUser.getUsername());
        report.setStatus(ReportStatus.COMPLETED);
        report.setReportData("{\"employees\": []}");
        report = reportRepository.save(report);

        mockMvc.perform(get("/api/reports/" + report.getId() + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDownloadReport_NotFound() throws Exception {
        mockMvc.perform(get("/api/reports/999/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testDownloadReport_NotCompleted() throws Exception {
        // Create a pending test report
        Report report = new Report(ReportType.EMPLOYEES, "Test Report", testUser.getUsername());
        report.setStatus(ReportStatus.PENDING);
        report = reportRepository.save(report);

        mockMvc.perform(get("/api/reports/" + report.getId() + "/download"))
                .andExpect(status().isBadRequest());
    }

    // ==================== FILTERING TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByType_Success() throws Exception {
        mockMvc.perform(get("/api/reports/type/EMPLOYEES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByType_InvalidType() throws Exception {
        mockMvc.perform(get("/api/reports/type/INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/reports/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByStatus_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/reports/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByDateRange_Success() throws Exception {
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";

        mockMvc.perform(get("/api/reports/date-range")
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByCreatedBy_Success() throws Exception {
        mockMvc.perform(get("/api/reports/created-by/test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== ANALYTICS TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetDashboardData_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").isNumber())
                .andExpect(jsonPath("$.totalDepartments").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetKpiData_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").isNumber())
                .andExpect(jsonPath("$.employeeTurnoverRate").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetEmployeeAnalytics_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").isNumber())
                .andExpect(jsonPath("$.averageSalary").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetDepartmentAnalytics_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").isNumber())
                .andExpect(jsonPath("$.employeeDistribution").exists());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetPositionAnalytics_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").isNumber())
                .andExpect(jsonPath("$.levelDistribution").exists());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetSalaryAnalysis_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/salaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").isNumber())
                .andExpect(jsonPath("$.medianSalary").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetPayrollAnalytics_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalaryExpense").isNumber())
                .andExpect(jsonPath("$.averageSalary").isNumber());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetTrendAnalysis_Success() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeGrowthRate").isNumber())
                .andExpect(jsonPath("$.employeeGrowth").exists());
    }

    // ==================== UTILITY TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportTypes_Success() throws Exception {
        mockMvc.perform(get("/api/reports/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8))); // Expecting 8 ReportType enum values
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportStatuses_Success() throws Exception {
        mockMvc.perform(get("/api/reports/statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetPaginatedReports_Success() throws Exception {
        mockMvc.perform(get("/api/reports")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.size").value(10));
    }

    // ==================== SECURITY TESTS ====================

    @Test
    void testAnalytics_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/reports/analytics/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testDeleteReport_InsufficientPermissions() throws Exception {
        Report report = new Report(ReportType.EMPLOYEES, "Test Report", testUser.getUsername());
        report = reportRepository.save(report);

        mockMvc.perform(delete("/api/reports/" + report.getId()))
                .andExpect(status().isForbidden());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(roles = {"HR"})
    void testAnalytics_WithEmptyData() throws Exception {
        // Clear all employees to test empty data scenario
        employeeRepository.deleteAll();

        mockMvc.perform(get("/api/reports/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").value(0));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void testGetReportsByDateRange_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/reports/date-range")
                .param("startDate", "invalid-date")
                .param("endDate", "2024-12-31"))
                .andExpect(status().isBadRequest());
    }
}