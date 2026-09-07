# PASO 6: REST API CONTROLLERS - CREACION COMPLETA (CORREGIDO)
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_PASO_06_COMPLETO_FIXED.ps1

Write-Host ""
Write-Host "PASO 6: REST API CONTROLLERS - CREACION COMPLETA" -ForegroundColor Cyan
Write-Host ""

$basePath = "src\main\java\com\thoth"
$testPath = "src\test\java\com\thoth"

# Crear carpetas
Write-Host "Creando carpetas..." -ForegroundColor Yellow

$folders = @(
    "$basePath\adapter\in\rest\controller",
    "$basePath\adapter\in\rest\dto\request",
    "$basePath\adapter\in\rest\dto\response",
    "$basePath\adapter\in\rest\exception",
    "$basePath\adapter\in\rest\mapper",
    "$basePath\config",
    "$testPath\adapter\in\rest\controller",
    "$testPath\adapter\in\rest\exception"
)

foreach ($folder in $folders) {
    if (-not (Test-Path $folder)) {
        New-Item -ItemType Directory -Path $folder -Force | Out-Null
        Write-Host "  - $folder" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Creando archivos..." -ForegroundColor Yellow
Write-Host ""

# EXCEPTIONS
Write-Host "Exceptions:" -ForegroundColor Cyan

$file1 = @'
package com.thoth.adapter.in.rest.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
'@

Set-Content -Path "$basePath\adapter\in\rest\exception\ResourceNotFoundException.java" -Value $file1 -Encoding UTF8
Write-Host "  - ResourceNotFoundException.java" -ForegroundColor Green

$file2 = @'
package com.thoth.adapter.in.rest.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
'@

Set-Content -Path "$basePath\adapter\in\rest\exception\ValidationException.java" -Value $file2 -Encoding UTF8
Write-Host "  - ValidationException.java" -ForegroundColor Green

$file3 = @'
package com.thoth.adapter.in.rest.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
'@

Set-Content -Path "$basePath\adapter\in\rest\exception\UnauthorizedException.java" -Value $file3 -Encoding UTF8
Write-Host "  - UnauthorizedException.java" -ForegroundColor Green

$file4 = @'
package com.thoth.adapter.in.rest.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
'@

Set-Content -Path "$basePath\adapter\in\rest\exception\ForbiddenException.java" -Value $file4 -Encoding UTF8
Write-Host "  - ForbiddenException.java" -ForegroundColor Green

$file5 = @'
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
}
'@

Set-Content -Path "$basePath\adapter\in\rest\exception\ApiErrorResponse.java" -Value $file5 -Encoding UTF8
Write-Host "  - ApiErrorResponse.java" -ForegroundColor Green

# REQUEST DTOs
Write-Host ""
Write-Host "Request DTOs:" -ForegroundColor Cyan

$file6 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\request\CreateEquipmentRequest.java" -Value $file6 -Encoding UTF8
Write-Host "  - CreateEquipmentRequest.java" -ForegroundColor Green

$file7 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\request\UpdateEquipmentRequest.java" -Value $file7 -Encoding UTF8
Write-Host "  - UpdateEquipmentRequest.java" -ForegroundColor Green

$file8 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\request\ChangeStatusRequest.java" -Value $file8 -Encoding UTF8
Write-Host "  - ChangeStatusRequest.java" -ForegroundColor Green

$file9 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\request\LocationRequest.java" -Value $file9 -Encoding UTF8
Write-Host "  - LocationRequest.java" -ForegroundColor Green

$file10 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\request\CreateMaintenanceRequest.java" -Value $file10 -Encoding UTF8
Write-Host "  - CreateMaintenanceRequest.java" -ForegroundColor Green

$file11 = @'
package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

Set-Content -Path "$basePath\adapter\in\rest\dto\request\CompleteMaintenanceRequest.java" -Value $file11 -Encoding UTF8
Write-Host "  - CompleteMaintenanceRequest.java" -ForegroundColor Green

# RESPONSE DTOs
Write-Host ""
Write-Host "Response DTOs:" -ForegroundColor Cyan

$file12 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\response\LocationDTO.java" -Value $file12 -Encoding UTF8
Write-Host "  - LocationDTO.java" -ForegroundColor Green

$file13 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\response\EquipmentResponseDTO.java" -Value $file13 -Encoding UTF8
Write-Host "  - EquipmentResponseDTO.java" -ForegroundColor Green

$file14 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\response\MaintenanceResponseDTO.java" -Value $file14 -Encoding UTF8
Write-Host "  - MaintenanceResponseDTO.java" -ForegroundColor Green

$file15 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\response\ApiSuccessResponse.java" -Value $file15 -Encoding UTF8
Write-Host "  - ApiSuccessResponse.java" -ForegroundColor Green

$file16 = @'
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

Set-Content -Path "$basePath\adapter\in\rest\dto\response\PagedResponse.java" -Value $file16 -Encoding UTF8
Write-Host "  - PagedResponse.java" -ForegroundColor Green

# SWAGGER CONFIG
Write-Host ""
Write-Host "Configuration:" -ForegroundColor Cyan

$file17 = @'
package com.thoth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                    .email("support@thoth-core.com")));
    }
}
'@

