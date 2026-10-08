package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.ChangeStatusRequest;
import com.thoth.adapter.in.rest.dto.request.CreateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.request.HardwareRequest;
import com.thoth.adapter.in.rest.dto.request.LocationRequest;
import com.thoth.adapter.in.rest.dto.request.RentalInfoRequest;
import com.thoth.adapter.in.rest.dto.request.UpdateEquipmentRequest;
import com.thoth.application.command.ChangeStatusCommand;
import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.dto.PageResponseDTO;
import com.thoth.application.port.input.ChangeEquipmentStatusUseCase;
import com.thoth.application.port.input.GetEquipmentUseCase;
import com.thoth.application.port.input.ListEquipmentUseCase;
import com.thoth.application.port.input.RegisterEquipmentUseCase;
import com.thoth.application.port.input.UpdateEquipmentUseCase;
import com.thoth.application.service.AuditService;
import com.thoth.application.service.EquipmentMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Equipos", description = "Gestion de equipos")
@RequiredArgsConstructor
public class EquipmentController {

    private final RegisterEquipmentUseCase registerEquipmentUseCase;
    private final GetEquipmentUseCase getEquipmentUseCase;
    private final ListEquipmentUseCase listEquipmentUseCase;
    private final UpdateEquipmentUseCase updateEquipmentUseCase;
    private final ChangeEquipmentStatusUseCase changeEquipmentStatusUseCase;
    private final AuditService auditService;
    private final EquipmentMonitorService equipmentMonitorService;

