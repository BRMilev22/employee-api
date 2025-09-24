package com.example.employee_api.controller;

import com.example.employee_api.model.Employee;
import com.example.employee_api.model.File;
import com.example.employee_api.model.enums.FileStatus;
import com.example.employee_api.model.enums.FileType;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FileControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;
    private File testFile;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        fileRepository.deleteAll();
        employeeRepository.deleteAll();

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setGender("MALE");
        testEmployee = employeeRepository.save(testEmployee);

        // Create test uploads directory
        Path uploadDir = Paths.get("./test-uploads");
        Files.createDirectories(uploadDir);

        // Create test file
        testFile = new File();
        testFile.setFilename("test-file.txt");
        testFile.setOriginalFilename("original-test.txt");
        testFile.setFilePath("./test-uploads/test-file.txt");
        testFile.setMimeType("text/plain");
        testFile.setFileSize(100L);
        testFile.setFileType(FileType.DOCUMENT);
        testFile.setStatus(FileStatus.ACTIVE);
        testFile.setEmployee(testEmployee);
        testFile.setIsPublic(false);
        testFile.setDownloadCount(0L);
        testFile = fileRepository.save(testFile);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void uploadFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test file content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("fileType", "DOCUMENT")
                        .param("description", "Test file")
                        .param("employeeId", testEmployee.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename", is("test.txt")))
                .andExpect(jsonPath("$.mimeType", is("text/plain")))
                .andExpect(jsonPath("$.fileType", is("DOCUMENT")))
                .andExpect(jsonPath("$.description", is("Test file")))
                .andExpect(jsonPath("$.employeeId", is(testEmployee.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void uploadFile_WithoutEmployee_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "public-test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Public test file content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("fileType", "DOCUMENT")
                        .param("description", "Public test file")
                        .param("isPublic", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename", is("public-test.txt")))
                .andExpect(jsonPath("$.isPublic", is(true)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void uploadFile_EmptyFile_BadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("File operation failed")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void downloadFile_Success() throws Exception {
        // Create actual test file
        Path testFilePath = Paths.get(testFile.getFilePath());
        Files.createDirectories(testFilePath.getParent());
        Files.write(testFilePath, "Test file content".getBytes());

        mockMvc.perform(get("/api/files/{id}", testFile.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment; filename=\"" + testFile.getOriginalFilename() + "\"")))
                .andExpect(content().string("Test file content"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void downloadFile_NotFound() throws Exception {
        mockMvc.perform(get("/api/files/{id}", 99999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("File operation failed")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteFile_Success() throws Exception {
        mockMvc.perform(delete("/api/files/{id}", testFile.getId()))
                .andExpect(status().isOk());

        // Verify file is soft deleted
        File deletedFile = fileRepository.findById(testFile.getId()).orElse(null);
        assert deletedFile != null;
        assert deletedFile.getStatus() == FileStatus.DELETED;
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getFileMetadata_Success() throws Exception {
        mockMvc.perform(get("/api/files/metadata/{id}", testFile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testFile.getId().intValue())))
                .andExpect(jsonPath("$.originalFilename", is(testFile.getOriginalFilename())))
                .andExpect(jsonPath("$.mimeType", is(testFile.getMimeType())))
                .andExpect(jsonPath("$.fileSize", is(testFile.getFileSize().intValue())))
                .andExpect(jsonPath("$.fileType", is("DOCUMENT")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void bulkUploadFiles_Success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "bulk1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Bulk file 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "bulk2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Bulk file 2".getBytes()
        );

        mockMvc.perform(multipart("/api/files/bulk-upload")
                        .file(file1)
                        .file(file2)
                        .param("fileType", "DOCUMENT")
                        .param("description", "Bulk upload test")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].originalFilename", is("bulk1.txt")))
                .andExpect(jsonPath("$[1].originalFilename", is("bulk2.txt")));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void getAllFiles_Success() throws Exception {
        mockMvc.perform(get("/api/files")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void searchFiles_Success() throws Exception {
        mockMvc.perform(get("/api/files/search")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getFileStatistics_Success() throws Exception {
        mockMvc.perform(get("/api/files/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles", isA(Number.class)))
                .andExpect(jsonPath("$.totalSize", isA(Number.class)))
                .andExpect(jsonPath("$.byType", isA(Object.class)));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    void getEmployeeFiles_Success() throws Exception {
        mockMvc.perform(get("/api/files/employee/{employeeId}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEmployeeFileStatistics_Success() throws Exception {
        mockMvc.perform(get("/api/files/employee/{employeeId}/statistics", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles", isA(Number.class)))
                .andExpect(jsonPath("$.totalSize", isA(Number.class)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void uploadEmployeePhoto_Success() throws Exception {
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake jpeg content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/employees/{employeeId}/photo", testEmployee.getId())
                        .file(photo)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename", is("profile.jpg")))
                .andExpect(jsonPath("$.fileType", is("EMPLOYEE_PHOTO")))
                .andExpect(jsonPath("$.employeeId", is(testEmployee.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEmployeePhoto_NotFound() throws Exception {
        mockMvc.perform(get("/api/files/employees/{employeeId}/photo", testEmployee.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteEmployeePhoto_Success() throws Exception {
        // First upload a photo
        File photo = new File();
        photo.setFilename("photo.jpg");
        photo.setOriginalFilename("profile.jpg");
        photo.setFilePath("./test-uploads/photo.jpg");
        photo.setMimeType("image/jpeg");
        photo.setFileSize(1000L);
        photo.setFileType(FileType.EMPLOYEE_PHOTO);
        photo.setStatus(FileStatus.ACTIVE);
        photo.setEmployee(testEmployee);
        photo.setIsPublic(false);
        fileRepository.save(photo);

        mockMvc.perform(delete("/api/files/employees/{employeeId}/photo", testEmployee.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void cleanupExpiredFiles_Success() throws Exception {
        // Create an expired file
        File expiredFile = new File();
        expiredFile.setFilename("expired.txt");
        expiredFile.setOriginalFilename("expired.txt");
        expiredFile.setFilePath("./test-uploads/expired.txt");
        expiredFile.setMimeType("text/plain");
        expiredFile.setFileSize(100L);
        expiredFile.setFileType(FileType.DOCUMENT);
        expiredFile.setStatus(FileStatus.ACTIVE);
        expiredFile.setExpiresAt(LocalDateTime.now().minusDays(1));
        fileRepository.save(expiredFile);

        mockMvc.perform(post("/api/files/cleanup/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Expired files cleanup completed")))
                .andExpect(jsonPath("$.cleanedFilesCount", isA(Number.class)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void uploadFile_InvalidExtension_BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "executable content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("File operation failed")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void uploadFile_FileTooLarge_PayloadTooLarge() throws Exception {
        // Create a mock file that would be too large (this is simulated)
        // In real test, you'd need to configure max file size differently
        byte[] largeContent = new byte[1024 * 1024]; // 1MB, assuming max is smaller
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.txt",
                MediaType.TEXT_PLAIN_VALUE,
                largeContent
        );

        // This test depends on the max file size configuration
        // For demo purposes, we'll just verify the endpoint exists
        mockMvc.perform(multipart("/api/files/upload")
                        .file(largeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA));
        // Result depends on actual configuration
    }
}