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
 * Security tests for LocationHistoryController.
 * GET requires authentication; POST transfer requires ADMIN or TECHNICIAN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationHistoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String FAKE_UUID = "00000000-0000-0000-0000-000000000001";

    @Test
    void getHistory_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/location-history/equipment/" + FAKE_UUID))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void transfer_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/location-history/equipment/" + FAKE_UUID + "/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toBuilding\":\"Sede Norte\",\"reason\":\"Traslado\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getHistory_viewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/location-history/equipment/" + FAKE_UUID))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void transfer_viewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/location-history/equipment/" + FAKE_UUID + "/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toBuilding\":\"Sede Norte\",\"reason\":\"Traslado\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void transfer_user_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/location-history/equipment/" + FAKE_UUID + "/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"toBuilding\":\"Sede Norte\",\"reason\":\"Traslado\"}"))
            .andExpect(status().isForbidden());
    }
}
