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