package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CompleteMaintenanceRequest;
import com.thoth.adapter.in.rest.dto.request.CreateMaintenanceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Mantenimientos", description = "Gestion de mantenimientos")
@RequiredArgsConstructor
public class MaintenanceController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear mantenimiento", description = "Registrar un mantenimiento")
    public ResponseEntity<?> createMaintenance(
            @Valid @RequestBody CreateMaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener mantenimiento", description = "Consultar un mantenimiento por ID")
    public ResponseEntity<?> getMaintenance(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "Listar mantenimientos", description = "Consultar todos los mantenimientos")
    public ResponseEntity<?> listMaintenance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Completar mantenimiento", description = "Marcar el mantenimiento como completado")
    public ResponseEntity<?> completeMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteMaintenanceRequest request) {
        return ResponseEntity.ok().build();
    }
}