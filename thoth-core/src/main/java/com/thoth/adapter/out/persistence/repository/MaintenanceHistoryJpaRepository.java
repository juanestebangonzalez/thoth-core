package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceHistoryJpaRepository extends JpaRepository<MaintenanceHistoryEntity, UUID> {

    List<MaintenanceHistoryEntity> findByEquipmentIdOrderByPerformedDateDesc(UUID equipmentId);

    long countByEquipmentId(UUID equipmentId);
}