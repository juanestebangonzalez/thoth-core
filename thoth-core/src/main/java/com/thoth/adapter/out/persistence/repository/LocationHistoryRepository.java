package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.LocationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistoryEntity, UUID> {
    List<LocationHistoryEntity> findByEquipmentIdOrderByTransferredAtDesc(UUID equipmentId);
}
