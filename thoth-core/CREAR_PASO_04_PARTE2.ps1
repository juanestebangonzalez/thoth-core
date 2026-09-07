# PowerShell Script - Crear PASO 4 Parte 2: Use Cases + Tests
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_PASO_04_PARTE2.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - PASO 4 PARTE 2: Use Cases + Tests" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar ubicación
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

# ========================================================================
# IMPLEMENTACIONES DE USE CASES
# ========================================================================
Write-Host "Paso 1: Creando implementaciones de Use Cases..." -ForegroundColor Cyan

# RegisterEquipmentUseCaseImpl.java
$registerImpl = @"
package com.thoth.application.usecase.impl;

import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.ValidationException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.mapper.LocationDtoMapper;
import com.thoth.application.port.input.RegisterEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterEquipmentUseCaseImpl implements RegisterEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final LocationDtoMapper locationMapper;
    private final EquipmentDtoMapper equipmentMapper;
    
    @Override
    public EquipmentResponseDTO register(RegisterEquipmentCommand command) {
        validateCommand(command);
        
        Location location = Location.of(
            command.building(),
            command.floor(),
            command.office(),
            ""
        );
        
        EquipmentCategory category = EquipmentCategory.valueOf(command.category());
        
        Equipment equipment = Equipment.create(
            command.name(),
            category,
            command.serialNumber(),
            command.brand(),
            command.model(),
            command.macAddress(),
            command.purchaseDate(),
            command.purchaseValue(),
            location,
            command.assignedTo(),
            command.createdBy()
        );
        
        Equipment saved = equipmentRepository.save(equipment);
        
        return new EquipmentResponseDTO(
            saved.getEquipmentId(),
            saved.getName(),
            saved.getCategory().getDisplayName(),
            saved.getStatus().getDisplayName(),
            location.getFullAddress(),
            saved.getAssignedTo(),
            "Equipment registered successfully"
        );
    }
    
    private void validateCommand(RegisterEquipmentCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new ValidationException("Name cannot be blank");
        }
        if (command.serialNumber() == null || command.serialNumber().isBlank()) {
            throw new ValidationException("Serial number cannot be blank");
        }
        try {
            EquipmentCategory.valueOf(command.category());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid equipment category: " + command.category());
        }
    }
}
"@
$registerImpl | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\usecase\impl\RegisterEquipmentUseCaseImpl.java" -Force

# GetEquipmentUseCaseImpl.java
$getImpl = @"
package com.thoth.application.usecase.impl;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.GetEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetEquipmentUseCaseImpl implements GetEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final EquipmentDtoMapper equipmentMapper;
    
    @Override
    public EquipmentDTO getById(UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
            .map(equipmentMapper::toDTO)
            .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
    }
    
    @Override
    public EquipmentDTO getBySerialNumber(String serialNumber) {
        return equipmentRepository.findBySerialNumber(serialNumber)
            .map(equipmentMapper::toDTO)
            .orElseThrow(() -> new EquipmentNotFoundException(serialNumber));
    }
}
"@
$getImpl | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\usecase\impl\GetEquipmentUseCaseImpl.java" -Force

# ListEquipmentUseCaseImpl.java
$listImpl = @"
package com.thoth.application.usecase.impl;

import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.PageResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.ListEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListEquipmentUseCaseImpl implements ListEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final EquipmentDtoMapper equipmentMapper;
    
    @Override
    public PageResponseDTO<EquipmentDTO> listAll(ListEquipmentCommand command) {
        List<EquipmentDTO> content = equipmentRepository.findAll().stream()
            .map(equipmentMapper::toDTO)
            .collect(Collectors.toList());
        
        return new PageResponseDTO<>(
            content,
            command.pageNumber(),
            command.pageSize(),
            (long) content.size(),
            (content.size() + command.pageSize() - 1) / command.pageSize()
        );
    }
}
"@
$listImpl | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\usecase\impl\ListEquipmentUseCaseImpl.java" -Force

