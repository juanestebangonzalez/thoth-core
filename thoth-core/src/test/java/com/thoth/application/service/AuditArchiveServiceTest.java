package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.AuditLogArchiveEntity;
import com.thoth.adapter.out.persistence.entity.AuditLogEntity;
import com.thoth.adapter.out.persistence.repository.AuditLogArchiveRepository;
import com.thoth.adapter.out.persistence.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditArchiveServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogArchiveRepository archiveRepository;

    @InjectMocks
    private AuditArchiveService service;

    @Captor
    private ArgumentCaptor<List<AuditLogArchiveEntity>> archiveCaptor;

    @Test
    void archiveOldLogs_withOldRecords_archivesAndDeletes() {
        AuditLogEntity old1 = buildAuditLog("LOGIN", "AUTH", "admin");
        AuditLogEntity old2 = buildAuditLog("CREATE_EQUIPMENT", "EQUIPMENT", "tech1");

        when(auditLogRepository.findByPerformedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(old1, old2));

        service.archiveOldLogs();

        verify(archiveRepository).saveAll(archiveCaptor.capture());
        List<AuditLogArchiveEntity> saved = archiveCaptor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getAction()).isEqualTo("LOGIN");
        assertThat(saved.get(0).getModule()).isEqualTo("AUTH");
        assertThat(saved.get(0).getArchivedAt()).isNotNull();
        assertThat(saved.get(1).getAction()).isEqualTo("CREATE_EQUIPMENT");

        verify(auditLogRepository).deleteAll(List.of(old1, old2));
    }

    @Test
    void archiveOldLogs_noOldRecords_doesNothing() {
        when(auditLogRepository.findByPerformedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        service.archiveOldLogs();

        verify(archiveRepository, never()).saveAll(any());
        verify(auditLogRepository, never()).deleteAll(anyList());
    }

    @Test
    void archiveManually_withOldRecords_returnsArchivedCount() {
        AuditLogEntity old1 = buildAuditLog("DELETE", "USERS", "admin");

        when(auditLogRepository.findByPerformedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(old1));

        Map<String, Object> result = service.archiveManually();

        assertThat(result.get("archived")).isEqualTo(1);
        assertThat(result.get("message")).isEqualTo("Archivado completado");
        assertThat(result).containsKey("cutoffDate");
        verify(archiveRepository).saveAll(any());
        verify(auditLogRepository).deleteAll(List.of(old1));
    }

    @Test
    void archiveManually_noOldRecords_returnsZero() {
        when(auditLogRepository.findByPerformedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result = service.archiveManually();

        assertThat(result.get("archived")).isEqualTo(0);
        assertThat(result.get("message")).isEqualTo("No hay registros para archivar");
        verify(archiveRepository, never()).saveAll(any());
    }

    @Test
    void getArchived_delegatesToRepository() {
        Page<AuditLogArchiveEntity> page = new PageImpl<>(Collections.emptyList());
        when(archiveRepository.findFiltered(any(), any(), any(), any()))
                .thenReturn(page);

        Page<AuditLogArchiveEntity> result = service.getArchived("USERS", "admin", "LOGIN", 0, 25);

        assertThat(result).isEqualTo(page);
        verify(archiveRepository).findFiltered("USERS", "admin", "LOGIN", PageRequest.of(0, 25));
    }

    @Test
    void getArchived_blankFilters_passNull() {
        Page<AuditLogArchiveEntity> page = new PageImpl<>(Collections.emptyList());
        when(archiveRepository.findFiltered(any(), any(), any(), any()))
                .thenReturn(page);

        service.getArchived("  ", "", " ", 0, 10);

        verify(archiveRepository).findFiltered(null, null, null, PageRequest.of(0, 10));
    }

    @Test
    void getStats_returnsCombinedCounts() {
        when(auditLogRepository.count()).thenReturn(150L);
        when(archiveRepository.count()).thenReturn(350L);

        Map<String, Object> stats = service.getStats();

        assertThat(stats.get("activeRecords")).isEqualTo(150L);
        assertThat(stats.get("archivedRecords")).isEqualTo(350L);
        assertThat(stats.get("totalRecords")).isEqualTo(500L);
    }

    @Test
    void archiveOldLogs_preservesAllFields() {
        UUID logId = UUID.randomUUID();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(logId)
                .action("CHANGE_ROLE")
                .module("USERS")
                .entityId("user-123")
                .entityName("jdoe")
                .details("Nuevo rol: ADMIN")
                .performedBy("superadmin")
                .performedAt(LocalDateTime.of(2025, 1, 15, 10, 30))
                .ipAddress("192.168.1.100")
                .build();

        when(auditLogRepository.findByPerformedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(entity));

        service.archiveOldLogs();

        verify(archiveRepository).saveAll(archiveCaptor.capture());
        AuditLogArchiveEntity archived = archiveCaptor.getValue().get(0);
        assertThat(archived.getId()).isEqualTo(logId);
        assertThat(archived.getAction()).isEqualTo("CHANGE_ROLE");
        assertThat(archived.getModule()).isEqualTo("USERS");
        assertThat(archived.getEntityId()).isEqualTo("user-123");
        assertThat(archived.getEntityName()).isEqualTo("jdoe");
        assertThat(archived.getDetails()).isEqualTo("Nuevo rol: ADMIN");
        assertThat(archived.getPerformedBy()).isEqualTo("superadmin");
        assertThat(archived.getPerformedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30));
        assertThat(archived.getIpAddress()).isEqualTo("192.168.1.100");
    }

    private AuditLogEntity buildAuditLog(String action, String module, String performedBy) {
        return AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action(action)
                .module(module)
                .entityId(UUID.randomUUID().toString())
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now().minusMonths(7))
                .build();
    }
}
