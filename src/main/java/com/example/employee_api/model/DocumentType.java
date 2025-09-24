package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Entity representing document types (e.g., Contract, Certification, Policy, etc.)
 */
@Entity
@Table(name = "document_types", indexes = {
    @Index(name = "idx_document_type_name", columnList = "name", unique = true),
    @Index(name = "idx_document_type_active", columnList = "active")
})
public class DocumentType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Document type name is required")
    @Size(max = 100, message = "Document type name must not exceed 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 255, message = "Allowed file types must not exceed 255 characters")
    @Column(name = "allowed_file_types", length = 255)
    private String allowedFileTypes; // e.g., "pdf,doc,docx,jpg,png"

    @Column(name = "max_file_size_mb")
    private Integer maxFileSizeMb = 10; // Default 10MB

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    // Constructors
    public DocumentType() {}

    public DocumentType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public DocumentType(String name, String description, Boolean requiresApproval) {
        this.name = name;
        this.description = description;
        this.requiresApproval = requiresApproval;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(String allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public Integer getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(Integer maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    // Business methods
    public boolean isFileTypeAllowed(String fileExtension) {
        if (allowedFileTypes == null || allowedFileTypes.trim().isEmpty()) {
            return true; // No restrictions
        }
        return allowedFileTypes.toLowerCase().contains(fileExtension.toLowerCase());
    }

    public boolean isFileSizeAllowed(long fileSizeBytes) {
        if (maxFileSizeMb == null) {
            return true; // No restrictions
        }
        long maxSizeBytes = maxFileSizeMb * 1024L * 1024L;
        return fileSizeBytes <= maxSizeBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentType that = (DocumentType) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "DocumentType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", requiresApproval=" + requiresApproval +
                '}';
    }
}