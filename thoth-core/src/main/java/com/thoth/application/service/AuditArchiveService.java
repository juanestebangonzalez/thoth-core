package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.AuditLogArchiveEntity;
import com.thoth.adapter.out.persistence.entity.AuditLogEntity;
import com.thoth.adapter.out.persistence.repository.AuditLogArchiveRepository;
import com.thoth.adapter.out.persistence.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditArchiveService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogArchiveRepository archiveRepository;

    /**
     * Ejecuta el primer dia de cada mes a las 3:00 AM.
     * Archiva registros con mas de 6 meses de antiguedad.
     * Se ejecuta mensualmente para no acumular demasiados registros,
     * pero solo mueve los que tengan >= 6 meses.
     */
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void archiveOldLogs() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        log.info("Iniciando archivado de audit logs anteriores a {}", sixMonthsAgo);

        List<AuditLogEntity> oldLogs = auditLogRepository.findByPerformedAtBefore(sixMonthsAgo);

        if (oldLogs.isEmpty()) {
            log.info("No hay registros para archivar");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<AuditLogArchiveEntity> archived = oldLogs.stream()
                .map(entry -> AuditLogArchiveEntity.builder()
                        .id(entry.getId())
                        .action(entry.getAction())
                        .module(entry.getModule())
                        .entityId(entry.getEntityId())
                        .entityName(entry.getEntityName())
                        .details(entry.getDetails())
                        .performedBy(entry.getPerformedBy())
                        .performedAt(entry.getPerformedAt())
                        .ipAddress(entry.getIpAddress())
                        .archivedAt(now)
                        .build())
                .toList();

        archiveRepository.saveAll(archived);
        auditLogRepository.deleteAll(oldLogs);

        log.info("Archivados {} registros de auditoria", archived.size());
    }

    /**
     * Ejecutar archivado manualmente (desde el controller).
     */
    @Transactional
    public Map<String, Object> archiveManually() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<AuditLogEntity> oldLogs = auditLogRepository.findByPerformedAtBefore(sixMonthsAgo);

        if (oldLogs.isEmpty()) {
            return Map.of("message", "No hay registros para archivar", "archived", 0);
        }

        LocalDateTime now = LocalDateTime.now();
        List<AuditLogArchiveEntity> archived = oldLogs.stream()
                .map(entry -> AuditLogArchiveEntity.builder()
                        .id(entry.getId())
                        .action(entry.getAction())
                        .module(entry.getModule())
                        .entityId(entry.getEntityId())
                        .entityName(entry.getEntityName())
                        .details(entry.getDetails())
                        .performedBy(entry.getPerformedBy())
                        .performedAt(entry.getPerformedAt())
                        .ipAddress(entry.getIpAddress())
                        .archivedAt(now)
                        .build())
                .toList();

        archiveRepository.saveAll(archived);
        auditLogRepository.deleteAll(oldLogs);

        return Map.of(
                "message", "Archivado completado",
                "archived", archived.size(),
                "cutoffDate", sixMonthsAgo.toString()
        );
    }

    /**
     * Consultar logs archivados con filtros.
     */
    public Page<AuditLogArchiveEntity> getArchived(String module, String user, String action, int page, int size) {
        return archiveRepository.findFiltered(
                module != null && module.isBlank() ? null : module,
                user != null && user.isBlank() ? null : user,
                action != null && action.isBlank() ? null : action,
                PageRequest.of(page, size)
        );
    }

    /**
     * Estadisticas de archivado.
     */
    public Map<String, Object> getStats() {
        long activeCount = auditLogRepository.count();
        long archivedCount = archiveRepository.count();
        return Map.of(
                "activeRecords", activeCount,
                "archivedRecords", archivedCount,
                "totalRecords", activeCount + archivedCount
        );
    }
}
