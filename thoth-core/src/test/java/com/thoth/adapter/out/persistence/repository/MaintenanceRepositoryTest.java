package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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