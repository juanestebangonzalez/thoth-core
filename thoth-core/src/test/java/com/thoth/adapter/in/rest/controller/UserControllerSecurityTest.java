package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security tests for UserController.
 * All endpoints require ADMIN role (class-level @PreAuthorize).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String FAKE_UUID = "00000000-0000-0000-0000-000000000001";

    @Test
    void listAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void changeRole_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + FAKE_UUID + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"VIEWER\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void toggleEnabled_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + FAKE_UUID + "/toggle-enabled"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPassword_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + FAKE_UUID + "/reset-password"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void changeEmail_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + FAKE_UUID + "/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + FAKE_UUID))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void listAll_viewer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void changeRole_technician_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + FAKE_UUID + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"VIEWER\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void toggleEnabled_user_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + FAKE_UUID + "/toggle-enabled"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void resetPassword_viewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + FAKE_UUID + "/reset-password"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void deleteUser_technician_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + FAKE_UUID))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAll_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk());
    }
}
