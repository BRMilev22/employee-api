package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LeaveDocumentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LeaveDocumentRepository leaveDocumentRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;
    private LeaveDocument testDocument;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Clean up
        leaveDocumentRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        leaveTypeRepository.deleteAll();

        // Create test department
        Department department = new Department();
        department.setName("IT");
        department.setDepartmentCode("IT001");
        department.setDescription("Information Technology");
        department = departmentRepository.save(department);

        // Create test position
        Position position = new Position();
        position.setTitle("Developer");
        position.setLevel(PositionLevel.MID);
        position.setStatus(PositionStatus.ACTIVE);
        position.setDepartment(department);
        position = positionRepository.save(position);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@company.com");
        testEmployee.setPhone("123-456-7890");
        testEmployee.setJobTitle("Software Developer");
        testEmployee.setGender("MALE");
        testEmployee.setDepartment(department);
        testEmployee.setCurrentPosition(position);
        testEmployee.setHireDate(LocalDate.now().minusYears(1));
        testEmployee.setSalary(new BigDecimal("50000.00"));
        testEmployee = employeeRepository.save(testEmployee);

        // Create test leave type
        LeaveType leaveType = new LeaveType();
        leaveType.setName("Sick Leave");
        leaveType.setDescription("Medical leave");
        leaveType.setDaysAllowed(10);
        leaveType.setRequiresApproval(true);
        leaveType.setRequiresDocuments(true);
        leaveType.setActive(true);
        leaveType = leaveTypeRepository.save(leaveType);

        // Create test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType(leaveType);
        testLeaveRequest.setStartDate(LocalDate.now().plusDays(1));
        testLeaveRequest.setEndDate(LocalDate.now().plusDays(3));
        testLeaveRequest.setTotalDays(3.0);
        testLeaveRequest.setReason("Medical appointment");
        testLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        testLeaveRequest = leaveRequestRepository.save(testLeaveRequest);

        // Create test document
        testDocument = new LeaveDocument();
        testDocument.setLeaveRequest(testLeaveRequest);
        testDocument.setDocumentName("medical_certificate.pdf");
        testDocument.setFileType("application/pdf");
        testDocument.setFileSize(1024L);
        testDocument.setDescription("Medical certificate");
        testDocument.setUploadedBy(testEmployee.getId());
        
        // Create a temporary file for testing download
        try {
            java.nio.file.Path tempDir = java.nio.file.Paths.get("uploads/leave-documents/");
            java.nio.file.Files.createDirectories(tempDir);
            java.nio.file.Path tempFile = tempDir.resolve("medical_certificate_" + System.currentTimeMillis() + ".pdf");
            java.nio.file.Files.write(tempFile, "Test PDF content".getBytes());
            testDocument.setFilePath(tempFile.toString());
        } catch (java.io.IOException e) {
            // Fallback to a path that will fail gracefully
            testDocument.setFilePath("/uploads/documents/medical_certificate.pdf");
        }
        
        testDocument = leaveDocumentRepository.save(testDocument);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void testUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test document content".getBytes()
        );

        mockMvc.perform(multipart("/api/leave-documents/upload")
                .file(file)
                .param("leaveRequestId", testLeaveRequest.getId().toString())
                .param("description", "Test document")
                .param("uploadedBy", "1")
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentName").value("test-document.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"))
                .andExpect(jsonPath("$.description").value("Test document"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetDocumentsByLeaveRequest() throws Exception {
        mockMvc.perform(get("/api/leave-documents/leave-request/" + testLeaveRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].documentName").value("medical_certificate.pdf"))
                .andExpect(jsonPath("$[0].description").value("Medical certificate"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetDocumentById() throws Exception {
        mockMvc.perform(get("/api/leave-documents/" + testDocument.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentName").value("medical_certificate.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testDownloadDocument() throws Exception {
        mockMvc.perform(get("/api/leave-documents/" + testDocument.getId() + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    "attachment; filename=\"" + testDocument.getDocumentName() + "\""));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetAllDocuments() throws Exception {
        mockMvc.perform(get("/api/leave-documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testSearchDocuments() throws Exception {
        mockMvc.perform(get("/api/leave-documents/search")
                .param("keyword", "medical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testUpdateDocumentDescription() throws Exception {
        String updateRequest = """
            {
                "description": "Updated medical certificate"
            }
            """;

        mockMvc.perform(put("/api/leave-documents/" + testDocument.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated medical certificate"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testDeleteDocument() throws Exception {
        mockMvc.perform(delete("/api/leave-documents/" + testDocument.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/leave-documents/" + testDocument.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetDocumentsByEmployee() throws Exception {
        mockMvc.perform(get("/api/leave-documents/employee/" + testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetDocumentsByDateRange() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().plusDays(30);

        mockMvc.perform(get("/api/leave-documents/date-range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetDocumentsByFileType() throws Exception {
        mockMvc.perform(get("/api/leave-documents/file-type")
                .param("fileType", "application/pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testGetDocumentStatistics() throws Exception {
        mockMvc.perform(get("/api/leave-documents/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments").exists())
                .andExpect(jsonPath("$.totalSizeBytes").exists());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testBulkDownload() throws Exception {
        mockMvc.perform(post("/api/leave-documents/bulk-download")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + testDocument.getId() + "]"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"));
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testValidateDocuments() throws Exception {
        mockMvc.perform(post("/api/leave-documents/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + testDocument.getId() + "]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"HR_MANAGER"})
    void testArchiveDocument() throws Exception {
        mockMvc.perform(put("/api/leave-documents/" + testDocument.getId() + "/archive")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // Security Tests
    @Test
    void testUploadDocumentWithoutAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/leave-documents/upload")
                .file(file)
                .param("leaveRequestId", testLeaveRequest.getId().toString())
                .param("uploadedBy", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testDeleteDocumentWithEmployeeRole() throws Exception {
        // Try to delete document with USER role - should be forbidden
        // Since the delete endpoint requires ADMIN or HR_MANAGER roles
        // Should return 403 Forbidden for insufficient permissions
        mockMvc.perform(delete("/api/leave-documents/" + testDocument.getId())
                .with(csrf()))
                .andExpect(status().isForbidden()); // Now properly returns 403
    }
}