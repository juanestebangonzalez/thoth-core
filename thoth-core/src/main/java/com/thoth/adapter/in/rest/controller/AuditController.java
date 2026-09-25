package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.response.AuditLogDTO;
import com.thoth.adapter.out.persistence.entity.AuditLogArchiveEntity;
import com.thoth.adapter.out.persistence.entity.AuditLogEntity;
import com.thoth.application.service.AuditArchiveService;
import com.thoth.application.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Log", description = "Log de auditoria del sistema")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final AuditArchiveService archiveService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener log de auditoria con filtros")
    public ResponseEntity<Map<String, Object>> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        Page<AuditLogEntity> result = auditService.getFiltered(module, user, action, safePage, safeSize);
        Page<AuditLogDTO> dtoPage = result.map(this::toDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoPage.getContent());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("pageNumber", result.getNumber());
        response.put("pageSize", result.getSize());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar logs archivados (> 6 meses)")
    public ResponseEntity<Map<String, Object>> getArchivedLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        Page<AuditLogArchiveEntity> result = archiveService.getArchived(module, user, action, safePage, safeSize);

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("pageNumber", result.getNumber());
        response.put("pageSize", result.getSize());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ejecutar archivado manual de logs > 6 meses")
    public ResponseEntity<Map<String, Object>> archiveNow() {
        return ResponseEntity.ok(archiveService.archiveManually());
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Estadisticas de auditoria (activos vs archivados)")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(archiveService.getStats());
    }

    private AuditLogDTO toDTO(AuditLogEntity entity) {
        return new AuditLogDTO(
            entity.getId(),
            entity.getAction(),
            entity.getModule(),
            entity.getEntityId(),
            entity.getEntityName(),
            entity.getDetails(),
            entity.getPerformedBy(),
            entity.getPerformedAt(),
            entity.getIpAddress()
        );
    }
}
