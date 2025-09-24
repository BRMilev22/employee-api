package com.example.employee_api.service;

import com.example.employee_api.dto.file.FileMetadataResponse;
import com.example.employee_api.dto.file.FileResponse;
import com.example.employee_api.dto.file.FileUploadRequest;
import com.example.employee_api.dto.response.PagedResponse;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.File;
import com.example.employee_api.model.enums.FileStatus;
import com.example.employee_api.model.enums.FileType;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final EmployeeRepository employeeRepository;
    private Path fileStorageLocation;

    // Configurable properties
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${app.file.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,txt}")
    private String allowedExtensions;

    @Autowired
    public FileService(FileRepository fileRepository, EmployeeRepository employeeRepository) {
        this.fileRepository = fileRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        createDirectoriesIfNotExist();
    }

    private void createDirectoriesIfNotExist() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    // File upload
    public FileResponse uploadFile(MultipartFile file, FileUploadRequest request) {
        validateFile(file);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String fileExtension = getFileExtension(file.getOriginalFilename());

        validateFileExtension(fileExtension);

        try {
            // Create file path
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Calculate checksum
            String checksum = calculateChecksum(file.getBytes());

            // Check for duplicates
            if (request.getEmployeeId() != null) {
                List<File> duplicates = fileRepository.findByEmployeeIdAndChecksum(request.getEmployeeId(), checksum);
                if (!duplicates.isEmpty()) {
                    // Delete the newly uploaded file since it's a duplicate
                    Files.deleteIfExists(targetLocation);
                    throw new RuntimeException("Duplicate file detected. File already exists.");
                }
            }

            // Create File entity
            File fileEntity = new File();
            fileEntity.setFilename(fileName);
            fileEntity.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));
            fileEntity.setFilePath(targetLocation.toString());
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setFileType(request.getFileType());
            fileEntity.setStatus(FileStatus.ACTIVE);
            fileEntity.setDescription(request.getDescription());
            fileEntity.setTags(request.getTags());
            fileEntity.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
            fileEntity.setChecksum(checksum);

            // Associate with employee if provided
            if (request.getEmployeeId() != null) {
                Employee employee = employeeRepository.findById(request.getEmployeeId())
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmployeeId()));
                fileEntity.setEmployee(employee);

                // If this is an employee photo, deactivate the old one
                if (request.getFileType() == FileType.EMPLOYEE_PHOTO) {
                    Optional<File> existingPhoto = fileRepository.findActiveEmployeePhoto(request.getEmployeeId());
                    if (existingPhoto.isPresent()) {
                        existingPhoto.get().setStatus(FileStatus.ARCHIVED);
                        fileRepository.save(existingPhoto.get());
                    }
                    // Update employee profile picture URL
                    employee.setProfilePictureUrl("/api/files/" + fileEntity.getId());
                    employeeRepository.save(employee);
                }
            }

            File savedFile = fileRepository.save(fileEntity);
            return convertToFileResponse(savedFile);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    // File download
    public Resource downloadFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        if (file.getStatus() != FileStatus.ACTIVE) {
            throw new RuntimeException("File is not available for download");
        }

        if (file.isExpired()) {
            throw new RuntimeException("File has expired");
        }

        try {
            Path filePath = Paths.get(file.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                // Update download count and last accessed time
                fileRepository.incrementDownloadCount(fileId, LocalDateTime.now());
                return resource;
            } else {
                throw new RuntimeException("File not found on filesystem: " + file.getFilename());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + file.getFilename(), ex);
        }
    }

    // Get file by ID
    @Transactional(readOnly = true)
    public FileResponse getFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
        return convertToFileResponse(file);
    }

    // Get file metadata
    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
        return convertToFileMetadataResponse(file);
    }

    // Get all files with pagination
    @Transactional(readOnly = true)
    public PagedResponse<FileResponse> getAllFiles(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<File> files = fileRepository.findByStatus(FileStatus.ACTIVE, pageable);
        List<FileResponse> fileResponses = files.getContent().stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(files, sortBy, sortDir, fileResponses);
    }

    // Get files by employee
    @Transactional(readOnly = true)
    public List<FileResponse> getEmployeeFiles(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        List<File> files = fileRepository.findByEmployeeIdAndStatus(employeeId, FileStatus.ACTIVE);
        return files.stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toList());
    }

    // Get employee photo
    @Transactional(readOnly = true)
    public Optional<FileResponse> getEmployeePhoto(Long employeeId) {
        Optional<File> photo = fileRepository.findActiveEmployeePhoto(employeeId);
        return photo.map(this::convertToFileResponse);
    }

    // Upload employee photo
    public FileResponse uploadEmployeePhoto(Long employeeId, MultipartFile file) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        validateImageFile(file);

        FileUploadRequest request = new FileUploadRequest();
        request.setFileType(FileType.EMPLOYEE_PHOTO);
        request.setEmployeeId(employeeId);
        request.setDescription("Employee profile photo");
        request.setIsPublic(false);

        return uploadFile(file, request);
    }

    // Delete employee photo
    public void deleteEmployeePhoto(Long employeeId) {
        Optional<File> photo = fileRepository.findActiveEmployeePhoto(employeeId);
        if (photo.isPresent()) {
            photo.get().setStatus(FileStatus.DELETED);
            fileRepository.save(photo.get());

            // Update employee profile picture URL
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
            employee.setProfilePictureUrl(null);
            employeeRepository.save(employee);
        }
    }

    // Delete file
    public void deleteFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        file.setStatus(FileStatus.DELETED);
        fileRepository.save(file);

        // If it's an employee photo, update the employee entity
        if (file.getFileType() == FileType.EMPLOYEE_PHOTO && file.getEmployee() != null) {
            Employee employee = file.getEmployee();
            employee.setProfilePictureUrl(null);
            employeeRepository.save(employee);
        }
    }

    // Search files
    @Transactional(readOnly = true)
    public PagedResponse<FileResponse> searchFiles(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<File> files = fileRepository.searchFiles(query, pageable);

        List<FileResponse> fileResponses = files.getContent().stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(files, "createdAt", "desc", fileResponses);
    }

    // Get file statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalFiles", fileRepository.getTotalActiveFileCount());
        stats.put("totalSize", fileRepository.getTotalActiveFileSize());

        List<Object[]> typeStats = fileRepository.getFileStatisticsByType();
        Map<String, Object> fileTypeStats = new HashMap<>();
        for (Object[] row : typeStats) {
            FileType type = (FileType) row[0];
            Long count = (Long) row[1];
            Long size = (Long) row[2];

            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("count", count);
            typeStat.put("size", size);
            fileTypeStats.put(type.name(), typeStat);
        }
        stats.put("byType", fileTypeStats);

        return stats;
    }

    // Get employee file statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeFileStatistics(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", fileRepository.getFileCountByEmployee(employeeId));
        stats.put("totalSize", fileRepository.getTotalFileSizeByEmployee(employeeId));

        return stats;
    }

    // Bulk upload
    public List<FileResponse> bulkUpload(List<MultipartFile> files, FileUploadRequest baseRequest) {
        List<FileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileResponse response = uploadFile(file, baseRequest);
                responses.add(response);
            } catch (Exception e) {
                // Log error but continue with other files
                FileResponse errorResponse = new FileResponse();
                errorResponse.setOriginalFilename(file.getOriginalFilename());
                // You might want to add an error field to FileResponse
                responses.add(errorResponse);
            }
        }

        return responses;
    }

    // Cleanup expired files
    @Transactional
    public int cleanupExpiredFiles() {
        return fileRepository.markExpiredFiles(LocalDateTime.now());
    }

    // Validation methods
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of " +
                                     (maxFileSize / 1024 / 1024) + "MB");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new RuntimeException("Filename contains invalid path sequence: " + fileName);
        }
    }

    private void validateImageFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
    }

    private void validateFileExtension(String extension) {
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            List<String> allowed = Arrays.asList(allowedExtensions.toLowerCase().split(","));
            if (!allowed.contains(extension.toLowerCase())) {
                throw new RuntimeException("File extension '" + extension + "' is not allowed. " +
                                         "Allowed extensions: " + allowedExtensions);
            }
        }
    }

    // Utility methods
    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String baseName = getBaseName(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return baseName + "_" + timestamp + "_" + uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String getBaseName(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return filename != null ? filename : "file";
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            BigInteger number = new BigInteger(1, hash);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating file checksum", e);
        }
    }

    // Conversion methods
    private FileResponse convertToFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId());
        response.setFilename(file.getFilename());
        response.setOriginalFilename(file.getOriginalFilename());
        response.setMimeType(file.getMimeType());
        response.setFileSize(file.getFileSize());
        response.setFormattedFileSize(file.getFormattedFileSize());
        response.setFileType(file.getFileType());
        response.setStatus(file.getStatus());
        response.setDescription(file.getDescription());
        response.setTags(file.getTags());
        response.setIsPublic(file.getIsPublic());
        response.setDownloadCount(file.getDownloadCount());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        response.setLastAccessedAt(file.getLastAccessedAt());
        response.setExpiresAt(file.getExpiresAt());
        response.setCreatedBy(file.getCreatedBy());
        response.setUpdatedBy(file.getUpdatedBy());

        if (file.getEmployee() != null) {
            response.setEmployeeId(file.getEmployee().getId());
            response.setEmployeeName(file.getEmployee().getFirstName() + " " + file.getEmployee().getLastName());
        }

        return response;
    }

    private FileMetadataResponse convertToFileMetadataResponse(File file) {
        FileMetadataResponse response = new FileMetadataResponse();
        response.setId(file.getId());
        response.setOriginalFilename(file.getOriginalFilename());
        response.setMimeType(file.getMimeType());
        response.setFileSize(file.getFileSize());
        response.setFormattedFileSize(file.getFormattedFileSize());
        response.setFileType(file.getFileType());
        response.setStatus(file.getStatus());
        response.setDescription(file.getDescription());
        response.setIsPublic(file.getIsPublic());
        response.setDownloadCount(file.getDownloadCount());
        response.setCreatedAt(file.getCreatedAt());
        response.setLastAccessedAt(file.getLastAccessedAt());
        response.setExpiresAt(file.getExpiresAt());
        response.setExpired(file.isExpired());

        return response;
    }
}