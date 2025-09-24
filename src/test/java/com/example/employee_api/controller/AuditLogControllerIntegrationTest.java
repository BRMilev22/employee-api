package com.example.employee_api.controller;

import com.example.employee_api.dto.AuditLogRequestDto;
import com.example.employee_api.model.AuditLog;
import com.example.employee_api.model.Role;
import com.example.employee_api.model.User;
import com.example.employee_api.repository.AuditLogRepository;
import com.example.employee_api.repository.RoleRepository;
import com.example.employee_api.repository.UserRepository;
import com.example.employee_api.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User hrUser;
    private User regularUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        Role adminRole = new Role("ROLE_ADMIN", "Administrator role");
        Role hrRole = new Role("ROLE_HR", "HR role");
        Role userRole = new Role("ROLE_USER", "Regular user role");

        roleRepository.save(adminRole);
        roleRepository.save(hrRole);
        roleRepository.save(userRole);

        // Create test users
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmailVerified(true);
        adminUser.setEnabled(true);
        adminUser.setRoles(Set.of(adminRole, userRole));
        adminUser = userRepository.save(adminUser);

        hrUser = new User();
        hrUser.setUsername("hruser");
        hrUser.setEmail("hr@example.com");
        hrUser.setPassword(passwordEncoder.encode("password"));
        hrUser.setFirstName("HR");
        hrUser.setLastName("User");
        hrUser.setEmailVerified(true);
        hrUser.setEnabled(true);
        hrUser.setRoles(Set.of(hrRole, userRole));
        hrUser = userRepository.save(hrUser);

        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setEmailVerified(true);
        regularUser.setEnabled(true);
        regularUser.setRoles(Set.of(userRole));
        regularUser = userRepository.save(regularUser);

        // Create some test audit logs
        createTestAuditLogs();
    }

    private void createTestAuditLogs() {
        // Login success
        AuditLog loginLog = new AuditLog();
        loginLog.setUser(regularUser);
        loginLog.setUsername(regularUser.getUsername());
        loginLog.setActionType(AuditLogService.ACTION_LOGIN);
        loginLog.setDescription("User login successful");
        loginLog.setSuccess(true);
        loginLog.setSecurityEvent(true);
        loginLog.setIpAddress("192.168.1.100");
        loginLog.setTimestamp(LocalDateTime.now().minusHours(1));
        auditLogRepository.save(loginLog);

        // Login failure
        AuditLog loginFailLog = new AuditLog();
        loginFailLog.setUsername("unknown_user");
        loginFailLog.setActionType(AuditLogService.ACTION_LOGIN_FAILED);
        loginFailLog.setDescription("User login failed: Invalid credentials");
        loginFailLog.setSuccess(false);
        loginFailLog.setSecurityEvent(true);
        loginFailLog.setIpAddress("192.168.1.101");
        loginFailLog.setErrorMessage("Invalid credentials");
        loginFailLog.setTimestamp(LocalDateTime.now().minusMinutes(30));
        auditLogRepository.save(loginFailLog);

        // Create operation
        AuditLog createLog = new AuditLog();
        createLog.setUser(adminUser);
        createLog.setUsername(adminUser.getUsername());
        createLog.setActionType(AuditLogService.ACTION_CREATE);
        createLog.setEntityType("Employee");
        createLog.setEntityId(123L);
        createLog.setDescription("Created new employee");
        createLog.setSuccess(true);
        createLog.setSecurityEvent(false);
        createLog.setTimestamp(LocalDateTime.now().minusMinutes(15));
        auditLogRepository.save(createLog);

        // Update operation
        AuditLog updateLog = new AuditLog();
        updateLog.setUser(hrUser);
        updateLog.setUsername(hrUser.getUsername());
        updateLog.setActionType(AuditLogService.ACTION_UPDATE);
        updateLog.setEntityType("Employee");
        updateLog.setEntityId(123L);
        updateLog.setDescription("Updated employee information");
        updateLog.setSuccess(true);
        updateLog.setSecurityEvent(false);
        updateLog.setOldValues("{\"salary\": 50000}");
        updateLog.setNewValues("{\"salary\": 55000}");
        updateLog.setTimestamp(LocalDateTime.now().minusMinutes(5));
        auditLogRepository.save(updateLog);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void createAuditLog_ShouldSucceed_WhenAdmin() throws Exception {
        AuditLogRequestDto requestDto = new AuditLogRequestDto();
        requestDto.setActionType("CUSTOM_ACTION");
        requestDto.setEntityType("TestEntity");
        requestDto.setEntityId(999L);
        requestDto.setDescription("Custom audit log entry");
        requestDto.setSecurityEvent(false);
        requestDto.setSuccess(true);

        mockMvc.perform(post("/api/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionType", is("CUSTOM_ACTION")))
                .andExpect(jsonPath("$.entityType", is("TestEntity")))
                .andExpect(jsonPath("$.entityId", is(999)))
                .andExpect(jsonPath("$.description", is("Custom audit log entry")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.securityEvent", is(false)));
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR", "USER"})
    void createAuditLog_ShouldFail_WhenHR() throws Exception {
        AuditLogRequestDto requestDto = new AuditLogRequestDto();
        requestDto.setActionType("CUSTOM_ACTION");
        requestDto.setDescription("Custom audit log entry");

        mockMvc.perform(post("/api/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAllAuditLogs_ShouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/audit")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.content[0].actionType", notNullValue()))
                .andExpect(jsonPath("$.content[0].timestamp", notNullValue()));
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR", "USER"})
    void getAllAuditLogs_ShouldSucceed_WhenHR() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllAuditLogs_ShouldFail_WhenRegularUser() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsWithFilters_ShouldFilterByActionType() throws Exception {
        mockMvc.perform(get("/api/audit/filter")
                .param("actionType", AuditLogService.ACTION_LOGIN)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].actionType", is(AuditLogService.ACTION_LOGIN)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsWithFilters_ShouldFilterByUser() throws Exception {
        mockMvc.perform(get("/api/audit/filter")
                .param("userId", adminUser.getId().toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].userId", is(adminUser.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsWithFilters_ShouldFilterBySecurityEvent() throws Exception {
        mockMvc.perform(get("/api/audit/filter")
                .param("securityEvent", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].securityEvent", is(true)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsByUser_ShouldReturnUserSpecificLogs() throws Exception {
        mockMvc.perform(get("/api/audit/user/{userId}", regularUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].userId", is(regularUser.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getRecentUserActivity_ShouldReturnRecentActivity() throws Exception {
        mockMvc.perform(get("/api/audit/user/{userId}/recent", regularUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].userId", is(regularUser.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getSecurityEvents_ShouldReturnOnlySecurityEvents() throws Exception {
        mockMvc.perform(get("/api/audit/security-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].securityEvent", is(true)));
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR", "USER"})
    void getSecurityEvents_ShouldFail_WhenHR() throws Exception {
        mockMvc.perform(get("/api/audit/security-events"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getFailedOperations_ShouldReturnOnlyFailures() throws Exception {
        mockMvc.perform(get("/api/audit/failures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].success", is(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditStatistics_ShouldReturnStatistics() throws Exception {
        mockMvc.perform(get("/api/audit/statistics")
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents", notNullValue()))
                .andExpect(jsonPath("$.securityEvents", notNullValue()))
                .andExpect(jsonPath("$.failedOperations", notNullValue()))
                .andExpect(jsonPath("$.actionTypeStatistics", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsByActionType_ShouldFilterCorrectly() throws Exception {
        mockMvc.perform(get("/api/audit/action/{actionType}", AuditLogService.ACTION_CREATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].actionType", is(AuditLogService.ACTION_CREATE)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsByEntity_ShouldFilterCorrectly() throws Exception {
        mockMvc.perform(get("/api/audit/entity/{entityType}/{entityId}", "Employee", 123L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].entityType", is("Employee")))
                .andExpect(jsonPath("$.content[0].entityId", is(123)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getAuditLogsByDateRange_ShouldFilterCorrectly() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        mockMvc.perform(get("/api/audit/date-range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void cleanupOldAuditLogs_ShouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/audit/cleanup")
                .param("daysToKeep", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Audit logs cleanup completed")))
                .andExpect(jsonPath("$.daysToKeep", is("30")));
    }

    @Test
    @WithMockUser(username = "hr", roles = {"HR", "USER"})
    void cleanupOldAuditLogs_ShouldFail_WhenHR() throws Exception {
        mockMvc.perform(delete("/api/audit/cleanup"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllAuditLogs_ShouldFail_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void createAuditLog_ShouldValidateInput() throws Exception {
        AuditLogRequestDto requestDto = new AuditLogRequestDto();
        // Missing required actionType

        mockMvc.perform(post("/api/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}