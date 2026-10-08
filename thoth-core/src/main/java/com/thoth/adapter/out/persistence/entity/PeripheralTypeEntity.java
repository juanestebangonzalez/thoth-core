package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/** Tipo de periferico (ej: TECLADO, MOUSE, DIADEMA). Catalogo administrable. */
@Entity
@Table(name = "peripheral_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PeripheralTypeEntity {

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
