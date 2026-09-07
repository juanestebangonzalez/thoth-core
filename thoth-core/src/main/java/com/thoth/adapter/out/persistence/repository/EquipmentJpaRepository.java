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