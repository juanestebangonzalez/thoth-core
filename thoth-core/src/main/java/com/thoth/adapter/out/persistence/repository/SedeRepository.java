package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.SedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SedeRepository extends JpaRepository<SedeEntity, UUID> {
    List<SedeEntity> findByActiveTrueOrderByNameAsc();
    Optional<SedeEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}