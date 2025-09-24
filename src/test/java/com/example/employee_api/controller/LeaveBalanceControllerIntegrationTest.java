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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LeaveBalanceControllerIntegrationTest {

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
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    private Employee testEmployee;
    private LeaveType testLeaveType;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up
        leaveBalanceRepository.deleteAll();
        employeeRepository.deleteAll();
        leaveTypeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create test department
        testDepartment = new Department();
        testDepartment.setDepartmentCode("TEST");
        testDepartment.setName("Test Department");
        testDepartment.setStatus(DepartmentStatus.ACTIVE);
        testDepartment = departmentRepository.save(testDepartment);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP999");
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("Employee");
        testEmployee.setEmail("test.employee@company.com");
        testEmployee.setJobTitle("Test Position");
        testEmployee.setHireDate(LocalDate.now().minusYears(1));
        testEmployee.setGender("MALE");
        testEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDepartment(testDepartment);
        testEmployee = employeeRepository.save(testEmployee);

        // Create test leave type
        testLeaveType = new LeaveType();
        testLeaveType.setName("Annual Leave");
        testLeaveType.setDescription("Paid annual vacation leave");
        testLeaveType.setDaysAllowed(25);
        testLeaveType.setRequiresApproval(true);
        testLeaveType.setCarryForward(true);
        testLeaveType.setActive(true);
        testLeaveType = leaveTypeRepository.save(testLeaveType);
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetAllLeaveBalances() throws Exception {
        // Create leave balance for test
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        balance.setPendingDays(2.00);
        balance.setCarryForwardDays(0.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetLeaveBalanceById() throws Exception {
        // Create leave balance for test
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        LeaveBalance savedBalance = leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/{id}", savedBalance.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedDays").value(25.00))
                .andExpect(jsonPath("$.usedDays").value(5.00));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetEmployeeLeaveBalances() throws Exception {
        // Create multiple leave balances
        LeaveBalance annualBalance = new LeaveBalance();
        annualBalance.setEmployee(testEmployee);
        annualBalance.setLeaveType(testLeaveType);
        annualBalance.setYear(2025);
        annualBalance.setAllocatedDays(25.00);
        annualBalance.setUsedDays(5.00);
        leaveBalanceRepository.save(annualBalance);

        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}", testEmployee.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetLeaveBalanceByEmployeeYearLeaveType() throws Exception {
        // Create leave balance for test
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        balance.setPendingDays(2.00);
        balance.setCarryForwardDays(0.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}/year/{year}", 
                        testEmployee.getId(), 2025)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].allocatedDays").value(25.00))
                .andExpect(jsonPath("$[0].usedDays").value(5.00));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetCurrentYearBalance() throws Exception {
        // Create current year balance
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(LocalDate.now().getYear());
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(3.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}/current-year", testEmployee.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetSpecificLeaveBalance() throws Exception {
        // Create balance
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(8.00);
        balance.setPendingDays(2.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}/leave-type/{leaveTypeId}/year/{year}", 
                        testEmployee.getId(), testLeaveType.getId(), 2025)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedDays").value(25.00))
                .andExpect(jsonPath("$.usedDays").value(8.00));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testCreateLeaveBalance() throws Exception {
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(0.00);

        mockMvc.perform(post("/api/leave-balances")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balance)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allocatedDays").value(25.00));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testUpdateBalance() throws Exception {
        // Create initial balance
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        balance = leaveBalanceRepository.save(balance);

        // Update the balance
        balance.setUsedDays(8.00);
        
        mockMvc.perform(put("/api/leave-balances/{id}", balance.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balance)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedDays").value(8.00));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testInitializeLeaveBalance() throws Exception {
        mockMvc.perform(post("/api/leave-balances/employee/{employeeId}/year/{year}/initialize", 
                        testEmployee.getId(), 2025)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testCheckSufficientBalance() throws Exception {
        // Create balance
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        balance.setPendingDays(0.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}/leave-type/{leaveTypeId}/year/{year}/check-balance", 
                        testEmployee.getId(), testLeaveType.getId(), 2025)
                .param("daysRequested", "15")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSufficientBalance").value(true));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetEmployeeLeaveStatistics() throws Exception {
        // Create balance for employee statistics
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(12.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}/year/{year}/statistics", 
                        testEmployee.getId(), 2025)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAllocated").exists())
                .andExpect(jsonPath("$.totalUsed").exists());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetExpiringLeaveBalances() throws Exception {
        // Create balance that might expire
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        leaveBalanceRepository.save(balance);

        mockMvc.perform(get("/api/leave-balances/expiring")
                .param("year", "2025")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testDeleteLeaveBalance() throws Exception {
        // Create balance to delete
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(testEmployee);
        balance.setLeaveType(testLeaveType);
        balance.setYear(2025);
        balance.setAllocatedDays(25.00);
        balance.setUsedDays(5.00);
        LeaveBalance savedBalance = leaveBalanceRepository.save(balance);

        mockMvc.perform(delete("/api/leave-balances/{id}", savedBalance.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUnauthenticatedUserCannotAccessBalances() throws Exception {
        mockMvc.perform(get("/api/leave-balances"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/api/leave-balances/employee/{employeeId}/current-year", 99999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testLeaveBalanceNotFound() throws Exception {
        mockMvc.perform(get("/api/leave-balances/{id}", 99999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}