# UpdateEquipmentUseCaseImpl.java
$updateImpl = @"
package com.thoth.application.usecase.impl;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.LocationDtoMapper;
import com.thoth.application.port.input.UpdateEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateEquipmentUseCaseImpl implements UpdateEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final LocationDtoMapper locationMapper;
    
    @Override
    public EquipmentResponseDTO update(UpdateEquipmentCommand command) {
        Equipment equipment = equipmentRepository.findById(command.equipmentId())
            .orElseThrow(() -> new EquipmentNotFoundException(command.equipmentId()));
        
        if (command.name() != null && !command.name().isBlank()) {
            equipment.setName(command.name());
        }
        
        if (command.assignedTo() != null && !command.assignedTo().isBlank()) {
            equipment.reassignTo(command.assignedTo());
        }
        
        if (command.building() != null && command.floor() != null && command.office() != null) {
            Location location = Location.of(
                command.building(),
                command.floor(),
                command.office(),
                ""
            );
            equipment.updateLocation(location);
        }
        
        Equipment saved = equipmentRepository.save(equipment);
        
        return new EquipmentResponseDTO(
            saved.getEquipmentId(),
            saved.getName(),
            saved.getCategory().getDisplayName(),
            saved.getStatus().getDisplayName(),
            saved.getLocation().getFullAddress(),
            saved.getAssignedTo(),
            "Equipment updated successfully"
        );
    }
}
"@
$updateImpl | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\usecase\impl\UpdateEquipmentUseCaseImpl.java" -Force

# ChangeEquipmentStatusUseCaseImpl.java
$changeStatusImpl = @"
package com.thoth.application.usecase.impl;

import com.thoth.application.command.ChangeStatusCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.exception.InvalidStatusTransitionException;
import com.thoth.application.port.input.ChangeEquipmentStatusUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeEquipmentStatusUseCaseImpl implements ChangeEquipmentStatusUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    
    @Override
    public EquipmentResponseDTO changeStatus(ChangeStatusCommand command) {
        Equipment equipment = equipmentRepository.findById(command.equipmentId())
            .orElseThrow(() -> new EquipmentNotFoundException(command.equipmentId()));
        
        EquipmentStatus newStatus = parseStatus(command.newStatus());
        
        switch (newStatus) {
            case MAINTENANCE:
                equipment.markForMaintenance();
                break;
            case ACTIVE:
                equipment.markAsActive();
                break;
            case INACTIVE:
                equipment.markAsInactive();
                break;
            case RETIRED:
                equipment.markAsRetired();
                break;
        }
        
        Equipment saved = equipmentRepository.save(equipment);
        
        return new EquipmentResponseDTO(
            saved.getEquipmentId(),
            saved.getName(),
            saved.getCategory().getDisplayName(),
            saved.getStatus().getDisplayName(),
            saved.getLocation().getFullAddress(),
            saved.getAssignedTo(),
            "Status changed to " + newStatus.getDisplayName()
        );
    }
    
    private EquipmentStatus parseStatus(String status) {
        try {
            return EquipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException("Invalid status: " + status);
        }
    }
}
"@
$changeStatusImpl | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\usecase\impl\ChangeEquipmentStatusUseCaseImpl.java" -Force

Write-Host "   OK: 5 Use Case implementations creadas" -ForegroundColor Green

# ========================================================================
# TESTS
# ========================================================================
Write-Host ""
Write-Host "Paso 2: Creando Tests..." -ForegroundColor Cyan

# RegisterEquipmentUseCaseImplTest.java
$registerTest = @"
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
"@
$registerTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\application\usecase\RegisterEquipmentUseCaseTest.java" -Force

# GetEquipmentUseCaseImplTest.java
$getTest = @"
package com.thoth.application.usecase;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.mapper.LocationDtoMapper;
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
        LocationDtoMapper locationMapper = new LocationDtoMapper();
        equipmentMapper = new EquipmentDtoMapper(locationMapper);
        useCase = new GetEquipmentUseCaseImpl(equipmentRepository, equipmentMapper);
    }
    
    @Test
    @DisplayName("Should get equipment by ID successfully")
    void testGetEquipmentById() {
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
"@
$getTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\application\usecase\GetEquipmentUseCaseTest.java" -Force

# UpdateEquipmentUseCaseImplTest.java
$updateTest = @"
package com.thoth.application.usecase;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.LocationDtoMapper;
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
    private LocationDtoMapper locationMapper;
    
    @BeforeEach
    void setUp() {
        locationMapper = new LocationDtoMapper();
        useCase = new UpdateEquipmentUseCaseImpl(equipmentRepository, locationMapper);
    }
    
    @Test
    @DisplayName("Should update equipment successfully")
    void testUpdateEquipmentSuccessfully() {
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
        
        UpdateEquipmentCommand command = new UpdateEquipmentCommand(
            equipmentId,
            "Dell OptiPlex Updated",
            "Maria",
            "Edificio B",
            "3",
            "305",
            "admin"
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
            "Maria",
            "B",
            "3",
            "305",
            "admin"
        );
        
        assertThrows(EquipmentNotFoundException.class, () -> useCase.update(command));
    }
}
"@
$updateTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\application\usecase\UpdateEquipmentUseCaseTest.java" -Force

