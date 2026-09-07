# PowerShell Script - Reparar EquipmentRepositoryPort
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\REPARAR_PASO_04.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - Reparar Paso 4 (EquipmentRepositoryPort)" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar ubicación
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

Write-Host "Creando stub de EquipmentRepositoryPort..." -ForegroundColor Cyan

# Crear carpeta si no existe
if (-not (Test-Path "src\main\java\com\thoth\adapter\out\persistence")) {
    New-Item -ItemType Directory -Path "src\main\java\com\thoth\adapter\out\persistence" -Force | Out-Null
}

# EquipmentRepositoryStub.java
$stub = @"
package com.thoth.adapter.out.persistence;

import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class EquipmentRepositoryStub implements EquipmentRepositoryPort {
    
    private final Map<UUID, Equipment> storage = new HashMap<>();
    
    @Override
    public Equipment save(Equipment equipment) {
        storage.put(equipment.getEquipmentId(), equipment);
        return equipment;
    }
    
    @Override
    public Optional<Equipment> findById(UUID equipmentId) {
        return Optional.ofNullable(storage.get(equipmentId));
    }
    
    @Override
    public Optional<Equipment> findBySerialNumber(String serialNumber) {
        return storage.values().stream()
            .filter(e -> e.getSerialNumber().equals(serialNumber))
            .findFirst();
    }
    
    @Override
    public List<Equipment> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public List<Equipment> findByStatus(String status) {
        return storage.values().stream()
            .filter(e -> e.getStatus().name().equals(status))
            .toList();
    }
    
    @Override
    public void deleteById(UUID equipmentId) {
        storage.remove(equipmentId);
    }
}
"@

$stub | Out-File -Encoding UTF8 "src\main\java\com\thoth\adapter\out\persistence\EquipmentRepositoryStub.java" -Force
Write-Host "   OK: EquipmentRepositoryStub creado" -ForegroundColor Green

# MaintenanceRepositoryStub.java
Write-Host ""
Write-Host "Creando stub de MaintenanceRepositoryPort..." -ForegroundColor Cyan

$maintenanceStub = @"
package com.thoth.adapter.out.persistence;

import com.thoth.application.port.output.MaintenanceRepositoryPort;
import com.thoth.domain.valueobject.MaintenanceType;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MaintenanceRepositoryStub implements MaintenanceRepositoryPort {
    
    private final Map<UUID, Object> storage = new HashMap<>();
    
    @Override
    public Object save(Object maintenance) {
        return maintenance;
    }
    
    @Override
    public Optional<Object> findById(UUID maintenanceId) {
        return Optional.ofNullable(storage.get(maintenanceId));
    }
    
    @Override
    public List<Object> findByEquipmentId(UUID equipmentId) {
        return new ArrayList<>();
    }
    
    @Override
    public List<Object> findByType(MaintenanceType type) {
        return new ArrayList<>();
    }
}
"@

$maintenanceStub | Out-File -Encoding UTF8 "src\main\java\com\thoth\adapter\out\persistence\MaintenanceRepositoryStub.java" -Force
Write-Host "   OK: MaintenanceRepositoryStub creado" -ForegroundColor Green

# EventPublisherStub.java
Write-Host ""
Write-Host "Creando stub de EventPublisherPort..." -ForegroundColor Cyan

$eventStub = @"
package com.thoth.adapter.out.persistence;

import com.thoth.application.port.output.EventPublisherPort;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherStub implements EventPublisherPort {
    
    @Override
    public void publishEvent(Object event) {
        // Stub: no hace nada
    }
    
    @Override
    public void publishEquipmentRegistered(Object event) {
        // Stub: no hace nada
    }
    
    @Override
    public void publishStatusChanged(Object event) {
        // Stub: no hace nada
    }
}
"@

$eventStub | Out-File -Encoding UTF8 "src\main\java\com\thoth\adapter\out\persistence\EventPublisherStub.java" -Force
Write-Host "   OK: EventPublisherStub creado" -ForegroundColor Green

# AIAgentStub.java
Write-Host ""
Write-Host "Creando stub de AIAgentPort..." -ForegroundColor Cyan

$aiStub = @"
package com.thoth.adapter.out.persistence;

import com.thoth.application.port.output.AIAgentPort;
import com.thoth.domain.model.Equipment;
import org.springframework.stereotype.Component;

@Component
public class AIAgentStub implements AIAgentPort {
    
    @Override
    public String analyzeMaintenance(Equipment equipment) {
        return "Maintenance analysis: OK";
    }
    
    @Override
    public String predictFailure(Equipment equipment) {
        return "No failures predicted";
    }
    
    @Override
    public String recommendReplacement(Equipment equipment) {
        return "Not recommended for replacement";
    }
}
"@

$aiStub | Out-File -Encoding UTF8 "src\main\java\com\thoth\adapter\out\persistence\AIAgentStub.java" -Force
Write-Host "   OK: AIAgentStub creado" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "REPARACION COMPLETADA!" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Stubs creados:" -ForegroundColor Yellow
Write-Host "  - EquipmentRepositoryStub.java" -ForegroundColor Yellow
Write-Host "  - MaintenanceRepositoryStub.java" -ForegroundColor Yellow
Write-Host "  - EventPublisherStub.java" -ForegroundColor Yellow
Write-Host "  - AIAgentStub.java" -ForegroundColor Yellow
Write-Host ""
Write-Host "Ahora ejecuta:" -ForegroundColor Cyan
Write-Host "  gradle clean" -ForegroundColor Cyan
Write-Host "  gradle build" -ForegroundColor Cyan
Write-Host "  gradle test" -ForegroundColor Cyan
Write-Host ""

Read-Host "Presione Enter para salir"
