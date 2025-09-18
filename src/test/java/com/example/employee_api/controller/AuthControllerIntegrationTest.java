package com.example.employee_api.controller;

import com.example.employee_api.dto.auth.LoginRequest;
import com.example.employee_api.dto.auth.RegisterRequest;
import com.example.employee_api.model.Role;
import com.example.employee_api.model.User;
import com.example.employee_api.repository.RoleRepository;
import com.example.employee_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role employeeRole;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create default role
        employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        employeeRole.setDescription("Standard employee role");
        employeeRole = roleRepository.save(employeeRole);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully. Please check your email to verify your account."))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testRegisterUser_DuplicateUsername() throws Exception {
        // Create existing user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Doe");
        existingUser.setRoles(new HashSet<>(Set.of(employeeRole)));
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser"); // Duplicate username
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Smith");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        // Create existing user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Doe");
        existingUser.setRoles(new HashSet<>(Set.of(employeeRole)));
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com"); // Duplicate email
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Smith");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Email Address already in use!"));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Create test user with proper initialization
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setEmailVerified(true);
        // Initialize roles properly to avoid NullPointerException
        testUser.setRoles(new HashSet<>(Set.of(employeeRole)));
        userRepository.save(testUser);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("nonexistent");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void testLogin_DisabledUser() throws Exception {
        // Create disabled user
        User disabledUser = new User();
        disabledUser.setUsername("disableduser");
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setPassword(passwordEncoder.encode("password123"));
        disabledUser.setFirstName("Disabled");
        disabledUser.setLastName("User");
        disabledUser.setEnabled(false); // Disabled account
        disabledUser.setAccountNonExpired(true);
        disabledUser.setAccountNonLocked(true);
        disabledUser.setCredentialsNonExpired(true);
        disabledUser.setEmailVerified(true);
        disabledUser.setRoles(new HashSet<>(Set.of(employeeRole)));
        userRepository.save(disabledUser);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("disableduser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

    @Test
    void testRegisterUser_ValidationErrors() throws Exception {
        RegisterRequest request = new RegisterRequest();
        // Missing required fields - should trigger validation errors
        request.setUsername(""); // Invalid - too short
        request.setEmail("invalid-email"); // Invalid email format
        request.setPassword("123"); // Invalid - too short
        request.setFirstName("");
        request.setLastName("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}