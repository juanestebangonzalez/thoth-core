package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.DeviceTypeEntity;
import com.thoth.adapter.out.persistence.repository.DeviceTypeJpaRepository;
import com.thoth.application.dto.DeviceTypeDTO;
import com.thoth.application.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.thoth.adapter.in.rest.dto.request.CreateCatalogItemRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/device-types")
@Tag(name = "Device Types", description = "Gestion de tipos de dispositivos")
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeJpaRepository deviceTypeRepository;
    private final AuditService auditService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar todos los tipos de dispositivos activos")
    public ResponseEntity<List<DeviceTypeDTO>> listActive() {
        return ResponseEntity.ok(deviceTypeRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(DeviceTypeController::toDTO)
            .toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    @Operation(summary = "Listar todos los tipos de dispositivos (incluyendo inactivos)")
    public ResponseEntity<List<DeviceTypeDTO>> listAll() {
        return ResponseEntity.ok(deviceTypeRepository.findAll().stream()
            .map(DeviceTypeController::toDTO)
            .toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de dispositivo por ID")
    public ResponseEntity<DeviceTypeDTO> getById(@PathVariable UUID id) {
        return deviceTypeRepository.findById(id)
            .map(DeviceTypeController::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Crear nuevo tipo de dispositivo")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCatalogItemRequest body, Principal principal) {
        String name = body.name();
        if (deviceTypeRepository.existsByNameIgnoreCase(name.trim())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ya existe un tipo de dispositivo con ese nombre"));
        }
        DeviceTypeEntity deviceType = DeviceTypeEntity.builder()
            .name(name.trim())
            .description(body.description() != null ? body.description() : "")
            .active(true)
            .build();
        DeviceTypeEntity saved = deviceTypeRepository.save(deviceType);
        auditService.log("CREATE", "DEVICE_TYPE", saved.getId().toString(), name,
                "Tipo de dispositivo creado: " + name,
                principal != null ? principal.getName() : "SYSTEM");
        return ResponseEntity.ok(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de dispositivo")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody CreateCatalogItemRequest body, Principal principal) {
        return deviceTypeRepository.findById(id).map(deviceType -> {
            String name = body.name();
            if (name != null && !name.isBlank()) {
                var existing = deviceTypeRepository.findByNameIgnoreCase(name.trim());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Ya existe otro tipo de dispositivo con ese nombre"));
                }
                deviceType.setName(name.trim());
            }
            if (body.description() != null) deviceType.setDescription(body.description());
            if (body.active() != null) deviceType.setActive(body.active());
            DeviceTypeEntity updated = deviceTypeRepository.save(deviceType);
            auditService.log("UPDATE", "DEVICE_TYPE", id.toString(), deviceType.getName(),
                    "Tipo de dispositivo actualizado: " + deviceType.getName(),
                    principal != null ? principal.getName() : "SYSTEM");
            return ResponseEntity.ok((Object) toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar tipo de dispositivo (soft delete)")
    public ResponseEntity<?> delete(@PathVariable UUID id, Principal principal) {
        return deviceTypeRepository.findById(id).map(deviceType -> {
            deviceType.setActive(false);
            deviceTypeRepository.save(deviceType);
            auditService.log("DELETE", "DEVICE_TYPE", id.toString(), deviceType.getName(),
                    "Tipo de dispositivo desactivado: " + deviceType.getName(),
                    principal != null ? principal.getName() : "SYSTEM");
            return ResponseEntity.ok(Map.of("message", "Tipo de dispositivo desactivado"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static DeviceTypeDTO toDTO(DeviceTypeEntity entity) {
        return new DeviceTypeDTO(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            Boolean.TRUE.equals(entity.getActive()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
