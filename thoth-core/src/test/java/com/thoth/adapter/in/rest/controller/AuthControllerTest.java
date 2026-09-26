package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @Test
    void register_ignoresRoleFieldAndAlwaysAssignsUserRole() throws Exception {
        String payload = """
            {
              "username": "attacker_sec002",
              "password": "Sup3rSecret!",
              "email": "attacker_sec002@example.com",
              "role": "ADMIN"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", "198.51.100.10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("USER"));

        Optional<UserEntity> saved = userRepository.findByUsername("attacker_sec002");
        assertTrue(saved.isPresent());
        assertEquals(UserEntity.UserRole.USER, saved.get().getRole());
    }

    @Test
    void register_duplicateUsername_returnsBadRequestWithoutCreatingAccount() throws Exception {
        String payload = """
            {
              "username": "dup_sec002",
              "password": "Sup3rSecret!",
              "email": "dup_sec002@example.com"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", "198.51.100.11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", "198.51.100.11")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    /**
     * SEC-007 (user enumeration): documents the accepted behavior - registration
     * returns a specific "username taken" message rather than a generic one.
     * This is a deliberate UX tradeoff, mitigated by RateLimitingFilter (SEC-008)
     * rather than by hiding the message.
     */
    @Test
    void register_existingUsername_returnsSpecificMessage() throws Exception {
        String payload = """
            {
              "username": "sec007_enum",
              "password": "Sup3rSecret!",
              "email": "sec007_enum@example.com"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", "198.51.100.12")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", "198.51.100.12")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El nombre de usuario ya existe"));
    }

    /**
     * SEC-008 (missing rate limiting): after the configured threshold of
     * attempts within the window, RateLimitingFilter must reject further
     * requests to /api/v1/auth/login with 429, regardless of credentials.
     * Uses a unique X-Forwarded-For per test run so it never collides with
     * the shared 127.0.0.1 bucket used by every other MockMvc-based test.
     */
    @Test
    void login_afterExceedingRateLimit_returns429() throws Exception {
        String fakeIp = "203.0.113." + (1 + new java.util.Random().nextInt(253));
        String payload = """
            {
              "username": "nonexistent_sec008",
              "password": "wrong-password"
            }
            """;

        org.springframework.test.web.servlet.ResultActions lastResult = null;
        for (int i = 0; i < 55; i++) {
            lastResult = mockMvc.perform(post("/api/v1/auth/login")
                .header("X-Forwarded-For", fakeIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
        }

        lastResult.andExpect(status().is(429));
    }
}
