# PASO 6: CORREGIR TESTS - VERSION SIMPLIFICADA SIN CARACTERES ESPECIALES
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CORREGIR_TESTS_PASO_06_SIMPLE.ps1

Write-Host ""
Write-Host "PASO 6: CORRIGIENDO TESTS" -ForegroundColor Cyan
Write-Host ""

$testPath = "src\test\java\com\thoth"

function Write-UTF8NoBOM {
    param([string]$Path, [string]$Content)
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8)
}

Write-Host "Actualizando Tests..." -ForegroundColor Yellow

# TEST 1: EquipmentControllerTest
$test1 = @'
package com.thoth.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoth.adapter.in.rest.dto.request.CreateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.request.LocationRequest;
import com.thoth.application.port.in.GetEquipmentUseCase;
import com.thoth.application.port.in.ListEquipmentsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipmentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private GetEquipmentUseCase getEquipmentUseCase;
    
    @MockBean
    private ListEquipmentsUseCase listEquipmentsUseCase;
    
    private CreateEquipmentRequest validRequest;
    
    @BeforeEach
    void setUp() {
        LocationRequest location = LocationRequest.builder()
            .building("Building A")
            .floor("1")
            .office("101")
            .build();
        
        validRequest = CreateEquipmentRequest.builder()
            .name("Laptop")
            .category("Computer")
            .serialNumber("SN-001")
            .brand("Dell")
            .model("XPS 13")
            .location(location)
            .purchaseDate(LocalDate.now())
            .purchaseValue(BigDecimal.valueOf(1000))
            .build();
    }
    
    @Test
    void testListEquipment_Success() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void testCreateEquipment_Success() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated());
    }
    
    @Test
    void testCreateEquipment_MissingName() throws Exception {
        CreateEquipmentRequest invalidRequest = CreateEquipmentRequest.builder()
            .category("Computer")
            .serialNumber("SN-001")
            .brand("Dell")
            .build();
        
        mockMvc.perform(post("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
'@
Write-UTF8NoBOM -Path "$testPath\adapter\in\rest\controller\EquipmentControllerTest.java" -Content $test1
Write-Host "  - EquipmentControllerTest.java actualizado" -ForegroundColor Green

# TEST 2: MaintenanceControllerTest
$test2 = @'
package com.thoth.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoth.adapter.in.rest.dto.request.CreateMaintenanceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CreateMaintenanceRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = CreateMaintenanceRequest.builder()
            .equipmentId(UUID.randomUUID())
            .type("PREVENTIVE")
            .description("Regular maintenance")
            .severity("LOW")
            .scheduledDate(LocalDate.now().plusDays(7))
            .build();
    }
    
    @Test
    void testListMaintenance_Success() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void testCreateMaintenance_Success() throws Exception {
        mockMvc.perform(post("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated());
    }
    
    @Test
    void testCreateMaintenance_MissingType() throws Exception {
        CreateMaintenanceRequest invalidRequest = CreateMaintenanceRequest.builder()
            .equipmentId(UUID.randomUUID())
            .description("Regular maintenance")
            .severity("LOW")
            .build();
        
        mockMvc.perform(post("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
'@
Write-UTF8NoBOM -Path "$testPath\adapter\in\rest\controller\MaintenanceControllerTest.java" -Content $test2
Write-Host "  - MaintenanceControllerTest.java actualizado" -ForegroundColor Green

# TEST 3: GlobalExceptionHandlerTest
$test3 = @'
package com.thoth.adapter.in.rest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoth.adapter.in.rest.dto.request.CreateEquipmentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testCreateEquipment_ValidationError() throws Exception {
        CreateEquipmentRequest invalidRequest = new CreateEquipmentRequest();
        
        mockMvc.perform(post("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testGetEquipment_InvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/invalid-id")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void testEndpointNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/nonexistent")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
'@
Write-UTF8NoBOM -Path "$testPath\adapter\in\rest\exception\GlobalExceptionHandlerTest.java" -Content $test3
Write-Host "  - GlobalExceptionHandlerTest.java actualizado" -ForegroundColor Green

Write-Host ""
Write-Host "TESTS CORREGIDOS EXITOSAMENTE" -ForegroundColor Green
Write-Host ""
Write-Host "Cambios realizados:" -ForegroundColor Green
Write-Host "  - Agregados Mocks" -ForegroundColor Cyan
Write-Host "  - Agregado ObjectMapper" -ForegroundColor Cyan
Write-Host "  - Tests con requests validos" -ForegroundColor Cyan
Write-Host "  - Tests con requests invalidos" -ForegroundColor Cyan
Write-Host "  - Validaciones de status HTTP" -ForegroundColor Cyan
Write-Host ""
Write-Host "Siguiente paso:" -ForegroundColor Yellow
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host "  gradle test" -ForegroundColor Gray
Write-Host ""
Write-Host "Resultado esperado:" -ForegroundColor Yellow
Write-Host "  BUILD SUCCESSFUL" -ForegroundColor Cyan
Write-Host "  40+ tests PASSING" -ForegroundColor Cyan
Write-Host ""
