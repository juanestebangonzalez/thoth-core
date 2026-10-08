package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.EquipmentPeripheralEntity;
import com.thoth.adapter.out.persistence.entity.PeripheralTypeEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentPeripheralRepository;
import com.thoth.adapter.out.persistence.repository.PeripheralTypeRepository;
import com.thoth.application.dto.PeripheralDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.exception.NotFoundException;
import com.thoth.application.port.output.EquipmentRepositoryPort;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PeripheralService Tests")
class PeripheralServiceTest {

    @Mock private EquipmentPeripheralRepository peripheralRepository;
    @Mock private PeripheralTypeRepository peripheralTypeRepository;
    @Mock private EquipmentRepositoryPort equipmentRepository;
    @Mock private AuditService auditService;

    private PeripheralService service;
    private UUID equipmentId;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        service = new PeripheralService(peripheralRepository, peripheralTypeRepository, equipmentRepository, auditService);
        equipmentId = UUID.randomUUID();
        equipment = Equipment.create("PC URGENCIAS", "DESKTOP", null, null, null, null,
            LocalDate.of(2024, 1, 1), null, null, null, "admin");
        equipment.setEquipmentId(equipmentId);
    }

    @Test
    @DisplayName("Tipo inexistente en el catalogo -> error y no se guarda")
    void unknownType() {
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(peripheralTypeRepository.findByNameIgnoreCase("IMPRESORA")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.create(equipmentId, "impresora", "hp", "admin"));
        assertTrue(ex.getMessage().contains("IMPRESORA"));
        verify(peripheralRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Tipo inactivo en el catalogo -> error")
    void inactiveType() {
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(peripheralTypeRepository.findByNameIgnoreCase("MOUSE"))
            .thenReturn(Optional.of(PeripheralTypeEntity.builder().name("MOUSE").active(false).build()));

        assertThrows(IllegalArgumentException.class, () -> service.create(equipmentId, "mouse", null, "admin"));
        verify(peripheralRepository, never()).save(any());
    }

    @Test
    @DisplayName("Equipo inexistente -> EquipmentNotFoundException (404)")
    void equipmentNotFound() {
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        assertThrows(EquipmentNotFoundException.class, () -> service.create(equipmentId, "MOUSE", null, "admin"));
        assertThrows(EquipmentNotFoundException.class, () -> service.list(equipmentId));
    }

    @Test
    @DisplayName("Alta valida: tipo y marca en mayusculas y auditoria CREATE/PERIPHERAL")
    void createOk() {
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(peripheralTypeRepository.findByNameIgnoreCase("TECLADO"))
            .thenReturn(Optional.of(PeripheralTypeEntity.builder().name("TECLADO").active(true).build()));
        when(peripheralRepository.save(any(EquipmentPeripheralEntity.class))).thenAnswer(inv -> {
            EquipmentPeripheralEntity e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        PeripheralDTO dto = service.create(equipmentId, " teclado ", "logitech", "tecnico1");

        assertEquals("TECLADO", dto.type());
        assertEquals("LOGITECH", dto.brand());
        assertEquals("tecnico1", dto.createdBy());
        verify(auditService).log(eq("CREATE"), eq("PERIPHERAL"), anyString(), eq("PC URGENCIAS"), anyString(), eq("tecnico1"));
    }

    @Test
    @DisplayName("Periferico de otro equipo -> NotFoundException (404)")
    void peripheralOfAnotherEquipment() {
        UUID peripheralId = UUID.randomUUID();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(peripheralRepository.findById(peripheralId)).thenReturn(Optional.of(
            EquipmentPeripheralEntity.builder().id(peripheralId).equipmentId(UUID.randomUUID()).type("MOUSE").build()));

        assertThrows(NotFoundException.class, () -> service.delete(equipmentId, peripheralId, "admin"));
        verify(peripheralRepository, never()).delete(any());
    }
}
