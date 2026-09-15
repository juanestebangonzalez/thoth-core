package com.thoth.adapter.out.scheduler;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
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

    /**
     * Corre todos los dias a las 6:00 AM.
     * Busca equipos cuya next_maintenance_date sea HOY y los pasa a MAINTENANCE.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void moveEquipmentsToMaintenance() {
        LocalDate today = LocalDate.now();
        log.info("[JobMantenimiento] Iniciando job diario - {}", today);

        List<EquipmentEntity> allEquipments = equipmentRepository.findAll();
        int updated = 0;

        for (EquipmentEntity eq : allEquipments) {
            if (eq.getNextMaintenanceDate() == null) continue;
            if (eq.getStatus() == EquipmentStatus.RETIRED) continue;
            if (eq.getStatus() == EquipmentStatus.MAINTENANCE) continue;

            if (!eq.getNextMaintenanceDate().isAfter(today)) {
                eq.setStatus(EquipmentStatus.MAINTENANCE);
                equipmentRepository.save(eq);
                updated++;
                log.info("[JobMantenimiento] Equipo {} -> MAINTENANCE (fecha programada: {})",
                    eq.getName(), eq.getNextMaintenanceDate());
            }
        }

        log.info("[JobMantenimiento] Job finalizado - {} equipos movidos a MAINTENANCE", updated);
    }

    /**
     * Job de prueba - corre cada 5 minutos para pruebas rapidas.
     * COMENTAR EN PRODUCCION!
     */
    // @Scheduled(fixedDelay = 300000) // cada 5 min
    // public void testJob() {
    //     moveEquipmentsToMaintenance();
    // }
}