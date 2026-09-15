package com.thoth.application.usecase;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.GetEquipmentUseCaseImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetEquipmentUseCase Tests")
class GetEquipmentUseCaseTest {
    
    @Mock
    private EquipmentRepositoryPort equipmentRepository;
    
    private GetEquipmentUseCaseImpl useCase;
    private EquipmentDtoMapper equipmentMapper;
    
    @BeforeEach
    void setUp() {
        equipmentMapper = new EquipmentDtoMapper();
        useCase = new GetEquipmentUseCaseImpl(equipmentRepository, equipmentMapper);
    }
    
    @Test
    @DisplayName("Should get equipment by ID successfully")
    void testGetEquipmentById() {
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
        
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        
        EquipmentDTO result = useCase.getById(equipmentId);
        
        assertNotNull(result);
        assertEquals("Dell OptiPlex", result.name());
    }
    
    @Test
    @DisplayName("Should throw when equipment not found")
    void testGetEquipmentThrowsWhenNotFound() {
        UUID equipmentId = UUID.randomUUID();
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());
        
        assertThrows(EquipmentNotFoundException.class, () -> useCase.getById(equipmentId));
    }
}
