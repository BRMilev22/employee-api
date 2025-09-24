package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.repository.*;
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
public class LeaveRequestControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    // Test data
    private Employee testEmployee;
    private Employee testManager;
    private LeaveType testLeaveType;
    private Department testDepartment;
    private LeaveRequest testLeaveRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up
        leaveRequestRepository.deleteAll();
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

        // Create test manager
        testManager = new Employee();
        testManager.setEmployeeId("MGR001");
        testManager.setFirstName("Test");
        testManager.setLastName("Manager");
        testManager.setEmail("test.manager@company.com");
        testManager.setJobTitle("Manager");
        testManager.setHireDate(LocalDate.now().minusYears(2));
        testManager.setGender("FEMALE");
        testManager.setEmploymentType(EmploymentType.FULL_TIME);
        testManager.setStatus(EmployeeStatus.ACTIVE);
        testManager.setDepartment(testDepartment);
        testManager = employeeRepository.save(testManager);

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
        testEmployee.setManager(testManager);
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

        // Create leave balance for test employee
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(testEmployee);
        leaveBalance.setLeaveType(testLeaveType);
        leaveBalance.setYear(LocalDate.now().getYear());
        leaveBalance.setAllocatedDays(25.0);
        leaveBalance.setUsedDays(0.0);
        leaveBalance.setPendingDays(5.0); // Set pending days to match the test leave request
        leaveBalanceRepository.save(leaveBalance);

        // Create test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType(testLeaveType);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(10));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(15));
        testLeaveRequest.setTotalDays(5.0);
        testLeaveRequest.setReason("Annual vacation");
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        testLeaveRequest.setAppliedDate(LocalDate.now());
        testLeaveRequest = leaveRequestRepository.save(testLeaveRequest);
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetAllLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/leave-requests")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testLeaveRequest.getId()))
                .andExpect(jsonPath("$.content[0].reason").value("Annual vacation"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetLeaveRequestById() throws Exception {
        mockMvc.perform(get("/api/leave-requests/" + testLeaveRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLeaveRequest.getId()))
                .andExpect(jsonPath("$.reason").value("Annual vacation"))
                .andExpect(jsonPath("$.totalDays").value(5.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetLeaveRequestsByEmployee() throws Exception {
        mockMvc.perform(get("/api/leave-requests/employee/" + testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testLeaveRequest.getId()));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetLeaveRequestsByEmployeeAndStatus() throws Exception {
        mockMvc.perform(get("/api/leave-requests/employee/" + testEmployee.getId() + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetPendingLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/leave-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testCreateLeaveRequest() throws Exception {
        String requestBody = String.format("""
            {
                "employee": {"id": %d},
                "leaveType": {"id": %d},
                "startDate": "%s",
                "endDate": "%s",
                "totalDays": 5.0,
                "reason": "Personal leave",
                "appliedDate": "%s"
            }
            """, testEmployee.getId(), testLeaveType.getId(), 
            LocalDate.now().plusDays(30).toString(),
            LocalDate.now().plusDays(35).toString(),
            LocalDate.now().toString());

        mockMvc.perform(post("/api/leave-requests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("Personal leave"))
                .andExpect(jsonPath("$.totalDays").value(6.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testUpdateLeaveRequest() throws Exception {
        // Simple update test - just change the reason, keep same dates
        String requestBody = String.format("""
            {
                "employee": {"id": %d},
                "leaveType": {"id": %d},
                "startDate": "%s",
                "endDate": "%s",
                "totalDays": %.1f,
                "reason": "Updated reason"
            }
            """, testEmployee.getId(), testLeaveType.getId(),
            testLeaveRequest.getStartDate().toString(),
            testLeaveRequest.getEndDate().toString(),
            testLeaveRequest.getTotalDays());

        mockMvc.perform(put("/api/leave-requests/" + testLeaveRequest.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Updated reason"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testApproveLeaveRequest() throws Exception {
        String approvalBody = String.format("""
            {
                "approvedById": %d,
                "comments": "Approved"
            }
            """, testManager.getId());

        mockMvc.perform(post("/api/leave-requests/" + testLeaveRequest.getId() + "/approve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(approvalBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testRejectLeaveRequest() throws Exception {
        String rejectionBody = String.format("""
            {
                "rejectedById": %d,
                "reason": "Not enough leave balance"
            }
            """, testManager.getId());

        mockMvc.perform(post("/api/leave-requests/" + testLeaveRequest.getId() + "/reject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(rejectionBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testCancelLeaveRequest() throws Exception {
        String cancellationBody = String.format("""
            {
                "cancelledById": %d,
                "reason": "Personal reasons"
            }
            """, testEmployee.getId());

        mockMvc.perform(post("/api/leave-requests/" + testLeaveRequest.getId() + "/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cancellationBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetLeaveCalendar() throws Exception {
        mockMvc.perform(get("/api/leave-requests/calendar")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(30).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetOverlappingLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/leave-requests/overlapping")
                .param("employeeId", testEmployee.getId().toString())
                .param("startDate", LocalDate.now().plusDays(10).toString())
                .param("endDate", LocalDate.now().plusDays(15).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testDeleteLeaveRequest() throws Exception {
        mockMvc.perform(delete("/api/leave-requests/" + testLeaveRequest.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetLeaveRequestByIdUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/leave-requests/" + testLeaveRequest.getId()))
                .andExpect(status().isUnauthorized());
    }
}