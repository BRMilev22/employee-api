package com.example.employee_api.controller;

import com.example.employee_api.dto.notification.NotificationTemplateRequest;
import com.example.employee_api.model.NotificationTemplate;
import com.example.employee_api.model.User;
import com.example.employee_api.model.enums.NotificationType;
import com.example.employee_api.repository.UserRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationTemplateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    private NotificationTemplate testTemplate;

    @BeforeEach
    void setUp() {
        notificationTemplateRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create admin user for the tests (required by getCurrentUser() in service)
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("password");
        adminUser.setEnabled(true);
        userRepository.save(adminUser);
        
        // Create test template
        testTemplate = new NotificationTemplate();
        testTemplate.setName("TEST_TEMPLATE");
        testTemplate.setSubjectTemplate("Test Template Subject");
        testTemplate.setMessageTemplate("Hello, this is a test template with {{variable}}.");
        testTemplate.setType(NotificationType.INFO);
        testTemplate.setIsActive(true);
        testTemplate = notificationTemplateRepository.save(testTemplate);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllTemplates() throws Exception {
        mockMvc.perform(get("/api/notifications/templates")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("TEST_TEMPLATE"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetTemplateById() throws Exception {
        mockMvc.perform(get("/api/notifications/templates/{id}", testTemplate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST_TEMPLATE"))
                .andExpect(jsonPath("$.subjectTemplate").value("Test Template Subject"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateTemplate() throws Exception {
        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setName("NEW_TEMPLATE");
        request.setSubjectTemplate("New Template");
        request.setMessageTemplate("This is a new template with {{name}}.");
        request.setType(NotificationType.ALERT);

        mockMvc.perform(post("/api/notifications/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // This will help us see what's actually happening
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NEW_TEMPLATE"))
                .andExpect(jsonPath("$.subjectTemplate").value("New Template"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateTemplate() throws Exception {
        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setName("UPDATED_TEMPLATE");
        request.setSubjectTemplate("Updated Template Subject");
        request.setMessageTemplate("Updated template content with {{data}}.");
        request.setType(NotificationType.REMINDER);

        mockMvc.perform(put("/api/notifications/templates/{id}", testTemplate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // This will help us see what's actually happening
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UPDATED_TEMPLATE"))
                .andExpect(jsonPath("$.subjectTemplate").value("Updated Template Subject"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteTemplate() throws Exception {
        mockMvc.perform(delete("/api/notifications/templates/{id}", testTemplate.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetTemplateById_NotFound() throws Exception {
        mockMvc.perform(get("/api/notifications/templates/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAccessDeniedForUserRole() throws Exception {
        mockMvc.perform(get("/api/notifications/templates"))
                .andExpect(status().isForbidden());
    }
}