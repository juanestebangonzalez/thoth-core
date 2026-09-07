# PowerShell Script - Crear carpetas y Domain Model
# Ubicación: C:\cursos\Proyectos\thoth-core\thoth-core\
# Ejecutar: powershell.exe -ExecutionPolicy Bypass -File .\CREAR_CARPETAS_Y_ARCHIVOS.ps1

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "THOTH C.O.R.E - CREAR CARPETAS Y ARCHIVOS JAVA" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que estamos en la carpeta correcta
if (-not (Test-Path "build.gradle")) {
    Write-Host "ERROR: No esta en la carpeta correcta!" -ForegroundColor Red
    Write-Host "Debe estar en: C:\cursos\Proyectos\thoth-core\thoth-core" -ForegroundColor Red
    exit 1
}

Write-Host "Paso 1: Creando estructura de carpetas..." -ForegroundColor Yellow
Write-Host ""

# Crear carpetas - DOMAIN
$carpetas = @(
    "src\main\java\com\thoth",
    "src\main\java\com\thoth\domain",
    "src\main\java\com\thoth\domain\model",
    "src\main\java\com\thoth\domain\valueobject",
    "src\main\java\com\thoth\domain\service",
    "src\main\java\com\thoth\domain\exception",
    "src\main\java\com\thoth\application",
    "src\main\java\com\thoth\application\port",
    "src\main\java\com\thoth\application\port\input",
    "src\main\java\com\thoth\application\port\output",
    "src\main\java\com\thoth\application\usecase",
    "src\main\java\com\thoth\application\dto",
    "src\main\java\com\thoth\adapter",
    "src\main\java\com\thoth\adapter\in",
    "src\main\java\com\thoth\adapter\in\rest",
    "src\main\java\com\thoth\adapter\in\rest\controller",
    "src\main\java\com\thoth\adapter\in\rest\mapper",
    "src\main\java\com\thoth\adapter\out",
    "src\main\java\com\thoth\adapter\out\persistence",
    "src\main\java\com\thoth\adapter\out\persistence\entity",
    "src\main\java\com\thoth\adapter\out\persistence\repository",
    "src\main\java\com\thoth\adapter\out\persistence\mapper",
    "src\main\java\com\thoth\adapter\out\ai",
    "src\main\java\com\thoth\adapter\out\config",
    "src\main\java\com\thoth\shared",
    "src\main\java\com\thoth\shared\event",
    "src\main\java\com\thoth\shared\exception",
    "src\main\java\com\thoth\shared\security",
    "src\main\java\com\thoth\shared\util",
    "src\test\java\com\thoth",
    "src\test\java\com\thoth\domain",
    "src\test\java\com\thoth\domain\model",
    "src\test\java\com\thoth\domain\valueobject",
    "src\main\resources"
)

