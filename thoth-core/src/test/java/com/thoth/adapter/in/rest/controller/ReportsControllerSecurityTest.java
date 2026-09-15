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
 * Confirms /api/v1/reports/** (aggregate KPIs over the whole equipment
 * inventory) already requires authentication via the anyRequest().authenticated()
 * catch-all in SecurityConfig - verified during the final audit pass, not a
 * vulnerability, but pinned down so it can't silently regress to public.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportsControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboard_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void dashboard_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard"))
            .andExpect(status().isOk());
    }
}
