package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/** Area / dependencia dentro de una sede (ej: ADMISIONES, URGENCIAS). Catalogo administrable. */
@Entity
@Table(name = "area")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AreaEntity {

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
