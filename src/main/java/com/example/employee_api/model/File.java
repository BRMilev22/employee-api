package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import com.example.employee_api.model.enums.FileStatus;
import com.example.employee_api.model.enums.FileType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing files in the system (employee photos, documents, etc.)
 */
@Entity
@Table(name = "files", indexes = {
    @Index(name = "idx_file_status", columnList = "status"),
    @Index(name = "idx_file_type", columnList = "file_type"),
    @Index(name = "idx_file_employee", columnList = "employee_id"),
    @Index(name = "idx_file_created_at", columnList = "created_at"),
    @Index(name = "idx_file_original_name", columnList = "original_filename")
})
public class File extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank(message = "Filename is required")
    @Size(max = 255, message = "Filename must not exceed 255 characters")
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @NotNull
    @NotBlank(message = "Original filename is required")
    @Size(max = 255, message = "Original filename must not exceed 255 characters")
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @NotNull
    @NotBlank(message = "File path is required")
    @Size(max = 1000, message = "File path must not exceed 1000 characters")
    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @NotNull
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @NotNull
    @Min(value = 0, message = "File size cannot be negative")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private FileType fileType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FileStatus status = FileStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private Employee employee;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 1000, message = "Tags must not exceed 1000 characters")
    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "download_count")
    private Long downloadCount = 0L;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Size(max = 64, message = "Checksum must not exceed 64 characters")
    @Column(name = "checksum", length = 64)
    private String checksum;

    // Constructors
    public File() {}

    public File(String filename, String originalFilename, String filePath,
                String mimeType, Long fileSize, FileType fileType) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.status = FileStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    // Utility methods
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0L : this.downloadCount) + 1;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(id, file.id) &&
               Objects.equals(filename, file.filename) &&
               Objects.equals(filePath, file.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename, filePath);
    }

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", fileSize=" + fileSize +
                ", fileType=" + fileType +
                ", status=" + status +
                '}';
    }
}