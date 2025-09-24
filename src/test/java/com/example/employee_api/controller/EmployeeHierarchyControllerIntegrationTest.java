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
import com.example.employee_api.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
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
 * Integration tests for EmployeeHierarchyController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EmployeeHierarchyControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Employee ceo;
    private Employee manager1;
    private Employee manager2;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private Department testDepartment;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

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

        // Create a hierarchy: CEO -> Manager1 -> Employee1, Employee2
        //                         -> Manager2 -> Employee3
        
        // CEO (no manager)
        ceo = createEmployee("CEO001", "John", "CEO", "john.ceo@example.com", null);

        // Manager1 (reports to CEO)
        manager1 = createEmployee("MGR001", "Jane", "Manager1", "jane.mgr1@example.com", ceo);

        // Manager2 (reports to CEO)
        manager2 = createEmployee("MGR002", "Bob", "Manager2", "bob.mgr2@example.com", ceo);

        // Employee1 (reports to Manager1)
        employee1 = createEmployee("EMP001", "Alice", "Employee1", "alice.emp1@example.com", manager1);

        // Employee2 (reports to Manager1)
        employee2 = createEmployee("EMP002", "Charlie", "Employee2", "charlie.emp2@example.com", manager1);

        // Employee3 (reports to Manager2)
        employee3 = createEmployee("EMP003", "David", "Employee3", "david.emp3@example.com", manager2);
    }

    private Employee createEmployee(String employeeId, String firstName, String lastName, String email, Employee manager) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setJobTitle("Test Job");
        employee.setGender("MALE");
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setHireDate(LocalDate.now());
        employee.setSalary(BigDecimal.valueOf(60000));
        employee.setDepartment(testDepartment);
        employee.setCurrentPosition(testPosition);
        employee.setManager(manager);
        return employeeRepository.save(employee);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void assignManager_ShouldAssignManager_WhenValidRequest() throws Exception {
        // Create a new employee without a manager
        Employee newEmployee = createEmployee("NEW001", "New", "Employee", "new.employee@example.com", null);

        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            newEmployee.getId(), manager1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(newEmployee.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("New")))
                .andExpect(jsonPath("$.lastName", is("Employee")));

        // Verify the relationship was created
        Employee updatedEmployee = employeeRepository.findById(newEmployee.getId()).orElse(null);
        assert updatedEmployee != null;
        assert updatedEmployee.getManager() != null;
        assert updatedEmployee.getManager().getId().equals(manager1.getId());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void assignManager_ShouldReturn404_WhenEmployeeNotFound() throws Exception {
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            999L, manager1.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void assignManager_ShouldReturn404_WhenManagerNotFound() throws Exception {
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            employee1.getId(), 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void assignManager_ShouldReturn400_WhenSelfAssignment() throws Exception {
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            employee1.getId(), employee1.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void assignManager_ShouldReturn400_WhenCircularReference() throws Exception {
        // Try to make manager1 report to employee1 (who reports to manager1)
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            manager1.getId(), employee1.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void removeManager_ShouldRemoveManager_WhenValidRequest() throws Exception {
        mockMvc.perform(delete("/api/employees/hierarchy/{employeeId}/manager", employee1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employee1.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("Alice")));

        // Verify the manager was removed
        Employee updatedEmployee = employeeRepository.findById(employee1.getId()).orElse(null);
        assert updatedEmployee != null;
        assert updatedEmployee.getManager() == null;
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void removeManager_ShouldReturn404_WhenEmployeeNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/hierarchy/{employeeId}/manager", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getSubordinates_ShouldReturnSubordinates_WhenManagerHasSubordinates() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/{managerId}/subordinates", manager1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].firstName", containsInAnyOrder("Alice", "Charlie")))
                .andExpect(jsonPath("$[*].lastName", containsInAnyOrder("Employee1", "Employee2")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getSubordinates_ShouldReturnEmptyList_WhenManagerHasNoSubordinates() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/{managerId}/subordinates", employee1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getSubordinates_ShouldReturn404_WhenManagerNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/{managerId}/subordinates", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getReportingChain_ShouldReturnReportingChain_WhenEmployeeHasManagers() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/{employeeId}/reporting-chain", employee1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Jane"))) // Direct manager
                .andExpect(jsonPath("$[1].firstName", is("John"))); // CEO
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getReportingChain_ShouldReturnEmptyList_WhenEmployeeHasNoManager() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/{employeeId}/reporting-chain", ceo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getReportingChain_ShouldReturn404_WhenEmployeeNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/{employeeId}/reporting-chain", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getOrganizationalChart_ShouldReturnOrgChart() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/org-chart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topLevel", hasSize(1)))
                .andExpect(jsonPath("$.topLevel[0].employee.firstName", is("John")))
                .andExpect(jsonPath("$.topLevel[0].subordinates", hasSize(2)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllManagers_ShouldReturnAllManagers() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // CEO, Manager1, Manager2
                .andExpect(jsonPath("$[*].firstName", containsInAnyOrder("John", "Jane", "Bob")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getHierarchyStatistics_ShouldReturnStatistics() throws Exception {
        mockMvc.perform(get("/api/employees/hierarchy/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees", is(6)))
                .andExpect(jsonPath("$.totalManagers", is(3)))
                .andExpect(jsonPath("$.topLevelEmployees", is(1)))
                .andExpect(jsonPath("$.employeesWithManagers", is(5)))
                .andExpect(jsonPath("$.averageSpanOfControl").exists());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void assignManager_ShouldReturn403_WhenInsufficientPermissions() throws Exception {
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            employee1.getId(), manager2.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignManager_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            employee1.getId(), manager2.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_HR")
    void assignManager_ShouldWork_WithHRRole() throws Exception {
        // Create a new employee without a manager
        Employee newEmployee = createEmployee("HR001", "HR", "Test", "hr.test@example.com", null);

        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            newEmployee.getId(), manager1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("HR")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_SUPER_ADMIN")
    void getAllEndpoints_ShouldWork_WithSuperAdminRole() throws Exception {
        // Test all endpoints work with SUPER_ADMIN role
        
        // Assign manager
        Employee newEmployee = createEmployee("SA001", "Super", "Admin", "sa.test@example.com", null);
        mockMvc.perform(post("/api/employees/hierarchy/{employeeId}/manager/{managerId}", 
                            newEmployee.getId(), manager1.getId()))
                .andExpect(status().isOk());
        
        // Remove manager
        mockMvc.perform(delete("/api/employees/hierarchy/{employeeId}/manager", newEmployee.getId()))
                .andExpect(status().isOk());
        
        // Get subordinates
        mockMvc.perform(get("/api/employees/hierarchy/{managerId}/subordinates", manager1.getId()))
                .andExpect(status().isOk());
        
        // Get reporting chain
        mockMvc.perform(get("/api/employees/hierarchy/{employeeId}/reporting-chain", employee1.getId()))
                .andExpect(status().isOk());
        
        // Get org chart
        mockMvc.perform(get("/api/employees/hierarchy/org-chart"))
                .andExpect(status().isOk());
        
        // Get managers
        mockMvc.perform(get("/api/employees/hierarchy/managers"))
                .andExpect(status().isOk());
        
        // Get statistics
        mockMvc.perform(get("/api/employees/hierarchy/statistics"))
                .andExpect(status().isOk());
    }
}