package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log_archive")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogArchiveEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String module;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "entity_name")
    private String entityName;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;
}
