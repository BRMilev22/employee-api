package com.example.employee_api.controller;

import com.example.employee_api.model.Department;
import com.example.employee_api.model.Position;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.repository.DepartmentRepository;
import com.example.employee_api.repository.PositionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PositionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create a test department
        testDepartment = new Department();
        testDepartment.setName("IT");
        testDepartment.setDepartmentCode("IT");
        testDepartment.setDescription("Information Technology Department");
        testDepartment = departmentRepository.save(testDepartment);
    }

    @Test
    void createPosition_ShouldReturnCreatedPosition() throws Exception {
        Position position = new Position();
        position.setTitle("Software Engineer");
        position.setDescription("Develops software applications");
        position.setLevel(PositionLevel.MID);
        position.setStatus(PositionStatus.ACTIVE);
        position.setDepartment(testDepartment);
        position.setMinSalary(new BigDecimal("50000"));
        position.setMaxSalary(new BigDecimal("80000"));
        position.setPayGrade("P3");
        position.setNumberOfOpenings(2);
        position.setTotalHeadcount(5);

        mockMvc.perform(post("/api/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Software Engineer")))
                .andExpect(jsonPath("$.level", is("MID")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.minSalary", is(50000)))
                .andExpect(jsonPath("$.maxSalary", is(80000)))
                .andExpect(jsonPath("$.payGrade", is("P3")))
                .andExpect(jsonPath("$.numberOfOpenings", is(2)))
                .andExpect(jsonPath("$.totalHeadcount", is(5)));
    }

    @Test
    void getAllPositions_ShouldReturnListOfPositions() throws Exception {
        // Create test positions
        Position position1 = new Position("Senior Developer", PositionLevel.SENIOR, testDepartment);
        position1.setMinSalary(new BigDecimal("80000"));
        position1.setMaxSalary(new BigDecimal("120000"));
        positionRepository.save(position1);

        Position position2 = new Position("Junior Developer", PositionLevel.JUNIOR, testDepartment);
        position2.setMinSalary(new BigDecimal("40000"));
        position2.setMaxSalary(new BigDecimal("60000"));
        positionRepository.save(position2);

        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", anyOf(is("Senior Developer"), is("Junior Developer"))))
                .andExpect(jsonPath("$[1].title", anyOf(is("Senior Developer"), is("Junior Developer"))));
    }

    @Test
    void getPositionById_ShouldReturnPosition() throws Exception {
        Position position = new Position("Tech Lead", PositionLevel.SENIOR, testDepartment);
        position.setDescription("Technical leadership role");
        position.setMinSalary(new BigDecimal("90000"));
        position.setMaxSalary(new BigDecimal("130000"));
        position = positionRepository.save(position);

        mockMvc.perform(get("/api/positions/{id}", position.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Tech Lead")))
                .andExpect(jsonPath("$.level", is("SENIOR")))
                .andExpect(jsonPath("$.description", is("Technical leadership role")))
                .andExpect(jsonPath("$.minSalary", is(90000)))
                .andExpect(jsonPath("$.maxSalary", is(130000)));
    }

    @Test
    void getPositionById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/positions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePosition_ShouldReturnUpdatedPosition() throws Exception {
        Position position = new Position("Developer", PositionLevel.MID, testDepartment);
        position.setMinSalary(new BigDecimal("60000"));
        position.setMaxSalary(new BigDecimal("85000"));
        position = positionRepository.save(position);

        Position updatedPosition = new Position();
        updatedPosition.setTitle("Senior Developer");
        updatedPosition.setLevel(PositionLevel.SENIOR);
        updatedPosition.setStatus(PositionStatus.ACTIVE);
        updatedPosition.setDepartment(testDepartment);
        updatedPosition.setMinSalary(new BigDecimal("80000"));
        updatedPosition.setMaxSalary(new BigDecimal("110000"));
        updatedPosition.setPayGrade("P4");

        mockMvc.perform(put("/api/positions/{id}", position.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPosition)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Senior Developer")))
                .andExpect(jsonPath("$.level", is("SENIOR")))
                .andExpect(jsonPath("$.minSalary", is(80000)))
                .andExpect(jsonPath("$.maxSalary", is(110000)))
                .andExpect(jsonPath("$.payGrade", is("P4")));
    }

    @Test
    void updatePosition_WithInvalidId_ShouldReturnNotFound() throws Exception {
        Position position = new Position();
        position.setTitle("Non-existent Position");
        position.setLevel(PositionLevel.MID);
        position.setStatus(PositionStatus.ACTIVE);
        position.setDepartment(testDepartment);

        mockMvc.perform(put("/api/positions/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePosition_ShouldReturnNoContentAndSetInactive() throws Exception {
        Position position = new Position("Temporary Position", PositionLevel.JUNIOR, testDepartment);
        position = positionRepository.save(position);

        mockMvc.perform(delete("/api/positions/{id}", position.getId()))
                .andExpect(status().isNoContent());

        // Verify position is soft deleted (status changed to INACTIVE)
        mockMvc.perform(get("/api/positions/{id}", position.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));
    }

    @Test
    void deletePosition_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/positions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPosition_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Position position = new Position();
        // Missing required fields like title
        position.setLevel(PositionLevel.MID);
        position.setStatus(PositionStatus.ACTIVE);

        mockMvc.perform(post("/api/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isBadRequest());
    }
}