package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.EquipmentPeripheralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EquipmentPeripheralRepository extends JpaRepository<EquipmentPeripheralEntity, UUID> {
    List<EquipmentPeripheralEntity> findByEquipmentIdOrderByCreatedAtAsc(UUID equipmentId);
}
