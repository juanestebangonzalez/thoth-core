package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-003: /api/v1/permissions/** only required authenticated(),
 * letting any authenticated user (e.g. VIEWER) read or overwrite anyone's granular
 * permissions, including their own, to escalate privileges.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUserPermissions_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getUserPermissions_withViewerRole_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/" + UUID.randomUUID()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserPermissions_withAdminRole_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/" + UUID.randomUUID()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void setUserPermissions_withViewerRole_cannotSelfEscalate_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/permissions/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"USERS\":[\"VIEW\",\"CREATE\",\"EDIT\",\"DELETE\"]}"))
            .andExpect(status().isForbidden());
    }
}
