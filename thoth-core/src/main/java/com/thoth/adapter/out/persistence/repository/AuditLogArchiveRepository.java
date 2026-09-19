package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.AuditLogArchiveEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AuditLogArchiveRepository extends JpaRepository<AuditLogArchiveEntity, UUID> {

    Page<AuditLogArchiveEntity> findAllByOrderByPerformedAtDesc(Pageable pageable);

    @Query("SELECT a FROM AuditLogArchiveEntity a WHERE " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:user IS NULL OR a.performedBy = :user) AND " +
           "(:action IS NULL OR a.action = :action) " +
           "ORDER BY a.performedAt DESC")
    Page<AuditLogArchiveEntity> findFiltered(
        @Param("module") String module,
        @Param("user") String user,
        @Param("action") String action,
        Pageable pageable
    );

    long count();
}
