package com.example.employee_api.service;

import com.example.employee_api.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails (basic implementation)
 * TODO: Implement with actual email provider (SendGrid, AWS SES, etc.)
 */
@Service
public class EmailService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send email verification email
     */
    public void sendVerificationEmail(User user) {
        // TODO: Implement actual email sending
        String verificationUrl = frontendUrl + "/verify-email?token=" + user.getEmailVerificationToken();
        
        System.out.println("=== EMAIL VERIFICATION ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Please verify your email address");
        System.out.println("Verification URL: " + verificationUrl);
        System.out.println("========================");
        
        // In production, replace with actual email sending logic:
        // emailProvider.send(user.getEmail(), "Please verify your email address", emailTemplate);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        // TODO: Implement actual email sending
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        
        System.out.println("=== PASSWORD RESET ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Password Reset Request");
        System.out.println("Reset URL: " + resetUrl);
        System.out.println("======================");
        
        // In production, replace with actual email sending logic:
        // emailProvider.send(user.getEmail(), "Password Reset Request", emailTemplate);
    }

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(User user) {
        // TODO: Implement actual email sending
        System.out.println("=== WELCOME EMAIL ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Welcome to Employee Management System");
        System.out.println("Welcome " + user.getFullName() + "!");
        System.out.println("====================");
        
        // In production, replace with actual email sending logic:
        // emailProvider.send(user.getEmail(), "Welcome to Employee Management System", emailTemplate);
    }

    /**
     * Send notification email
     */
    public void sendNotificationEmail(String toEmail, String subject, String content) {
        // TODO: Implement actual email sending
        System.out.println("=== NOTIFICATION EMAIL ===");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("========================");
        
        // In production, replace with actual email sending logic:
        // emailProvider.send(toEmail, subject, content);
    }
}