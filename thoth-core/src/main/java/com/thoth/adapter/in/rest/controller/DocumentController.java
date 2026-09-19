package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.DocumentEntity;
import com.thoth.adapter.out.persistence.repository.DocumentRepository;
import com.thoth.application.dto.DocumentResponseDTO;
import com.thoth.application.service.AuditService;
import com.thoth.application.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Gestion de documentos (facturas, contratos, etc)")
@RequiredArgsConstructor
public class DocumentController {

    private static final String[] ALLOWED_TYPES = {"application/pdf", "image/jpeg", "image/png", "image/gif",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};

    private static final Tika TIKA = new Tika();

    private final DocumentRepository documentRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    @Operation(summary = "Subir documento para un equipo")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("equipmentId") UUID equipmentId,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El archivo esta vacio"));
            }

            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().body(Map.of("message", "El archivo excede 10MB"));
            }

            boolean declaredTypeAllowed = Arrays.asList(ALLOWED_TYPES).contains(file.getContentType());
            if (!declaredTypeAllowed) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tipo de archivo no permitido. Use PDF, imagenes, Word o Excel"));
            }

            // SEC-012: don't trust the client-declared Content-Type - sniff the
            // real magic bytes and make sure they match what was declared.
            byte[] fileBytes = file.getBytes();
            String detectedType = TIKA.detect(fileBytes, file.getOriginalFilename());
            if (!detectedType.equals(file.getContentType())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "El contenido del archivo no coincide con el tipo declarado (" + file.getContentType() + ")"));
            }

            Path uploadPath = Paths.get(uploadDir, equipmentId.toString());
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, fileBytes);

            DocumentEntity doc = DocumentEntity.builder()
                .equipmentId(equipmentId)
                .fileName(fileName)
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .documentType(documentType.toUpperCase())
                .description(description)
                .uploadedBy("SYSTEM")
                .uploadedAt(LocalDateTime.now())
                .build();

            DocumentEntity saved = documentRepository.save(doc);
            String user = authentication != null ? authentication.getName() : "SYSTEM";
            auditService.log("UPLOAD", "DOCUMENT", saved.getId().toString(), file.getOriginalFilename(),
                    "Documento subido: " + documentType + " - " + file.getOriginalFilename() + " para equipo " + equipmentId,
                    user);
            return ResponseEntity.ok(toDTO(saved));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error al guardar archivo: " + e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "Listar documentos de un equipo")
    public ResponseEntity<List<DocumentResponseDTO>> listByEquipment(@PathVariable UUID equipmentId) {
        return ResponseEntity.ok(documentRepository.findByEquipmentIdOrderByUploadedAtDesc(equipmentId).stream()
            .map(DocumentController::toDTO)
            .toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar documento")
    public ResponseEntity<?> download(@PathVariable UUID id, Authentication authentication) {
        permissionService.requireModulePermission(authentication.getName(), "DOCUMENTS", "VIEW");
        return documentRepository.findById(id).map(doc -> {
            try {
                Path filePath = Paths.get(uploadDir, doc.getEquipmentId().toString(), doc.getFileName());
                Resource resource = new UrlResource(filePath.toUri());
                if (!resource.exists()) {
                    return ResponseEntity.notFound().build();
                }
                // SEC-015: build Content-Disposition via Spring's RFC 6266 encoder
                // instead of string concatenation - doc.getOriginalName() is
                // client-controlled at upload time and must not be interpolated
                // raw into a response header.
                ContentDisposition disposition = ContentDisposition.attachment()
                    .filename(doc.getOriginalName() != null ? doc.getOriginalName() : "download", StandardCharsets.UTF_8)
                    .build();
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType() != null ? doc.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body((Object) resource);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento")
    public ResponseEntity<?> delete(@PathVariable UUID id, Authentication authentication) {
        return documentRepository.findById(id).map(doc -> {
            try {
                Path filePath = Paths.get(uploadDir, doc.getEquipmentId().toString(), doc.getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
            documentRepository.delete(doc);
            String user = authentication != null ? authentication.getName() : "SYSTEM";
            auditService.log("DELETE", "DOCUMENT", id.toString(), doc.getOriginalName(),
                    "Documento eliminado: " + doc.getOriginalName(),
                    user);
            return ResponseEntity.ok(Map.of("message", "Documento eliminado"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static DocumentResponseDTO toDTO(DocumentEntity entity) {
        return new DocumentResponseDTO(
            entity.getId(),
            entity.getEquipmentId(),
            entity.getOriginalName(),
            entity.getContentType(),
            entity.getFileSize(),
            entity.getDocumentType(),
            entity.getDescription(),
            entity.getUploadedBy(),
            entity.getUploadedAt()
        );
    }
}