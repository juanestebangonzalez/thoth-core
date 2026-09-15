package com.thoth.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-012: upload only trusted the client-supplied
 * Content-Type header, so a malicious file could be smuggled in by declaring
 * a fake but allowed Content-Type. Real content is now sniffed via Tika and
 * must match what was declared.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentUploadContentSniffingTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndGetToken(String username) throws Exception {
        String payload = """
            {"username":"%s","password":"Sup3rSecret!","email":"%s@example.com"}
            """.formatted(username, username);

        String body = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void upload_withContentTypeMismatchingRealBytes_isRejected() throws Exception {
        String token = registerAndGetToken("s12mism_" + shortId());

        MockMultipartFile fakePdf = new MockMultipartFile(
            "file", "invoice.pdf", "application/pdf",
            "this is just plain text, not a real pdf".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(fakePdf)
                .param("equipmentId", UUID.randomUUID().toString())
                .param("documentType", "INVOICE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest());
    }

    @Test
    void upload_withContentMatchingDeclaredType_isAccepted() throws Exception {
        String token = registerAndGetToken("s12ok_" + shortId());

        MockMultipartFile realPdf = new MockMultipartFile(
            "file", "invoice.pdf", "application/pdf",
            "%PDF-1.4\n%useless comment to pad the header\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(realPdf)
                .param("equipmentId", UUID.randomUUID().toString())
                .param("documentType", "INVOICE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    /**
     * Architecture/security regression: the upload response used to be the raw
     * JPA DocumentEntity, leaking the internal on-disk fileName (UUID-prefixed
     * storage name). It must now be a DocumentResponseDTO exposing only
     * originalName.
     */
    @Test
    void upload_response_doesNotLeakInternalStorageFileName() throws Exception {
        String token = registerAndGetToken("s12dto_" + shortId());

        MockMultipartFile realPdf = new MockMultipartFile(
            "file", "invoice.pdf", "application/pdf",
            "%PDF-1.4\n%useless comment to pad the header\n".getBytes(StandardCharsets.UTF_8));

        String body = mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(realPdf)
                .param("equipmentId", UUID.randomUUID().toString())
                .param("documentType", "INVOICE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        org.junit.jupiter.api.Assertions.assertFalse(json.has("fileName"),
            "response must not expose the internal on-disk storage file name");
        org.junit.jupiter.api.Assertions.assertEquals("invoice.pdf", json.get("originalName").asText());
    }

    @Test
    void listByEquipment_response_doesNotLeakInternalStorageFileName() throws Exception {
        String token = registerAndGetToken("s12list_" + shortId());
        UUID equipmentId = UUID.randomUUID();

        MockMultipartFile realPdf = new MockMultipartFile(
            "file", "invoice.pdf", "application/pdf",
            "%PDF-1.4\n%useless comment to pad the header\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(realPdf)
                .param("equipmentId", equipmentId.toString())
                .param("documentType", "INVOICE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        String body = mockMvc.perform(get("/api/v1/documents/equipment/" + equipmentId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode list = objectMapper.readTree(body);
        org.junit.jupiter.api.Assertions.assertEquals(1, list.size());
        org.junit.jupiter.api.Assertions.assertFalse(list.get(0).has("fileName"));
        org.junit.jupiter.api.Assertions.assertEquals("invoice.pdf", list.get(0).get("originalName").asText());
    }

    /**
     * Regression test for SEC-015: the Content-Disposition header on download
     * used to be built by concatenating the client-controlled original
     * filename straight into the header value ("attachment; filename=\"" +
     * name + "\""), so a filename containing a double quote could break out
     * of the quoted value. It's now built via Spring's ContentDisposition,
     * which RFC 6266-encodes the value.
     */
    @Test
    void download_withQuoteInOriginalFilename_doesNotBreakContentDispositionHeader() throws Exception {
        String token = registerAndGetToken("s15cd_" + shortId());
        UUID equipmentId = UUID.randomUUID();

        MockMultipartFile trickyPdf = new MockMultipartFile(
            "file", "evil\".pdf\"; foo=bar", "application/pdf",
            "%PDF-1.4\n%useless comment to pad the header\n".getBytes(StandardCharsets.UTF_8));

        String body = mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(trickyPdf)
                .param("equipmentId", equipmentId.toString())
                .param("documentType", "INVOICE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        UUID documentId = UUID.fromString(objectMapper.readTree(body).get("id").asText());

        String contentDisposition = mockMvc.perform(get("/api/v1/documents/" + documentId + "/download")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getHeader("Content-Disposition");

        org.junit.jupiter.api.Assertions.assertNotNull(contentDisposition);
        // The raw double quote from the filename must not terminate the
        // quoted-string early - it must be escaped or percent-encoded.
        org.junit.jupiter.api.Assertions.assertFalse(
            contentDisposition.matches(".*filename=\"[^\"\\\\]*\"; foo=bar.*"),
            "attacker-controlled filename must not be able to inject extra Content-Disposition parameters: " + contentDisposition);
    }
}
