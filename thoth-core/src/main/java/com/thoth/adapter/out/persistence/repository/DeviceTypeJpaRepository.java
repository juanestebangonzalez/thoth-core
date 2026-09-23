package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.DeviceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTypeJpaRepository extends JpaRepository<DeviceTypeEntity, UUID> {
    List<DeviceTypeEntity> findByActiveTrueOrderByNameAsc();
    Optional<DeviceTypeEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
