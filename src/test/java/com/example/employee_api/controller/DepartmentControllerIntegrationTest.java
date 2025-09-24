package com.example.employee_api.controller;

import com.example.employee_api.model.Department;
import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
        
        testDepartment = new Department();
        testDepartment.setDepartmentCode("IT");
        testDepartment.setName("Information Technology");
        testDepartment.setDescription("IT Department");
        testDepartment.setLocation("Building A");
        testDepartment.setStatus(DepartmentStatus.ACTIVE);
        testDepartment.setBudget(BigDecimal.valueOf(500000));
        testDepartment.setEmail("it@company.com");
        testDepartment.setPhone("+1234567890");
        testDepartment = departmentRepository.save(testDepartment);
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllDepartments() throws Exception {
        mockMvc.perform(get("/api/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].departmentCode").value("IT"))
                .andExpect(jsonPath("$[0].name").value("Information Technology"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetDepartmentById() throws Exception {
        mockMvc.perform(get("/api/departments/{id}", testDepartment.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentCode").value("IT"))
                .andExpect(jsonPath("$.name").value("Information Technology"))
                .andExpect(jsonPath("$.description").value("IT Department"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetDepartmentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/departments/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateDepartment() throws Exception {
        Department newDepartment = new Department();
        newDepartment.setDepartmentCode("HR");
        newDepartment.setName("Human Resources");
        newDepartment.setDescription("HR Department");
        newDepartment.setLocation("Building B");
        newDepartment.setStatus(DepartmentStatus.ACTIVE);
        newDepartment.setBudget(BigDecimal.valueOf(200000));
        newDepartment.setEmail("hr@company.com");
        newDepartment.setPhone("+1234567891");

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDepartment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentCode").value("HR"))
                .andExpect(jsonPath("$.name").value("Human Resources"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateDepartment_DuplicateCode() throws Exception {
        Department duplicateDepartment = new Department();
        duplicateDepartment.setDepartmentCode("IT"); // Same as existing
        duplicateDepartment.setName("Information Technology 2");
        duplicateDepartment.setDescription("Another IT Department");
        duplicateDepartment.setLocation("Building C");
        duplicateDepartment.setStatus(DepartmentStatus.ACTIVE);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateDepartment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateDepartment() throws Exception {
        testDepartment.setName("Updated IT Department");
        testDepartment.setDescription("Updated Description");

        mockMvc.perform(put("/api/departments/{id}", testDepartment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDepartment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated IT Department"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateDepartment_NotFound() throws Exception {
        Department nonExistentDepartment = new Department();
        nonExistentDepartment.setId(999L);
        nonExistentDepartment.setDepartmentCode("FAKE"); // Max 10 chars
        nonExistentDepartment.setName("Non-existent Department");
        nonExistentDepartment.setStatus(DepartmentStatus.ACTIVE);

        mockMvc.perform(put("/api/departments/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentDepartment)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteDepartment() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", testDepartment.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteDepartment_NotFound() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testSearchDepartments() throws Exception {
        mockMvc.perform(get("/api/departments/search")
                .param("query", "Information")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Information Technology"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetDepartmentsByStatus() throws Exception {
        mockMvc.perform(get("/api/departments/status/{status}", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}