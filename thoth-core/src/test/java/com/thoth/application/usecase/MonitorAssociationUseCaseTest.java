package com.thoth.application.usecase;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.UpdateEquipmentUseCaseImpl;
import com.thoth.domain.model.Equipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Asociacion de monitores al actualizar un equipo")
class MonitorAssociationUseCaseTest {

    @Mock
    private EquipmentRepositoryPort equipmentRepository;

    private UpdateEquipmentUseCaseImpl useCase;
    private Equipment monitor;
    private Equipment pc;

    @BeforeEach
    void setUp() {
        useCase = new UpdateEquipmentUseCaseImpl(equipmentRepository, new EquipmentDtoMapper());
        monitor = Equipment.create("MONITOR LG", "MONITOR", null, null, null, null,
            LocalDate.of(2024, 1, 1), null, null, null, "admin");
        monitor.setEquipmentId(UUID.randomUUID());
        pc = Equipment.create("PC URGENCIAS", "DESKTOP", null, null, null, null,
            LocalDate.of(2024, 1, 1), null, null, null, "admin");
        pc.setEquipmentId(UUID.randomUUID());
        pc.setInventoryNumber("INV-PC1");
    }

    private static UpdateEquipmentCommand command(UUID id, String associatedEquipmentId) {
        return new UpdateEquipmentCommand(id, null, null, null, null, null, null, "admin",
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, associatedEquipmentId);
    }

    @Test
    @DisplayName("Asociar un monitor a un PC devuelve el nombre e inventario del PC")
    void associate() {
        when(equipmentRepository.findById(monitor.getEquipmentId())).thenReturn(Optional.of(monitor));
        when(equipmentRepository.findById(pc.getEquipmentId())).thenReturn(Optional.of(pc));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentResponseDTO dto = useCase.update(command(monitor.getEquipmentId(), pc.getEquipmentId().toString()));

        assertEquals(pc.getEquipmentId(), dto.associatedEquipmentId());
        assertEquals("PC URGENCIAS", dto.associatedEquipmentName());
        assertEquals("INV-PC1", dto.associatedEquipmentInventory());
    }

    @Test
    @DisplayName("Cadena vacia desasocia y null no modifica")
    void clearAndKeep() {
        monitor.setAssociatedEquipmentId(pc.getEquipmentId());
        when(equipmentRepository.findById(monitor.getEquipmentId())).thenReturn(Optional.of(monitor));
        when(equipmentRepository.findById(pc.getEquipmentId())).thenReturn(Optional.of(pc));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(pc.getEquipmentId(), useCase.update(command(monitor.getEquipmentId(), null)).associatedEquipmentId());
        assertNull(useCase.update(command(monitor.getEquipmentId(), "")).associatedEquipmentId());
    }

    @Test
    @DisplayName("Destino retirado o inexistente -> IllegalArgumentException (400) sin guardar")
    void invalidTarget() {
        pc.markAsRetired();
        UUID unknown = UUID.randomUUID();
        when(equipmentRepository.findById(monitor.getEquipmentId())).thenReturn(Optional.of(monitor));
        when(equipmentRepository.findById(pc.getEquipmentId())).thenReturn(Optional.of(pc));
        when(equipmentRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> useCase.update(command(monitor.getEquipmentId(), pc.getEquipmentId().toString())));
        assertThrows(IllegalArgumentException.class,
            () -> useCase.update(command(monitor.getEquipmentId(), unknown.toString())));
        assertThrows(IllegalArgumentException.class,
            () -> useCase.update(command(monitor.getEquipmentId(), "no-es-uuid")));
        verify(equipmentRepository, never()).save(any());
    }
}
