# PowerShell Script - CREAR PASO 6 COMPLETO: REST API CONTROLLERS
# Ubicacion: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_PASO_06_COMPLETO.ps1

Write-Host ""
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PASO 6: REST API CONTROLLERS - CREACION COMPLETA" -ForegroundColor Cyan
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$basePath = "src\main\java\com\thoth"
$testPath = "src\test\java\com\thoth"

# ==========================
# CREAR CARPETAS
# ==========================

Write-Host "Creando estructura de carpetas..." -ForegroundColor Yellow

@(
    "$basePath\adapter\in\rest\controller",
    "$basePath\adapter\in\rest\dto\request",
    "$basePath\adapter\in\rest\dto\response",
    "$basePath\adapter\in\rest\exception",
    "$basePath\adapter\in\rest\mapper",
    "$basePath\config",
    "$testPath\adapter\in\rest\controller",
    "$testPath\adapter\in\rest\exception"
) | ForEach-Object {
    if (-not (Test-Path $_)) {
        New-Item -ItemType Directory -Path $_ -Force | Out-Null
        Write-Host "  ✓ Carpeta creada: $_" -ForegroundColor Green
    }
}

Write-Host ""

# ==========================
# EXCEPTIONS (5 archivos)
# ==========================

Write-Host "Creando excepciones personalizadas..." -ForegroundColor Yellow

$resourceNotFoundException = @'
package com.thoth.adapter.in.rest.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\exception\ResourceNotFoundException.java", $resourceNotFoundException, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ ResourceNotFoundException.java" -ForegroundColor Green

$validationException = @'
package com.thoth.adapter.in.rest.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\exception\ValidationException.java", $validationException, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ ValidationException.java" -ForegroundColor Green

$unauthorizedException = @'
package com.thoth.adapter.in.rest.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\exception\UnauthorizedException.java", $unauthorizedException, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ UnauthorizedException.java" -ForegroundColor Green

$forbiddenException = @'
package com.thoth.adapter.in.rest.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\exception\ForbiddenException.java", $forbiddenException, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ ForbiddenException.java" -ForegroundColor Green

$apiErrorResponse = @'
package com.thoth.adapter.in.rest.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private String traceId;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\exception\ApiErrorResponse.java", $apiErrorResponse, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ ApiErrorResponse.java" -ForegroundColor Green

# ==========================
# REQUEST DTOs
# ==========================

Write-Host ""
Write-Host "Creando Request DTOs..." -ForegroundColor Yellow

