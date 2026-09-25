package com.thoth.application.usecase;

import com.thoth.application.command.CreateMaintenanceHistoryCommand;
import com.thoth.application.dto.MaintenanceHistoryDTO;
import com.thoth.application.mapper.MaintenanceHistoryDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.port.output.MaintenanceHistoryRepositoryPort;
import com.thoth.application.service.MaintenanceSchedulerService;
import com.thoth.application.usecase.impl.CreateMaintenanceHistoryUseCaseImpl;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.model.MaintenanceHistory;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.MaintenanceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenanceHistoryUseCaseImplTest {

    @Mock
    private MaintenanceHistoryRepositoryPort repository;

    @Mock
    private MaintenanceHistoryDtoMapper mapper;

    @Mock
    private EquipmentRepositoryPort equipmentRepository;

    @Mock
    private MaintenanceSchedulerService schedulerService;

    @InjectMocks
    private CreateMaintenanceHistoryUseCaseImpl useCase;

    private final UUID equipmentId = UUID.randomUUID();

    @Test
    void create_happyPath_savesAndReturnsDTO() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "PREVENTIVE", "Juan Perez", null,
                "Limpieza general", "Se limpio todo el equipo",
                LocalDate.of(2026, 6, 1), null, "admin", null, null);

        Equipment equipment = Equipment.builder()
                .equipmentId(equipmentId).name("PC-001").category("LAPTOP")
                .status(EquipmentStatus.ACTIVE).build();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        MaintenanceHistoryDTO result = useCase.create(cmd);

        assertThat(result).isNotNull();
        verify(repository).save(any(MaintenanceHistory.class));
        verify(equipmentRepository).save(equipment);
    }

    @Test
    void create_uppercasesTextFields() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "corrective", "maria lopez", null,
                "disco danado", "se reemplazo el disco ssd",
                null, null, "admin", null, null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        ArgumentCaptor<MaintenanceHistory> captor = ArgumentCaptor.forClass(MaintenanceHistory.class);
        verify(repository).save(captor.capture());
        MaintenanceHistory saved = captor.getValue();
        assertThat(saved.getTechnicianName()).isEqualTo("MARIA LOPEZ");
        assertThat(saved.getReason()).isEqualTo("DISCO DANADO");
        assertThat(saved.getDescription()).isEqualTo("SE REEMPLAZO EL DISCO SSD");
    }

    @Test
    void create_withParts_savesPartsWithUppercase() {
        var part1 = new CreateMaintenanceHistoryCommand.PartCommand(
                "disco ssd", "SN-123", "desgaste", LocalDate.of(2026, 3, 15), "TK-001");
        var part2 = new CreateMaintenanceHistoryCommand.PartCommand(
                "memoria ram", null, "ampliacion", null, null);

        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "CORRECTIVE", "Pedro", null,
                "Upgrade", null, null, List.of(part1, part2), "admin", null, null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        ArgumentCaptor<MaintenanceHistory> captor = ArgumentCaptor.forClass(MaintenanceHistory.class);
        verify(repository).save(captor.capture());
        MaintenanceHistory saved = captor.getValue();
        assertThat(saved.getPartsReplaced()).hasSize(2);
        assertThat(saved.getPartsReplaced().get(0).getPartName()).isEqualTo("DISCO SSD");
        assertThat(saved.getPartsReplaced().get(0).getPartSerialNumber()).isEqualTo("SN-123");
        assertThat(saved.getPartsReplaced().get(0).getPurchaseDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(saved.getPartsReplaced().get(0).getTicketNumber()).isEqualTo("TK-001");
        assertThat(saved.getPartsReplaced().get(1).getPartName()).isEqualTo("MEMORIA RAM");
        assertThat(saved.getPartsReplaced().get(1).getPartSerialNumber()).isNull();
    }

    @Test
    void create_withNoParts_savesEmptyList() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "PREVENTIVE", "Ana", null,
                "Revision", null, null, null, "admin", null, null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        ArgumentCaptor<MaintenanceHistory> captor = ArgumentCaptor.forClass(MaintenanceHistory.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPartsReplaced()).isEmpty();
    }

    @Test
    void create_invalidMaintenanceType_throwsException() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "INVALID_TYPE", "Tech", null,
                "Motivo", null, null, null, "admin", null, null);

        assertThatThrownBy(() -> useCase.create(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid maintenance type");
    }

    @Test
    void create_preventiveWithoutNextDate_calculatesFromScheduler() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "PREVENTIVE", "Tech", null,
                "Revision programada", null, null, null, "admin", null, null);

        Equipment equipment = Equipment.builder()
                .equipmentId(equipmentId).name("SRV-001").category("SERVER")
                .status(EquipmentStatus.ACTIVE).build();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(schedulerService.calculateNextMaintenanceDate("SERVER", LocalDate.now()))
                .thenReturn(LocalDate.now().plusMonths(3));
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        verify(schedulerService).calculateNextMaintenanceDate("SERVER", LocalDate.now());
        ArgumentCaptor<Equipment> eqCaptor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(eqCaptor.capture());
        assertThat(eqCaptor.getValue().getNextMaintenanceDate()).isEqualTo(LocalDate.now().plusMonths(3));
    }

    @Test
    void create_equipmentInMaintenance_setsBackToActive() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "CORRECTIVE", "Tech", null,
                "Reparacion completa", null, null, null, "admin", null, null);

        Equipment equipment = Equipment.builder()
                .equipmentId(equipmentId).name("PC-002").category("DESKTOP")
                .status(EquipmentStatus.MAINTENANCE).build();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        ArgumentCaptor<Equipment> eqCaptor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(eqCaptor.capture());
        assertThat(eqCaptor.getValue().getStatus()).isEqualTo(EquipmentStatus.ACTIVE);
    }

    @Test
    void create_withSignature_setsSignatureFields() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "PREVENTIVE", "Tech", null,
                "Revision", null, null, null, "admin",
                "base64data==", "Dr. Garcia");

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        ArgumentCaptor<MaintenanceHistory> captor = ArgumentCaptor.forClass(MaintenanceHistory.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSignatureBase64()).isEqualTo("base64data==");
        assertThat(captor.getValue().getSignedBy()).isEqualTo("Dr. Garcia");
    }

    @Test
    void create_equipmentNotFound_stillSavesMaintenanceRecord() {
        CreateMaintenanceHistoryCommand cmd = new CreateMaintenanceHistoryCommand(
                equipmentId, "CORRECTIVE", "Tech", null,
                "Motivo", null, null, null, "admin", null, null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        when(repository.save(any(MaintenanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(mock(MaintenanceHistoryDTO.class));

        useCase.create(cmd);

        verify(repository).save(any(MaintenanceHistory.class));
        verify(equipmentRepository, never()).save(any());
    }
}