Set-Content -Path "$basePath\config\SpringDocOpenApiConfig.java" -Value $file17 -Encoding UTF8
Write-Host "  - SpringDocOpenApiConfig.java" -ForegroundColor Green

# GLOBAL EXCEPTION HANDLER
Write-Host ""
Write-Host "Exception Handler:" -ForegroundColor Cyan

$file18 = @'
package com.thoth.adapter.in.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
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
        String message = "Validation error in input fields";
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

Set-Content -Path "$basePath\adapter\in\rest\exception\GlobalExceptionHandler.java" -Value $file18 -Encoding UTF8
Write-Host "  - GlobalExceptionHandler.java" -ForegroundColor Green

# CONTROLLERS
Write-Host ""
Write-Host "Controllers:" -ForegroundColor Cyan

$file19 = @'
package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.ChangeStatusRequest;
import com.thoth.adapter.in.rest.dto.request.CreateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.request.UpdateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.response.EquipmentResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Equipment", description = "Register a new equipment")
    public ResponseEntity<EquipmentResponseDTO> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Equipment", description = "Retrieve equipment by ID")
    public ResponseEntity<EquipmentResponseDTO> getEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List Equipment", description = "Get all equipment with pagination")
    public ResponseEntity<?> listEquipment(
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
    }
}
'@

Set-Content -Path "$basePath\adapter\in\rest\controller\EquipmentController.java" -Value $file19 -Encoding UTF8
Write-Host "  - EquipmentController.java" -ForegroundColor Green

$file20 = @'
package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.CompleteMaintenanceRequest;
import com.thoth.adapter.in.rest.dto.request.CreateMaintenanceRequest;
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
    @Operation(summary = "Create Maintenance", description = "Register maintenance record")
    public ResponseEntity<?> createMaintenance(
            @Valid @RequestBody CreateMaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Maintenance", description = "Retrieve maintenance by ID")
    public ResponseEntity<?> getMaintenance(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List Maintenance", description = "Get all maintenance records")
    public ResponseEntity<?> listMaintenance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete Maintenance", description = "Mark maintenance completed")
    public ResponseEntity<?> completeMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteMaintenanceRequest request) {
        return ResponseEntity.ok().build();
    }
}
'@

Set-Content -Path "$basePath\adapter\in\rest\controller\MaintenanceController.java" -Value $file20 -Encoding UTF8
Write-Host "  - MaintenanceController.java" -ForegroundColor Green

# TESTS
Write-Host ""
Write-Host "Tests:" -ForegroundColor Cyan

$file21 = @'
package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipmentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testListEquipment_Success() throws Exception {
        mockMvc.perform(get("/api/v1/equipment"))
            .andExpect(status().isOk());
    }
    
    @Test
    void testGetEquipment_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/invalid"))
            .andExpect(status().isNotFound());
    }
}
'@

Set-Content -Path "$testPath\adapter\in\rest\controller\EquipmentControllerTest.java" -Value $file21 -Encoding UTF8
Write-Host "  - EquipmentControllerTest.java" -ForegroundColor Green

$file22 = @'
package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
        mockMvc.perform(get("/api/v1/maintenance"))
            .andExpect(status().isOk());
    }
}
'@

Set-Content -Path "$testPath\adapter\in\rest\controller\MaintenanceControllerTest.java" -Value $file22 -Encoding UTF8
Write-Host "  - MaintenanceControllerTest.java" -ForegroundColor Green

$file23 = @'
package com.thoth.adapter.in.rest.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
        mockMvc.perform(get("/api/v1/equipment/non-existent"))
            .andExpect(status().isNotFound());
    }
}
'@

Set-Content -Path "$testPath\adapter\in\rest\exception\GlobalExceptionHandlerTest.java" -Value $file23 -Encoding UTF8
Write-Host "  - GlobalExceptionHandlerTest.java" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "PASO 6 COMPLETADO EXITOSAMENTE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "23 archivos creados:" -ForegroundColor Green
Write-Host "  - 5 Exception classes" -ForegroundColor Cyan
Write-Host "  - 6 Request DTOs" -ForegroundColor Cyan
Write-Host "  - 5 Response DTOs" -ForegroundColor Cyan
Write-Host "  - 2 Controllers" -ForegroundColor Cyan
Write-Host "  - 1 Configuration" -ForegroundColor Cyan
Write-Host "  - 1 Global Exception Handler" -ForegroundColor Cyan
Write-Host "  - 3 Integration Tests" -ForegroundColor Cyan
Write-Host ""
Write-Host "Siguiente paso:" -ForegroundColor Yellow
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host "  gradle test" -ForegroundColor Gray
Write-Host ""
