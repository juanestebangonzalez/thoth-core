package com.thoth.application.usecase;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.UpdateEquipmentUseCaseImpl;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateEquipmentUseCase Tests")
class UpdateEquipmentUseCaseTest {
    
    @Mock
    private EquipmentRepositoryPort equipmentRepository;

    private UpdateEquipmentUseCaseImpl useCase;
    private EquipmentDtoMapper equipmentMapper;

    @BeforeEach
    void setUp() {
        equipmentMapper = new EquipmentDtoMapper();
        useCase = new UpdateEquipmentUseCaseImpl(equipmentRepository, equipmentMapper);
    }
    
    @Test
    @DisplayName("Should update equipment successfully")
    void testUpdateEquipmentSuccessfully() {
        UUID equipmentId = UUID.randomUUID();
        Location location = Location.of("Edificio A", "2", "201", "");
        Equipment equipment = Equipment.create(
            "Dell OptiPlex",
            EquipmentCategory.DESKTOP,
            "SN-2024-00001",
            "Dell",
            "OptiPlex",
            "00:1A:2B:3C:4D:5E",
            LocalDate.of(2023, 1, 15),
            BigDecimal.valueOf(1200),
            location,
            "Juan",
            "admin"
        );
        equipment.setEquipmentId(equipmentId);
        
        UpdateEquipmentCommand command = new UpdateEquipmentCommand(
            equipmentId,
            "Dell OptiPlex Updated",
            null,
            "Maria",
            "Edificio B",
            "3",
            "305",
            "admin",
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null
        );
        
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        
        EquipmentResponseDTO response = useCase.update(command);
        
        assertNotNull(response);
        verify(equipmentRepository, times(1)).save(any());
    }
    
    @Test
    @DisplayName("Should throw when equipment not found")
    void testUpdateThrowsWhenNotFound() {
        UUID equipmentId = UUID.randomUUID();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        
        UpdateEquipmentCommand command = new UpdateEquipmentCommand(
            equipmentId,
            "Updated",
            null,
            "Maria",
            "B",
            "3",
            "305",
            "admin",
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null
        );
        
        assertThrows(EquipmentNotFoundException.class, () -> useCase.update(command));
    }
}
