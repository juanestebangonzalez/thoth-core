package com.thoth.application.usecase.impl;

import com.thoth.application.command.CreateMaintenanceHistoryCommand;
import com.thoth.application.dto.MaintenanceHistoryDTO;
import com.thoth.application.mapper.MaintenanceHistoryDtoMapper;
import com.thoth.application.port.input.CreateMaintenanceHistoryUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.port.output.MaintenanceHistoryRepositoryPort;
import com.thoth.application.service.MaintenanceSchedulerService;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.model.MaintenanceHistory;
import com.thoth.domain.valueobject.PartReplaced;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateMaintenanceHistoryUseCaseImpl implements CreateMaintenanceHistoryUseCase {

    private final MaintenanceHistoryRepositoryPort repository;
    private final MaintenanceHistoryDtoMapper mapper;
    private final EquipmentRepositoryPort equipmentRepository;
    private final MaintenanceSchedulerService schedulerService;

    @Override
    public MaintenanceHistoryDTO create(CreateMaintenanceHistoryCommand command) {
        String type = command.maintenanceType();
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("El tipo de mantenimiento es obligatorio");
        }
        type = type.toUpperCase().trim();

        List<PartReplaced> parts = new ArrayList<>();
        if (command.partsReplaced() != null) {
            for (CreateMaintenanceHistoryCommand.PartCommand p : command.partsReplaced()) {
                parts.add(PartReplaced.builder()
                    .partName(toUpper(p.partName()))
                    .partSerialNumber(toUpper(p.partSerialNumber()))
                    .reason(toUpper(p.reason()))
                    .purchaseDate(p.purchaseDate())
                    .ticketNumber(toUpper(p.ticketNumber()))
                    .build());
            }
        }

        MaintenanceHistory maintenance = MaintenanceHistory.create(
            command.equipmentId(),
            type,
            toUpper(command.technicianName()),
            toUpper(command.reason()),
            toUpper(command.description()),
            command.nextScheduledDate(),
            parts,
            command.createdBy()
        );

        // Firma digital
        if (command.signatureBase64() != null && !command.signatureBase64().isBlank()) {
            maintenance.setSignatureBase64(command.signatureBase64());
        }
        if (command.signedBy() != null && !command.signedBy().isBlank()) {
            maintenance.setSignedBy(command.signedBy());
        }

        MaintenanceHistory saved = repository.save(maintenance);

        // Actualizar el equipo: recalcular proxima fecha si es preventivo
        // Tambien si estaba en MAINTENANCE, ponerlo ACTIVE
        try {
            Equipment equipment = equipmentRepository.findById(command.equipmentId()).orElse(null);
            if (equipment != null) {
                LocalDate nextDate = command.nextScheduledDate();
                if (nextDate == null && "PREVENTIVE".equals(type)) {
                    nextDate = schedulerService.calculateNextMaintenanceDate(equipment.getCategory(), LocalDate.now());
                }
                if (nextDate != null) {
                    equipment.setNextMaintenanceDate(nextDate);
                }
                // Si estaba en mantenimiento, volver a activo
                if (equipment.getStatus() != null && equipment.getStatus().name().equals("MAINTENANCE")) {
                    equipment.markAsActive();
                }
                equipmentRepository.save(equipment);
            }
        } catch (Exception e) {
            // No fallar si no se puede actualizar el equipo
        }

        return mapper.toDTO(saved);
    }

    private String toUpper(String value) {
        return value != null ? value.toUpperCase().trim() : null;
    }
}
