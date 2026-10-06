package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import com.thoth.adapter.out.persistence.repository.MaintenanceHistoryJpaRepository;
import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.port.input.RegisterEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Importa UNA fila en su propia transaccion (REQUIRES_NEW), de modo que el fallo
 * de una fila no deshaga las demas. Es un bean separado para evitar self-invocation.
 */
@Component
@RequiredArgsConstructor
public class EquipmentImportRowProcessor {

    static final String IMPORT_REASON = "MANTENIMIENTO IMPORTADO DESDE PLANTILLA";
    static final String DEFAULT_TECHNICIAN = "IMPORTACION";

    private final RegisterEquipmentUseCase registerEquipmentUseCase;
    private final EquipmentRepositoryPort equipmentRepository;
    private final MaintenanceHistoryJpaRepository maintenanceHistoryRepository;
    private final MaintenanceSchedulerService schedulerService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID importRow(RegisterEquipmentCommand command,
                          LocalDate lastMaintenanceDate,
                          String lastMaintenanceType,
                          String lastMaintenanceTechnician,
                          String lastMaintenanceDescription,
                          String user) {
        EquipmentResponseDTO created = registerEquipmentUseCase.register(command);
        UUID equipmentId = created.equipmentId();

        if (lastMaintenanceDate != null && lastMaintenanceType != null && !lastMaintenanceType.isBlank()) {
            String category = command.category() != null ? command.category().trim().toUpperCase() : null;
            LocalDate next = schedulerService.calculateNextMaintenanceDate(category, lastMaintenanceDate);

            MaintenanceHistoryEntity history = MaintenanceHistoryEntity.builder()
                .maintenanceId(UUID.randomUUID())
                .equipmentId(equipmentId)
                .maintenanceType(lastMaintenanceType.trim().toUpperCase())
                .performedDate(lastMaintenanceDate.atStartOfDay())
                .technicianName(lastMaintenanceTechnician != null && !lastMaintenanceTechnician.isBlank()
                    ? lastMaintenanceTechnician.trim().toUpperCase() : DEFAULT_TECHNICIAN)
                .reason(IMPORT_REASON)
                .description(lastMaintenanceDescription != null ? lastMaintenanceDescription.trim() : null)
                .nextScheduledDate(next)
                .createdAt(LocalDateTime.now())
                .createdBy(user)
                .build();
            maintenanceHistoryRepository.save(history);

            // La proxima fecha se calcula desde el ultimo mantenimiento (aunque quede vencida)
            Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalStateException("No se encontro el equipo recien importado"));
            equipment.setNextMaintenanceDate(next);
            equipmentRepository.save(equipment);
        }
        return equipmentId;
    }
}
