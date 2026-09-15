package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-013: /api/v1/alerts/** was fully permitAll(),
 * letting anyone dump the entire equipment inventory (names, serial
 * numbers, hardware health, rental company) unauthenticated - bypassing
 * the SEC-001 fix on /api/v1/equipment/** through a side door that queries
 * the same repository directly.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void upcomingMaintenance_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/upcoming-maintenance"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void hardwareCritical_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/hardware-critical"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rentalExpiring_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/rental-expiring"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void summary_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/summary"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void upcomingMaintenance_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/upcoming-maintenance"))
            .andExpect(status().isOk());
    }
}
