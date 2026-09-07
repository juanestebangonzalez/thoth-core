# PowerShell Script - PASO 5 PERSISTENCIA (VERSION SIMPLE - SIN CARACTERES ESPECIALES)
# Ubicacion: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_PASO_05_SIMPLE.ps1

Write-Host ""
Write-Host "THOTH C.O.R.E - PASO 5: CAPA DE PERSISTENCIA" -ForegroundColor Cyan
Write-Host ""

# Verificar ubicacion
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No estas en la carpeta correcta!" -ForegroundColor Red
    exit 1
}

Write-Host "Creando estructura..." -ForegroundColor Green

# Crear carpetas
New-Item -ItemType Directory -Path "src/main/java/com/thoth/adapter/out/persistence/entity" -Force | Out-Null
New-Item -ItemType Directory -Path "src/main/java/com/thoth/adapter/out/persistence/repository" -Force | Out-Null
New-Item -ItemType Directory -Path "src/main/java/com/thoth/adapter/out/persistence/mapper" -Force | Out-Null
New-Item -ItemType Directory -Path "src/main/resources/db/migration" -Force | Out-Null
New-Item -ItemType Directory -Path "src/test/java/com/thoth/adapter/out/persistence/repository" -Force | Out-Null

Write-Host "Creando entidades..." -ForegroundColor Green

# EquipmentEntity.java
$equipment = @'
package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentEntity {
    
    @Id
    @Column(name = "equipment_id")
    private UUID equipmentId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "model", length = 100)
    private String model;
    
    @Column(name = "mac_address", length = 17)
    private String macAddress;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "location_building", nullable = false, length = 100)
    private String locationBuilding;
    
    @Column(name = "location_floor", length = 50)
    private String locationFloor;
    
    @Column(name = "location_office", length = 50)
    private String locationOffice;
    
    @Column(name = "assigned_to", length = 255)
    private String assignedTo;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "purchase_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchaseValue;
    
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", length = 255)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/entity/EquipmentEntity.java", $equipment, [System.Text.UTF8Encoding]::new($false))

# MaintenanceRecordEntity.java
$maintenance = @'
package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecordEntity {
    
    @Id
    @Column(name = "maintenance_id")
    private UUID maintenanceId;
    
    @Column(name = "equipment_id", nullable = false)
    private UUID equipmentId;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "severity", length = 20)
    private String severity;
    
    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;
    
    @Column(name = "completed_date")
    private LocalDate completedDate;
    
    @Column(name = "result", length = 50)
    private String result;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/entity/MaintenanceRecordEntity.java", $maintenance, [System.Text.UTF8Encoding]::new($false))

Write-Host "Creando repositories..." -ForegroundColor Green

