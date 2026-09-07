package com.thoth.application.usecase;

import com.thoth.application.command.ChangeStatusCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.ChangeEquipmentStatusUseCaseImpl;
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
@DisplayName("ChangeEquipmentStatusUseCase Tests")
class ChangeEquipmentStatusUseCaseTest {
    
    @Mock
    private EquipmentRepositoryPort equipmentRepository;
    
    private ChangeEquipmentStatusUseCaseImpl useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new ChangeEquipmentStatusUseCaseImpl(equipmentRepository);
    }
    
    @Test
    @DisplayName("Should change status to MAINTENANCE successfully")
    void testChangeStatusToMaintenance() {
        UUID equipmentId = UUID.randomUUID();
        Location location = Location.of("Edificio A", "2", "201", "");
        Equipment equipment = Equipment.create(
            "Dell OptiPlex",
            EquipmentCategory.DESKTOP_PC,
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
        
        ChangeStatusCommand command = new ChangeStatusCommand(
            equipmentId,
            "MAINTENANCE",
            "admin"
        );
        
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        
        EquipmentResponseDTO response = useCase.changeStatus(command);
        
        assertNotNull(response);
        verify(equipmentRepository, times(1)).save(any());
    }
    
    @Test
    @DisplayName("Should throw when equipment not found")
    void testChangeStatusThrowsWhenNotFound() {
        UUID equipmentId = UUID.randomUUID();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        
        ChangeStatusCommand command = new ChangeStatusCommand(
            equipmentId,
            "MAINTENANCE",
            "admin"
        );
        
        assertThrows(EquipmentNotFoundException.class, () -> useCase.changeStatus(command));
    }
}
