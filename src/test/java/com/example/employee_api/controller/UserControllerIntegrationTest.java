package com.example.employee_api.controller;

import com.example.employee_api.dto.UserRolesUpdateRequest;
import com.example.employee_api.dto.UserUpdateRequest;
import com.example.employee_api.model.Permission;
import com.example.employee_api.model.Role;
import com.example.employee_api.model.User;
import com.example.employee_api.repository.PermissionRepository;
import com.example.employee_api.repository.RoleRepository;
import com.example.employee_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Role adminRole;
    private Role userRole;
    private Permission readPermission;
    private Permission writePermission;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create permissions
        readPermission = new Permission();
        readPermission.setName("READ");
        readPermission.setDescription("Read permission");
        readPermission.setResource("USER");
        readPermission.setAction("READ");
        readPermission = permissionRepository.save(readPermission);

        writePermission = new Permission();
        writePermission.setName("WRITE");
        writePermission.setDescription("Write permission");
        writePermission.setResource("USER");
        writePermission.setAction("WRITE");
        writePermission = permissionRepository.save(writePermission);

        // Create roles
        adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role");
        adminRole.setPermissions(new HashSet<>(Set.of(readPermission, writePermission)));
        adminRole = roleRepository.save(adminRole);

        userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("User role");
        userRole.setPermissions(new HashSet<>(Set.of(readPermission)));
        userRole = roleRepository.save(userRole);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser.setEmailVerified(true);
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnPagedResponse() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "username")
                .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username", is("testuser")))
                .andExpect(jsonPath("$.content[0].email", is("test@example.com")))
                .andExpect(jsonPath("$.content[0].firstName", is("Test")))
                .andExpect(jsonPath("$.content[0].lastName", is("User")))
                .andExpect(jsonPath("$.content[0].enabled", is(true)))
                .andExpect(jsonPath("$.content[0].emailVerified", is(true)))
                .andExpect(jsonPath("$.content[0].roles", hasItem("USER")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.roles", hasItem("USER")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturn404_WhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldUpdateUser_WhenValidRequest() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
                "Updated",
                "Name",
                "updated@example.com"
        );

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Name")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldReturn400_WhenInvalidEmail() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
                "Updated",
                "Name",
                "invalid-email"
        );

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldReturn400_WhenEmailAlreadyTaken() throws Exception {
        // Create another user with a different email
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setEnabled(true);
        anotherUser.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(anotherUser);

        UserUpdateRequest request = new UserUpdateRequest(
                "Updated",
                "Name",
                "another@example.com" // Try to use existing email
        );

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldSoftDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user is disabled, not deleted
        User deletedUser = userRepository.findById(testUser.getId()).orElse(null);
        assert deletedUser != null;
        assert !deletedUser.getEnabled();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserRoles_ShouldReturnUserRoles() throws Exception {
        mockMvc.perform(get("/api/users/{id}/roles", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$", hasItem("USER")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_ShouldUpdateRoles_WhenValidRequest() throws Exception {
        UserRolesUpdateRequest request = new UserRolesUpdateRequest(
                new HashSet<>(Set.of(adminRole.getId(), userRole.getId()))
        );

        mockMvc.perform(put("/api/users/{id}/roles", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", hasItems("ADMIN", "USER")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_ShouldReturn400_WhenInvalidRoleId() throws Exception {
        UserRolesUpdateRequest request = new UserRolesUpdateRequest(
                new HashSet<>(Set.of(999L)) // Invalid role ID
        );

        mockMvc.perform(put("/api/users/{id}/roles", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_ShouldActivateUser() throws Exception {
        // First disable the user
        testUser.setEnabled(false);
        testUser.setAccountNonLocked(false);
        testUser.setFailedLoginAttempts(3);
        userRepository.save(testUser);

        mockMvc.perform(post("/api/users/{id}/activate", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)));

        // Verify user is activated
        User activatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assert activatedUser != null;
        assert activatedUser.getEnabled();
        assert activatedUser.getAccountNonLocked();
        assert activatedUser.getFailedLoginAttempts() == 0;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_ShouldDeactivateUser() throws Exception {
        mockMvc.perform(post("/api/users/{id}/deactivate", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));

        // Verify user is deactivated
        User deactivatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assert deactivatedUser != null;
        assert !deactivatedUser.getEnabled();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_ShouldReturnFilteredResults_WhenUsernameFilter() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("username", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username", is("testuser")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_ShouldReturnFilteredResults_WhenEmailFilter() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("email", "test@")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is("test@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_ShouldReturnFilteredResults_WhenEnabledFilter() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("enabled", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].enabled", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_ShouldReturnFilteredResults_WhenRoleFilter() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("role", "USER")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].roles", hasItem("USER")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_ShouldReturnEmptyResults_WhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("username", "nonexistent")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAllUsers_ShouldReturn403_WhenInsufficientPermissions() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}