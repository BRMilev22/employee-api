package com.example.employee_api.controller;

import com.example.employee_api.model.Document;
import com.example.employee_api.model.DocumentCategory;
import com.example.employee_api.model.DocumentType;
import com.example.employee_api.model.enums.DocumentApprovalStatus;
import com.example.employee_api.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing documents
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    // Document management endpoints

    /**
     * Get all documents with pagination
     */
    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(Pageable pageable) {
        Page<Document> documents = documentService.getAllDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Optional<Document> document = documentService.getDocumentById(id);
        return document.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get documents by employee ID
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Document>> getDocumentsByEmployeeId(@PathVariable Long employeeId) {
        List<Document> documents = documentService.getDocumentsByEmployeeId(employeeId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get active documents by employee ID
     */
    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<List<Document>> getActiveDocumentsByEmployeeId(@PathVariable Long employeeId) {
        List<Document> documents = documentService.getActiveDocumentsByEmployeeId(employeeId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by document type ID
     */
    @GetMapping("/type/{documentTypeId}")
    public ResponseEntity<List<Document>> getDocumentsByDocumentTypeId(@PathVariable Long documentTypeId) {
        List<Document> documents = documentService.getDocumentsByDocumentTypeId(documentTypeId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by document category ID
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Document>> getDocumentsByDocumentCategoryId(@PathVariable Long categoryId) {
        List<Document> documents = documentService.getDocumentsByDocumentCategoryId(categoryId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by approval status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Document>> getDocumentsByApprovalStatus(@PathVariable DocumentApprovalStatus status) {
        List<Document> documents = documentService.getDocumentsByApprovalStatus(status);
        return ResponseEntity.ok(documents);
    }

    /**
     * Search documents globally
     */
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String searchTerm) {
        List<Document> documents = documentService.searchDocuments(searchTerm);
        return ResponseEntity.ok(documents);
    }

    /**
     * Create a new document
     */
    @PostMapping
    public ResponseEntity<Document> createDocument(@Valid @RequestBody Document document) {
        Document createdDocument = documentService.createDocument(document);
        return new ResponseEntity<>(createdDocument, HttpStatus.CREATED);
    }

    /**
     * Update an existing document
     */
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, 
                                                  @RequestBody Document document) {
        Document updatedDocument = documentService.updateDocument(id, document);
        return ResponseEntity.ok(updatedDocument);
    }

    /**
     * Delete a document (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload document file
     */
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("documentTypeId") Long documentTypeId,
            @RequestParam(value = "documentCategoryId", required = false) Long documentCategoryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "expiryDate", required = false) LocalDate expiryDate,
            @RequestParam(value = "isConfidential", required = false) Boolean isConfidential,
            @RequestParam("uploadedBy") Long uploadedBy) {
        
        Document document = documentService.uploadDocument(employeeId, documentTypeId, documentCategoryId,
                file, description, tags, expiryDate, isConfidential, uploadedBy);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    /**
     * Download document file
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<Document> documentOpt = documentService.getDocumentById(id);
        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document document = documentOpt.get();
        byte[] fileContent = documentService.downloadDocument(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getFileType()));
        headers.setContentDispositionFormData("attachment", document.getDocumentName());
        headers.setContentLength(fileContent.length);

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    /**
     * Approve a document
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Document> approveDocument(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> request) {
        Long approvedBy = Long.valueOf(request.get("approvedBy").toString());
        String notes = request.get("notes") != null ? request.get("notes").toString() : null;
        
        Document document = documentService.approveDocument(id, approvedBy, notes);
        return ResponseEntity.ok(document);
    }

    /**
     * Reject a document
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Document> rejectDocument(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> request) {
        Long rejectedBy = Long.valueOf(request.get("rejectedBy").toString());
        String notes = request.get("notes") != null ? request.get("notes").toString() : null;
        
        Document document = documentService.rejectDocument(id, rejectedBy, notes);
        return ResponseEntity.ok(document);
    }

    // Document Type management endpoints

    /**
     * Get all document types
     */
    @GetMapping("/types")
    public ResponseEntity<List<DocumentType>> getAllDocumentTypes() {
        List<DocumentType> documentTypes = documentService.getAllDocumentTypes();
        return ResponseEntity.ok(documentTypes);
    }

    /**
     * Get active document types
     */
    @GetMapping("/types/active")
    public ResponseEntity<List<DocumentType>> getActiveDocumentTypes() {
        List<DocumentType> documentTypes = documentService.getActiveDocumentTypes();
        return ResponseEntity.ok(documentTypes);
    }

    /**
     * Get document type by ID
     */
    @GetMapping("/types/{id}")
    public ResponseEntity<DocumentType> getDocumentTypeById(@PathVariable Long id) {
        Optional<DocumentType> documentType = documentService.getDocumentTypeById(id);
        return documentType.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create document type
     */
    @PostMapping("/types")
    public ResponseEntity<DocumentType> createDocumentType(@Valid @RequestBody DocumentType documentType) {
        DocumentType createdType = documentService.createDocumentType(documentType);
        return new ResponseEntity<>(createdType, HttpStatus.CREATED);
    }

    /**
     * Update document type
     */
    @PutMapping("/types/{id}")
    public ResponseEntity<DocumentType> updateDocumentType(@PathVariable Long id,
                                                          @Valid @RequestBody DocumentType documentType) {
        DocumentType updatedType = documentService.updateDocumentType(id, documentType);
        return ResponseEntity.ok(updatedType);
    }

    /**
     * Delete document type
     */
    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteDocumentType(@PathVariable Long id) {
        documentService.deleteDocumentType(id);
        return ResponseEntity.noContent().build();
    }

    // Document Category management endpoints

    /**
     * Get all document categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<DocumentCategory>> getAllDocumentCategories() {
        List<DocumentCategory> categories = documentService.getAllDocumentCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get active document categories
     */
    @GetMapping("/categories/active")
    public ResponseEntity<List<DocumentCategory>> getActiveDocumentCategories() {
        List<DocumentCategory> categories = documentService.getActiveDocumentCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get document category by ID
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<DocumentCategory> getDocumentCategoryById(@PathVariable Long id) {
        Optional<DocumentCategory> category = documentService.getDocumentCategoryById(id);
        return category.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create document category
     */
    @PostMapping("/categories")
    public ResponseEntity<DocumentCategory> createDocumentCategory(@Valid @RequestBody DocumentCategory category) {
        DocumentCategory createdCategory = documentService.createDocumentCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    /**
     * Update document category
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<DocumentCategory> updateDocumentCategory(@PathVariable Long id,
                                                                  @Valid @RequestBody DocumentCategory category) {
        DocumentCategory updatedCategory = documentService.updateDocumentCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Delete document category
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteDocumentCategory(@PathVariable Long id) {
        documentService.deleteDocumentCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Utility endpoints

    /**
     * Find documents expiring within specified days
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<Document>> findDocumentsExpiringWithinDays(@RequestParam int days) {
        List<Document> documents = documentService.findDocumentsExpiringWithinDays(days);
        return ResponseEntity.ok(documents);
    }

    /**
     * Find expired documents
     */
    @GetMapping("/expired")
    public ResponseEntity<List<Document>> findExpiredDocuments() {
        List<Document> documents = documentService.findExpiredDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Find pending approval documents
     */
    @GetMapping("/pending-approval")
    public ResponseEntity<List<Document>> findPendingApprovalDocuments() {
        List<Document> documents = documentService.findPendingApprovalDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get total file size for employee
     */
    @GetMapping("/employee/{employeeId}/total-size")
    public ResponseEntity<Long> getTotalFileSizeByEmployeeId(@PathVariable Long employeeId) {
        Long totalSize = documentService.getTotalFileSizeByEmployeeId(employeeId);
        return ResponseEntity.ok(totalSize);
    }

    /**
     * Check if employee has document of specific type
     */
    @GetMapping("/employee/{employeeId}/has-type/{documentTypeId}")
    public ResponseEntity<Boolean> hasDocumentOfType(@PathVariable Long employeeId,
                                                    @PathVariable Long documentTypeId) {
        boolean hasDocument = documentService.hasDocumentOfType(employeeId, documentTypeId);
        return ResponseEntity.ok(hasDocument);
    }

    /**
     * Count documents by employee
     */
    @GetMapping("/employee/{employeeId}/count")
    public ResponseEntity<Long> countDocumentsByEmployeeId(@PathVariable Long employeeId) {
        long count = documentService.countDocumentsByEmployeeId(employeeId);
        return ResponseEntity.ok(count);
    }
}