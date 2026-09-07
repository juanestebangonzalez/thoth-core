package com.thoth.application.mapper;

import com.thoth.application.dto.LocationDTO;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocationDtoMapper Tests")
class LocationDtoMapperTest {
    
    private LocationDtoMapper mapper = new LocationDtoMapper();
    
    @Test
    @DisplayName("Should convert Location domain to DTO")
    void testToDtoSuccess() {
        Location domain = Location.of("Edificio A", "2", "201", "Oficina principal");
        
        LocationDTO dto = mapper.toDTO(domain);
        
        assertNotNull(dto);
        assertEquals("Edificio A", dto.building());
        assertEquals("2", dto.floor());
        assertEquals("201", dto.office());
    }
    
    @Test
    @DisplayName("Should convert LocationDTO to domain")
    void testToDomainSuccess() {
        LocationDTO dto = new LocationDTO("Edificio A", "2", "201", "Oficina");
        
        Location domain = mapper.toDomain(dto);
        
        assertNotNull(domain);
        assertEquals("Edificio A", domain.getBuilding());
        assertEquals("2", domain.getFloor());
    }
}
