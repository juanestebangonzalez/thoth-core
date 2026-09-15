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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-005: /api/v1/documents/{id}/download was fully
 * permitAll(), letting anyone download uploaded documents (invoices,
 * contracts) without authentication or a DOCUMENTS:VIEW permission check.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndGetToken(String username, String role) throws Exception {
        String payload = """
            {"username":"%s","password":"Sup3rSecret!","email":"%s@example.com"}
            """.formatted(username, username);

        String body = mockMvc.perform(post("/api/v1/auth/register")
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
    void download_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/documents/" + UUID.randomUUID() + "/download"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void download_withViewerRole_returns403() throws Exception {
        String token = registerAndGetToken("s5v_" + shortId(), "VIEWER");

        mockMvc.perform(get("/api/v1/documents/" + UUID.randomUUID() + "/download")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void download_withUserRole_onMissingDocument_returns404() throws Exception {
        String token = registerAndGetToken("s5u_" + shortId(), "USER");

        mockMvc.perform(get("/api/v1/documents/" + UUID.randomUUID() + "/download")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
