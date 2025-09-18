package com.example.employee_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Public endpoints for testing and health checks
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {

    /**
     * Test endpoint that doesn't require authentication
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "message", "Employee API is running"
        ));
    }

    /**
     * Test endpoint to check authentication status
     */
    @GetMapping("/auth-test")
    public ResponseEntity<?> authTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(Map.of(
            "authenticated", authentication != null && authentication.isAuthenticated(),
            "principal", authentication != null ? authentication.getName() : "anonymous",
            "authorities", authentication != null ? authentication.getAuthorities() : "none",
            "timestamp", LocalDateTime.now()
        ));
    }
}