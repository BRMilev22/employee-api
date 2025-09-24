package com.example.employee_api.controller;

import com.example.employee_api.dto.file.FileMetadataResponse;
import com.example.employee_api.dto.file.FileResponse;
import com.example.employee_api.dto.file.FileUploadRequest;
import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.model.enums.FileType;
import com.example.employee_api.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for file and media management operations
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * POST /api/files/upload - General file upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) FileType fileType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) Long employeeId) {

        FileUploadRequest request = new FileUploadRequest();
        request.setFileType(fileType != null ? fileType : FileType.DOCUMENT);
        request.setDescription(description);
        request.setTags(tags);
        request.setIsPublic(isPublic);
        request.setEmployeeId(employeeId);

        FileResponse response = fileService.uploadFile(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/files/{id} - Download file by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = fileService.downloadFile(id);
        FileResponse fileInfo = fileService.getFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                .body(resource);
    }

    /**
     * DELETE /api/files/{id} - Delete file
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/files/metadata/{id} - Get file metadata
     */
    @GetMapping("/metadata/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(@PathVariable Long id) {
        FileMetadataResponse metadata = fileService.getFileMetadata(id);
        return ResponseEntity.ok(metadata);
    }

    /**
     * POST /api/files/bulk-upload - Bulk file upload
     */
    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<FileResponse>> bulkUploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) FileType fileType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) Long employeeId) {

        FileUploadRequest baseRequest = new FileUploadRequest();
        baseRequest.setFileType(fileType != null ? fileType : FileType.DOCUMENT);
        baseRequest.setDescription(description);
        baseRequest.setTags(tags);
        baseRequest.setIsPublic(isPublic);
        baseRequest.setEmployeeId(employeeId);

        List<FileResponse> responses = fileService.bulkUpload(files, baseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * GET /api/files - Get all files with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PagedResponse<FileResponse>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<FileResponse> response = fileService.getAllFiles(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/files/search - Search files
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PagedResponse<FileResponse>> searchFiles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<FileResponse> response = fileService.searchFiles(query, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/files/statistics - Get file statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getFileStatistics() {
        Map<String, Object> stats = fileService.getFileStatistics();
        return ResponseEntity.ok(stats);
    }

    // Employee-specific endpoints

    /**
     * GET /api/files/employee/{employeeId} - Get files for specific employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<FileResponse>> getEmployeeFiles(@PathVariable Long employeeId) {
        List<FileResponse> files = fileService.getEmployeeFiles(employeeId);
        return ResponseEntity.ok(files);
    }

    /**
     * GET /api/files/employee/{employeeId}/statistics - Get file statistics for employee
     */
    @GetMapping("/employee/{employeeId}/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getEmployeeFileStatistics(@PathVariable Long employeeId) {
        Map<String, Object> stats = fileService.getEmployeeFileStatistics(employeeId);
        return ResponseEntity.ok(stats);
    }

    // Employee photo management endpoints

    /**
     * GET /api/files/employees/{employeeId}/photo - Get employee photo
     */
    @GetMapping("/employees/{employeeId}/photo")
    public ResponseEntity<Resource> getEmployeePhoto(@PathVariable Long employeeId) {
        Optional<FileResponse> photoResponse = fileService.getEmployeePhoto(employeeId);

        if (photoResponse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileService.downloadFile(photoResponse.get().getId());
        FileResponse fileInfo = photoResponse.get();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                .body(resource);
    }

    /**
     * POST /api/files/employees/{employeeId}/photo - Upload employee photo
     */
    @PostMapping("/employees/{employeeId}/photo")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<FileResponse> uploadEmployeePhoto(
            @PathVariable Long employeeId,
            @RequestParam("photo") MultipartFile photo) {

        FileResponse response = fileService.uploadEmployeePhoto(employeeId, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/files/employees/{employeeId}/photo - Delete employee photo
     */
    @DeleteMapping("/employees/{employeeId}/photo")
    @PreAuthorize("hasRole('USER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteEmployeePhoto(@PathVariable Long employeeId) {
        fileService.deleteEmployeePhoto(employeeId);
        return ResponseEntity.ok().build();
    }

    // Administrative endpoints

    /**
     * POST /api/files/cleanup/expired - Cleanup expired files
     */
    @PostMapping("/cleanup/expired")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupExpiredFiles() {
        int cleanedCount = fileService.cleanupExpiredFiles();
        return ResponseEntity.ok(Map.of(
                "message", "Expired files cleanup completed",
                "cleanedFilesCount", cleanedCount
        ));
    }

    /**
     * Exception handler for this controller
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "File operation failed",
                        "message", ex.getMessage()
                ));
    }

    /**
     * Exception handler for file size exceeded
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of(
                        "error", "File too large",
                        "message", "File size exceeds maximum allowed size"
                ));
    }

    /**
     * Exception handler for missing file parameter
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(
            org.springframework.web.bind.MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Missing required parameter",
                        "message", ex.getMessage()
                ));
    }
}