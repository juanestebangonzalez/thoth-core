package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CreateCatalogItemRequest;
import com.thoth.adapter.out.persistence.entity.CostCenterEntity;
import com.thoth.adapter.out.persistence.repository.CostCenterRepository;
import com.thoth.application.dto.CostCenterDTO;
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
@RequestMapping("/api/v1/cost-centers")
@Tag(name = "Centros de costo", description = "Gestion de centros de costo")
@RequiredArgsConstructor
public class CostCenterController {

    private final CostCenterRepository costCenterRepository;
    private final AuditService auditService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar centros de costo activos")
    public ResponseEntity<List<CostCenterDTO>> listActive() {
        return ResponseEntity.ok(costCenterRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(CostCenterController::toDTO).toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    @Operation(summary = "Listar todos los centros de costo (incluyendo inactivos)")
    public ResponseEntity<List<CostCenterDTO>> listAll() {
        return ResponseEntity.ok(costCenterRepository.findAllByOrderByNameAsc().stream()
            .map(CostCenterController::toDTO).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Crear centro de costo")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCatalogItemRequest body, Principal principal) {
        String name = body.name().trim().toUpperCase();
        if (costCenterRepository.existsByNameIgnoreCase(name)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ya existe un centro de costo con ese nombre"));
        }
        CostCenterEntity saved = costCenterRepository.save(CostCenterEntity.builder()
            .name(name)
            .description(body.description() != null ? body.description().trim() : "")
            .active(true)
            .build());
        auditService.log("CREATE", "COST_CENTER", saved.getId().toString(), name,
                "Centro de costo creado: " + name, user(principal));
        return ResponseEntity.ok(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar centro de costo")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CreateCatalogItemRequest body, Principal principal) {
        return costCenterRepository.findById(id).map(cc -> {
            if (body.name() != null && !body.name().isBlank()) {
                String name = body.name().trim().toUpperCase();
                var existing = costCenterRepository.findByNameIgnoreCase(name);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Ya existe otro centro de costo con ese nombre"));
                }
                cc.setName(name);
            }
            if (body.description() != null) cc.setDescription(body.description().trim());
            if (body.active() != null) cc.setActive(body.active());
            CostCenterEntity updated = costCenterRepository.save(cc);
            auditService.log("UPDATE", "COST_CENTER", id.toString(), cc.getName(),
                    "Centro de costo actualizado: " + cc.getName(), user(principal));
            return ResponseEntity.ok((Object) toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar centro de costo")
    public ResponseEntity<?> delete(@PathVariable UUID id, Principal principal) {
        return costCenterRepository.findById(id).map(cc -> {
            costCenterRepository.delete(cc);
            auditService.log("DELETE", "COST_CENTER", id.toString(), cc.getName(),
                    "Centro de costo eliminado: " + cc.getName(), user(principal));
            return ResponseEntity.ok((Object) Map.of("message", "Centro de costo eliminado"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static String user(Principal principal) {
        return principal != null ? principal.getName() : "SYSTEM";
    }

    private static CostCenterDTO toDTO(CostCenterEntity e) {
        return new CostCenterDTO(e.getId(), e.getName(), e.getDescription(), Boolean.TRUE.equals(e.getActive()));
    }
}
