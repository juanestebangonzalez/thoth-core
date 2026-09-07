# PowerShell Script - Reparar archivos con BOM
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\REPARAR_BOM_PASO_04.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - Reparar BOM en archivos (Paso 4)" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar ubicación
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

Write-Host "Eliminando archivos con BOM..." -ForegroundColor Yellow

# Eliminar archivos viejos
Remove-Item -Force "src\main\java\com\thoth\adapter\out\persistence\AIAgentStub.java" -ErrorAction SilentlyContinue
Remove-Item -Force "src\main\java\com\thoth\adapter\out\persistence\EquipmentRepositoryStub.java" -ErrorAction SilentlyContinue
Remove-Item -Force "src\main\java\com\thoth\adapter\out\persistence\EventPublisherStub.java" -ErrorAction SilentlyContinue
Remove-Item -Force "src\main\java\com\thoth\adapter\out\persistence\MaintenanceRepositoryStub.java" -ErrorAction SilentlyContinue

Write-Host "OK: Archivos viejos eliminados" -ForegroundColor Green
Write-Host ""
Write-Host "Creando nuevos archivos SIN BOM..." -ForegroundColor Yellow

# Crear carpeta
if (-not (Test-Path "src\main\java\com\thoth\adapter\out\persistence")) {
    New-Item -ItemType Directory -Path "src\main\java\com\thoth\adapter\out\persistence" -Force | Out-Null
}

# EquipmentRepositoryStub.java (SIN BOM)
$equipmentStub = @"
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

# Escribir sin BOM
[System.IO.File]::WriteAllText(
    "src\main\java\com\thoth\adapter\out\persistence\EquipmentRepositoryStub.java",
    $equipmentStub,
    [System.Text.UTF8Encoding]::new($false)
)
Write-Host "   OK: EquipmentRepositoryStub.java" -ForegroundColor Green

# MaintenanceRepositoryStub.java (SIN BOM)
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

[System.IO.File]::WriteAllText(
    "src\main\java\com\thoth\adapter\out\persistence\MaintenanceRepositoryStub.java",
    $maintenanceStub,
    [System.Text.UTF8Encoding]::new($false)
)
Write-Host "   OK: MaintenanceRepositoryStub.java" -ForegroundColor Green

# EventPublisherStub.java (SIN BOM)
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

[System.IO.File]::WriteAllText(
    "src\main\java\com\thoth\adapter\out\persistence\EventPublisherStub.java",
    $eventStub,
    [System.Text.UTF8Encoding]::new($false)
)
Write-Host "   OK: EventPublisherStub.java" -ForegroundColor Green

# AIAgentStub.java (SIN BOM)
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

[System.IO.File]::WriteAllText(
    "src\main\java\com\thoth\adapter\out\persistence\AIAgentStub.java",
    $aiStub,
    [System.Text.UTF8Encoding]::new($false)
)
Write-Host "   OK: AIAgentStub.java" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "REPARACION COMPLETADA!" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos recreados sin BOM:" -ForegroundColor Yellow
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
