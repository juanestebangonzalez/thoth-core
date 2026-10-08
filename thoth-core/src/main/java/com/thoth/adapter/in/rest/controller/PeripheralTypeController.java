package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CreateCatalogItemRequest;
import com.thoth.adapter.out.persistence.entity.PeripheralTypeEntity;
import com.thoth.adapter.out.persistence.repository.PeripheralTypeRepository;
import com.thoth.application.dto.PeripheralTypeDTO;
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
@RequestMapping("/api/v1/peripheral-types")
@Tag(name = "Tipos de periferico", description = "Catalogo de tipos de periferico (teclado, mouse, diadema...)")
@RequiredArgsConstructor
public class PeripheralTypeController {

    private final PeripheralTypeRepository peripheralTypeRepository;
    private final AuditService auditService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar tipos de periferico activos")
    public ResponseEntity<List<PeripheralTypeDTO>> listActive() {
        return ResponseEntity.ok(peripheralTypeRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(PeripheralTypeController::toDTO).toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    @Operation(summary = "Listar todos los tipos de periferico (incluyendo inactivos)")
    public ResponseEntity<List<PeripheralTypeDTO>> listAll() {
        return ResponseEntity.ok(peripheralTypeRepository.findAllByOrderByNameAsc().stream()
            .map(PeripheralTypeController::toDTO).toList());
    }

    @PreAuthorize("@perm.can(authentication,'CATALOGS','EDIT')")
    @PostMapping
    @Operation(summary = "Crear tipo de periferico")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCatalogItemRequest body, Principal principal) {
        String name = body.name().trim().toUpperCase();
        if (peripheralTypeRepository.existsByNameIgnoreCase(name)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ya existe un tipo de periferico con ese nombre"));
        }
        PeripheralTypeEntity saved = peripheralTypeRepository.save(PeripheralTypeEntity.builder()
            .name(name)
            .description(body.description() != null ? body.description().trim() : "")
            .active(true)
            .build());
        auditService.log("CREATE", "PERIPHERAL_TYPE", saved.getId().toString(), name,
                "Tipo de periferico creado: " + name, user(principal));
        return ResponseEntity.ok(toDTO(saved));
    }

    @PreAuthorize("@perm.can(authentication,'CATALOGS','EDIT')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de periferico")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CreateCatalogItemRequest body, Principal principal) {
        return peripheralTypeRepository.findById(id).map(type -> {
            if (body.name() != null && !body.name().isBlank()) {
                String name = body.name().trim().toUpperCase();
                var existing = peripheralTypeRepository.findByNameIgnoreCase(name);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Ya existe otro tipo de periferico con ese nombre"));
                }
                type.setName(name);
            }
            if (body.description() != null) type.setDescription(body.description().trim());
            if (body.active() != null) type.setActive(body.active());
            PeripheralTypeEntity updated = peripheralTypeRepository.save(type);
            auditService.log("UPDATE", "PERIPHERAL_TYPE", id.toString(), type.getName(),
                    "Tipo de periferico actualizado: " + type.getName(), user(principal));
            return ResponseEntity.ok((Object) toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("@perm.can(authentication,'CATALOGS','EDIT')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de periferico")
    public ResponseEntity<?> delete(@PathVariable UUID id, Principal principal) {
        return peripheralTypeRepository.findById(id).map(type -> {
            peripheralTypeRepository.delete(type);
            auditService.log("DELETE", "PERIPHERAL_TYPE", id.toString(), type.getName(),
                    "Tipo de periferico eliminado: " + type.getName(), user(principal));
            return ResponseEntity.ok((Object) Map.of("message", "Tipo de periferico eliminado"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static String user(Principal principal) {
        return principal != null ? principal.getName() : "SYSTEM";
    }

    private static PeripheralTypeDTO toDTO(PeripheralTypeEntity e) {
        return new PeripheralTypeDTO(e.getId(), e.getName(), e.getDescription(), Boolean.TRUE.equals(e.getActive()));
    }
}
