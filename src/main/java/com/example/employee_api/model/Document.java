package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.DocumentApprovalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing documents in the system (employee documents, contracts, certifications, etc.)
 */
@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_employee", columnList = "employee_id"),
    @Index(name = "idx_document_type", columnList = "document_type_id"),
    @Index(name = "idx_document_category", columnList = "document_category_id"),
    @Index(name = "idx_document_status", columnList = "approval_status"),
    @Index(name = "idx_document_expiry", columnList = "expiry_date"),
    @Index(name = "idx_document_active", columnList = "active")
})
public class Document extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_type_id", nullable = false)
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_category_id")
    private DocumentCategory documentCategory;

    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name must not exceed 255 characters")
    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path must not exceed 500 characters")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Size(max = 50, message = "File type must not exceed 50 characters")
    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "is_confidential", nullable = false)
    private Boolean isConfidential = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private DocumentApprovalStatus approvalStatus = DocumentApprovalStatus.PENDING;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Size(max = 500, message = "Approval notes must not exceed 500 characters")
    @Column(name = "approval_notes", length = 500)
    private String approvalNotes;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated tags for searching

    // Constructors
    public Document() {}

    public Document(Employee employee, DocumentType documentType, String documentName, String filePath) {
        this.employee = employee;
        this.documentType = documentType;
        this.documentName = documentName;
        this.filePath = filePath;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public DocumentCategory getDocumentCategory() {
        return documentCategory;
    }

    public void setDocumentCategory(DocumentCategory documentCategory) {
        this.documentCategory = documentCategory;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getIsConfidential() {
        return isConfidential;
    }

    public void setIsConfidential(Boolean isConfidential) {
        this.isConfidential = isConfidential;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public DocumentApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(DocumentApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Business methods
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int days) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(days));
    }

    public boolean isApproved() {
        return approvalStatus == DocumentApprovalStatus.APPROVED;
    }

    public boolean isPending() {
        return approvalStatus == DocumentApprovalStatus.PENDING;
    }

    public boolean isRejected() {
        return approvalStatus == DocumentApprovalStatus.REJECTED;
    }

    public boolean requiresApproval() {
        return documentType != null && documentType.getRequiresApproval();
    }

    public void approve(Long approvedByUserId, String notes) {
        this.approvalStatus = DocumentApprovalStatus.APPROVED;
        this.approvedBy = approvedByUserId;
        this.approvedAt = LocalDateTime.now();
        this.approvalNotes = notes;
    }

    public void reject(Long rejectedByUserId, String notes) {
        this.approvalStatus = DocumentApprovalStatus.REJECTED;
        this.approvedBy = rejectedByUserId;
        this.approvedAt = LocalDateTime.now();
        this.approvalNotes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", documentName='" + documentName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", version=" + version +
                ", approvalStatus=" + approvalStatus +
                ", active=" + active +
                '}';
    }
}