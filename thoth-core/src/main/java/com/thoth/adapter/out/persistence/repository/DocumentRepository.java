package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    List<DocumentEntity> findByEquipmentIdOrderByUploadedAtDesc(UUID equipmentId);
    long countByEquipmentId(UUID equipmentId);
}