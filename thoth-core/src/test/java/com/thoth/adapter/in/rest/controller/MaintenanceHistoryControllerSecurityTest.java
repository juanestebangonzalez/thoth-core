package com.thoth.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoth.application.service.UserService;
import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-009: any authenticated user, regardless of role or
 * granted permissions, could read any equipment's maintenance history
 * (PermissionService.hasPermission existed but was never enforced).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceHistoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Each call uses its own random fake source IP so this test's
    // /auth/register traffic never shares RateLimitingFilter's per-IP
    // bucket with the dozens of other registrations the rest of the suite
    // performs (from the default MockMvc client address, or from another
    // test class's own fake IPs).
    private String registerAndGetToken(String username, String role) throws Exception {
        String payload = """
            {"username":"%s","password":"Sup3rSecret!","email":"%s@example.com"}
            """.formatted(username, username);

        String body = mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", randomFakeIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        String token = json.get("token").asText();

        if (!"USER".equals(role)) {
            UserEntity user = userRepository.findByUsername(username).orElseThrow();
            userService.changeRole(user.getId(), role);
        }

        return token;
    }

    @Test
    void getByEquipment_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance-history/equipment/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getByEquipment_withViewerRole_returns403() throws Exception {
        String token = registerAndGetToken("s9v_" + shortId(), "VIEWER");

        mockMvc.perform(get("/api/v1/maintenance-history/equipment/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void getByEquipment_withUserRole_returns200() throws Exception {
        String token = registerAndGetToken("s9u_" + shortId(), "USER");

        mockMvc.perform(get("/api/v1/maintenance-history/equipment/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void getById_withViewerRole_returns403() throws Exception {
        String token = registerAndGetToken("s9v2_" + shortId(), "VIEWER");

        mockMvc.perform(get("/api/v1/maintenance-history/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    private static final String CREATE_PAYLOAD = """
        {
          "equipmentId": "%s",
          "maintenanceType": "PREVENTIVE",
          "technicianName": "Juan Perez",
          "reason": "Mantenimiento rutinario"
        }
        """;

    /**
     * SEC-016: the create (POST) endpoint had no permission check at all,
     * unlike the GET endpoints which require MAINTENANCE:VIEW - any
     * authenticated user, including VIEWER (who has zero MAINTENANCE
     * permissions by design), could fabricate maintenance records for any
     * equipment.
     */
    @Test
    void create_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/maintenance-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYLOAD.formatted(UUID.randomUUID())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withViewerRole_returns403() throws Exception {
        String token = registerAndGetToken("s16v_" + shortId(), "VIEWER");

        mockMvc.perform(post("/api/v1/maintenance-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYLOAD.formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void create_withUserRole_returns403() throws Exception {
        // USER's default permissions only grant MAINTENANCE:VIEW, not CREATE.
        String token = registerAndGetToken("s16u_" + shortId(), "USER");

        mockMvc.perform(post("/api/v1/maintenance-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYLOAD.formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void create_withTechnicianRole_returns201() throws Exception {
        String token = registerAndGetToken("s16t_" + shortId(), "TECHNICIAN");

        mockMvc.perform(post("/api/v1/maintenance-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYLOAD.formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated());
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String randomFakeIp() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "10." + r.nextInt(1, 255) + "." + r.nextInt(1, 255) + "." + r.nextInt(1, 255);
    }
}
