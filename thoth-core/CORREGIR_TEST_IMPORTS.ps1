# PowerShell Script - Corregir Imports en Archivos de Test
# Ubicacion: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CORREGIR_TEST_IMPORTS.ps1

Write-Host ""
Write-Host "Corrigiendo imports en archivos de test..." -ForegroundColor Cyan
Write-Host ""

# Archivo 1: EquipmentRepositoryTest.java
$equipTestPath = "src\test\java\com\thoth\adapter\out\persistence\repository\EquipmentRepositoryTest.java"

Write-Host "Corrigiendo: EquipmentRepositoryTest.java" -ForegroundColor Yellow

$equipTest = @'
package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EquipmentRepositoryTest {
    
    @Autowired
    private EquipmentJpaRepository repository;
    
    @Test
    void testSaveEquipment() {
        EquipmentEntity entity = EquipmentEntity.builder()
            .equipmentId(UUID.randomUUID())
            .name("Dell Precision 7550")
            .category("LAPTOP")
            .serialNumber("ABC123456")
            .brand("Dell")
            .status("ACTIVE")
            .locationBuilding("Tower A")
            .purchaseDate(LocalDate.of(2022, 1, 15))
            .purchaseValue(BigDecimal.valueOf(1500.00))
            .createdBy("admin")
            .build();
        
        EquipmentEntity saved = repository.save(entity);
        
        assertNotNull(saved);
        assertEquals("Dell Precision 7550", saved.getName());
    }
    
    @Test
    void testFindBySerialNumber() {
        String serialNumber = "XYZ789012";
        EquipmentEntity entity = EquipmentEntity.builder()
            .equipmentId(UUID.randomUUID())
            .name("HP ProDesk 600")
            .category("DESKTOP_PC")
            .serialNumber(serialNumber)
            .status("ACTIVE")
            .locationBuilding("Tower B")
            .purchaseDate(LocalDate.of(2021, 6, 20))
            .purchaseValue(BigDecimal.valueOf(800.00))
            .createdBy("admin")
            .build();
        repository.save(entity);
        
        Optional<EquipmentEntity> found = repository.findBySerialNumber(serialNumber);
        
        assertTrue(found.isPresent());
        assertEquals("HP ProDesk 600", found.get().getName());
    }
    
    @Test
    void testFindByStatus() {
        EquipmentEntity entity = EquipmentEntity.builder()
            .equipmentId(UUID.randomUUID())
            .name("Lenovo ThinkPad")
            .category("LAPTOP")
            .serialNumber("LEN123456")
            .status("MAINTENANCE")
            .locationBuilding("Tower C")
            .purchaseDate(LocalDate.of(2020, 3, 10))
            .purchaseValue(BigDecimal.valueOf(1200.00))
            .createdBy("admin")
            .build();
        repository.save(entity);
        
        var result = repository.findByStatus("MAINTENANCE");
        
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(e -> e.getSerialNumber().equals("LEN123456")));
    }
}
'@

[System.IO.File]::WriteAllText($equipTestPath, $equipTest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ EquipmentRepositoryTest.java corregido" -ForegroundColor Green

# Archivo 2: MaintenanceRepositoryTest.java
$maintTestPath = "src\test\java\com\thoth\adapter\out\persistence\repository\MaintenanceRepositoryTest.java"

Write-Host "Corrigiendo: MaintenanceRepositoryTest.java" -ForegroundColor Yellow

$maintTest = @'
package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MaintenanceRepositoryTest {
    
    @Autowired
    private MaintenanceRecordJpaRepository repository;
    
    @Test
    void testSaveMaintenanceRecord() {
        UUID equipmentId = UUID.randomUUID();
        MaintenanceRecordEntity entity = MaintenanceRecordEntity.builder()
            .maintenanceId(UUID.randomUUID())
            .equipmentId(equipmentId)
            .type("PREVENTIVE")
            .description("Software update")
            .severity("LOW")
            .scheduledDate(LocalDate.now())
            .createdBy("tech01")
            .build();
        
        MaintenanceRecordEntity saved = repository.save(entity);
        
        assertNotNull(saved);
        assertEquals("PREVENTIVE", saved.getType());
    }
    
    @Test
    void testFindByEquipmentId() {
        UUID equipmentId = UUID.randomUUID();
        MaintenanceRecordEntity entity = MaintenanceRecordEntity.builder()
            .maintenanceId(UUID.randomUUID())
            .equipmentId(equipmentId)
            .type("CORRECTIVE")
            .description("Keyboard replacement")
            .severity("MEDIUM")
            .scheduledDate(LocalDate.now())
            .createdBy("tech02")
            .build();
        repository.save(entity);
        
        var result = repository.findByEquipmentId(equipmentId);
        
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(m -> m.getType().equals("CORRECTIVE")));
    }
}
'@

[System.IO.File]::WriteAllText($maintTestPath, $maintTest, [System.Text.UTF8Encoding]::new($false))
Write-Host "  ✓ MaintenanceRepositoryTest.java corregido" -ForegroundColor Green

Write-Host ""
Write-Host "Cambios realizados:" -ForegroundColor Green
Write-Host ""
Write-Host "Linea 6 - ANTES:" -ForegroundColor Yellow
Write-Host "  import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;" -ForegroundColor Red
Write-Host ""
Write-Host "Linea 6 - DESPUES:" -ForegroundColor Green
Write-Host "  import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos corregidos exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "Siguiente paso:" -ForegroundColor Cyan
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host "  gradle test" -ForegroundColor Gray
Write-Host ""
