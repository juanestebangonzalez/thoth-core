package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Periferico registrado para un equipo (tipo del catalogo peripheral_type + marca). */
@Entity
@Table(name = "equipment_peripheral")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquipmentPeripheralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "equipment_id", nullable = false)
    private UUID equipmentId;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
