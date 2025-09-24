package com.example.employee_api.controller;

import com.example.employee_api.dto.auth.*;
import com.example.employee_api.model.User;
import com.example.employee_api.service.AuthService;
import com.example.employee_api.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private AuditLogService auditLogService;

    /**
     * Authenticate user
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            
            // Log successful login
            auditLogService.logAuthenticationEvent(
                loginRequest.getUsernameOrEmail(),
                AuditLogService.ACTION_LOGIN,
                true,
                null,
                ipAddress
            );
            
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            // Log failed login
            auditLogService.logAuthenticationEvent(
                loginRequest.getUsernameOrEmail(),
                AuditLogService.ACTION_LOGIN_FAILED,
                false,
                e.getMessage(),
                ipAddress
            );
            
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Authentication failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            User user = authService.registerUser(signUpRequest);
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully. Please check your email to verify your account.",
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Registration failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            TokenRefreshResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Token refresh failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String refreshToken = requestBody.get("refreshToken");
            
            // Get current user for logging
            User currentUser = authService.getCurrentUser();
            String username = currentUser != null ? currentUser.getUsername() : "UNKNOWN";
            
            // Logout the user and revoke refresh token
            authService.logoutUser(refreshToken);
            
            // Log logout event
            auditLogService.logAuthenticationEvent(
                username,
                AuditLogService.ACTION_LOGOUT,
                true,
                null,
                ipAddress
            );
            
            return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Logout failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Verify email address
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            boolean isVerified = authService.verifyEmail(token);
            if (isVerified) {
                return ResponseEntity.ok(Map.of(
                        "message", "Email verified successfully",
                        "verified", true
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Email verification failed",
                                "message", "Invalid or expired verification token"
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Email verification failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            authService.requestPasswordReset(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Password reset request failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Password reset failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Change password for authenticated user
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            authService.changePassword(currentPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Password change failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Resend email verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Verification email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Resend verification failed",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "emailVerified", user.getEmailVerified(),
                        "enabled", user.isEnabled(),
                        "createdAt", user.getCreatedAt(),
                        "lastLoginAt", user.getLastLoginAt()
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Failed to get user info",
                            "message", e.getMessage()
                    ));
        }
    }
    
    /**
     * Helper method to get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }
        
        return request.getRemoteAddr();
    }
}