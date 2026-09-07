# PowerShell Script - Crear PASO 4 Automáticamente
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_PASO_04.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - PASO 4: Puertos y Casos de Uso" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Creando 39 archivos + 60+ tests..." -ForegroundColor Yellow
Write-Host ""

# Verificar ubicación
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

# CREAR CARPETAS
Write-Host "Paso 1: Creando carpetas..." -ForegroundColor Cyan

$carpetas = @(
    "src/main/java/com/thoth/application/port/input",
    "src/main/java/com/thoth/application/port/output",
    "src/main/java/com/thoth/application/dto",
    "src/main/java/com/thoth/application/command",
    "src/main/java/com/thoth/application/mapper",
    "src/main/java/com/thoth/application/usecase/impl",
    "src/main/java/com/thoth/application/exception",
    "src/test/java/com/thoth/application/usecase",
    "src/test/java/com/thoth/application/mapper"
)

foreach ($carpeta in $carpetas) {
    if (-not (Test-Path $carpeta)) {
        New-Item -ItemType Directory -Path $carpeta -Force | Out-Null
    }
}
Write-Host "   OK: Carpetas creadas" -ForegroundColor Green

# ========================================================================
# EXCEPCIONES
# ========================================================================
Write-Host ""
Write-Host "Paso 2: Creando excepciones..." -ForegroundColor Cyan

# BusinessException.java
$businessException = @"
package com.thoth.application.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
"@
$businessException | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\exception\BusinessException.java" -Force

# EquipmentNotFoundException.java
$equipmentNotFound = @"
package com.thoth.application.exception;

import java.util.UUID;

public class EquipmentNotFoundException extends BusinessException {
    public EquipmentNotFoundException(UUID equipmentId) {
        super("Equipment not found: " + equipmentId);
    }
    
    public EquipmentNotFoundException(String serialNumber) {
        super("Equipment not found: " + serialNumber);
    }
}
"@
$equipmentNotFound | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\exception\EquipmentNotFoundException.java" -Force

# ValidationException.java
$validationException = @"
package com.thoth.application.exception;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("Validation failed: " + message);
    }
}
"@
$validationException | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\exception\ValidationException.java" -Force

# InvalidStatusTransitionException.java
$invalidStatus = @"
package com.thoth.application.exception;

public class InvalidStatusTransitionException extends BusinessException {
    public InvalidStatusTransitionException(String message) {
        super("Invalid status transition: " + message);
    }
}
"@
$invalidStatus | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\exception\InvalidStatusTransitionException.java" -Force

Write-Host "   OK: 4 excepciones creadas" -ForegroundColor Green

# ========================================================================
# DTOs
# ========================================================================
Write-Host ""
Write-Host "Paso 3: Creando DTOs..." -ForegroundColor Cyan

# LocationDTO.java
$locationDTO = @"
package com.thoth.application.dto;

public record LocationDTO(
    String building,
    String floor,
    String office,
    String description
) {
    public String getFullAddress() {
        return String.format("%s, Piso %s, Oficina %s", building, floor, office);
    }
}
"@
$locationDTO | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\LocationDTO.java" -Force

# EquipmentDTO.java
$equipmentDTO = @"
package com.thoth.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentDTO(
    UUID equipmentId,
    String name,
    String category,
    String serialNumber,
    String macAddress,
    String brand,
    String model,
    String status,
    LocalDate purchaseDate,
    BigDecimal purchaseValue,
    LocationDTO location,
    String assignedTo,
    String createdBy
) {}
"@
$equipmentDTO | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\EquipmentDTO.java" -Force

# EquipmentResponseDTO.java
$equipmentResponseDTO = @"
package com.thoth.application.dto;

import java.util.UUID;

public record EquipmentResponseDTO(
    UUID equipmentId,
    String name,
    String category,
    String status,
    String location,
    String assignedTo,
    String message
) {}
"@
$equipmentResponseDTO | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\EquipmentResponseDTO.java" -Force

# MaintenanceRecordDTO.java
$maintenanceDTO = @"
package com.thoth.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceRecordDTO(
    UUID maintenanceId,
    UUID equipmentId,
    String maintenanceType,
    String description,
    String severity,
    LocalDate scheduledDate,
    LocalDate completedDate
) {}
"@
$maintenanceDTO | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\MaintenanceRecordDTO.java" -Force

# CreateEquipmentRequest.java
$createRequest = @"
package com.thoth.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEquipmentRequest(
    String name,
    String category,
    String serialNumber,
    String brand,
    String model,
    String macAddress,
    LocalDate purchaseDate,
    BigDecimal purchaseValue,
    String building,
    String floor,
    String office,
    String assignedTo,
    String createdBy
) {}
"@
$createRequest | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\CreateEquipmentRequest.java" -Force

