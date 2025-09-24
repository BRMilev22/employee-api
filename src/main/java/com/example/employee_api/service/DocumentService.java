package com.example.employee_api.service;

import com.example.employee_api.exception.ResourceNotFoundException;
import com.example.employee_api.model.Document;
import com.example.employee_api.model.DocumentCategory;
import com.example.employee_api.model.DocumentType;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.DocumentApprovalStatus;
import com.example.employee_api.repository.DocumentCategoryRepository;
import com.example.employee_api.repository.DocumentRepository;
import com.example.employee_api.repository.DocumentTypeRepository;
import com.example.employee_api.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing documents
 */
@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationService notificationService;

    // Base directory for storing documents
    private static final String UPLOAD_DIR = "uploads/documents/";

    // Document management methods

    /**
     * Get all documents with pagination
     */
    @Transactional(readOnly = true)
    public Page<Document> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Get documents by employee ID
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return documentRepository.findByEmployee(employee);
    }

    /**
     * Get active documents by employee ID
     */
    @Transactional(readOnly = true)
    public List<Document> getActiveDocumentsByEmployeeId(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return documentRepository.findActiveByEmployeeId(employeeId);
    }

    /**
     * Get documents by document type ID
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByDocumentTypeId(Long documentTypeId) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with id: " + documentTypeId));
        return documentRepository.findByDocumentType(documentType);
    }

    /**
     * Get documents by document category ID
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByDocumentCategoryId(Long categoryId) {
        if (categoryId != null) {
            DocumentCategory category = documentCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + categoryId));
            return documentRepository.findByDocumentCategory(category);
        }
        return documentRepository.findAll();
    }

    /**
     * Get documents by approval status
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByApprovalStatus(DocumentApprovalStatus status) {
        return documentRepository.findByApprovalStatus(status);
    }

    /**
     * Create a new document
     */
    public Document createDocument(Document document) {
        // Validate employee exists
        if (document.getEmployee() != null && document.getEmployee().getId() != null) {
            Employee employee = employeeRepository.findById(document.getEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + document.getEmployee().getId()));
            document.setEmployee(employee);
        }

        // Validate document type exists
        if (document.getDocumentType() != null && document.getDocumentType().getId() != null) {
            DocumentType documentType = documentTypeRepository.findById(document.getDocumentType().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document type not found with id: " + document.getDocumentType().getId()));
            document.setDocumentType(documentType);
        }

        // Validate document category exists (optional)
        if (document.getDocumentCategory() != null && document.getDocumentCategory().getId() != null) {
            DocumentCategory category = documentCategoryRepository.findById(document.getDocumentCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + document.getDocumentCategory().getId()));
            document.setDocumentCategory(category);
        }

        // Set initial approval status based on document type requirements
        if (document.getDocumentType() != null && document.getDocumentType().getRequiresApproval()) {
            document.setApprovalStatus(DocumentApprovalStatus.PENDING);
        } else {
            document.setApprovalStatus(DocumentApprovalStatus.APPROVED);
        }

        // Set version number (override default value with calculated version)
        System.out.println("DEBUG: createDocument called");
        System.out.println("DEBUG: document.getVersion() = " + document.getVersion());
        
        // Always calculate version for new documents (even if default value is set)
        System.out.println("DEBUG: Calculating version for new document");
        // Find latest version for this employee and document type
        Optional<Document> latestVersion = documentRepository.findFirstByEmployeeIdAndDocumentTypeIdAndActiveTrueOrderByVersionDesc(
                document.getEmployee().getId(), document.getDocumentType().getId());
        
        // Debug logging
        System.out.println("DEBUG: Looking for documents with employeeId=" + document.getEmployee().getId() + 
                          " and documentTypeId=" + document.getDocumentType().getId());
        System.out.println("DEBUG: Found existing document: " + latestVersion.isPresent());
        if (latestVersion.isPresent()) {
            System.out.println("DEBUG: Existing document version: " + latestVersion.get().getVersion());
            System.out.println("DEBUG: Existing document id: " + latestVersion.get().getId());
            System.out.println("DEBUG: Existing document name: " + latestVersion.get().getDocumentName());
            document.setVersion(latestVersion.get().getVersion() + 1);
        } else {
            System.out.println("DEBUG: No existing document found, setting version to 1");
            document.setVersion(1);
        }
        System.out.println("DEBUG: Final version set: " + document.getVersion());

        return documentRepository.save(document);
    }

    /**
     * Update an existing document
     */
    public Document updateDocument(Long id, Document updatedDocument) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        // Update allowed fields
        existingDocument.setDocumentName(updatedDocument.getDocumentName());
        existingDocument.setDescription(updatedDocument.getDescription());
        existingDocument.setIsConfidential(updatedDocument.getIsConfidential());
        existingDocument.setExpiryDate(updatedDocument.getExpiryDate());
        existingDocument.setTags(updatedDocument.getTags());

        // Update document category if provided
        if (updatedDocument.getDocumentCategory() != null && updatedDocument.getDocumentCategory().getId() != null) {
            DocumentCategory category = documentCategoryRepository.findById(updatedDocument.getDocumentCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + updatedDocument.getDocumentCategory().getId()));
            existingDocument.setDocumentCategory(category);
        }

        return documentRepository.save(existingDocument);
    }

    /**
     * Delete a document (soft delete)
     */
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        document.setActive(false);
        documentRepository.save(document);
    }

    /**
     * Upload and store document file
     */
    public Document uploadDocument(Long employeeId, Long documentTypeId, Long documentCategoryId, 
                                  MultipartFile file, String description, String tags, 
                                  LocalDate expiryDate, Boolean isConfidential, Long uploadedBy) {
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Get employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Get document type
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with id: " + documentTypeId));

        // Validate file type if restrictions exist
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!documentType.isFileTypeAllowed(fileExtension)) {
            throw new IllegalArgumentException("File type not allowed for this document type");
        }

        // Validate file size if restrictions exist
        if (!documentType.isFileSizeAllowed(file.getSize())) {
            throw new IllegalArgumentException("File size exceeds maximum allowed for this document type");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create document entity
            Document document = new Document();
            document.setEmployee(employee);
            document.setDocumentType(documentType);
            
            // Set document category if provided
            if (documentCategoryId != null) {
                DocumentCategory category = documentCategoryRepository.findById(documentCategoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + documentCategoryId));
                document.setDocumentCategory(category);
            }

            document.setDocumentName(file.getOriginalFilename());
            document.setFilePath(filePath.toString());
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setDescription(description);
            document.setExpiryDate(expiryDate);
            document.setIsConfidential(isConfidential != null ? isConfidential : false);
            document.setTags(tags);
            document.setUploadedBy(uploadedBy);

            return createDocument(document);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    /**
     * Download document file
     */
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        try {
            Path filePath = Paths.get(document.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * Approve a document
     */
    public Document approveDocument(Long id, Long approvedBy, String notes) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        document.approve(approvedBy, notes);
        Document savedDocument = documentRepository.save(document);
        
        // Send notification to document owner about approval
        try {
            Employee approver = employeeRepository.findById(approvedBy).orElse(null);
            if (approver != null && document.getEmployee() != null) {
                notificationService.sendDocumentApprovalNotification(
                    savedDocument.getId(),
                    document.getEmployee().getId(),
                    approver.getFirstName() + " " + approver.getLastName(),
                    document.getDocumentName()
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the approval
            System.err.println("Failed to send document approval notification: " + e.getMessage());
        }
        
        return savedDocument;
    }

    /**
     * Reject a document
     */
    public Document rejectDocument(Long id, Long rejectedBy, String notes) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        document.reject(rejectedBy, notes);
        return documentRepository.save(document);
    }

    // Document Type management methods

    /**
     * Get all document types
     */
    @Transactional(readOnly = true)
    public List<DocumentType> getAllDocumentTypes() {
        return documentTypeRepository.findAll();
    }

    /**
     * Get active document types
     */
    @Transactional(readOnly = true)
    public List<DocumentType> getActiveDocumentTypes() {
        return documentTypeRepository.findByActiveTrue();
    }

    /**
     * Get document type by ID
     */
    @Transactional(readOnly = true)
    public Optional<DocumentType> getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id);
    }

    /**
     * Create document type
     */
    public DocumentType createDocumentType(DocumentType documentType) {
        // Check if name already exists
        if (documentTypeRepository.existsByNameIgnoreCase(documentType.getName())) {
            throw new IllegalArgumentException("Document type with name '" + documentType.getName() + "' already exists");
        }
        return documentTypeRepository.save(documentType);
    }

    /**
     * Update document type
     */
    public DocumentType updateDocumentType(Long id, DocumentType updatedDocumentType) {
        DocumentType existingType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with id: " + id));

        // Check if name already exists (excluding current record)
        if (documentTypeRepository.existsByNameIgnoreCaseAndIdNot(updatedDocumentType.getName(), id)) {
            throw new IllegalArgumentException("Document type with name '" + updatedDocumentType.getName() + "' already exists");
        }

        existingType.setName(updatedDocumentType.getName());
        existingType.setDescription(updatedDocumentType.getDescription());
        existingType.setActive(updatedDocumentType.getActive());
        existingType.setAllowedFileTypes(updatedDocumentType.getAllowedFileTypes());
        existingType.setMaxFileSizeMb(updatedDocumentType.getMaxFileSizeMb());
        existingType.setRequiresApproval(updatedDocumentType.getRequiresApproval());

        return documentTypeRepository.save(existingType);
    }

    /**
     * Delete document type
     */
    public void deleteDocumentType(Long id) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with id: " + id));
        
        // Check if any documents are using this type
        long documentCount = documentRepository.countByDocumentTypeId(id);
        if (documentCount > 0) {
            throw new IllegalStateException("Cannot delete document type that is used by " + documentCount + " documents");
        }

        documentTypeRepository.delete(documentType);
    }

    // Document Category management methods

    /**
     * Get all document categories
     */
    @Transactional(readOnly = true)
    public List<DocumentCategory> getAllDocumentCategories() {
        return documentCategoryRepository.findAll();
    }

    /**
     * Get active document categories
     */
    @Transactional(readOnly = true)
    public List<DocumentCategory> getActiveDocumentCategories() {
        return documentCategoryRepository.findByActiveTrue();
    }

    /**
     * Get document category by ID
     */
    @Transactional(readOnly = true)
    public Optional<DocumentCategory> getDocumentCategoryById(Long id) {
        return documentCategoryRepository.findById(id);
    }

    /**
     * Create document category
     */
    public DocumentCategory createDocumentCategory(DocumentCategory documentCategory) {
        // Check if name already exists
        if (documentCategoryRepository.existsByNameIgnoreCase(documentCategory.getName())) {
            throw new IllegalArgumentException("Document category with name '" + documentCategory.getName() + "' already exists");
        }
        return documentCategoryRepository.save(documentCategory);
    }

    /**
     * Update document category
     */
    public DocumentCategory updateDocumentCategory(Long id, DocumentCategory updatedCategory) {
        DocumentCategory existingCategory = documentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + id));

        // Check if name already exists (excluding current record)
        if (documentCategoryRepository.existsByNameIgnoreCaseAndIdNot(updatedCategory.getName(), id)) {
            throw new IllegalArgumentException("Document category with name '" + updatedCategory.getName() + "' already exists");
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setActive(updatedCategory.getActive());
        existingCategory.setColor(updatedCategory.getColor());
        existingCategory.setIcon(updatedCategory.getIcon());

        return documentCategoryRepository.save(existingCategory);
    }

    /**
     * Delete document category
     */
    public void deleteDocumentCategory(Long id) {
        DocumentCategory category = documentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + id));
        
        // Check if any documents are using this category
        long documentCount = documentRepository.countByDocumentCategoryId(id);
        if (documentCount > 0) {
            throw new IllegalStateException("Cannot delete document category that is used by " + documentCount + " documents");
        }

        documentCategoryRepository.delete(category);
    }

    // Utility and query methods

    /**
     * Search documents globally
     */
    @Transactional(readOnly = true)
    public List<Document> searchDocuments(String searchTerm) {
        return documentRepository.globalTextSearch(searchTerm);
    }

    /**
     * Find documents expiring within days
     */
    @Transactional(readOnly = true)
    public List<Document> findDocumentsExpiringWithinDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return documentRepository.findExpiringWithinDays(today, futureDate);
    }

    /**
     * Find expired documents
     */
    @Transactional(readOnly = true)
    public List<Document> findExpiredDocuments() {
        return documentRepository.findExpiredDocuments(LocalDate.now());
    }

    /**
     * Find pending approval documents
     */
    @Transactional(readOnly = true)
    public List<Document> findPendingApprovalDocuments() {
        return documentRepository.findPendingApprovalDocuments();
    }

    /**
     * Get total file size for employee
     */
    @Transactional(readOnly = true)
    public Long getTotalFileSizeByEmployeeId(Long employeeId) {
        return documentRepository.getTotalFileSizeByEmployeeId(employeeId);
    }

    /**
     * Check if employee has document of specific type
     */
    @Transactional(readOnly = true)
    public boolean hasDocumentOfType(Long employeeId, Long documentTypeId) {
        return documentRepository.hasDocumentOfType(employeeId, documentTypeId);
    }

    /**
     * Count documents by employee
     */
    @Transactional(readOnly = true)
    public long countDocumentsByEmployeeId(Long employeeId) {
        return documentRepository.countByEmployeeId(employeeId);
    }

    // Private utility methods

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}