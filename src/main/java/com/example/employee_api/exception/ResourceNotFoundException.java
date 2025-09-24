package com.example.employee_api.exception;

/**
 * Generic exception for when a requested resource is not found.
 * This will result in a 404 HTTP status code.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}