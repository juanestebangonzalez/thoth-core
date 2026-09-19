package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    Page<AuditLogEntity> findAllByOrderByPerformedAtDesc(Pageable pageable);

    Page<AuditLogEntity> findByModuleOrderByPerformedAtDesc(String module, Pageable pageable);

    Page<AuditLogEntity> findByPerformedByOrderByPerformedAtDesc(String performedBy, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE a.performedAt >= :from AND a.performedAt <= :to ORDER BY a.performedAt DESC")
    Page<AuditLogEntity> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:user IS NULL OR a.performedBy = :user) AND " +
           "(:action IS NULL OR a.action = :action) " +
           "ORDER BY a.performedAt DESC")
    Page<AuditLogEntity> findFiltered(
        @Param("module") String module,
        @Param("user") String user,
        @Param("action") String action,
        Pageable pageable
    );

    List<AuditLogEntity> findByPerformedAtBefore(LocalDateTime date);
}
