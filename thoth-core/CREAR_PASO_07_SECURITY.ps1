# PASO 7: SECURITY CONFIG + ARREGLAR TESTS
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_PASO_07_SECURITY.ps1

Write-Host ""
Write-Host "PASO 7: SPRING SECURITY + ARREGLAR TESTS" -ForegroundColor Cyan
Write-Host ""

$basePath = "src\main\java\com\thoth"
$testPath = "src\test\java\com\thoth"

function Write-UTF8NoBOM {
    param([string]$Path, [string]$Content)
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8)
}

# Crear carpetas
$secDir = "$basePath\config"
if (-not (Test-Path $secDir)) {
    New-Item -ItemType Directory -Path $secDir -Force | Out-Null
}

Write-Host "1. Creando SecurityConfig..." -ForegroundColor Yellow

$securityConfig = @'
package com.thoth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
'@
Write-UTF8NoBOM -Path "$basePath\config\SecurityConfig.java" -Content $securityConfig
Write-Host "  - SecurityConfig.java creado" -ForegroundColor Green

Write-Host ""
Write-Host "2. Actualizando tests con @Import(SecurityConfig)..." -ForegroundColor Yellow

$test1 = @'
package com.thoth.adapter.in.rest.controller;

import com.thoth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquipmentController.class)
@Import(SecurityConfig.class)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListEquipment_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testGetEquipment_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/550e8400-e29b-41d4-a716-446655440000")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteEquipment_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/550e8400-e29b-41d4-a716-446655440000")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}
'@
Write-UTF8NoBOM -Path "$testPath\adapter\in\rest\controller\EquipmentControllerTest.java" -Content $test1
Write-Host "  - EquipmentControllerTest.java actualizado" -ForegroundColor Green

$test2 = @'
package com.thoth.adapter.in.rest.controller;

import com.thoth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceController.class)
@Import(SecurityConfig.class)
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListMaintenance_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testGetMaintenance_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance/550e8400-e29b-41d4-a716-446655440000")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
'@
Write-UTF8NoBOM -Path "$testPath\adapter\in\rest\controller\MaintenanceControllerTest.java" -Content $test2
Write-Host "  - MaintenanceControllerTest.java actualizado" -ForegroundColor Green

$test3 = @'
package com.thoth.adapter.in.rest.exception;

import com.thoth.adapter.in.rest.controller.EquipmentController;
import com.thoth.adapter.in.rest.controller.MaintenanceController;
import com.thoth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EquipmentController.class, MaintenanceController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEquipmentEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testMaintenanceEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
'@
Write-UTF8NoBOM -Path "$testPath\adapter\in\rest\exception\GlobalExceptionHandlerTest.java" -Content $test3
Write-Host "  - GlobalExceptionHandlerTest.java actualizado" -ForegroundColor Green

Write-Host ""
Write-Host "PASO 7 COMPLETADO" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos creados/actualizados:" -ForegroundColor Green
Write-Host "  - SecurityConfig.java (permite /api/v1/** sin auth)" -ForegroundColor Cyan
Write-Host "  - EquipmentControllerTest.java (con @Import SecurityConfig)" -ForegroundColor Cyan
Write-Host "  - MaintenanceControllerTest.java (con @Import SecurityConfig)" -ForegroundColor Cyan
Write-Host "  - GlobalExceptionHandlerTest.java (con @Import SecurityConfig)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Siguiente paso:" -ForegroundColor Yellow
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host "  gradle test" -ForegroundColor Gray
Write-Host ""
Write-Host "Resultado esperado:" -ForegroundColor Yellow
Write-Host "  BUILD SUCCESSFUL" -ForegroundColor Cyan
Write-Host "  39+ tests PASSING, 0 failures" -ForegroundColor Cyan
Write-Host ""
