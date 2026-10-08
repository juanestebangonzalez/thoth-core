package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.PeripheralRequest;
import com.thoth.application.dto.PeripheralDTO;
import com.thoth.application.service.PeripheralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment/{equipmentId}/peripherals")
@Tag(name = "Perifericos", description = "Perifericos registrados por equipo")
@RequiredArgsConstructor
public class EquipmentPeripheralController {

    private final PeripheralService peripheralService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar perifericos del equipo")
    public ResponseEntity<List<PeripheralDTO>> list(@PathVariable UUID equipmentId) {
        return ResponseEntity.ok(peripheralService.list(equipmentId));
    }

    @PreAuthorize("@perm.can(authentication,'EQUIPMENT','CREATE')")
    @PostMapping
    @Operation(summary = "Agregar periferico al equipo", description = "El tipo debe existir en el catalogo activo de tipos de periferico")
    public ResponseEntity<PeripheralDTO> create(@PathVariable UUID equipmentId,
                                                @Valid @RequestBody PeripheralRequest body, Principal principal) {
        PeripheralDTO created = peripheralService.create(equipmentId, body.type(), body.brand(), user(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("@perm.can(authentication,'EQUIPMENT','EDIT')")
    @PutMapping("/{peripheralId}")
    @Operation(summary = "Actualizar periferico del equipo")
    public ResponseEntity<PeripheralDTO> update(@PathVariable UUID equipmentId, @PathVariable UUID peripheralId,
                                                @Valid @RequestBody PeripheralRequest body, Principal principal) {
        return ResponseEntity.ok(peripheralService.update(equipmentId, peripheralId, body.type(), body.brand(), user(principal)));
    }

    @PreAuthorize("@perm.can(authentication,'EQUIPMENT','DELETE')")
    @DeleteMapping("/{peripheralId}")
    @Operation(summary = "Eliminar periferico del equipo")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID equipmentId, @PathVariable UUID peripheralId,
                                                      Principal principal) {
        peripheralService.delete(equipmentId, peripheralId, user(principal));
        return ResponseEntity.ok(Map.of("message", "Periferico eliminado"));
    }

    private static String user(Principal principal) {
        return principal != null ? principal.getName() : "SYSTEM";
    }
}
