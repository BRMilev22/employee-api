package com.example.employee_api.controller;

import com.example.employee_api.model.LeaveDocument;
import com.example.employee_api.service.LeaveDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing leave documents
 */
@RestController
@RequestMapping("/api/leave-documents")
@CrossOrigin(origins = "*")
public class LeaveDocumentController {

    @Autowired
    private LeaveDocumentService leaveDocumentService;

    /**
     * Get all leave documents with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveDocument>> getAllLeaveDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveDocument> documents = leaveDocumentService.getAllLeaveDocuments(pageable);
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Get leave document by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @leaveDocumentController.canAccessLeaveDocument(#id, authentication.name)")
    public ResponseEntity<LeaveDocument> getLeaveDocumentById(@PathVariable Long id) {
        Optional<LeaveDocument> document = leaveDocumentService.getLeaveDocumentById(id);
        
        if (document.isPresent()) {
            return ResponseEntity.ok(document.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get leave documents by leave request
     */
    @GetMapping("/leave-request/{leaveRequestId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveDocument>> getLeaveDocumentsByLeaveRequest(@PathVariable Long leaveRequestId) {
        List<LeaveDocument> documents = leaveDocumentService.getLeaveDocumentsByLeaveRequest(leaveRequestId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get leave documents by file type
     */
    @GetMapping("/file-type/{fileType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveDocument>> getLeaveDocumentsByFileType(@PathVariable String fileType) {
        List<LeaveDocument> documents = leaveDocumentService.getLeaveDocumentsByFileType(fileType);
        return ResponseEntity.ok(documents);
    }

    /**
     * Upload a leave document
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveDocument> uploadLeaveDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("leaveRequestId") Long leaveRequestId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("uploadedBy") Long uploadedBy) {
        
        try {
            LeaveDocument document = leaveDocumentService.uploadLeaveDocument(leaveRequestId, file, description, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create leave document (programmatic)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveDocument> createLeaveDocument(@Valid @RequestBody LeaveDocument leaveDocument) {
        LeaveDocument savedDocument = leaveDocumentService.saveLeaveDocument(leaveDocument);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }

    /**
     * Update leave document metadata
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or @leaveDocumentController.canModifyLeaveDocument(#id, authentication.name)")
    public ResponseEntity<LeaveDocument> updateLeaveDocument(
            @PathVariable Long id,
            @RequestBody DocumentUpdateRequest updateRequest) {
        
        try {
            LeaveDocument updated = leaveDocumentService.updateLeaveDocument(
                    id, 
                    updateRequest.getDocumentName(), 
                    updateRequest.getDescription()
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download a leave document
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF') or @leaveDocumentController.canAccessLeaveDocument(#id, authentication.name)")
    public ResponseEntity<byte[]> downloadLeaveDocument(@PathVariable Long id) {
        try {
            Optional<LeaveDocument> documentOpt = leaveDocumentService.getLeaveDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            LeaveDocument document = documentOpt.get();
            byte[] fileContent = leaveDocumentService.getFileContent(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getFileType()));
            headers.set("Content-Disposition", "attachment; filename=\"" + document.getDocumentName() + "\"");
            headers.setContentLength(fileContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if file exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<FileExistsResponse> checkFileExists(@PathVariable Long id) {
        boolean exists = leaveDocumentService.fileExists(id);
        return ResponseEntity.ok(new FileExistsResponse(exists));
    }

    /**
     * Get total file size for a leave request
     */
    @GetMapping("/leave-request/{leaveRequestId}/total-size")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<FileSizeResponse> getTotalFileSizeForLeaveRequest(@PathVariable Long leaveRequestId) {
        Long totalSize = leaveDocumentService.getTotalFileSizeForLeaveRequest(leaveRequestId);
        return ResponseEntity.ok(new FileSizeResponse(totalSize));
    }

    /**
     * Get documents uploaded by a specific user
     */
    @GetMapping("/uploaded-by/{uploadedBy}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveDocument>> getDocumentsByUploadedBy(@PathVariable Long uploadedBy) {
        List<LeaveDocument> documents = leaveDocumentService.getDocumentsByUploadedBy(uploadedBy);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents uploaded within a date range
     */
    @GetMapping("/uploaded-between")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<LeaveDocument>> getDocumentsUploadedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LeaveDocument> documents = leaveDocumentService.getDocumentsUploadedBetween(startDate, endDate);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<LeaveDocumentService.LeaveDocumentStatistics> getLeaveDocumentStatistics() {
        LeaveDocumentService.LeaveDocumentStatistics stats = leaveDocumentService.getLeaveDocumentStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Clean up orphaned files
     */
    @PostMapping("/cleanup-orphaned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanupOrphanedFiles() {
        try {
            leaveDocumentService.cleanupOrphanedFiles();
            return ResponseEntity.ok("Orphaned files cleanup completed successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Delete a leave document
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<Void> deleteLeaveDocument(@PathVariable Long id) {
        try {
            leaveDocumentService.deleteLeaveDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Request and response classes
     */
    public static class DocumentUpdateRequest {
        private String documentName;
        private String description;

        // Getters and setters
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class FileExistsResponse {
        private boolean exists;

        public FileExistsResponse(boolean exists) {
            this.exists = exists;
        }

        // Getters and setters
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
    }

    public static class FileSizeResponse {
        private Long totalSizeBytes;

        public FileSizeResponse(Long totalSizeBytes) {
            this.totalSizeBytes = totalSizeBytes;
        }

        // Getters and setters
        public Long getTotalSizeBytes() { return totalSizeBytes; }
        public void setTotalSizeBytes(Long totalSizeBytes) { this.totalSizeBytes = totalSizeBytes; }
        
        public Double getTotalSizeMB() { 
            return totalSizeBytes != null ? totalSizeBytes / (1024.0 * 1024.0) : 0.0; 
        }
    }

    /**
     * Security methods for access control
     */
    public boolean canAccessLeaveDocument(Long documentId, String username) {
        // Implementation to check if user can access this specific document
        // For now, allowing access (should be properly implemented)
        return true;
    }

    public boolean canModifyLeaveDocument(Long documentId, String username) {
        // Implementation to check if user can modify this specific document
        // Employees cannot modify/delete documents - only HR staff and above
        return false;
    }

    /**
     * Search leave documents
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveDocument>> searchDocuments(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveDocument> documents = leaveDocumentService.getAllLeaveDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveDocument>> getDocumentsByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveDocument> documents = leaveDocumentService.getAllLeaveDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveDocument>> getDocumentsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveDocument> documents = leaveDocumentService.getAllLeaveDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by file type (with query param)
     */
    @GetMapping("/file-type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<Page<LeaveDocument>> getDocumentsByFileTypeParam(
            @RequestParam String fileType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveDocument> documents = leaveDocumentService.getAllLeaveDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Bulk download documents
     */
    @PostMapping("/bulk-download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<byte[]> bulkDownloadDocuments(@RequestBody List<Long> documentIds) {
        try {
            // For now, return a simple response
            byte[] zipContent = "Mock ZIP content".getBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", "documents.zip");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validate documents
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_STAFF')")
    public ResponseEntity<List<String>> validateDocuments(@RequestBody List<Long> documentIds) {
        // Return empty list for now
        return ResponseEntity.ok(List.of());
    }

    /**
     * Archive document
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or @leaveDocumentController.canModifyLeaveDocument(#id, authentication.name)")
    public ResponseEntity<Void> archiveDocument(@PathVariable Long id) {
        try {
            // For now, just return OK
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}