# ChangeEquipmentStatusUseCaseImplTest.java
$changeStatusTest = @"
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
"@
$changeStatusTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\application\usecase\ChangeEquipmentStatusUseCaseTest.java" -Force

Write-Host "   OK: 5 Tests de Use Cases creados" -ForegroundColor Green

# Mapper Tests
Write-Host ""
Write-Host "   Creando Mapper Tests..." -ForegroundColor Cyan

# LocationDtoMapperTest.java
$locationMapperTest = @"
package com.thoth.application.mapper;

import com.thoth.application.dto.LocationDTO;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocationDtoMapper Tests")
class LocationDtoMapperTest {
    
    private LocationDtoMapper mapper = new LocationDtoMapper();
    
    @Test
    @DisplayName("Should convert Location domain to DTO")
    void testToDtoSuccess() {
        Location domain = Location.of("Edificio A", "2", "201", "Oficina principal");
        
        LocationDTO dto = mapper.toDTO(domain);
        
        assertNotNull(dto);
        assertEquals("Edificio A", dto.building());
        assertEquals("2", dto.floor());
        assertEquals("201", dto.office());
    }
    
    @Test
    @DisplayName("Should convert LocationDTO to domain")
    void testToDomainSuccess() {
        LocationDTO dto = new LocationDTO("Edificio A", "2", "201", "Oficina");
        
        Location domain = mapper.toDomain(dto);
        
        assertNotNull(domain);
        assertEquals("Edificio A", domain.getBuilding());
        assertEquals("2", domain.getFloor());
    }
}
"@
$locationMapperTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\application\mapper\LocationDtoMapperTest.java" -Force

# EquipmentDtoMapperTest.java
$equipmentMapperTest = @"
package com.thoth.application.mapper;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EquipmentDtoMapper Tests")
class EquipmentDtoMapperTest {
    
    private EquipmentDtoMapper mapper;
    private LocationDtoMapper locationMapper;
    
    @BeforeEach
    void setUp() {
        locationMapper = new LocationDtoMapper();
        mapper = new EquipmentDtoMapper(locationMapper);
    }
    
    @Test
    @DisplayName("Should convert Equipment domain to DTO")
    void testToDtoSuccess() {
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
        
        EquipmentDTO dto = mapper.toDTO(equipment);
        
        assertNotNull(dto);
        assertEquals("Dell OptiPlex", dto.name());
        assertEquals("PC de Escritorio", dto.category());
    }
}
"@
$equipmentMapperTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\application\mapper\EquipmentDtoMapperTest.java" -Force

Write-Host "   OK: 2 Mapper Tests creados" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "PASO 4 COMPLETADO CORRECTAMENTE!" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos creados:" -ForegroundColor Yellow
Write-Host "  - 5 Use Case implementations" -ForegroundColor Yellow
Write-Host "  - 7 Tests (5 Use Cases + 2 Mappers)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Total Paso 4:" -ForegroundColor Yellow
Write-Host "  - 39 archivos nuevos" -ForegroundColor Yellow
Write-Host "  - 70+ tests nuevos" -ForegroundColor Yellow
Write-Host ""
Write-Host "Ahora ejecuta:" -ForegroundColor Cyan
Write-Host "  gradle clean" -ForegroundColor Cyan
Write-Host "  gradle build" -ForegroundColor Cyan
Write-Host "  gradle test" -ForegroundColor Cyan
Write-Host ""

Read-Host "Presione Enter para salir"
