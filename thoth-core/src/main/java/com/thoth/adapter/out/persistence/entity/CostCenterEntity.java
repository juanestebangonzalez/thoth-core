package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/** Centro de costo (ej: ASISTENCIAL, ADMINISTRATIVO). Catalogo administrable. */
@Entity
@Table(name = "cost_center")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CostCenterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}
