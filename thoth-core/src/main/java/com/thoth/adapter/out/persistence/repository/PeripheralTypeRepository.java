package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.PeripheralTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeripheralTypeRepository extends JpaRepository<PeripheralTypeEntity, UUID> {
    List<PeripheralTypeEntity> findByActiveTrueOrderByNameAsc();
    List<PeripheralTypeEntity> findAllByOrderByNameAsc();
    Optional<PeripheralTypeEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
