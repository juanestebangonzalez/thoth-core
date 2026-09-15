package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.UserPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, UUID> {
    List<UserPermissionEntity> findByUserId(UUID userId);

    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);

    boolean existsByUserIdAndModuleAndAction(UUID userId, String module, String action);
}