package com.thoth.application.mapper;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EquipmentDtoMapper Tests")
class EquipmentDtoMapperTest {
    
    private EquipmentDtoMapper mapper;
    private LocationDtoMapper locationMapper;
    
    @BeforeEach
    void setUp() {
        locationMapper = new LocationDtoMapper();
        mapper = new EquipmentDtoMapper(locationMapper);
    }
    
    @Test
    @DisplayName("Should convert Equipment domain to DTO")
    void testToDtoSuccess() {
        Location location = Location.of("Edificio A", "2", "201", "");
        Equipment equipment = Equipment.create(
            "Dell OptiPlex",
            EquipmentCategory.DESKTOP_PC,
            "SN-2024-00001",
            "Dell",
            "OptiPlex",
            "00:1A:2B:3C:4D:5E",
            LocalDate.of(2023, 1, 15),
            BigDecimal.valueOf(1200),
            location,
            "Juan",
            "admin"
        );
        
        EquipmentDTO dto = mapper.toDTO(equipment);
        
        assertNotNull(dto);
        assertEquals("Dell OptiPlex", dto.name());
        assertEquals("PC de Escritorio", dto.category());
    }
}