foreach ($carpeta in $carpetas) {
    if (-not (Test-Path $carpeta)) {
        New-Item -ItemType Directory -Path $carpeta -Force | Out-Null
        Write-Host "   Creado: $carpeta" -ForegroundColor Green
    } else {
        Write-Host "   Existe: $carpeta" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Paso 2: Creando archivos Java..." -ForegroundColor Yellow
Write-Host ""

# 1. EquipmentStatus.java
Write-Host "   1. EquipmentStatus.java..." -ForegroundColor Cyan
$equipmentStatus = @'
package com.thoth.domain.valueobject;

public enum EquipmentStatus {
    ACTIVE("Activo"),
    MAINTENANCE("En Mantenimiento"),
    INACTIVE("Inactivo"),
    RETIRED("Retirado");
    
    private final String displayName;
    
    EquipmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
'@

$equipmentStatus | Out-File -Encoding UTF8 "src\main\java\com\thoth\domain\valueobject\EquipmentStatus.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 2. EquipmentCategory.java
Write-Host "   2. EquipmentCategory.java..." -ForegroundColor Cyan
$equipmentCategory = @'
package com.thoth.domain.valueobject;

public enum EquipmentCategory {
    DESKTOP_PC("PC de Escritorio"),
    LAPTOP("Portatil"),
    SERVER("Servidor"),
    PRINTER("Impresora"),
    NETWORK_DEVICE("Dispositivo de Red"),
    PERIPHERAL("Periferico"),
    STORAGE("Almacenamiento"),
    MONITOR("Monitor"),
    UPS("SAI/UPS"),
    OTHER("Otro");
    
    private final String displayName;
    
    EquipmentCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
'@

$equipmentCategory | Out-File -Encoding UTF8 "src\main\java\com\thoth\domain\valueobject\EquipmentCategory.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 3. MaintenanceType.java
Write-Host "   3. MaintenanceType.java..." -ForegroundColor Cyan
$maintenanceType = @'
package com.thoth.domain.valueobject;

public enum MaintenanceType {
    PREVENTIVE("Preventivo"),
    CORRECTIVE("Correctivo"),
    EMERGENCY("Emergencia");
    
    private final String displayName;
    
    MaintenanceType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
'@

$maintenanceType | Out-File -Encoding UTF8 "src\main\java\com\thoth\domain\valueobject\MaintenanceType.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 4. Severity.java
Write-Host "   4. Severity.java..." -ForegroundColor Cyan
$severity = @'
package com.thoth.domain.valueobject;

public enum Severity {
    LOW("Bajo", 1),
    MEDIUM("Medio", 2),
    HIGH("Alto", 3),
    CRITICAL("Critico", 4);
    
    private final String displayName;
    private final int level;
    
    Severity(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isMoreSevereThan(Severity other) {
        return this.level > other.level;
    }
}
'@

$severity | Out-File -Encoding UTF8 "src\main\java\com\thoth\domain\valueobject\Severity.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 5. Location.java
Write-Host "   5. Location.java..." -ForegroundColor Cyan
$location = @'
package com.thoth.domain.valueobject;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String building;
    private String floor;
    private String office;
    private String description;
    
    public static Location of(String building, String floor, String office, String description) {
        if (building == null || building.isBlank()) {
            throw new IllegalArgumentException("Building cannot be blank");
        }
        if (floor == null || floor.isBlank()) {
            throw new IllegalArgumentException("Floor cannot be blank");
        }
        if (office == null || office.isBlank()) {
            throw new IllegalArgumentException("Office cannot be blank");
        }
        
        return new Location(
            building.trim(),
            floor.trim(),
            office.trim(),
            description != null ? description.trim() : ""
        );
    }
    
    public String getFullAddress() {
        return String.format("%s, Piso %s, Oficina %s", building, floor, office);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(building, location.building) &&
               Objects.equals(floor, location.floor) &&
               Objects.equals(office, location.office) &&
               Objects.equals(description, location.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(building, floor, office, description);
    }
}
'@

$location | Out-File -Encoding UTF8 "src\main\java\com\thoth\domain\valueobject\Location.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 6. Equipment.java
Write-Host "   6. Equipment.java..." -ForegroundColor Cyan
$equipment = @'
package com.thoth.domain.model;

import com.thoth.domain.valueobject.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"location"})
public class Equipment {
    
    private UUID equipmentId;
    private String name;
    private EquipmentCategory category;
    private String serialNumber;
    private String macAddress;
    private String brand;
    private String model;
    private EquipmentStatus status;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private Location location;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;
    
    public static Equipment create(
            String name,
            EquipmentCategory category,
            String serialNumber,
            String brand,
            String model,
            String macAddress,
            LocalDate purchaseDate,
            BigDecimal purchaseValue,
            Location location,
            String assignedTo,
            String createdBy) {
        
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Equipment name cannot be blank");
        }
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be blank");
        }
        if (purchaseValue == null || purchaseValue.signum() <= 0) {
            throw new IllegalArgumentException("Purchase value must be positive");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date cannot be null");
        }
        if (purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Purchase date cannot be in the future");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (macAddress != null && !isValidMacAddress(macAddress)) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Equipment equipment = Equipment.builder()
            .equipmentId(UUID.randomUUID())
            .name(name.trim())
            .category(category)
            .serialNumber(serialNumber.trim())
            .brand(brand != null ? brand.trim() : "")
            .model(model != null ? model.trim() : "")
            .macAddress(macAddress != null ? macAddress.trim() : "")
            .status(EquipmentStatus.ACTIVE)
            .purchaseDate(purchaseDate)
            .purchaseValue(purchaseValue)
            .location(location)
            .assignedTo(assignedTo != null ? assignedTo.trim() : "")
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy != null ? createdBy.trim() : "SYSTEM")
            .lastModifiedBy(createdBy != null ? createdBy.trim() : "SYSTEM")
            .build();
        
        return equipment;
    }
    
    public void markForMaintenance() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException(
                "Cannot mark retired equipment for maintenance. Equipment: " + this.name);
        }
        if (this.status != EquipmentStatus.MAINTENANCE) {
            this.status = EquipmentStatus.MAINTENANCE;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void markAsActive() {
        this.status = EquipmentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsInactive() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException(
                "Cannot mark retired equipment as inactive. Equipment: " + this.name);
        }
        this.status = EquipmentStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsRetired() {
        this.status = EquipmentStatus.RETIRED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isOperational() {
        return this.status == EquipmentStatus.ACTIVE;
    }
    
    public long getDaysOwnedCount() {
        return ChronoUnit.DAYS.between(this.purchaseDate, LocalDate.now());
    }
    
    public double getYearsOwned() {
        long days = getDaysOwnedCount();
        return days / 365.0;
    }
    
    public boolean isOld() {
        return getYearsOwned() > 3;
    }
    
    public boolean isVeryOld() {
        return getYearsOwned() > 5;
    }
    
    public void updateLocation(Location newLocation) {
        if (newLocation == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reassignTo(String newAssignee) {
        this.assignedTo = newAssignee != null ? newAssignee.trim() : "";
        this.updatedAt = LocalDateTime.now();
    }
    
    private static boolean isValidMacAddress(String mac) {
        if (mac == null || mac.isBlank()) {
            return true;
        }
        return mac.matches("^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Objects.equals(equipmentId, equipment.equipmentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(equipmentId);
    }
}
'@

$equipment | Out-File -Encoding UTF8 "src\main\java\com\thoth\domain\model\Equipment.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 7. EquipmentTest.java
Write-Host "   7. EquipmentTest.java..." -ForegroundColor Cyan
$equipmentTest = @'
package com.thoth.domain.model;

import com.thoth.domain.valueobject.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Equipment - Unit Tests")
class EquipmentTest {
    
    private Equipment equipment;
    private Location testLocation;
    
    @BeforeEach
    void setUp() {
        testLocation = Location.of(
            "Edificio A",
            "2",
            "201",
            "Oficina principal"
        );
        
        equipment = Equipment.create(
            "Dell OptiPlex 7090",
            EquipmentCategory.DESKTOP_PC,
            "SN-2024-00001",
            "Dell",
            "OptiPlex 7090",
            "00:1A:2B:3C:4D:5E",
            LocalDate.of(2023, 1, 15),
            BigDecimal.valueOf(1200),
            testLocation,
            "Juan Perez",
            "admin"
        );
    }
    
    @Test
    @DisplayName("Should create equipment with ACTIVE status")
    void testCreateEquipmentInitializeWithActiveStatus() {
        assertNotNull(equipment.getEquipmentId());
        assertEquals(EquipmentStatus.ACTIVE, equipment.getStatus());
        assertEquals("Dell OptiPlex 7090", equipment.getName());
        assertEquals(EquipmentCategory.DESKTOP_PC, equipment.getCategory());
        assertNotNull(equipment.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should throw when name is blank")
    void testCreateEquipmentThrowsWhenNameBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Equipment.create(
                "",
                EquipmentCategory.LAPTOP,
                "SN123",
                "Brand",
                "Model",
                "00:1A:2B:3C:4D:5E",
                LocalDate.now().minusYears(1),
                BigDecimal.valueOf(1000),
                testLocation,
                "User",
                "admin"
            )
        );
        assertEquals("Equipment name cannot be blank", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should mark equipment for maintenance")
    void testMarkForMaintenanceChangesStatus() {
        equipment.markForMaintenance();
        assertEquals(EquipmentStatus.MAINTENANCE, equipment.getStatus());
    }
    
    @Test
    @DisplayName("Should mark equipment as active")
    void testMarkAsActive() {
        equipment.markForMaintenance();
        equipment.markAsActive();
        assertEquals(EquipmentStatus.ACTIVE, equipment.getStatus());
    }
    
    @Test
    @DisplayName("Should mark equipment as inactive")
    void testMarkAsInactive() {
        equipment.markAsInactive();
        assertEquals(EquipmentStatus.INACTIVE, equipment.getStatus());
    }
    
    @Test
    @DisplayName("Should mark equipment as retired")
    void testMarkAsRetired() {
        equipment.markAsRetired();
        assertEquals(EquipmentStatus.RETIRED, equipment.getStatus());
    }
    
    @Test
    @DisplayName("Should verify equipment is operational")
    void testIsOperationalWhenActive() {
        assertTrue(equipment.isOperational());
    }
    
    @Test
    @DisplayName("Should return false when not operational")
    void testIsNotOperationalWhenMaintenance() {
        equipment.markForMaintenance();
        assertFalse(equipment.isOperational());
    }
    
    @Test
    @DisplayName("Should update location")
    void testUpdateLocation() {
        Location newLocation = Location.of(
            "Edificio B",
            "3",
            "305",
            "Nueva ubicacion"
        );
        
        equipment.updateLocation(newLocation);
        
        assertEquals("Edificio B", equipment.getLocation().getBuilding());
        assertEquals("3", equipment.getLocation().getFloor());
    }
    
    @Test
    @DisplayName("Should reassign equipment")
    void testReassignEquipment() {
        equipment.reassignTo("Maria Garcia");
        assertEquals("Maria Garcia", equipment.getAssignedTo());
    }
    
    @Test
    @DisplayName("Should have equals based on ID")
    void testEquipmentEqualityBasedOnId() {
        Equipment same = equipment;
        Equipment different = Equipment.create(
            "Otro equipo",
            EquipmentCategory.LAPTOP,
            "SN-2024-00002",
            "HP",
            "ProBook",
            "00:1A:2B:3C:4D:5F",
            LocalDate.of(2023, 6, 1),
            BigDecimal.valueOf(1500),
            testLocation,
            "Otro usuario",
            "admin"
        );
        
        assertEquals(equipment, same);
        assertNotEquals(equipment, different);
    }
}
'@

$equipmentTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\domain\model\EquipmentTest.java" -Force
Write-Host "      OK" -ForegroundColor Green

# 8. LocationTest.java
Write-Host "   8. LocationTest.java..." -ForegroundColor Cyan
$locationTest = @'
package com.thoth.domain.valueobject;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Location - Value Object Tests")
class LocationTest {
    
    @Test
    @DisplayName("Should create location with valid data")
    void testCreateLocationSuccessfully() {
        Location location = Location.of(
            "Edificio A",
            "2",
            "201",
            "Oficina principal"
        );
        
        assertNotNull(location);
        assertEquals("Edificio A", location.getBuilding());
        assertEquals("2", location.getFloor());
        assertEquals("201", location.getOffice());
        assertEquals("Oficina principal", location.getDescription());
    }
    
    @Test
    @DisplayName("Should throw when building is blank")
    void testCreateLocationThrowsWhenBuildingBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of("", "2", "201", "desc")
        );
        assertEquals("Building cannot be blank", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should return full address")
    void testGetFullAddress() {
        Location location = Location.of("Edificio A", "2", "201", "desc");
        String address = location.getFullAddress();
        assertEquals("Edificio A, Piso 2, Oficina 201", address);
    }
    
    @Test
    @DisplayName("Should have value equality")
    void testLocationValueEquality() {
        Location loc1 = Location.of("Edificio A", "2", "201", "desc");
        Location loc2 = Location.of("Edificio A", "2", "201", "desc");
        Location loc3 = Location.of("Edificio B", "2", "201", "desc");
        
        assertEquals(loc1, loc2);
        assertNotEquals(loc1, loc3);
    }
}
'@

$locationTest | Out-File -Encoding UTF8 "src\test\java\com\thoth\domain\valueobject\LocationTest.java" -Force
Write-Host "      OK" -ForegroundColor Green

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "LISTO: Carpetas y archivos creados" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Ahora ejecuta:" -ForegroundColor Yellow
Write-Host "   gradle clean" -ForegroundColor Yellow
Write-Host "   gradle build" -ForegroundColor Yellow
Write-Host "   gradle test" -ForegroundColor Yellow
Write-Host ""
Write-Host "O ejecuta directamente:" -ForegroundColor Yellow
Write-Host "   powershell.exe -ExecutionPolicy Bypass -File .\VALIDAR_PROYECTO_FIXED.ps1" -ForegroundColor Yellow
Write-Host ""

Read-Host "Presione Enter para salir"
