package com.example.employee_api.service;

import com.example.employee_api.model.LeaveDocument;
import com.example.employee_api.model.LeaveRequest;
import com.example.employee_api.repository.LeaveDocumentRepository;
import com.example.employee_api.repository.LeaveRequestRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing leave documents
 */
@Service
@Transactional
public class LeaveDocumentService {

    @Autowired
    private LeaveDocumentRepository leaveDocumentRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Base directory for storing leave documents
    private static final String UPLOAD_DIR = "uploads/leave-documents/";

    /**
     * Get all leave documents with pagination
     */
    @Transactional(readOnly = true)
    public Page<LeaveDocument> getAllLeaveDocuments(Pageable pageable) {
        return leaveDocumentRepository.findAll(pageable);
    }

    /**
     * Get leave document by ID
     */
    @Transactional(readOnly = true)
    public Optional<LeaveDocument> getLeaveDocumentById(Long id) {
        return leaveDocumentRepository.findById(id);
    }

    /**
     * Get leave documents by leave request
     */
    @Transactional(readOnly = true)
    public List<LeaveDocument> getLeaveDocumentsByLeaveRequest(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + leaveRequestId));
        return leaveDocumentRepository.findByLeaveRequest(leaveRequest);
    }

    /**
     * Get leave documents by leave request ID
     */
    @Transactional(readOnly = true)
    public List<LeaveDocument> getLeaveDocumentsByLeaveRequestId(Long leaveRequestId) {
        return leaveDocumentRepository.findByLeaveRequestId(leaveRequestId);
    }

    /**
     * Get leave documents by file type
     */
    @Transactional(readOnly = true)
    public List<LeaveDocument> getLeaveDocumentsByFileType(String fileType) {
        return leaveDocumentRepository.findByFileType(fileType);
    }

    /**
     * Upload and save a leave document
     */
    public LeaveDocument uploadLeaveDocument(Long leaveRequestId, MultipartFile file, 
                                           String description, Long uploadedBy) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (!isValidFileType(contentType)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: PDF, DOC, DOCX, JPG, JPEG, PNG");
        }

        // Get leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + leaveRequestId));

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document entity
        LeaveDocument document = new LeaveDocument();
        document.setLeaveRequest(leaveRequest);
        document.setDocumentName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setFileType(contentType);
        document.setFileSize(file.getSize());
        document.setDescription(description);
        document.setUploadedBy(uploadedBy);

        return leaveDocumentRepository.save(document);
    }

    /**
     * Save a leave document (for programmatic creation)
     */
    public LeaveDocument saveLeaveDocument(LeaveDocument leaveDocument) {
        return leaveDocumentRepository.save(leaveDocument);
    }

    /**
     * Update leave document metadata
     */
    public LeaveDocument updateLeaveDocument(Long id, String documentName, String description) {
        LeaveDocument document = leaveDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave document not found with id: " + id));

        if (documentName != null && !documentName.trim().isEmpty()) {
            document.setDocumentName(documentName);
        }
        
        if (description != null) {
            document.setDescription(description);
        }

        return leaveDocumentRepository.save(document);
    }

    /**
     * Delete a leave document
     */
    public void deleteLeaveDocument(Long id) {
        LeaveDocument document = leaveDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave document not found with id: " + id));

        // Delete file from disk
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't fail the deletion
            System.err.println("Failed to delete file: " + document.getFilePath() + " - " + e.getMessage());
        }

        // Delete from database
        leaveDocumentRepository.deleteById(id);
    }

    /**
     * Get file content for download
     */
    public byte[] getFileContent(Long id) throws IOException {
        LeaveDocument document = leaveDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave document not found with id: " + id));

        Path filePath = Paths.get(document.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found on disk: " + document.getFilePath());
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Check if file exists on disk
     */
    @Transactional(readOnly = true)
    public boolean fileExists(Long id) {
        Optional<LeaveDocument> documentOpt = leaveDocumentRepository.findById(id);
        if (documentOpt.isEmpty()) {
            return false;
        }

        Path filePath = Paths.get(documentOpt.get().getFilePath());
        return Files.exists(filePath);
    }

    /**
     * Get total file size for a leave request
     */
    @Transactional(readOnly = true)
    public Long getTotalFileSizeForLeaveRequest(Long leaveRequestId) {
        List<LeaveDocument> documents = getLeaveDocumentsByLeaveRequestId(leaveRequestId);
        return documents.stream()
                .mapToLong(doc -> doc.getFileSize() != null ? doc.getFileSize() : 0L)
                .sum();
    }

    /**
     * Get documents uploaded by a specific user
     */
    @Transactional(readOnly = true)
    public List<LeaveDocument> getDocumentsByUploadedBy(Long uploadedBy) {
        return leaveDocumentRepository.findByUploadedBy(uploadedBy);
    }

    /**
     * Get documents uploaded within a date range
     */
    @Transactional(readOnly = true)
    public List<LeaveDocument> getDocumentsUploadedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return leaveDocumentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Validate if file type is allowed
     */
    private boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("text/plain");
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * Get document statistics
     */
    @Transactional(readOnly = true)
    public LeaveDocumentStatistics getLeaveDocumentStatistics() {
        long totalDocuments = leaveDocumentRepository.count();
        
        List<LeaveDocument> allDocuments = leaveDocumentRepository.findAll();
        long totalSize = allDocuments.stream()
                .mapToLong(doc -> doc.getFileSize() != null ? doc.getFileSize() : 0L)
                .sum();
        
        // Count by file type
        long pdfCount = allDocuments.stream()
                .filter(doc -> "application/pdf".equals(doc.getFileType()))
                .count();
        
        long imageCount = allDocuments.stream()
                .filter(doc -> doc.getFileType() != null && doc.getFileType().startsWith("image/"))
                .count();
        
        long documentCount = allDocuments.stream()
                .filter(doc -> doc.getFileType() != null && 
                             (doc.getFileType().contains("word") || doc.getFileType().contains("document")))
                .count();

        return new LeaveDocumentStatistics(totalDocuments, totalSize, pdfCount, imageCount, documentCount);
    }

    /**
     * Clean up orphaned files (files on disk without database records)
     */
    public void cleanupOrphanedFiles() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            return;
        }

        List<String> dbFilePaths = leaveDocumentRepository.findAll()
                .stream()
                .map(LeaveDocument::getFilePath)
                .toList();

        Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String filePath = file.toString();
                    if (!dbFilePaths.contains(filePath)) {
                        try {
                            Files.delete(file);
                            System.out.println("Deleted orphaned file: " + filePath);
                        } catch (IOException e) {
                            System.err.println("Failed to delete orphaned file: " + filePath + " - " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * Inner class for document statistics
     */
    public static class LeaveDocumentStatistics {
        private final long totalDocuments;
        private final long totalSizeBytes;
        private final long pdfCount;
        private final long imageCount;
        private final long documentCount;

        public LeaveDocumentStatistics(long totalDocuments, long totalSizeBytes, 
                                     long pdfCount, long imageCount, long documentCount) {
            this.totalDocuments = totalDocuments;
            this.totalSizeBytes = totalSizeBytes;
            this.pdfCount = pdfCount;
            this.imageCount = imageCount;
            this.documentCount = documentCount;
        }

        // Getters
        public long getTotalDocuments() { return totalDocuments; }
        public long getTotalSizeBytes() { return totalSizeBytes; }
        public double getTotalSizeMB() { return totalSizeBytes / (1024.0 * 1024.0); }
        public long getPdfCount() { return pdfCount; }
        public long getImageCount() { return imageCount; }
        public long getDocumentCount() { return documentCount; }
    }
}