package com.example.employee_api.controller;

import com.example.employee_api.dto.notification.NotificationRequest;
import com.example.employee_api.model.Notification;
import com.example.employee_api.model.NotificationTemplate;
import com.example.employee_api.model.User;
import com.example.employee_api.model.enums.NotificationStatus;
import com.example.employee_api.model.enums.NotificationType;
import com.example.employee_api.model.enums.Priority;
import com.example.employee_api.repository.UserRepository;
import com.example.employee_api.repository.notification.NotificationRepository;
import com.example.employee_api.repository.notification.NotificationTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    private User testUser;
    private User adminUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        notificationTemplateRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("password");
        adminUser.setEnabled(true);
        adminUser = userRepository.save(adminUser);

        // Create test notification template
        NotificationTemplate template = new NotificationTemplate();
        template.setName("LEAVE_REQUEST");
        template.setSubjectTemplate("Leave Request Notification");
        template.setMessageTemplate("Hello {{employeeName}}, your leave request has been processed.");
        template.setType(NotificationType.LEAVE_REQUEST);
        template.setIsActive(true);
        notificationTemplateRepository.save(template);

        // Create test notification
        testNotification = new Notification();
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("This is a test notification message");
        testNotification.setType(NotificationType.INFO);
        testNotification.setPriority(Priority.MEDIUM);
        testNotification.setStatus(NotificationStatus.UNREAD);
        testNotification.setRecipient(testUser);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification = notificationRepository.save(testNotification);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetUserNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Test Notification"))
                .andExpect(jsonPath("$.content[0].status").value("UNREAD"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetNotificationsByStatus() throws Exception {
        mockMvc.perform(get("/api/notifications/by-status")
                .param("status", "UNREAD")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("UNREAD"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateNotification() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("New Notification");
        request.setMessage("New notification message");
        request.setType(NotificationType.INFO);
        request.setPriority(Priority.HIGH);
        request.setRecipientId(testUser.getId());

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Notification"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateNotificationFromTemplate() throws Exception {
        mockMvc.perform(post("/api/notifications/from-template")
                .param("templateName", "LEAVE_REQUEST")
                .param("recipientId", testUser.getId().toString())
                .param("variables", "John Doe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testMarkAsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/{id}/read", testNotification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testMarkAllAsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/mark-all-read"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteNotification() throws Exception {
        mockMvc.perform(delete("/api/notifications/{id}", testNotification.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetUnreadCount() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testSearchNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/search")
                .param("query", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Test Notification"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAccessDeniedForAdminEndpoints() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Unauthorized");
        request.setMessage("This should not work");
        request.setType(NotificationType.INFO);
        request.setPriority(Priority.MEDIUM);
        request.setRecipientId(testUser.getId());

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}