package com.example.employee_api.controller;

import com.example.employee_api.model.Department;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.Position;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.model.enums.EmploymentType;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.repository.DepartmentRepository;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.EmployeeStatusHistoryRepository;
import com.example.employee_api.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmployeeLifecycleController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EmployeeLifecycleControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Employee testEmployee;
    private Employee activeEmployee;
    private Employee inactiveEmployee;
    private Employee terminatedEmployee;
    private Department testDepartment;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean up
        statusHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();

        // Create test department and position
        testDepartment = new Department();
        testDepartment.setName("Test Department");
        testDepartment.setDescription("Test Department Description");
        testDepartment.setDepartmentCode("TEST001");
        testDepartment.setStatus(DepartmentStatus.ACTIVE);
        testDepartment = departmentRepository.save(testDepartment);

        testPosition = new Position();
        testPosition.setTitle("Test Position");
        testPosition.setDepartment(testDepartment);
        testPosition.setPayGrade("A1");
        testPosition.setMinSalary(BigDecimal.valueOf(50000));
        testPosition.setMaxSalary(BigDecimal.valueOf(100000));
        testPosition.setStatus(PositionStatus.ACTIVE);
        testPosition.setLevel(PositionLevel.MID);
        testPosition = positionRepository.save(testPosition);

        // Create test employees
        testEmployee = createEmployee("TEST001", "John", "Doe", EmployeeStatus.ACTIVE);
        activeEmployee = createEmployee("TEST002", "Jane", "Smith", EmployeeStatus.ACTIVE);
        inactiveEmployee = createEmployee("TEST003", "Bob", "Johnson", EmployeeStatus.INACTIVE);
        terminatedEmployee = createEmployee("TEST004", "Alice", "Williams", EmployeeStatus.TERMINATED);
    }

    private Employee createEmployee(String employeeId, String firstName, String lastName, EmployeeStatus status) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.com");
        employee.setJobTitle("Test Position");
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setStatus(status);
        employee.setHireDate(LocalDate.now().minusYears(1));
        employee.setSalary(BigDecimal.valueOf(60000));
        employee.setDepartment(testDepartment);
        employee.setCurrentPosition(testPosition);
        employee.setGender("MALE");
        return employeeRepository.save(employee);
    }

    // Activate Employee Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void activateEmployee_ShouldReturn200_WhenEmployeeIsInactive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", inactiveEmployee.getId())
                .param("reason", "Employee returning from leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inactiveEmployee.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void activateEmployee_ShouldReturn400_WhenEmployeeAlreadyActive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", activeEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void activateEmployee_ShouldReturn400_WhenEmployeeIsTerminated() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", terminatedEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void activateEmployee_ShouldReturn404_WhenEmployeeNotFound() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", 99999L)
                .param("reason", "Test reason"))
                .andExpect(status().isNotFound());
    }

    // Deactivate Employee Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deactivateEmployee_ShouldReturn200_WhenEmployeeIsActive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/deactivate", activeEmployee.getId())
                .param("reason", "Employee going on extended leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeEmployee.getId()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deactivateEmployee_ShouldReturn400_WhenEmployeeAlreadyInactive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/deactivate", inactiveEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deactivateEmployee_ShouldReturn400_WhenEmployeeIsTerminated() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/deactivate", terminatedEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    // Terminate Employee Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void terminateEmployee_ShouldReturn200_WhenEmployeeIsActive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/terminate", activeEmployee.getId())
                .param("reason", "End of contract")
                .param("terminationDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeEmployee.getId()))
                .andExpect(jsonPath("$.status").value("TERMINATED"))
                .andExpect(jsonPath("$.terminationDate").value("2024-12-31"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void terminateEmployee_ShouldReturn200_WhenEmployeeIsInactive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/terminate", inactiveEmployee.getId())
                .param("reason", "Resignation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inactiveEmployee.getId()))
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void terminateEmployee_ShouldReturn400_WhenEmployeeAlreadyTerminated() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/terminate", terminatedEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    // Onboard Employee Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void onboardEmployee_ShouldReturn200_WhenEmployeeIsActive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/onboard", activeEmployee.getId())
                .param("reason", "Starting probation period"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeEmployee.getId()))
                .andExpect(jsonPath("$.status").value("PROBATION"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void onboardEmployee_ShouldReturn200_WhenEmployeeIsInactive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/onboard", inactiveEmployee.getId())
                .param("reason", "New hire onboarding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inactiveEmployee.getId()))
                .andExpect(jsonPath("$.status").value("PROBATION"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void onboardEmployee_ShouldReturn400_WhenEmployeeIsTerminated() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/onboard", terminatedEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    // Offboard Employee Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void offboardEmployee_ShouldReturn200_WhenEmployeeIsActive() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/offboard", activeEmployee.getId())
                .param("reason", "End of contract")
                .param("lastWorkingDay", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeEmployee.getId()))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.terminationDate").value("2024-12-31"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void offboardEmployee_ShouldReturn400_WhenEmployeeIsTerminated() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/offboard", terminatedEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isBadRequest());
    }

    // Status History Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getEmployeeStatusHistory_ShouldReturn200_WithHistory() throws Exception {
        // First change the status to create history
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/deactivate", activeEmployee.getId())
                .param("reason", "Test deactivation"));

        mockMvc.perform(get("/api/employees/lifecycle/{employeeId}/status-history", activeEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].newStatus").value("INACTIVE"))
                .andExpect(jsonPath("$[0].reason").value("Test deactivation"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getEmployeeStatusHistory_ShouldReturn404_WhenEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/lifecycle/{employeeId}/status-history", 99999L))
                .andExpect(status().isNotFound());
    }

    // Statistics Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getStatusChangeStatistics_ShouldReturn200_WithStatistics() throws Exception {
        mockMvc.perform(get("/api/employees/lifecycle/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalChanges").exists())
                .andExpect(jsonPath("$.activations").exists())
                .andExpect(jsonPath("$.deactivations").exists())
                .andExpect(jsonPath("$.terminations").exists())
                .andExpect(jsonPath("$.onboardings").exists());
    }

    // Date Range Tests
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getActivatedEmployees_ShouldReturn200_WithDateRange() throws Exception {
        String startDate = "2024-01-01T00:00:00";
        String endDate = "2024-12-31T23:59:59";

        mockMvc.perform(get("/api/employees/lifecycle/activated")
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getTerminatedEmployees_ShouldReturn200_WithDateRange() throws Exception {
        String startDate = "2024-01-01T00:00:00";
        String endDate = "2024-12-31T23:59:59";

        mockMvc.perform(get("/api/employees/lifecycle/terminated")
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Authorization Tests
    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void activateEmployee_ShouldReturn403_WhenInsufficientPermissions() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", testEmployee.getId())
                .param("reason", "Test reason"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_HR"})
    void activateEmployee_ShouldReturn200_WithHRRole() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", inactiveEmployee.getId())
                .param("reason", "HR approval"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SUPER_ADMIN"})
    void terminateEmployee_ShouldReturn200_WithSuperAdminRole() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/terminate", activeEmployee.getId())
                .param("reason", "Administrative decision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    void getStatusHistory_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/employees/lifecycle/{employeeId}/status-history", testEmployee.getId()))
                .andExpect(status().isUnauthorized());
    }

    // Edge Cases
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void activateEmployee_ShouldWork_WithoutReason() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", inactiveEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void terminateEmployee_ShouldWork_WithoutTerminationDate() throws Exception {
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/terminate", activeEmployee.getId())
                .param("reason", "Immediate termination"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINATED"))
                .andExpect(jsonPath("$.terminationDate").exists());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getStatusHistory_ShouldWork_WithPagination() throws Exception {
        // Create some history first
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/deactivate", activeEmployee.getId())
                .param("reason", "Test 1"));
        mockMvc.perform(post("/api/employees/lifecycle/{employeeId}/activate", activeEmployee.getId())
                .param("reason", "Test 2"));

        mockMvc.perform(get("/api/employees/lifecycle/{employeeId}/status-history", activeEmployee.getId())
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}