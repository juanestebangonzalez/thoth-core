package com.thoth.application.usecase;

import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.ValidationException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.mapper.LocationDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.RegisterEquipmentUseCaseImpl;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterEquipmentUseCase Tests")
class RegisterEquipmentUseCaseTest {
    
    @Mock
    private EquipmentRepositoryPort equipmentRepository;
    
    private RegisterEquipmentUseCaseImpl useCase;
    private EquipmentDtoMapper equipmentMapper;
    private LocationDtoMapper locationMapper;
    
    @BeforeEach
    void setUp() {
        locationMapper = new LocationDtoMapper();
        equipmentMapper = new EquipmentDtoMapper(locationMapper);
        useCase = new RegisterEquipmentUseCaseImpl(equipmentRepository, locationMapper, equipmentMapper);
    }
    
    @Test
    @DisplayName("Should register equipment successfully")
    void testRegisterEquipmentSuccessfully() {
        RegisterEquipmentCommand command = new RegisterEquipmentCommand(
            "Dell OptiPlex 7090",
            "DESKTOP_PC",
            "SN-2024-00001",
            "Dell",
            "OptiPlex 7090",
            "00:1A:2B:3C:4D:5E",
            LocalDate.of(2023, 1, 15),
            BigDecimal.valueOf(1200),
            "Edificio A",
            "2",
            "201",
            "Juan Perez",
            "admin"
        );
        
        Equipment mockEquipment = Equipment.create(
            command.name(),
            EquipmentCategory.DESKTOP_PC,
            command.serialNumber(),
            command.brand(),
            command.model(),
            command.macAddress(),
            command.purchaseDate(),
            command.purchaseValue(),
            Location.of(command.building(), command.floor(), command.office(), ""),
            command.assignedTo(),
            command.createdBy()
        );
        
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(mockEquipment);
        
        EquipmentResponseDTO response = useCase.register(command);
        
        assertNotNull(response);
        assertNotNull(response.equipmentId());
        assertEquals("Dell OptiPlex 7090", response.name());
        verify(equipmentRepository, times(1)).save(any());
    }
    
    @Test
    @DisplayName("Should throw ValidationException when name is blank")
    void testRegisterThrowsWhenNameBlank() {
        RegisterEquipmentCommand command = new RegisterEquipmentCommand(
            "",
            "DESKTOP_PC",
            "SN-2024-00001",
            "Dell",
            "OptiPlex 7090",
            "00:1A:2B:3C:4D:5E",
            LocalDate.of(2023, 1, 15),
            BigDecimal.valueOf(1200),
            "Edificio A",
            "2",
            "201",
            "Juan Perez",
            "admin"
        );
        
        assertThrows(ValidationException.class, () -> useCase.register(command));
    }
}
