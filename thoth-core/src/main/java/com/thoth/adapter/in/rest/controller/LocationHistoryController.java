package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.entity.LocationHistoryEntity;
import com.thoth.adapter.out.persistence.repository.LocationHistoryRepository;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.application.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/location-history")
@Tag(name = "Location History", description = "Historico de ubicaciones de equipos")
@RequiredArgsConstructor
public class LocationHistoryController {

    private final LocationHistoryRepository locationHistoryRepository;
    private final EquipmentJpaRepository equipmentRepository;
    private final AuditService auditService;

    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener historial de ubicaciones de un equipo")
    public ResponseEntity<List<LocationHistoryEntity>> getHistory(@PathVariable UUID equipmentId) {
        return ResponseEntity.ok(
            locationHistoryRepository.findByEquipmentIdOrderByTransferredAtDesc(equipmentId)
        );
    }

    @PostMapping("/equipment/{equipmentId}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Operation(summary = "Registrar traslado de equipo")
    public ResponseEntity<?> transferEquipment(
            @PathVariable UUID equipmentId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        EquipmentEntity equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment == null) {
            return ResponseEntity.notFound().build();
        }

        String toBuilding = body.get("toBuilding");
        String toFloor = body.get("toFloor");
        String toOffice = body.get("toOffice");
        String reason = body.get("reason");

        if (toBuilding == null || toBuilding.isBlank() || reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sede destino y motivo son obligatorios"));
        }

        // Save history entry
        LocationHistoryEntity history = LocationHistoryEntity.builder()
                .equipmentId(equipmentId)
                .fromBuilding(equipment.getLocationBuilding())
                .fromFloor(equipment.getLocationFloor())
                .fromOffice(equipment.getLocationOffice())
                .toBuilding(toBuilding)
                .toFloor(toFloor != null ? toFloor : "")
                .toOffice(toOffice != null ? toOffice : "")
                .reason(reason)
                .performedBy(principal.getName())
                .transferredAt(LocalDateTime.now())
                .build();
        locationHistoryRepository.save(history);

        // Update equipment location
        equipment.setLocationBuilding(toBuilding);
        equipment.setLocationFloor(toFloor != null ? toFloor : "");
        equipment.setLocationOffice(toOffice != null ? toOffice : "");
        equipment.setUpdatedAt(LocalDateTime.now());
        equipment.setUpdatedBy(principal.getName());
        equipmentRepository.save(equipment);

        // Audit log
        auditService.log("TRANSFER", "LOCATION", equipmentId.toString(), equipment.getName(),
                "De: " + history.getFromBuilding() + " A: " + toBuilding + " - " + reason,
                principal.getName());

        return ResponseEntity.ok(Map.of("message", "Equipo trasladado exitosamente"));
    }
}
