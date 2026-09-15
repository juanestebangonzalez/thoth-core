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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-001: /api/v1/equipment/** was fully permitAll(),
 * allowing unauthenticated CRUD over the equipment inventory.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipmentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_EQUIPMENT_PAYLOAD = """
        {
          "name": "Dell OptiPlex 7090",
          "category": "DESKTOP",
          "serialNumber": "SEC001-%s",
          "brand": "Dell",
          "location": { "building": "A", "floor": "1", "office": "101" },
          "purchaseDate": "2023-01-15",
          "purchaseValue": 1200
        }
        """;

    @Test
    void listEquipment_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/equipment"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void listEquipment_authenticatedViewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/equipment"))
            .andExpect(status().isOk());
    }

    @Test
    void createEquipment_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_EQUIPMENT_PAYLOAD.formatted(UUID.randomUUID())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createEquipment_withViewerRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_EQUIPMENT_PAYLOAD.formatted(UUID.randomUUID())))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void createEquipment_withTechnicianRole_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_EQUIPMENT_PAYLOAD.formatted(UUID.randomUUID())))
            .andExpect(status().isCreated());
    }

    @Test
    void updateEquipment_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/equipment/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void updateEquipment_withViewerRole_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/equipment/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteEquipment_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteEquipment_withUserRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/" + UUID.randomUUID()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEquipment_withAdminRole_onMissingEquipment_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
