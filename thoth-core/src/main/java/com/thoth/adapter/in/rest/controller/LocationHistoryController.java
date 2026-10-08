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

import com.thoth.adapter.in.rest.dto.request.TransferEquipmentRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/location-history")
@Tag(name = "Historial de ubicaciones", description = "Historico de ubicaciones de equipos")
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
            @Valid @RequestBody TransferEquipmentRequest body,
            Principal principal) {

        EquipmentEntity equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment == null) {
            return ResponseEntity.notFound().build();
        }

        String toBuilding = body.toBuilding();
        String toFloor = body.toFloor();
        String toOffice = body.toOffice();
        String reason = body.reason();

        transferOne(equipment, toBuilding, toFloor, toOffice, reason, principal.getName(), null);

        // Monitores asociados: se trasladan con el equipo (mismo destino y motivo) salvo includeMonitors=false
        int monitors = 0;
        if (body.shouldIncludeMonitors()) {
            for (EquipmentEntity monitor : equipmentRepository.findByAssociatedEquipmentId(equipmentId)) {
                transferOne(monitor, toBuilding, toFloor, toOffice, reason, principal.getName(), equipment.getName());
                monitors++;
            }
        }

        String message = monitors > 0
                ? "Equipo trasladado exitosamente junto con " + monitors + (monitors == 1 ? " monitor asociado" : " monitores asociados")
                : "Equipo trasladado exitosamente";
        return ResponseEntity.ok(Map.of("message", message, "monitorsTransferred", monitors));
    }

    /** Registra el historial, actualiza la ubicacion y deja la auditoria de un equipo. */
    private void transferOne(EquipmentEntity equipment, String toBuilding, String toFloor, String toOffice,
                             String reason, String user, String withEquipmentName) {
        LocationHistoryEntity history = LocationHistoryEntity.builder()
                .equipmentId(equipment.getEquipmentId())
                .fromBuilding(equipment.getLocationBuilding())
                .fromFloor(equipment.getLocationFloor())
                .fromOffice(equipment.getLocationOffice())
                .toBuilding(toBuilding)
                .toFloor(toFloor != null ? toFloor : "")
                .toOffice(toOffice != null ? toOffice : "")
                .reason(reason)
                .performedBy(user)
                .transferredAt(LocalDateTime.now())
                .build();
        locationHistoryRepository.save(history);

        equipment.setLocationBuilding(toBuilding);
        equipment.setLocationFloor(toFloor != null ? toFloor : "");
        equipment.setLocationOffice(toOffice != null ? toOffice : "");
        equipment.setUpdatedAt(LocalDateTime.now());
        equipment.setUpdatedBy(user);
        equipmentRepository.save(equipment);

        auditService.log("TRANSFER", "LOCATION", equipment.getEquipmentId().toString(), equipment.getName(),
                "De: " + history.getFromBuilding() + " A: " + toBuilding + " - " + reason
                        + (withEquipmentName != null ? " (monitor trasladado con el equipo " + withEquipmentName + ")" : ""),
                user);
    }
}
