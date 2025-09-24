package com.example.employee_api.controller;

import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.model.enums.EmploymentType;
import com.example.employee_api.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        employeeRepository.deleteAll();
        
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setGender("MALE");
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        testEmployee = employeeRepository.save(testEmployee);
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[0].jobTitle", is("Software Engineer")));
    }

    @Test
    void getEmployeeById_WhenEmployeeExists_ShouldReturnEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.jobTitle", is("Software Engineer")));
    }

    @Test
    void getEmployeeById_WhenEmployeeNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void createEmployee_WithValidData_ShouldCreateEmployee() throws Exception {
        Employee newEmployee = new Employee();
        newEmployee.setEmployeeId("EMP002");
        newEmployee.setFirstName("Jane");
        newEmployee.setLastName("Smith");
        newEmployee.setEmail("jane.smith@example.com");
        newEmployee.setJobTitle("Product Manager");
        newEmployee.setGender("FEMALE");
        newEmployee.setStatus(EmployeeStatus.ACTIVE);
        newEmployee.setEmploymentType(EmploymentType.FULL_TIME);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")))
                .andExpect(jsonPath("$.jobTitle", is("Product Manager")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void createEmployee_WithInvalidData_ShouldReturn400() throws Exception {
        Employee invalidEmployee = new Employee();
        invalidEmployee.setEmployeeId("EMP003");
        invalidEmployee.setFirstName(""); // Invalid empty first name
        invalidEmployee.setLastName("Smith");
        invalidEmployee.setEmail("invalid-email"); // Invalid email
        invalidEmployee.setJobTitle("Developer");
        invalidEmployee.setGender("MALE");
        invalidEmployee.setStatus(EmployeeStatus.ACTIVE);
        invalidEmployee.setEmploymentType(EmploymentType.FULL_TIME);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployee_WithValidData_ShouldUpdateEmployee() throws Exception {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmployeeId("EMP004");
        updatedEmployee.setFirstName("John");
        updatedEmployee.setLastName("Updated");
        updatedEmployee.setEmail("john.updated@example.com");
        updatedEmployee.setJobTitle("Senior Software Engineer");
        updatedEmployee.setGender("MALE");
        updatedEmployee.setStatus(EmployeeStatus.ACTIVE);
        updatedEmployee.setEmploymentType(EmploymentType.FULL_TIME);

        mockMvc.perform(put("/api/employees/{id}", testEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")))
                .andExpect(jsonPath("$.jobTitle", is("Senior Software Engineer")));
    }

    @Test
    void updateEmployee_WhenEmployeeNotExists_ShouldReturn404() throws Exception {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmployeeId("EMP005");
        updatedEmployee.setFirstName("John");
        updatedEmployee.setLastName("Updated");
        updatedEmployee.setEmail("john.updated@example.com");
        updatedEmployee.setJobTitle("Senior Software Engineer");
        updatedEmployee.setGender("MALE");
        updatedEmployee.setStatus(EmployeeStatus.ACTIVE);
        updatedEmployee.setEmploymentType(EmploymentType.FULL_TIME);

        mockMvc.perform(put("/api/employees/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_WhenEmployeeExists_ShouldDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", testEmployee.getId()))
                .andExpect(status().isNoContent());

        // Verify employee was deleted
        mockMvc.perform(get("/api/employees/{id}", testEmployee.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_WhenEmployeeNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeesByRole_ShouldReturnFilteredEmployees() throws Exception {
        // Create another employee with different role
        Employee employee2 = new Employee();
        employee2.setEmployeeId("EMP006");
        employee2.setFirstName("Alice");
        employee2.setLastName("Johnson");
        employee2.setEmail("alice.johnson@example.com");
        employee2.setJobTitle("Designer");
        employee2.setGender("FEMALE");
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setEmploymentType(EmploymentType.FULL_TIME);
        employeeRepository.save(employee2);

        mockMvc.perform(get("/api/employees/job-title/{jobTitle}", "Software Engineer"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")))
                .andExpect(jsonPath("$[0].jobTitle", is("Software Engineer")));
    }

    @Test
    void exportEmployees_ShouldReturnCSVContent() throws Exception {
        mockMvc.perform(get("/api/employees/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"employees.csv\""))
                .andExpect(content().string(containsString("Employee ID,First Name,Last Name,Email,Job Title")))
                .andExpect(content().string(containsString("EMP001,John,Doe,john.doe@example.com,Software Engineer")));
    }

    @Test
    void exportEmployees_WithFilters_ShouldReturnFilteredCSV() throws Exception {
        // Create employee with manager
        Employee manager = new Employee();
        manager.setEmployeeId("MGR001");
        manager.setFirstName("Manager");
        manager.setLastName("Smith");
        manager.setEmail("manager.smith@example.com");
        manager.setJobTitle("Engineering Manager");
        manager.setGender("MALE");
        manager.setStatus(EmployeeStatus.ACTIVE);
        manager.setEmploymentType(EmploymentType.FULL_TIME);
        employeeRepository.save(manager);

        // Update existing employee to have manager
        testEmployee.setManager(manager);
        employeeRepository.save(testEmployee);

        mockMvc.perform(get("/api/employees/export")
                .param("status", "ACTIVE")
                .param("employmentType", "FULL_TIME"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(containsString("John,Doe")))
                .andExpect(content().string(containsString("Manager,Smith")));
    }

    @Test
    void getEmployeesByLocation_WithCity_ShouldReturnFilteredEmployees() throws Exception {
        // Set up test employee with location data
        testEmployee.setCity("New York");
        testEmployee.setState("NY");
        testEmployee.setPostalCode("10001");
        testEmployee = employeeRepository.save(testEmployee);

        // Create another employee in different city
        Employee employee2 = new Employee();
        employee2.setEmployeeId("EMP007");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@example.com");
        employee2.setJobTitle("Designer");
        employee2.setGender("FEMALE");
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setEmploymentType(EmploymentType.FULL_TIME);
        employee2.setCity("Los Angeles");
        employee2.setState("CA");
        employee2.setPostalCode("90001");
        employeeRepository.save(employee2);

        mockMvc.perform(get("/api/employees/by-location")
                .param("city", "New York"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].city", is("New York")));
    }

    @Test
    void getEmployeesByLocation_WithState_ShouldReturnFilteredEmployees() throws Exception {
        // Set up test employee with location data  
        testEmployee.setCity("New York");
        testEmployee.setState("NY");
        testEmployee.setPostalCode("10001");
        testEmployee = employeeRepository.save(testEmployee);

        mockMvc.perform(get("/api/employees/by-location")
                .param("state", "NY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].state", is("NY")));
    }

    @Test
    void getEmployeesByLocation_WithPostalCode_ShouldReturnFilteredEmployees() throws Exception {
        // Set up test employee with location data
        testEmployee.setCity("New York");
        testEmployee.setState("NY");
        testEmployee.setPostalCode("10001");
        testEmployee = employeeRepository.save(testEmployee);

        mockMvc.perform(get("/api/employees/by-location")
                .param("postalCode", "10001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].postalCode", is("10001")));
    }

    @Test
    void getEmployeesByLocation_WithMultipleParameters_ShouldReturnFilteredEmployees() throws Exception {
        // Set up test employee with location data
        testEmployee.setCity("New York");
        testEmployee.setState("NY");
        testEmployee.setPostalCode("10001");
        testEmployee = employeeRepository.save(testEmployee);

        mockMvc.perform(get("/api/employees/by-location")
                .param("city", "New York")
                .param("state", "NY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].city", is("New York")))
                .andExpect(jsonPath("$[0].state", is("NY")));
    }

    @Test
    void getEmployeesByLocation_WithNoParameters_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/employees/by-location"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeesWithoutManager_ShouldReturnUnmanagedEmployees() throws Exception {
        // Create employee with manager
        Employee manager = new Employee();
        manager.setEmployeeId("MGR002");
        manager.setFirstName("Boss");
        manager.setLastName("Leader");
        manager.setEmail("boss.leader@example.com");
        manager.setJobTitle("Director");
        manager.setGender("MALE");
        manager.setStatus(EmployeeStatus.ACTIVE);
        manager.setEmploymentType(EmploymentType.FULL_TIME);
        employeeRepository.save(manager);

        Employee managedEmployee = new Employee();
        managedEmployee.setEmployeeId("EMP008");
        managedEmployee.setFirstName("Managed");
        managedEmployee.setLastName("Employee");
        managedEmployee.setEmail("managed.employee@example.com");
        managedEmployee.setJobTitle("Junior Developer");
        managedEmployee.setGender("FEMALE");
        managedEmployee.setStatus(EmployeeStatus.ACTIVE);
        managedEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        managedEmployee.setManager(manager);
        employeeRepository.save(managedEmployee);

        mockMvc.perform(get("/api/employees/without-manager"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].manager", everyItem(nullValue())));
    }

    @Test
    void getEmployeesWithoutManager_WithActiveStatusFilter_ShouldReturnActiveUnmanagedEmployees() throws Exception {
        // Create inactive employee without manager
        Employee inactiveEmployee = new Employee();
        inactiveEmployee.setEmployeeId("EMP009");
        inactiveEmployee.setFirstName("Inactive");
        inactiveEmployee.setLastName("Employee");
        inactiveEmployee.setEmail("inactive.employee@example.com");
        inactiveEmployee.setJobTitle("Former Developer");
        inactiveEmployee.setGender("MALE");
        inactiveEmployee.setStatus(EmployeeStatus.INACTIVE);
        inactiveEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        employeeRepository.save(inactiveEmployee);

        mockMvc.perform(get("/api/employees/without-manager")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].status", everyItem(is("ACTIVE"))))
                .andExpect(jsonPath("$[*].manager", everyItem(nullValue())));
    }
}