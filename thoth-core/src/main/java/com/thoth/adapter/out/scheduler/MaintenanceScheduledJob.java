package com.thoth.adapter.out.scheduler;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.application.service.AuditService;
import com.thoth.domain.valueobject.EquipmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceScheduledJob {

    private final EquipmentJpaRepository equipmentRepository;
    private final AuditService auditService;

    /**
     * Corre todos los dias a las 6:00 AM.
     * Busca equipos cuya next_maintenance_date sea HOY o anterior y los pasa a MAINTENANCE.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void moveEquipmentsToMaintenance() {
        LocalDate today = LocalDate.now();
        log.info("[JobMantenimiento] Iniciando job diario - {}", today);

        try {
            // DT-19: Query filtrada en DB en lugar de findAll()
            List<EquipmentEntity> dueEquipments = equipmentRepository.findDueForMaintenance(today);
            int updated = 0;

            for (EquipmentEntity eq : dueEquipments) {
                eq.setStatus(EquipmentStatus.MAINTENANCE);
                equipmentRepository.save(eq);
                updated++;
                log.info("[JobMantenimiento] Equipo {} -> MAINTENANCE (fecha programada: {})",
                    eq.getName(), eq.getNextMaintenanceDate());
            }

            log.info("[JobMantenimiento] Job finalizado - {} equipos movidos a MAINTENANCE", updated);

            // DT-32: Registrar ejecución exitosa en auditoría
            if (updated > 0) {
                auditService.log("SCHEDULED_JOB", "MAINTENANCE", null, null,
                    "Job diario ejecutado: " + updated + " equipos movidos a MAINTENANCE", "SYSTEM");
            }
        } catch (Exception e) {
            // DT-32: Registrar fallo del job para que el admin lo vea en auditoría
            log.error("[JobMantenimiento] ERROR en job diario: {}", e.getMessage(), e);
            try {
                auditService.log("SCHEDULED_JOB_ERROR", "MAINTENANCE", null, null,
                    "Error en job diario de mantenimiento: " + e.getMessage(), "SYSTEM");
            } catch (Exception auditEx) {
                log.error("[JobMantenimiento] No se pudo registrar el error en auditoria: {}", auditEx.getMessage());
            }
        }
    }
}