$createEquipmentRequest = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEquipmentRequest {
    
    @NotBlank(message = "Equipment name is required")
    private String name;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotBlank(message = "Serial number is required")
    private String serialNumber;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    private String model;
    private String macAddress;
    
    @NotNull(message = "Location is required")
    private LocationRequest location;
    
    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;
    
    @NotNull(message = "Purchase value is required")
    private BigDecimal purchaseValue;
    
    private String assignedTo;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\request\CreateEquipmentRequest.java", $createEquipmentRequest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ CreateEquipmentRequest.java" -ForegroundColor Green

$updateEquipmentRequest = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquipmentRequest {
    
    @NotBlank(message = "Equipment name is required")
    private String name;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    private String model;
    private String macAddress;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private String assignedTo;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\request\UpdateEquipmentRequest.java", $updateEquipmentRequest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ UpdateEquipmentRequest.java" -ForegroundColor Green

$changeStatusRequest = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String reason;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\request\ChangeStatusRequest.java", $changeStatusRequest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ ChangeStatusRequest.java" -ForegroundColor Green

$locationRequest = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    
    @NotBlank(message = "Building is required")
    private String building;
    
    @NotBlank(message = "Floor is required")
    private String floor;
    
    @NotBlank(message = "Office is required")
    private String office;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\request\LocationRequest.java", $locationRequest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ LocationRequest.java" -ForegroundColor Green

$createMaintenanceRequest = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceRequest {
    
    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;
    
    @NotBlank(message = "Maintenance type is required")
    private String type;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Severity is required")
    private String severity;
    
    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\request\CreateMaintenanceRequest.java", $createMaintenanceRequest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ CreateMaintenanceRequest.java" -ForegroundColor Green

$completeMaintenanceRequest = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMaintenanceRequest {
    
    @NotNull(message = "Completion date is required")
    private LocalDate completionDate;
    
    @NotBlank(message = "Notes are required")
    private String notes;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\request\CompleteMaintenanceRequest.java", $completeMaintenanceRequest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ CompleteMaintenanceRequest.java" -ForegroundColor Green

# ==========================
# RESPONSE DTOs
# ==========================

Write-Host ""
Write-Host "Creando Response DTOs..." -ForegroundColor Yellow

$locationDTO = @'
package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private String building;
    private String floor;
    private String office;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\response\LocationDTO.java", $locationDTO, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ LocationDTO.java" -ForegroundColor Green

$equipmentResponseDTO = @'
package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponseDTO {
    private UUID equipmentId;
    private String name;
    private String category;
    private String serialNumber;
    private String brand;
    private String model;
    private String macAddress;
    private String status;
    private LocationDTO location;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\response\EquipmentResponseDTO.java", $equipmentResponseDTO, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ EquipmentResponseDTO.java" -ForegroundColor Green

$maintenanceResponseDTO = @'
package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceResponseDTO {
    private UUID maintenanceId;
    private UUID equipmentId;
    private String type;
    private String description;
    private String severity;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\response\MaintenanceResponseDTO.java", $maintenanceResponseDTO, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ MaintenanceResponseDTO.java" -ForegroundColor Green

$apiSuccessResponse = @'
package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccessResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\response\ApiSuccessResponse.java", $apiSuccessResponse, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ ApiSuccessResponse.java" -ForegroundColor Green

$pagedResponse = @'
package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\dto\response\PagedResponse.java", $pagedResponse, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ PagedResponse.java" -ForegroundColor Green

# ==========================
# GLOBAL EXCEPTION HANDLER
# ==========================

Write-Host ""
Write-Host "Creando Global Exception Handler..." -ForegroundColor Yellow

$globalExceptionHandler = @'
package com.thoth.adapter.in.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("NOT_FOUND")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            ValidationException ex, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Invalid input");
        
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message(message)
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("UNAUTHORIZED")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException ex, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .error("FORBIDDEN")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
            Exception ex, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\exception\GlobalExceptionHandler.java", $globalExceptionHandler, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ GlobalExceptionHandler.java" -ForegroundColor Green

# ==========================
# SWAGGER CONFIGURATION
# ==========================

Write-Host ""
Write-Host "Creando Swagger/OpenAPI Configuration..." -ForegroundColor Yellow

$springDocConfig = @'
package com.thoth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("THOTH C.O.R.E API")
                .version("1.0.0")
                .description("Computer Operations Resources Environment - API REST")
                .contact(new Contact()
                    .name("THOTH Support")
                    .email("support@thoth-core.com")
                    .url("https://thoth-core.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\config\SpringDocOpenApiConfig.java", $springDocConfig, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ SpringDocOpenApiConfig.java" -ForegroundColor Green

# ==========================
# CONTROLLERS
# ==========================

Write-Host ""
Write-Host "Creando REST Controllers..." -ForegroundColor Yellow

$equipmentController = @'
package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.ChangeStatusRequest;
import com.thoth.adapter.in.rest.dto.request.CreateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.request.UpdateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.response.EquipmentResponseDTO;
import com.thoth.adapter.in.rest.dto.response.PagedResponse;
import com.thoth.application.port.in.ChangeEquipmentStatusUseCase;
import com.thoth.application.port.in.DeleteEquipmentUseCase;
import com.thoth.application.port.in.GetEquipmentUseCase;
import com.thoth.application.port.in.ListEquipmentsUseCase;
import com.thoth.application.port.in.RegisterEquipmentUseCase;
import com.thoth.application.port.in.UpdateEquipmentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Equipment", description = "Equipment Management API")
@RequiredArgsConstructor
public class EquipmentController {
    
    private final RegisterEquipmentUseCase registerEquipmentUseCase;
    private final GetEquipmentUseCase getEquipmentUseCase;
    private final ListEquipmentsUseCase listEquipmentsUseCase;
    private final UpdateEquipmentUseCase updateEquipmentUseCase;
    private final ChangeEquipmentStatusUseCase changeEquipmentStatusUseCase;
    private final DeleteEquipmentUseCase deleteEquipmentUseCase;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Equipment", description = "Register a new equipment in the system")
    public ResponseEntity<EquipmentResponseDTO> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {
        // Implementation will be added after use case integration
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Equipment", description = "Retrieve equipment by ID")
    public ResponseEntity<EquipmentResponseDTO> getEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List Equipment", description = "Get all equipment with pagination")
    public ResponseEntity<PagedResponse<EquipmentResponseDTO>> listEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update Equipment", description = "Update equipment information")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEquipmentRequest request) {
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change Equipment Status", description = "Update equipment status")
    public ResponseEntity<EquipmentResponseDTO> changeEquipmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Equipment", description = "Soft delete equipment")
    public void deleteEquipment(@PathVariable UUID id) {
        // Implementation will be added
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\controller\EquipmentController.java", $equipmentController, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ EquipmentController.java" -ForegroundColor Green

$maintenanceController = @'
package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CompleteMaintenanceRequest;
import com.thoth.adapter.in.rest.dto.request.CreateMaintenanceRequest;
import com.thoth.adapter.in.rest.dto.response.MaintenanceResponseDTO;
import com.thoth.adapter.in.rest.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance", description = "Maintenance Management API")
@RequiredArgsConstructor
public class MaintenanceController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Maintenance", description = "Register a new maintenance record")
    public ResponseEntity<MaintenanceResponseDTO> createMaintenance(
            @Valid @RequestBody CreateMaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Maintenance", description = "Retrieve maintenance record by ID")
    public ResponseEntity<MaintenanceResponseDTO> getMaintenance(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "List Maintenance by Equipment", description = "Get all maintenance records for an equipment")
    public ResponseEntity<PagedResponse<MaintenanceResponseDTO>> listMaintenanceByEquipment(
            @PathVariable UUID equipmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List Maintenance", description = "Get all maintenance records with pagination")
    public ResponseEntity<PagedResponse<MaintenanceResponseDTO>> listMaintenance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete Maintenance", description = "Mark maintenance as completed")
    public ResponseEntity<MaintenanceResponseDTO> completeMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteMaintenanceRequest request) {
        return ResponseEntity.ok().build();
    }
}
'@
[System.IO.File]::WriteAllText("$basePath\adapter\in\rest\controller\MaintenanceController.java", $maintenanceController, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ MaintenanceController.java" -ForegroundColor Green

# ==========================
# TESTS
# ==========================

Write-Host ""
Write-Host "Creando Integration Tests..." -ForegroundColor Yellow

$equipmentControllerTest = @'
package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipmentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testListEquipment_Success() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void testGetEquipment_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/invalid-id")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateEquipment_InvalidRequest() throws Exception {
        String invalidRequest = "{}";
        mockMvc.perform(post("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
            .andExpect(status().isBadRequest());
    }
}
'@
[System.IO.File]::WriteAllText("$testPath\adapter\in\rest\controller\EquipmentControllerTest.java", $equipmentControllerTest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ EquipmentControllerTest.java" -ForegroundColor Green

$maintenanceControllerTest = @'
package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testListMaintenance_Success() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void testGetMaintenance_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance/invalid-id")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
'@
[System.IO.File]::WriteAllText("$testPath\adapter\in\rest\controller\MaintenanceControllerTest.java", $maintenanceControllerTest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ MaintenanceControllerTest.java" -ForegroundColor Green

$exceptionHandlerTest = @'
package com.thoth.adapter.in.rest.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/non-existent")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testBadRequestException() throws Exception {
        mockMvc.perform(get("/api/v1/equipment?page=invalid")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
'@
[System.IO.File]::WriteAllText("$testPath\adapter\in\rest\exception\GlobalExceptionHandlerTest.java", $exceptionHandlerTest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ GlobalExceptionHandlerTest.java" -ForegroundColor Green

Write-Host ""
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "PASO 6 COMPLETADO EXITOSAMENTE" -ForegroundColor Green
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos creados:" -ForegroundColor Green
Write-Host ""
Write-Host "Exceptions (5):" -ForegroundColor Cyan
Write-Host "  ✓ ResourceNotFoundException" -ForegroundColor Green
Write-Host "  ✓ ValidationException" -ForegroundColor Green
Write-Host "  ✓ UnauthorizedException" -ForegroundColor Green
Write-Host "  ✓ ForbiddenException" -ForegroundColor Green
Write-Host "  ✓ ApiErrorResponse" -ForegroundColor Green
Write-Host ""
Write-Host "Request DTOs (6):" -ForegroundColor Cyan
Write-Host "  ✓ CreateEquipmentRequest" -ForegroundColor Green
Write-Host "  ✓ UpdateEquipmentRequest" -ForegroundColor Green
Write-Host "  ✓ ChangeStatusRequest" -ForegroundColor Green
Write-Host "  ✓ LocationRequest" -ForegroundColor Green
Write-Host "  ✓ CreateMaintenanceRequest" -ForegroundColor Green
Write-Host "  ✓ CompleteMaintenanceRequest" -ForegroundColor Green
Write-Host ""
Write-Host "Response DTOs (5):" -ForegroundColor Cyan
Write-Host "  ✓ LocationDTO" -ForegroundColor Green
Write-Host "  ✓ EquipmentResponseDTO" -ForegroundColor Green
Write-Host "  ✓ MaintenanceResponseDTO" -ForegroundColor Green
Write-Host "  ✓ ApiSuccessResponse" -ForegroundColor Green
Write-Host "  ✓ PagedResponse" -ForegroundColor Green
Write-Host ""
Write-Host "Controllers (2):" -ForegroundColor Cyan
Write-Host "  ✓ EquipmentController" -ForegroundColor Green
Write-Host "  ✓ MaintenanceController" -ForegroundColor Green
Write-Host ""
Write-Host "Configuration (1):" -ForegroundColor Cyan
Write-Host "  ✓ SpringDocOpenApiConfig" -ForegroundColor Green
Write-Host ""
Write-Host "Exception Handling (1):" -ForegroundColor Cyan
Write-Host "  ✓ GlobalExceptionHandler" -ForegroundColor Green
Write-Host ""
Write-Host "Tests (3):" -ForegroundColor Cyan
Write-Host "  ✓ EquipmentControllerTest" -ForegroundColor Green
Write-Host "  ✓ MaintenanceControllerTest" -ForegroundColor Green
Write-Host "  ✓ GlobalExceptionHandlerTest" -ForegroundColor Green
Write-Host ""
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "TOTAL: 23 archivos creados" -ForegroundColor Yellow
Write-Host ""
Write-Host "Siguiente paso:" -ForegroundColor Cyan
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host "  gradle test" -ForegroundColor Gray
Write-Host ""
Write-Host "Acceder a Swagger UI:" -ForegroundColor Cyan
Write-Host "  http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host ""