# UpdateEquipmentRequest.java
$updateRequest = @"
package com.thoth.application.dto;

public record UpdateEquipmentRequest(
    String name,
    String assignedTo,
    String building,
    String floor,
    String office
) {}
"@
$updateRequest | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\UpdateEquipmentRequest.java" -Force

# PageResponseDTO.java
$pageResponse = @"
package com.thoth.application.dto;

import java.util.List;

public record PageResponseDTO<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages
) {}
"@
$pageResponse | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\dto\PageResponseDTO.java" -Force

Write-Host "   OK: 7 DTOs creados" -ForegroundColor Green

# ========================================================================
# COMMANDS
# ========================================================================
Write-Host ""
Write-Host "Paso 4: Creando Commands..." -ForegroundColor Cyan

# RegisterEquipmentCommand.java
$registerCommand = @"
package com.thoth.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterEquipmentCommand(
    String name,
    String category,
    String serialNumber,
    String brand,
    String model,
    String macAddress,
    LocalDate purchaseDate,
    BigDecimal purchaseValue,
    String building,
    String floor,
    String office,
    String assignedTo,
    String createdBy
) {}
"@
$registerCommand | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\command\RegisterEquipmentCommand.java" -Force

# UpdateEquipmentCommand.java
$updateCommand = @"
package com.thoth.application.command;

import java.util.UUID;

public record UpdateEquipmentCommand(
    UUID equipmentId,
    String name,
    String assignedTo,
    String building,
    String floor,
    String office,
    String modifiedBy
) {}
"@
$updateCommand | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\command\UpdateEquipmentCommand.java" -Force

# ChangeStatusCommand.java
$changeStatusCommand = @"
package com.thoth.application.command;

import java.util.UUID;

public record ChangeStatusCommand(
    UUID equipmentId,
    String newStatus,
    String modifiedBy
) {}
"@
$changeStatusCommand | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\command\ChangeStatusCommand.java" -Force

# ListEquipmentCommand.java
$listCommand = @"
package com.thoth.application.command;

public record ListEquipmentCommand(
    int pageNumber,
    int pageSize,
    String sortBy,
    String status,
    String category
) {}
"@
$listCommand | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\command\ListEquipmentCommand.java" -Force

Write-Host "   OK: 4 Commands creados" -ForegroundColor Green

# ========================================================================
# PUERTOS DE SALIDA (Output Ports)
# ========================================================================
Write-Host ""
Write-Host "Paso 5: Creando Puertos de Salida..." -ForegroundColor Cyan

# EquipmentRepositoryPort.java
$equipmentRepoPort = @"
package com.thoth.application.port.output;

import com.thoth.domain.model.Equipment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepositoryPort {
    Equipment save(Equipment equipment);
    Optional<Equipment> findById(UUID equipmentId);
    Optional<Equipment> findBySerialNumber(String serialNumber);
    List<Equipment> findAll();
    List<Equipment> findByStatus(String status);
    void deleteById(UUID equipmentId);
}
"@
$equipmentRepoPort | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\output\EquipmentRepositoryPort.java" -Force

# MaintenanceRepositoryPort.java
$maintenanceRepoPort = @"
package com.thoth.application.port.output;

import com.thoth.domain.valueobject.MaintenanceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceRepositoryPort {
    Object save(Object maintenance);
    Optional<Object> findById(UUID maintenanceId);
    List<Object> findByEquipmentId(UUID equipmentId);
    List<Object> findByType(MaintenanceType type);
}
"@
$maintenanceRepoPort | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\output\MaintenanceRepositoryPort.java" -Force

# EventPublisherPort.java
$eventPublisherPort = @"
package com.thoth.application.port.output;

public interface EventPublisherPort {
    void publishEvent(Object event);
    void publishEquipmentRegistered(Object event);
    void publishStatusChanged(Object event);
}
"@
$eventPublisherPort | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\output\EventPublisherPort.java" -Force

# AIAgentPort.java
$aiAgentPort = @"
package com.thoth.application.port.output;

import com.thoth.domain.model.Equipment;

public interface AIAgentPort {
    String analyzeMaintenance(Equipment equipment);
    String predictFailure(Equipment equipment);
    String recommendReplacement(Equipment equipment);
}
"@
$aiAgentPort | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\output\AIAgentPort.java" -Force

Write-Host "   OK: 4 Puertos de Salida creados" -ForegroundColor Green

# ========================================================================
# PUERTOS DE ENTRADA (Input Ports)
# ========================================================================
Write-Host ""
Write-Host "Paso 6: Creando Puertos de Entrada..." -ForegroundColor Cyan

