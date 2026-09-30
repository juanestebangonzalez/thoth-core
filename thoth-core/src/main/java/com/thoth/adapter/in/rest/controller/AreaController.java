package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CreateCatalogItemRequest;
import com.thoth.adapter.out.persistence.entity.AreaEntity;
import com.thoth.adapter.out.persistence.repository.AreaRepository;
import com.thoth.application.dto.AreaDTO;
import com.thoth.application.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/areas")
@Tag(name = "Areas", description = "Gestion de areas / dependencias")
@RequiredArgsConstructor
public class AreaController {

    private final AreaRepository areaRepository;
    private final AuditService auditService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar areas activas")
    public ResponseEntity<List<AreaDTO>> listActive() {
        return ResponseEntity.ok(areaRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(AreaController::toDTO).toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    @Operation(summary = "Listar todas las areas (incluyendo inactivas)")
    public ResponseEntity<List<AreaDTO>> listAll() {
        return ResponseEntity.ok(areaRepository.findAllByOrderByNameAsc().stream()
            .map(AreaController::toDTO).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Crear area")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCatalogItemRequest body, Principal principal) {
        String name = body.name().trim().toUpperCase();
        if (areaRepository.existsByNameIgnoreCase(name)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ya existe un area con ese nombre"));
        }
        AreaEntity saved = areaRepository.save(AreaEntity.builder()
            .name(name)
            .description(body.description() != null ? body.description().trim() : "")
            .active(true)
            .build());
        auditService.log("CREATE", "AREA", saved.getId().toString(), name,
                "Area creada: " + name, user(principal));
        return ResponseEntity.ok(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar area")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CreateCatalogItemRequest body, Principal principal) {
        return areaRepository.findById(id).map(area -> {
            if (body.name() != null && !body.name().isBlank()) {
                String name = body.name().trim().toUpperCase();
                var existing = areaRepository.findByNameIgnoreCase(name);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Ya existe otra area con ese nombre"));
                }
                area.setName(name);
            }
            if (body.description() != null) area.setDescription(body.description().trim());
            if (body.active() != null) area.setActive(body.active());
            AreaEntity updated = areaRepository.save(area);
            auditService.log("UPDATE", "AREA", id.toString(), area.getName(),
                    "Area actualizada: " + area.getName(), user(principal));
            return ResponseEntity.ok((Object) toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar area")
    public ResponseEntity<?> delete(@PathVariable UUID id, Principal principal) {
        return areaRepository.findById(id).map(area -> {
            areaRepository.delete(area);
            auditService.log("DELETE", "AREA", id.toString(), area.getName(),
                    "Area eliminada: " + area.getName(), user(principal));
            return ResponseEntity.ok((Object) Map.of("message", "Area eliminada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static String user(Principal principal) {
        return principal != null ? principal.getName() : "SYSTEM";
    }

    private static AreaDTO toDTO(AreaEntity e) {
        return new AreaDTO(e.getId(), e.getName(), e.getDescription(), Boolean.TRUE.equals(e.getActive()));
    }
}
