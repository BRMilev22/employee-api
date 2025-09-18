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
}