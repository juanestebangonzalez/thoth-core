package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.MaintenanceCategoryEntity;
import com.thoth.adapter.out.persistence.repository.MaintenanceCategoryJpaRepository;
import com.thoth.application.dto.MaintenanceCategoryDTO;
import com.thoth.application.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/maintenance-categories")
@Tag(name = "Maintenance Categories", description = "Gestion de categorias de mantenimiento")
@RequiredArgsConstructor
public class MaintenanceCategoryController {

    private final MaintenanceCategoryJpaRepository maintenanceCategoryRepository;
    private final AuditService auditService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar todas las categorias de mantenimiento activas")
    public ResponseEntity<List<MaintenanceCategoryDTO>> listActive() {
        return ResponseEntity.ok(maintenanceCategoryRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(MaintenanceCategoryController::toDTO)
            .toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    @Operation(summary = "Listar todas las categorias de mantenimiento (incluyendo inactivas)")
    public ResponseEntity<List<MaintenanceCategoryDTO>> listAll() {
        return ResponseEntity.ok(maintenanceCategoryRepository.findAll().stream()
            .map(MaintenanceCategoryController::toDTO)
            .toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoria de mantenimiento por ID")
    public ResponseEntity<MaintenanceCategoryDTO> getById(@PathVariable UUID id) {
        return maintenanceCategoryRepository.findById(id)
            .map(MaintenanceCategoryController::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Crear nueva categoria de mantenimiento")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Principal principal) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre es obligatorio"));
        }
        if (maintenanceCategoryRepository.existsByNameIgnoreCase(name.trim())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ya existe una categoria de mantenimiento con ese nombre"));
        }
        MaintenanceCategoryEntity category = MaintenanceCategoryEntity.builder()
            .name(name.trim())
            .description(body.getOrDefault("description", ""))
            .active(true)
            .build();
        MaintenanceCategoryEntity saved = maintenanceCategoryRepository.save(category);
        auditService.log("CREATE", "MAINTENANCE_CATEGORY", saved.getId().toString(), name,
                "Categoria de mantenimiento creada: " + name,
                principal != null ? principal.getName() : "SYSTEM");
        return ResponseEntity.ok(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoria de mantenimiento")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Map<String, String> body, Principal principal) {
        return maintenanceCategoryRepository.findById(id).map(category -> {
            String name = body.get("name");
            if (name != null && !name.isBlank()) {
                var existing = maintenanceCategoryRepository.findByNameIgnoreCase(name.trim());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Ya existe otra categoria de mantenimiento con ese nombre"));
                }
                category.setName(name.trim());
            }
            if (body.containsKey("description")) category.setDescription(body.get("description"));
            if (body.containsKey("active")) category.setActive(Boolean.parseBoolean(body.get("active")));
            MaintenanceCategoryEntity updated = maintenanceCategoryRepository.save(category);
            auditService.log("UPDATE", "MAINTENANCE_CATEGORY", id.toString(), category.getName(),
                    "Categoria de mantenimiento actualizada: " + category.getName(),
                    principal != null ? principal.getName() : "SYSTEM");
            return ResponseEntity.ok((Object) toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar categoria de mantenimiento (soft delete)")
    public ResponseEntity<?> delete(@PathVariable UUID id, Principal principal) {
        return maintenanceCategoryRepository.findById(id).map(category -> {
            category.setActive(false);
            maintenanceCategoryRepository.save(category);
            auditService.log("DELETE", "MAINTENANCE_CATEGORY", id.toString(), category.getName(),
                    "Categoria de mantenimiento desactivada: " + category.getName(),
                    principal != null ? principal.getName() : "SYSTEM");
            return ResponseEntity.ok(Map.of("message", "Categoria de mantenimiento desactivada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static MaintenanceCategoryDTO toDTO(MaintenanceCategoryEntity entity) {
        return new MaintenanceCategoryDTO(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            Boolean.TRUE.equals(entity.getActive()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
