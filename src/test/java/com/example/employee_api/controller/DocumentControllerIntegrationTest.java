package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.model.enums.DocumentApprovalStatus;
import com.example.employee_api.model.enums.EmployeeStatus;
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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DocumentController
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class DocumentControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    private Employee testEmployee;
    private DocumentType testDocumentType;
    private DocumentCategory testDocumentCategory;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test department
        Department department = new Department();
        department.setName("IT Department");
        department.setDepartmentCode("IT");
        department.setDescription("Information Technology Department");
        department.setLocation("Building A");
        department.setBudget(new BigDecimal("500000.0"));
        department.setStatus(DepartmentStatus.ACTIVE);
        department = departmentRepository.save(department);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@company.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setGender("MALE");
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setHireDate(LocalDate.now().minusYears(2));
        testEmployee.setDepartment(department);
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setSalary(new BigDecimal("75000.0"));
        testEmployee = employeeRepository.save(testEmployee);

        // Create test document type
        testDocumentType = new DocumentType();
        testDocumentType.setName("Contract");
        testDocumentType.setDescription("Employment contract documents");
        testDocumentType.setActive(true);
        testDocumentType.setAllowedFileTypes("pdf,doc,docx");
        testDocumentType.setMaxFileSizeMb(10);
        testDocumentType.setRequiresApproval(true);
        testDocumentType = documentTypeRepository.save(testDocumentType);

        // Create test document category
        testDocumentCategory = new DocumentCategory();
        testDocumentCategory.setName("Legal");
        testDocumentCategory.setDescription("Legal documents");
        testDocumentCategory.setActive(true);
        testDocumentCategory.setColor("#FF5722");
        testDocumentCategory.setIcon("gavel");
        testDocumentCategory = documentCategoryRepository.save(testDocumentCategory);

        // Create test document
        testDocument = new Document();
        testDocument.setEmployee(testEmployee);
        testDocument.setDocumentType(testDocumentType);
        testDocument.setDocumentCategory(testDocumentCategory);
        testDocument.setDocumentName("employment_contract.pdf");
        testDocument.setFilePath("/uploads/documents/test_document.pdf");
        testDocument.setFileType("application/pdf");
        testDocument.setFileSize(1024L);
        testDocument.setDescription("Employment contract for John Doe");
        testDocument.setApprovalStatus(DocumentApprovalStatus.PENDING);
        testDocument.setVersion(1);
        testDocument.setActive(true);
        testDocument.setUploadedBy(1L);
        testDocument = documentRepository.save(testDocument);
    }

    // Document CRUD Operations Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDocuments_ShouldReturnPagedDocuments() throws Exception {
        mockMvc.perform(get("/api/documents")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].documentName", is("employment_contract.pdf")))
                .andExpect(jsonPath("$.content[0].approvalStatus", is("PENDING")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentById_ShouldReturnDocument() throws Exception {
        mockMvc.perform(get("/api/documents/{id}", testDocument.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentName", is("employment_contract.pdf")))
                .andExpect(jsonPath("$.employee.firstName", is("John")))
                .andExpect(jsonPath("$.documentType.name", is("Contract")))
                .andExpect(jsonPath("$.documentCategory.name", is("Legal")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/documents/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDocument_ShouldCreateSuccessfully() throws Exception {
        String documentJson = """
            {
                "employee": {"id": %d},
                "documentType": {"id": %d},
                "documentCategory": {"id": %d},
                "documentName": "new_contract.pdf",
                "filePath": "/uploads/documents/new_contract.pdf",
                "fileType": "application/pdf",
                "fileSize": 2048,
                "description": "New employment contract",
                "uploadedBy": 1
            }
            """.formatted(testEmployee.getId(), testDocumentType.getId(), testDocumentCategory.getId());

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentName", is("new_contract.pdf")))
                .andExpect(jsonPath("$.approvalStatus", is("PENDING")))
                .andExpect(jsonPath("$.version", is(2))); // Should be version 2 since employee already has version 1
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDocument_ShouldUpdateSuccessfully() throws Exception {
        String updateJson = """
            {
                "documentName": "updated_contract.pdf",
                "description": "Updated employment contract",
                "isConfidential": true,
                "expiryDate": "%s",
                "tags": "important,confidential"
            }
            """.formatted(LocalDate.now().plusYears(1).toString());

        mockMvc.perform(put("/api/documents/{id}", testDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentName", is("updated_contract.pdf")))
                .andExpect(jsonPath("$.description", is("Updated employment contract")))
                .andExpect(jsonPath("$.isConfidential", is(true)))
                .andExpect(jsonPath("$.tags", is("important,confidential")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDocument_ShouldSoftDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/documents/{id}", testDocument.getId()))
                .andExpect(status().isNoContent());

        // Verify document is soft deleted
        mockMvc.perform(get("/api/documents/{id}", testDocument.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    // Document Query Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByEmployeeId_ShouldReturnEmployeeDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/employee/{employeeId}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].documentName", is("employment_contract.pdf")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveDocumentsByEmployeeId_ShouldReturnActiveDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/employee/{employeeId}/active", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByDocumentTypeId_ShouldReturnTypeDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/type/{documentTypeId}", testDocumentType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].documentType.name", is("Contract")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByDocumentCategoryId_ShouldReturnCategoryDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/category/{categoryId}", testDocumentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].documentCategory.name", is("Legal")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByApprovalStatus_ShouldReturnFilteredDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/status/{status}", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].approvalStatus", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchDocuments_ShouldReturnMatchingDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/search")
                        .param("searchTerm", "contract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].documentName", containsString("contract")));
    }

    // File Upload/Download Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadDocument_ShouldUploadSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_upload.pdf",
                "application/pdf",
                "Test PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("employeeId", testEmployee.getId().toString())
                        .param("documentTypeId", testDocumentType.getId().toString())
                        .param("documentCategoryId", testDocumentCategory.getId().toString())
                        .param("description", "Test upload document")
                        .param("tags", "test,upload")
                        .param("isConfidential", "false")
                        .param("uploadedBy", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentName", is("test_upload.pdf")))
                .andExpect(jsonPath("$.fileType", is("application/pdf")))
                .andExpect(jsonPath("$.description", is("Test upload document")))
                .andExpect(jsonPath("$.tags", is("test,upload")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadDocument_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(emptyFile)
                        .param("employeeId", testEmployee.getId().toString())
                        .param("documentTypeId", testDocumentType.getId().toString())
                        .param("uploadedBy", "1"))
                .andExpect(status().isBadRequest());
    }

    // Approval Workflow Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveDocument_ShouldApproveSuccessfully() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("approvedBy", 1L);
        request.put("notes", "Document approved");

        mockMvc.perform(post("/api/documents/{id}/approve", testDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus", is("APPROVED")))
                .andExpect(jsonPath("$.approvedBy", is(1)))
                .andExpect(jsonPath("$.approvalNotes", is("Document approved")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectDocument_ShouldRejectSuccessfully() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("rejectedBy", 1L);
        request.put("notes", "Document rejected - missing signatures");

        mockMvc.perform(post("/api/documents/{id}/reject", testDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus", is("REJECTED")))
                .andExpect(jsonPath("$.approvedBy", is(1)))
                .andExpect(jsonPath("$.approvalNotes", is("Document rejected - missing signatures")));
    }

    // Document Type Management Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDocumentTypes_ShouldReturnAllTypes() throws Exception {
        mockMvc.perform(get("/api/documents/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Contract")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveDocumentTypes_ShouldReturnActiveTypes() throws Exception {
        mockMvc.perform(get("/api/documents/types/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDocumentType_ShouldCreateSuccessfully() throws Exception {
        DocumentType newType = new DocumentType();
        newType.setName("Resume");
        newType.setDescription("Employee resume documents");
        newType.setActive(true);
        newType.setAllowedFileTypes("pdf,doc");
        newType.setMaxFileSizeMb(5);
        newType.setRequiresApproval(false);

        mockMvc.perform(post("/api/documents/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newType)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Resume")))
                .andExpect(jsonPath("$.requiresApproval", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDocumentType_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        DocumentType duplicateType = new DocumentType();
        duplicateType.setName("Contract"); // Same name as existing type
        duplicateType.setDescription("Duplicate contract type");
        duplicateType.setActive(true);

        mockMvc.perform(post("/api/documents/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateType)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDocumentType_ShouldUpdateSuccessfully() throws Exception {
        DocumentType updatedType = new DocumentType();
        updatedType.setName("Updated Contract");
        updatedType.setDescription("Updated contract description");
        updatedType.setActive(true);
        updatedType.setAllowedFileTypes("pdf,doc,docx,txt");
        updatedType.setMaxFileSizeMb(15);
        updatedType.setRequiresApproval(false);

        mockMvc.perform(put("/api/documents/types/{id}", testDocumentType.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Contract")))
                .andExpect(jsonPath("$.maxFileSizeMb", is(15)))
                .andExpect(jsonPath("$.requiresApproval", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDocumentType_WithDocuments_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/documents/types/{id}", testDocumentType.getId()))
                .andExpect(status().isBadRequest()); // Should fail because documents exist
    }

    // Document Category Management Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDocumentCategories_ShouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/api/documents/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Legal")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDocumentCategory_ShouldCreateSuccessfully() throws Exception {
        DocumentCategory newCategory = new DocumentCategory();
        newCategory.setName("HR");
        newCategory.setDescription("Human Resources documents");
        newCategory.setActive(true);
        newCategory.setColor("#2196F3");
        newCategory.setIcon("people");

        mockMvc.perform(post("/api/documents/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("HR")))
                .andExpect(jsonPath("$.color", is("#2196F3")))
                .andExpect(jsonPath("$.icon", is("people")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDocumentCategory_ShouldUpdateSuccessfully() throws Exception {
        DocumentCategory updatedCategory = new DocumentCategory();
        updatedCategory.setName("Legal & Compliance");
        updatedCategory.setDescription("Legal and compliance documents");
        updatedCategory.setActive(true);
        updatedCategory.setColor("#E91E63");
        updatedCategory.setIcon("balance-scale");

        mockMvc.perform(put("/api/documents/categories/{id}", testDocumentCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Legal & Compliance")))
                .andExpect(jsonPath("$.color", is("#E91E63")))
                .andExpect(jsonPath("$.icon", is("balance-scale")));
    }

    // Utility Endpoints Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void findDocumentsExpiringWithinDays_ShouldReturnExpiringDocuments() throws Exception {
        // Update test document to have expiry date
        testDocument.setExpiryDate(LocalDate.now().plusDays(15));
        documentRepository.save(testDocument);

        mockMvc.perform(get("/api/documents/expiring")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].documentName", is("employment_contract.pdf")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findExpiredDocuments_ShouldReturnExpiredDocuments() throws Exception {
        // Update test document to be expired
        testDocument.setExpiryDate(LocalDate.now().minusDays(1));
        documentRepository.save(testDocument);

        mockMvc.perform(get("/api/documents/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].documentName", is("employment_contract.pdf")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findPendingApprovalDocuments_ShouldReturnPendingDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/pending-approval"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].approvalStatus", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTotalFileSizeByEmployeeId_ShouldReturnTotalSize() throws Exception {
        mockMvc.perform(get("/api/documents/employee/{employeeId}/total-size", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(1024))); // Size of test document
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void hasDocumentOfType_ShouldReturnTrue() throws Exception {
        mockMvc.perform(get("/api/documents/employee/{employeeId}/has-type/{documentTypeId}",
                        testEmployee.getId(), testDocumentType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void countDocumentsByEmployeeId_ShouldReturnCount() throws Exception {
        mockMvc.perform(get("/api/documents/employee/{employeeId}/count", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(1)));
    }

    // Error Handling Tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByEmployeeId_WithInvalidEmployeeId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/documents/employee/{employeeId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByDocumentTypeId_WithInvalidTypeId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/documents/type/{documentTypeId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDocumentsByDocumentCategoryId_WithInvalidCategoryId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/documents/category/{categoryId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveDocument_WithInvalidId_ShouldReturnNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("approvedBy", 1L);
        request.put("notes", "Document approved");

        mockMvc.perform(post("/api/documents/{id}/approve", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}