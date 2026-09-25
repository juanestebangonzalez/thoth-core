package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security tests for AuditController.
 * GET endpoints require authentication; POST archive requires ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuditLog_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getArchived_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit/archive"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void archiveNow_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/audit/archive"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getStats_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit/stats"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getAuditLog_viewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getArchived_viewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit/archive"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void archiveNow_viewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/audit/archive"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void archiveNow_technician_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/audit/archive"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getStats_viewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit/stats"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void archiveNow_admin_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/audit/archive"))
            .andExpect(status().isOk());
    }
}
