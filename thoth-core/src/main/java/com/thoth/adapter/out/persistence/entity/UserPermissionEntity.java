package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "user_permission",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module", "action"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "action", nullable = false, length = 20)
    private String action;
}