# RegisterEquipmentUseCase.java
$registerUseCase = @"
package com.thoth.application.port.input;

import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;

public interface RegisterEquipmentUseCase {
    EquipmentResponseDTO register(RegisterEquipmentCommand command);
}
"@
$registerUseCase | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\input\RegisterEquipmentUseCase.java" -Force

# GetEquipmentUseCase.java
$getUseCase = @"
package com.thoth.application.port.input;

import com.thoth.application.dto.EquipmentDTO;
import java.util.UUID;

public interface GetEquipmentUseCase {
    EquipmentDTO getById(UUID equipmentId);
    EquipmentDTO getBySerialNumber(String serialNumber);
}
"@
$getUseCase | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\input\GetEquipmentUseCase.java" -Force

# ListEquipmentUseCase.java
$listUseCase = @"
package com.thoth.application.port.input;

import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.PageResponseDTO;

public interface ListEquipmentUseCase {
    PageResponseDTO<EquipmentDTO> listAll(ListEquipmentCommand command);
}
"@
$listUseCase | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\input\ListEquipmentUseCase.java" -Force

# UpdateEquipmentUseCase.java
$updateUseCase = @"
package com.thoth.application.port.input;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;

public interface UpdateEquipmentUseCase {
    EquipmentResponseDTO update(UpdateEquipmentCommand command);
}
"@
$updateUseCase | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\input\UpdateEquipmentUseCase.java" -Force

# ChangeEquipmentStatusUseCase.java
$changeStatusUseCase = @"
package com.thoth.application.port.input;

import com.thoth.application.command.ChangeStatusCommand;
import com.thoth.application.dto.EquipmentResponseDTO;

public interface ChangeEquipmentStatusUseCase {
    EquipmentResponseDTO changeStatus(ChangeStatusCommand command);
}
"@
$changeStatusUseCase | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\port\input\ChangeEquipmentStatusUseCase.java" -Force

Write-Host "   OK: 5 Puertos de Entrada creados" -ForegroundColor Green

# ========================================================================
# MAPPERS
# ========================================================================
Write-Host ""
Write-Host "Paso 7: Creando Mappers..." -ForegroundColor Cyan

# LocationDtoMapper.java
$locationMapper = @"
package com.thoth.application.mapper;

import com.thoth.application.dto.LocationDTO;
import com.thoth.domain.valueobject.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationDtoMapper {
    
    public LocationDTO toDTO(Location domain) {
        if (domain == null) return null;
        
        return new LocationDTO(
            domain.getBuilding(),
            domain.getFloor(),
            domain.getOffice(),
            domain.getDescription()
        );
    }
    
    public Location toDomain(LocationDTO dto) {
        if (dto == null) return null;
        
        return Location.of(
            dto.building(),
            dto.floor(),
            dto.office(),
            dto.description()
        );
    }
}
"@
$locationMapper | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\mapper\LocationDtoMapper.java" -Force

# EquipmentDtoMapper.java
$equipmentMapper = @"
package com.thoth.application.mapper;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EquipmentDtoMapper {
    
    private final LocationDtoMapper locationMapper;
    
    public EquipmentDTO toDTO(Equipment domain) {
        if (domain == null) return null;
        
        return new EquipmentDTO(
            domain.getEquipmentId(),
            domain.getName(),
            domain.getCategory().getDisplayName(),
            domain.getSerialNumber(),
            domain.getMacAddress(),
            domain.getBrand(),
            domain.getModel(),
            domain.getStatus().getDisplayName(),
            domain.getPurchaseDate(),
            domain.getPurchaseValue(),
            locationMapper.toDTO(domain.getLocation()),
            domain.getAssignedTo(),
            domain.getCreatedBy()
        );
    }
}
"@
$equipmentMapper | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\mapper\EquipmentDtoMapper.java" -Force

# MaintenanceRecordDtoMapper.java
$maintenanceMapper = @"
package com.thoth.application.mapper;

import com.thoth.application.dto.MaintenanceRecordDTO;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceRecordDtoMapper {
    
    public MaintenanceRecordDTO toDTO(Object domain) {
        // Implementar cuando se cree la entidad Maintenance
        return null;
    }
}
"@
$maintenanceMapper | Out-File -Encoding UTF8 "src\main\java\com\thoth\application\mapper\MaintenanceRecordDtoMapper.java" -Force

Write-Host "   OK: 3 Mappers creados" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Yellow
Write-Host "Primera parte del script completada!" -ForegroundColor Yellow
Write-Host "========================================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Siguiente: Ejecutar CREAR_PASO_04_PARTE2.ps1" -ForegroundColor Yellow
Write-Host ""

Read-Host "Presione Enter para continuar con Parte 2"
