package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.AreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<AreaEntity, UUID> {
    List<AreaEntity> findByActiveTrueOrderByNameAsc();
    List<AreaEntity> findAllByOrderByNameAsc();
    Optional<AreaEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
