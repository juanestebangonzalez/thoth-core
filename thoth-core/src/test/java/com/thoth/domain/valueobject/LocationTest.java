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
