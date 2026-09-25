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
 * Security tests for MaintenanceCategoryController.
 * GET endpoints require authentication; POST/PUT/DELETE require ADMIN role.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceCategoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String FAKE_UUID = "00000000-0000-0000-0000-000000000001";

    @Test
    void listActive_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance-categories"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance-categories/all"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/maintenance-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Preventivo\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void update_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/maintenance-categories/" + FAKE_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Correctivo\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/maintenance-categories/" + FAKE_UUID))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void listActive_viewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance-categories"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void listAll_technician_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance-categories/all"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void create_viewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/maintenance-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Preventivo\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void update_technician_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/maintenance-categories/" + FAKE_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Correctivo\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_user_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/maintenance-categories/" + FAKE_UUID))
            .andExpect(status().isForbidden());
    }
}
