package com.thoth.application.service;

import com.thoth.application.dto.AssociatedMonitorDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Consulta y mantenimiento de los monitores asociados a un equipo. */
@Service
@RequiredArgsConstructor
public class EquipmentMonitorService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentMonitorService.class);

    private final EquipmentRepositoryPort equipmentRepository;
    private final AuditService auditService;

    /** Monitores asociados al equipo. 404 (EquipmentNotFoundException) si el equipo no existe. */
    @Transactional(readOnly = true)
    public List<AssociatedMonitorDTO> listMonitors(UUID equipmentId) {
        if (equipmentRepository.findById(equipmentId).isEmpty()) {
            throw new EquipmentNotFoundException(equipmentId);
        }
        return equipmentRepository.findByAssociatedEquipmentId(equipmentId).stream()
            .sorted(Comparator.comparing(e -> e.getName() != null ? e.getName() : ""))
            .map(EquipmentMonitorService::toDTO)
            .toList();
    }

    /**
     * Desasocia todos los monitores del equipo (por ejemplo, al darlo de baja) y deja el registro
     * en la auditoria. Devuelve cuantos monitores se desasociaron.
     */
    @Transactional
    public int detachMonitors(UUID equipmentId, String performedBy) {
        List<Equipment> monitors = equipmentRepository.findByAssociatedEquipmentId(equipmentId);
        if (monitors.isEmpty()) return 0;
        String user = performedBy != null && !performedBy.isBlank() ? performedBy : "SYSTEM";
        String pcName = equipmentRepository.findById(equipmentId).map(Equipment::getName).orElse(equipmentId.toString());
        for (Equipment monitor : monitors) {
            monitor.clearAssociation();
            monitor.setLastModifiedBy(user);
            equipmentRepository.save(monitor);
            auditService.log("UPDATE", "EQUIPMENT", monitor.getEquipmentId().toString(), monitor.getName(),
                "Monitor desasociado por baja del equipo " + pcName, user);
        }
        log.info("Se desasociaron {} monitores del equipo {}", monitors.size(), equipmentId);
        return monitors.size();
    }

    public static AssociatedMonitorDTO toDTO(Equipment e) {
        return new AssociatedMonitorDTO(
            e.getEquipmentId(),
            e.getName(),
            e.getInventoryNumber(),
            e.getBrand(),
            e.getModel(),
            e.getSerialNumber(),
            e.getStatus() != null ? e.getStatus().name() : null,
            e.getOwnershipType() != null ? e.getOwnershipType().name() : "OWNED",
            e.getRentalInfo() != null ? e.getRentalInfo().getRentalCompany() : null,
            e.getRentalInfo() != null ? e.getRentalInfo().getMonthlyValue() : null
        );
    }
}
