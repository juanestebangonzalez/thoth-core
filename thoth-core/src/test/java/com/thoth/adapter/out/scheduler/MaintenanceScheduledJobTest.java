package com.thoth.adapter.out.scheduler;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.application.service.AuditService;
import com.thoth.domain.valueobject.EquipmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceScheduledJobTest {

    @Mock private EquipmentJpaRepository equipmentRepository;
    @Mock private AuditService auditService;

    @InjectMocks private MaintenanceScheduledJob job;

    @Test
    void moveEquipmentsToMaintenance_updatesOverdueEquipment() {
        EquipmentEntity eq = EquipmentEntity.builder()
            .equipmentId(UUID.randomUUID())
            .name("Laptop-001")
            .status(EquipmentStatus.ACTIVE)
            .nextMaintenanceDate(LocalDate.now().minusDays(1))
            .build();
        when(equipmentRepository.findDueForMaintenance(any(LocalDate.class)))
            .thenReturn(List.of(eq));

        job.moveEquipmentsToMaintenance();

        assertThat(eq.getStatus()).isEqualTo(EquipmentStatus.MAINTENANCE);
        verify(equipmentRepository).save(eq);
        verify(auditService).log(eq("SCHEDULED_JOB"), eq("MAINTENANCE"),
            isNull(), isNull(), contains("1 equipos"), eq("SYSTEM"));
    }

    @Test
    void moveEquipmentsToMaintenance_noEquipmentsDue() {
        when(equipmentRepository.findDueForMaintenance(any(LocalDate.class)))
            .thenReturn(List.of());

        job.moveEquipmentsToMaintenance();

        verify(equipmentRepository, never()).save(any());
        verify(auditService, never()).log(anyString(), anyString(),
            any(), any(), anyString(), anyString());
    }

    @Test
    void moveEquipmentsToMaintenance_handlesException() {
        when(equipmentRepository.findDueForMaintenance(any(LocalDate.class)))
            .thenThrow(new RuntimeException("DB connection lost"));

        // Should not throw — error is caught and logged
        job.moveEquipmentsToMaintenance();

        verify(auditService).log(eq("SCHEDULED_JOB_ERROR"), eq("MAINTENANCE"),
            isNull(), isNull(), contains("DB connection lost"), eq("SYSTEM"));
    }

    @Test
    void moveEquipmentsToMaintenance_multipleEquipments() {
        EquipmentEntity eq1 = EquipmentEntity.builder()
            .equipmentId(UUID.randomUUID()).name("PC-001")
            .status(EquipmentStatus.ACTIVE).nextMaintenanceDate(LocalDate.now())
            .build();
        EquipmentEntity eq2 = EquipmentEntity.builder()
            .equipmentId(UUID.randomUUID()).name("Monitor-002")
            .status(EquipmentStatus.ACTIVE).nextMaintenanceDate(LocalDate.now().minusDays(3))
            .build();
        when(equipmentRepository.findDueForMaintenance(any(LocalDate.class)))
            .thenReturn(List.of(eq1, eq2));

        job.moveEquipmentsToMaintenance();

        assertThat(eq1.getStatus()).isEqualTo(EquipmentStatus.MAINTENANCE);
        assertThat(eq2.getStatus()).isEqualTo(EquipmentStatus.MAINTENANCE);
        verify(equipmentRepository, times(2)).save(any());
        verify(auditService).log(eq("SCHEDULED_JOB"), eq("MAINTENANCE"),
            isNull(), isNull(), contains("2 equipos"), eq("SYSTEM"));
    }
}
