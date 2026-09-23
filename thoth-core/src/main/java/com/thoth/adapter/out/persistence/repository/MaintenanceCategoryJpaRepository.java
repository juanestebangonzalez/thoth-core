package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.MaintenanceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceCategoryJpaRepository extends JpaRepository<MaintenanceCategoryEntity, UUID> {
    List<MaintenanceCategoryEntity> findByActiveTrueOrderByNameAsc();
    Optional<MaintenanceCategoryEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
