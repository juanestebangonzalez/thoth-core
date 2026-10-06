package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CostCenterRepository extends JpaRepository<CostCenterEntity, UUID> {
    List<CostCenterEntity> findByActiveTrueOrderByNameAsc();
    List<CostCenterEntity> findAllByOrderByNameAsc();
    Optional<CostCenterEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
