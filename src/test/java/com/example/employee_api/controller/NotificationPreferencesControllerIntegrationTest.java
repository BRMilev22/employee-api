package com.example.employee_api.controller;

import com.example.employee_api.dto.notification.NotificationPreferencesRequest;
import com.example.employee_api.model.NotificationPreferences;
import com.example.employee_api.model.User;
import com.example.employee_api.model.enums.NotificationType;
import com.example.employee_api.repository.UserRepository;
import com.example.employee_api.repository.notification.NotificationPreferencesRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationPreferencesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationPreferencesRepository notificationPreferencesRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private NotificationPreferences testPreferences;

    @BeforeEach
    void setUp() {
        notificationPreferencesRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // Create test preferences
        testPreferences = new NotificationPreferences();
        testPreferences.setUser(testUser);
        testPreferences.setNotificationType(NotificationType.LEAVE_REQUEST);
        testPreferences.setEmailEnabled(true);
        testPreferences.setInAppEnabled(false);
        testPreferences = notificationPreferencesRepository.save(testPreferences);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetPreferences() throws Exception {
        mockMvc.perform(get("/api/notifications/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notificationType").value("LEAVE_REQUEST"))
                .andExpect(jsonPath("$[0].emailEnabled").value(true))
                .andExpect(jsonPath("$[0].inAppEnabled").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdatePreferences() throws Exception {
        NotificationPreferencesRequest request = new NotificationPreferencesRequest();
        request.setNotificationType(NotificationType.PERFORMANCE_REVIEW);
        request.setEmailEnabled(false);
        request.setInAppEnabled(true);

        mockMvc.perform(put("/api/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationType").value("PERFORMANCE_REVIEW"))
                .andExpect(jsonPath("$.emailEnabled").value(false))
                .andExpect(jsonPath("$.inAppEnabled").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetEmptyPreferences() throws Exception {
        // Clear existing preferences
        notificationPreferencesRepository.deleteAll();
        
        mockMvc.perform(get("/api/notifications/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}