package com.example.employee_api.controller;

import com.example.employee_api.model.Permission;
import com.example.employee_api.model.Role;
import com.example.employee_api.repository.PermissionRepository;
import com.example.employee_api.repository.RoleRepository;
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

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create test permission
        testPermission = new Permission();
        testPermission.setName("TEST_PERMISSION");
        testPermission.setResource("TEST_RESOURCE");
        testPermission.setAction("READ");
        testPermission.setDescription("Test permission for integration testing");
        testPermission = permissionRepository.save(testPermission);

        // Create test role
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setDescription("Test role for integration testing");
        testRole.setSystemRole(false);
        testRole.setPermissions(Set.of(testPermission));
        testRole = roleRepository.save(testRole);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllRoles() throws Exception {
        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("TEST_ROLE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRoleById() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", testRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST_ROLE"))
                .andExpect(jsonPath("$.description").value("Test role for integration testing"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRoleById_NotFound() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateRole() throws Exception {
        Role newRole = new Role();
        newRole.setName("NEW_ROLE");
        newRole.setDescription("New test role");
        newRole.setSystemRole(false);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NEW_ROLE"))
                .andExpect(jsonPath("$.description").value("New test role"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateRole_DuplicateName() throws Exception {
        Role duplicateRole = new Role();
        duplicateRole.setName("TEST_ROLE"); // Same as existing
        duplicateRole.setDescription("Another test role");

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRole)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateRole() throws Exception {
        testRole.setDescription("Updated description");

        mockMvc.perform(put("/api/roles/{id}", testRole.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateRole_NotFound() throws Exception {
        Role nonExistentRole = new Role();
        nonExistentRole.setId(999L);
        nonExistentRole.setName("NON_EXISTENT");
        nonExistentRole.setDescription("Non-existent role");

        mockMvc.perform(put("/api/roles/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteRole() throws Exception {
        mockMvc.perform(delete("/api/roles/{id}", testRole.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteRole_NotFound() throws Exception {
        mockMvc.perform(delete("/api/roles/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAssignPermissionToRole() throws Exception {
        // Create another permission to assign
        Permission newPermission = new Permission();
        newPermission.setName("ANOTHER_PERMISSION");
        newPermission.setResource("ANOTHER_RESOURCE");
        newPermission.setAction("WRITE");
        newPermission.setDescription("Another test permission");
        newPermission = permissionRepository.save(newPermission);

        mockMvc.perform(post("/api/roles/{roleId}/permissions/{permissionId}", 
                testRole.getId(), newPermission.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRemovePermissionFromRole() throws Exception {
        mockMvc.perform(delete("/api/roles/{roleId}/permissions/{permissionId}", 
                testRole.getId(), testPermission.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRolePermissions() throws Exception {
        mockMvc.perform(get("/api/roles/{id}/permissions", testRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("TEST_PERMISSION"));
    }
}