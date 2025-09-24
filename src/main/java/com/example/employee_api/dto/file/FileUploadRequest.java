package com.example.employee_api.dto.file;

import com.example.employee_api.model.enums.FileType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for file upload requests
 */
public class FileUploadRequest {

    @NotNull(message = "File type is required")
    private FileType fileType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 1000, message = "Tags must not exceed 1000 characters")
    private String tags;

    private Boolean isPublic = false;

    private Long employeeId;

    // Constructors
    public FileUploadRequest() {}

    public FileUploadRequest(FileType fileType, String description) {
        this.fileType = fileType;
        this.description = description;
    }

    // Getters and Setters
    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
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

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}