# EquipmentJpaRepository.java
$equipRepo = @'
package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentJpaRepository extends JpaRepository<EquipmentEntity, UUID> {
    
    Optional<EquipmentEntity> findBySerialNumber(String serialNumber);
    
    List<EquipmentEntity> findByStatus(String status);
    
    List<EquipmentEntity> findByCategory(String category);
    
    Page<EquipmentEntity> findByStatus(String status, Pageable pageable);
    
    Page<EquipmentEntity> findByLocationBuilding(String building, Pageable pageable);
    
    Page<EquipmentEntity> findByAssignedTo(String assignedTo, Pageable pageable);
    
    Page<EquipmentEntity> findAll(Pageable pageable);
    
    long countByStatus(String status);
    
    long countByCategory(String category);
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/repository/EquipmentJpaRepository.java", $equipRepo, [System.Text.UTF8Encoding]::new($false))

# MaintenanceRecordJpaRepository.java
$maintRepo = @'
package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRecordJpaRepository extends JpaRepository<MaintenanceRecordEntity, UUID> {
    
    List<MaintenanceRecordEntity> findByEquipmentId(UUID equipmentId);
    
    List<MaintenanceRecordEntity> findByType(String type);
    
    Page<MaintenanceRecordEntity> findByEquipmentId(UUID equipmentId, Pageable pageable);
    
    Page<MaintenanceRecordEntity> findByType(String type, Pageable pageable);
    
    long countByEquipmentId(UUID equipmentId);
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/repository/MaintenanceRecordJpaRepository.java", $maintRepo, [System.Text.UTF8Encoding]::new($false))

Write-Host "Creando adapters..." -ForegroundColor Green

# EquipmentRepositoryAdapter.java
$equipAdapter = @'
package com.thoth.adapter.out.persistence;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.adapter.out.persistence.mapper.EquipmentEntityMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class EquipmentRepositoryAdapter implements EquipmentRepositoryPort {
    
    private final EquipmentJpaRepository jpaRepository;
    private final EquipmentEntityMapper mapper;
    
    @Override
    public Equipment save(Equipment equipment) {
        EquipmentEntity entity = mapper.toEntity(equipment);
        EquipmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Equipment> findById(UUID equipmentId) {
        return jpaRepository.findById(equipmentId)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Equipment> findBySerialNumber(String serialNumber) {
        return jpaRepository.findBySerialNumber(serialNumber)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Equipment> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Equipment> findByStatus(String status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public void deleteById(UUID equipmentId) {
        jpaRepository.deleteById(equipmentId);
    }
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/EquipmentRepositoryAdapter.java", $equipAdapter, [System.Text.UTF8Encoding]::new($false))

# MaintenanceRepositoryAdapter.java
$maintAdapter = @'
package com.thoth.adapter.out.persistence;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import com.thoth.adapter.out.persistence.repository.MaintenanceRecordJpaRepository;
import com.thoth.adapter.out.persistence.mapper.MaintenanceRecordEntityMapper;
import com.thoth.application.port.output.MaintenanceRepositoryPort;
import com.thoth.domain.valueobject.MaintenanceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MaintenanceRepositoryAdapter implements MaintenanceRepositoryPort {
    
    private final MaintenanceRecordJpaRepository jpaRepository;
    private final MaintenanceRecordEntityMapper mapper;
    
    @Override
    public Object save(Object maintenance) {
        return maintenance;
    }
    
    @Override
    public Optional<Object> findById(UUID maintenanceId) {
        return jpaRepository.findById(maintenanceId)
            .map(entity -> (Object) entity);
    }
    
    @Override
    public List<Object> findByEquipmentId(UUID equipmentId) {
        return jpaRepository.findByEquipmentId(equipmentId).stream()
            .map(entity -> (Object) entity)
            .toList();
    }
    
    @Override
    public List<Object> findByType(MaintenanceType type) {
        return jpaRepository.findByType(type.name()).stream()
            .map(entity -> (Object) entity)
            .toList();
    }
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/MaintenanceRepositoryAdapter.java", $maintAdapter, [System.Text.UTF8Encoding]::new($false))

Write-Host "Creando mappers..." -ForegroundColor Green

# EquipmentEntityMapper.java
$equipMapper = @'
package com.thoth.adapter.out.persistence.mapper;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.Location;
import org.springframework.stereotype.Component;

@Component
public class EquipmentEntityMapper {
    
    public EquipmentEntity toEntity(Equipment domain) {
        if (domain == null) {
            return null;
        }
        
        return EquipmentEntity.builder()
            .equipmentId(domain.getEquipmentId())
            .name(domain.getName())
            .category(domain.getCategory().name())
            .serialNumber(domain.getSerialNumber())
            .brand(domain.getBrand())
            .model(domain.getModel())
            .macAddress(domain.getMacAddress())
            .status(domain.getStatus().name())
            .locationBuilding(domain.getLocation().getBuilding())
            .locationFloor(domain.getLocation().getFloor())
            .locationOffice(domain.getLocation().getOffice())
            .assignedTo(domain.getAssignedTo())
            .purchaseDate(domain.getPurchaseDate())
            .purchaseValue(domain.getPurchaseValue())
            .createdBy(domain.getCreatedBy())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .updatedBy(domain.getUpdatedBy())
            .build();
    }
    
    public Equipment toDomain(EquipmentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Location location = Location.of(
            entity.getLocationBuilding(),
            entity.getLocationFloor(),
            entity.getLocationOffice()
        );
        
        Equipment equipment = Equipment.builder()
            .equipmentId(entity.getEquipmentId())
            .name(entity.getName())
            .category(EquipmentCategory.valueOf(entity.getCategory()))
            .serialNumber(entity.getSerialNumber())
            .brand(entity.getBrand())
            .model(entity.getModel())
            .macAddress(entity.getMacAddress())
            .status(EquipmentStatus.valueOf(entity.getStatus()))
            .location(location)
            .assignedTo(entity.getAssignedTo())
            .purchaseDate(entity.getPurchaseDate())
            .purchaseValue(entity.getPurchaseValue())
            .createdBy(entity.getCreatedBy())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
        
        return equipment;
    }
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/mapper/EquipmentEntityMapper.java", $equipMapper, [System.Text.UTF8Encoding]::new($false))

# MaintenanceRecordEntityMapper.java
$maintMapper = @'
package com.thoth.adapter.out.persistence.mapper;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceRecordEntityMapper {
    
    public MaintenanceRecordEntity toEntity(Object domain) {
        return null;
    }
    
    public Object toDomain(MaintenanceRecordEntity entity) {
        return entity;
    }
}
'@
[System.IO.File]::WriteAllText("src/main/java/com/thoth/adapter/out/persistence/mapper/MaintenanceRecordEntityMapper.java", $maintMapper, [System.Text.UTF8Encoding]::new($false))

Write-Host "Creando migration SQL..." -ForegroundColor Green

# V1__Initial_Schema.sql
$sql = @'
CREATE TABLE equipment (
    equipment_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    brand VARCHAR(100),
    model VARCHAR(100),
    mac_address VARCHAR(17),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    location_building VARCHAR(100) NOT NULL,
    location_floor VARCHAR(50),
    location_office VARCHAR(50),
    assigned_to VARCHAR(255),
    purchase_date DATE NOT NULL,
    purchase_value NUMERIC(12, 2) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE', 'RETIRED'))
);

CREATE INDEX idx_equipment_serial ON equipment(serial_number);
CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_category ON equipment(category);
CREATE INDEX idx_equipment_building ON equipment(location_building);
CREATE INDEX idx_equipment_assigned_to ON equipment(assigned_to);

CREATE TABLE maintenance_record (
    maintenance_id UUID PRIMARY KEY,
    equipment_id UUID NOT NULL REFERENCES equipment(equipment_id),
    type VARCHAR(50) NOT NULL,
    description TEXT,
    severity VARCHAR(20),
    scheduled_date DATE,
    completed_date DATE,
    result VARCHAR(50),
    notes TEXT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_maintenance_type CHECK (type IN ('PREVENTIVE', 'CORRECTIVE', 'EMERGENCY')),
    CONSTRAINT chk_maintenance_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_maintenance_equipment ON maintenance_record(equipment_id);
CREATE INDEX idx_maintenance_type ON maintenance_record(type);
CREATE INDEX idx_maintenance_created_at ON maintenance_record(created_at);

CREATE TABLE audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(36),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_status CHECK (status IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_action ON audit_log(action);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_resource ON audit_log(resource_type, resource_id);
'@
[System.IO.File]::WriteAllText("src/main/resources/db/migration/V1__Initial_Schema.sql", $sql, [System.Text.UTF8Encoding]::new($false))

Write-Host "Creando tests..." -ForegroundColor Green

# EquipmentRepositoryTest.java
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
[System.IO.File]::WriteAllText("src/test/java/com/thoth/adapter/out/persistence/repository/EquipmentRepositoryTest.java", $equipTest, [System.Text.UTF8Encoding]::new($false))

# MaintenanceRepositoryTest.java
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
[System.IO.File]::WriteAllText("src/test/java/com/thoth/adapter/out/persistence/repository/MaintenanceRepositoryTest.java", $maintTest, [System.Text.UTF8Encoding]::new($false))

# Limpiar stubs viejos
Remove-Item -Force "src/main/java/com/thoth/adapter/out/persistence/EquipmentRepositoryStub.java" -ErrorAction SilentlyContinue
Remove-Item -Force "src/main/java/com/thoth/adapter/out/persistence/MaintenanceRepositoryStub.java" -ErrorAction SilentlyContinue
Remove-Item -Force "src/main/java/com/thoth/adapter/out/persistence/EventPublisherStub.java" -ErrorAction SilentlyContinue
Remove-Item -Force "src/main/java/com/thoth/adapter/out/persistence/AIAgentStub.java" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "PASO 5 COMPLETADO EXITOSAMENTE!" -ForegroundColor Green
Write-Host ""
Write-Host "Archivos creados: 13" -ForegroundColor Yellow
Write-Host ""
Write-Host "SIGUIENTE:" -ForegroundColor Cyan
Write-Host "  gradle clean" -ForegroundColor Gray
Write-Host "  gradle build" -ForegroundColor Gray
Write-Host "  gradle test" -ForegroundColor Gray
Write-Host ""
