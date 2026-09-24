package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.domain.valueobject.EquipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
            .status(EquipmentStatus.ACTIVE)
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
            .category("DESKTOP")
            .serialNumber(serialNumber)
            .status(EquipmentStatus.ACTIVE)
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
            .status(EquipmentStatus.MAINTENANCE)
            .locationBuilding("Tower C")
            .purchaseDate(LocalDate.of(2020, 3, 10))
            .purchaseValue(BigDecimal.valueOf(1200.00))
            .createdBy("admin")
            .build();
        repository.save(entity);
        
        var result = repository.findByStatus(EquipmentStatus.MAINTENANCE);
        
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(e -> e.getSerialNumber().equals("LEN123456")));
    }
}