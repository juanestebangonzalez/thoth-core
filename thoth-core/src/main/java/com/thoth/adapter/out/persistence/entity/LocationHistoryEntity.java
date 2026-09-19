package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "location_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "equipment_id", nullable = false)
    private UUID equipmentId;

    @Column(name = "from_building")
    private String fromBuilding;

    @Column(name = "from_floor")
    private String fromFloor;

    @Column(name = "from_office")
    private String fromOffice;

    @Column(name = "to_building", nullable = false)
    private String toBuilding;

    @Column(name = "to_floor")
    private String toFloor;

    @Column(name = "to_office")
    private String toOffice;

    @Column(nullable = false)
    private String reason;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt;
}
