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
            EquipmentCategory.DESKTOP,
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
        assertEquals(EquipmentCategory.DESKTOP, equipment.getCategory());
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
    @DisplayName("SEC-011: Should not allow reactivating a retired equipment")
    void testMarkAsActiveThrowsWhenRetired() {
        equipment.markAsRetired();
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> equipment.markAsActive()
        );
        assertTrue(exception.getMessage().contains("Cannot reactivate retired equipment"));
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