    /** UUID opcional: vacio -> null; formato invalido -> 400. */
    private static UUID parseOptionalUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El identificador del equipo asociado no es valido: " + value);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping
    @Operation(summary = "Crear equipo", description = "Registrar un nuevo equipo")
    public ResponseEntity<EquipmentResponseDTO> createEquipment(@Valid @RequestBody CreateEquipmentRequest request, Principal principal) {
        HardwareRequest hw = request.getHardware();
        RentalInfoRequest rental = request.getRentalInfo();
        LocationRequest loc = request.getLocation();

        RegisterEquipmentCommand command = new RegisterEquipmentCommand(
            request.getName(),
            request.getCategory(),
            request.getSerialNumber(),
                request.getInventoryNumber(),
            request.getBrand(),
            request.getModel(),
            request.getMacAddress(),
            request.getPurchaseDate(),
            request.getPurchaseValue(),
            loc != null ? loc.getBuilding() : null,
            loc != null ? loc.getFloor() : null,
            loc != null ? loc.getOffice() : null,
            request.getAssignedTo(),
            principal != null ? principal.getName() : "SYSTEM",
            request.getOwnershipType(),
            rental != null ? rental.getRentalCompany() : null,
            rental != null ? rental.getContactName() : null,
            rental != null ? rental.getContactPhone() : null,
            rental != null ? rental.getContactEmail() : null,
            rental != null ? rental.getStartDate() : null,
            rental != null ? rental.getEndDate() : null,
            rental != null ? rental.getContractNumber() : null,
            rental != null ? rental.getContractFileUrl() : null,
            rental != null ? rental.getNotes() : null,
            hw != null ? hw.getProcessor() : null,
            hw != null ? hw.getRamSizeGb() : null,
            hw != null ? hw.getRamType() : null,
            hw != null ? hw.getDiskType() : null,
            hw != null ? hw.getDiskSizeGb() : null,
            hw != null ? hw.getDiskHealthPercent() : null,
            hw != null ? hw.getDiskTemperatureCelsius() : null,
            request.getMacAddress2(),
            request.getCostCenter(),
            rental != null ? rental.getMonthlyValue() : null,
            request.getOperatingSystem(),
            request.getOsVersion(),
            request.getResponsiblePosition(),
            request.getResponsibleDocument(),
            request.getResponsiblePhone(),
            request.getResponsibleEmail(),
            request.getIpAddress(),
            request.getIpAssignment(),
            parseOptionalUuid(request.getAssociatedEquipmentId())
        );
        EquipmentResponseDTO response = registerEquipmentUseCase.register(command);
        auditService.log("CREATE", "EQUIPMENT", response.equipmentId().toString(), request.getName(),
                "Equipo creado: " + request.getName()
                        + (request.getSerialNumber() != null && !request.getSerialNumber().isBlank() ? " (S/N: " + request.getSerialNumber() + ")" : ""),
                principal != null ? principal.getName() : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener equipo por ID")
    public ResponseEntity<EquipmentDTO> getEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok(getEquipmentUseCase.getById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Listar equipos", description = "Filtros opcionales: status (ACTIVE, MAINTENANCE, INACTIVE, RETIRED) y category (sin distinguir mayusculas)")
    public ResponseEntity<PageResponseDTO<EquipmentDTO>> listEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        ListEquipmentCommand command = new ListEquipmentCommand(page, size, "name", status, category);
        return ResponseEntity.ok(listEquipmentUseCase.listAll(command));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar equipo")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEquipmentRequest request, Principal principal) {
        HardwareRequest hw = request.getHardware();
        RentalInfoRequest rental = request.getRentalInfo();

        UpdateEquipmentCommand command = new UpdateEquipmentCommand(
              id,
              request.getName(),
              request.getInventoryNumber(),
              request.getAssignedTo(),
              request.getLocation() != null ? request.getLocation().getBuilding() : null,
              request.getLocation() != null ? request.getLocation().getFloor() : null,
              request.getLocation() != null ? request.getLocation().getOffice() : null,
              principal != null ? principal.getName() : "SYSTEM",
              request.getBrand(),
              request.getModel(),
              request.getMacAddress(),
              request.getOwnershipType(),
              rental != null ? rental.getRentalCompany() : null,
              rental != null ? rental.getContactName() : null,
              rental != null ? rental.getContactPhone() : null,
              rental != null ? rental.getContactEmail() : null,
              rental != null ? rental.getStartDate() : null,
              rental != null ? rental.getEndDate() : null,
              rental != null ? rental.getContractNumber() : null,
              rental != null ? rental.getContractFileUrl() : null,
              rental != null ? rental.getNotes() : null,
              hw != null ? hw.getProcessor() : null,
              hw != null ? hw.getRamSizeGb() : null,
              hw != null ? hw.getRamType() : null,
              hw != null ? hw.getDiskType() : null,
              hw != null ? hw.getDiskSizeGb() : null,
              hw != null ? hw.getDiskHealthPercent() : null,
              hw != null ? hw.getDiskTemperatureCelsius() : null,
              request.getMacAddress2(),
              request.getCostCenter(),
              rental != null ? rental.getMonthlyValue() : null,
              request.getOperatingSystem(),
              request.getOsVersion(),
              request.getResponsiblePosition(),
              request.getResponsibleDocument(),
              request.getResponsiblePhone(),
              request.getResponsibleEmail(),
              request.getIpAddress(),
              request.getIpAssignment(),
              request.getAssociatedEquipmentId()
          );
        EquipmentResponseDTO updated = updateEquipmentUseCase.update(command);
        auditService.log("UPDATE", "EQUIPMENT", id.toString(), request.getName(),
                "Equipo actualizado: " + request.getName(),
                principal != null ? principal.getName() : "SYSTEM");
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado del equipo")
    public ResponseEntity<EquipmentResponseDTO> changeEquipmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request, Principal principal) {
        ChangeStatusCommand command = new ChangeStatusCommand(id, request.getStatus(), principal != null ? principal.getName() : "SYSTEM");
        EquipmentResponseDTO result = changeEquipmentStatusUseCase.changeStatus(command);
        String reason = request.getReason() != null && !request.getReason().isBlank()
                ? ". Motivo: " + request.getReason().trim() : "";
        auditService.log("CHANGE_STATUS", "EQUIPMENT", id.toString(), result.name() != null ? result.name() : "",
                "Estado cambiado a: " + request.getStatus() + reason,
                principal != null ? principal.getName() : "SYSTEM");
        // Al dar de baja un equipo se desasocian sus monitores
        if ("RETIRED".equalsIgnoreCase(result.status())) {
            equipmentMonitorService.detachMonitors(id, principal != null ? principal.getName() : "SYSTEM");
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Retirar equipo (borrado logico)")
    public ResponseEntity<Void> deleteEquipment(@PathVariable UUID id, Principal principal) {
        ChangeStatusCommand command = new ChangeStatusCommand(id, "RETIRED", principal != null ? principal.getName() : "SYSTEM");
        changeEquipmentStatusUseCase.changeStatus(command);
        auditService.log("DELETE", "EQUIPMENT", id.toString(), "",
                "Equipo retirado (soft delete)",
                principal != null ? principal.getName() : "SYSTEM");
        equipmentMonitorService.detachMonitors(id, principal != null ? principal.getName() : "SYSTEM");
        return ResponseEntity.noContent().build();
    }
}