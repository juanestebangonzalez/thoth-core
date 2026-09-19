package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CreateMaintenanceHistoryRequest;
import com.thoth.application.command.CreateMaintenanceHistoryCommand;
import com.thoth.application.dto.MaintenanceHistoryDTO;
import com.thoth.application.port.input.CreateMaintenanceHistoryUseCase;
import com.thoth.application.port.input.GetMaintenanceHistoryUseCase;
import com.thoth.application.service.AuditService;
import com.thoth.application.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-history")
@Tag(name = "Maintenance History", description = "Historial de mantenimientos (HV del equipo)")
@RequiredArgsConstructor
public class MaintenanceHistoryController {

    private final CreateMaintenanceHistoryUseCase createUseCase;
    private final GetMaintenanceHistoryUseCase getUseCase;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @PostMapping
    @Operation(summary = "Registrar mantenimiento en la HV del equipo")
    public ResponseEntity<MaintenanceHistoryDTO> create(
            @Valid @RequestBody CreateMaintenanceHistoryRequest request, Authentication authentication) {
        permissionService.requireModulePermission(authentication.getName(), "MAINTENANCE", "CREATE");
        List<CreateMaintenanceHistoryCommand.PartCommand> parts = null;
        if (request.getPartsReplaced() != null) {
            parts = request.getPartsReplaced().stream()
                .map(p -> new CreateMaintenanceHistoryCommand.PartCommand(
                    p.getPartName(),
                    p.getPartSerialNumber(),
                    p.getReason()
                ))
                .toList();
        }

        CreateMaintenanceHistoryCommand command = new CreateMaintenanceHistoryCommand(
            request.getEquipmentId(),
            request.getMaintenanceType(),
            request.getTechnicianName(),
            request.getTechnicianId(),
            request.getReason(),
            request.getDescription(),
            request.getNextScheduledDate(),
            parts,
            "SYSTEM",
            request.getSignatureBase64(),
            request.getSignedBy()
        );

        MaintenanceHistoryDTO result = createUseCase.create(command);
        auditService.log("CREATE", "MAINTENANCE", result.maintenanceId().toString(),
                request.getMaintenanceType(),
                "Mantenimiento registrado: " + request.getMaintenanceType() + " por " + request.getTechnicianName() + " en equipo " + request.getEquipmentId(),
                authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "Obtener HV completa de un equipo")
    public ResponseEntity<List<MaintenanceHistoryDTO>> getByEquipment(
            @PathVariable UUID equipmentId, Authentication authentication) {
        permissionService.requireModulePermission(authentication.getName(), "MAINTENANCE", "VIEW");
        return ResponseEntity.ok(getUseCase.getByEquipmentId(equipmentId));
    }

    @GetMapping("/equipment/{equipmentId}/count")
    @Operation(summary = "Contar mantenimientos de un equipo")
    public ResponseEntity<Long> countByEquipment(
            @PathVariable UUID equipmentId, Authentication authentication) {
        permissionService.requireModulePermission(authentication.getName(), "MAINTENANCE", "VIEW");
        return ResponseEntity.ok(getUseCase.countByEquipmentId(equipmentId));
    }

    @GetMapping("/{maintenanceId}")
    @Operation(summary = "Obtener un mantenimiento por ID")
    public ResponseEntity<MaintenanceHistoryDTO> getById(
            @PathVariable UUID maintenanceId, Authentication authentication) {
        permissionService.requireModulePermission(authentication.getName(), "MAINTENANCE", "VIEW");
        return ResponseEntity.ok(getUseCase.getById(maintenanceId));
    }
}