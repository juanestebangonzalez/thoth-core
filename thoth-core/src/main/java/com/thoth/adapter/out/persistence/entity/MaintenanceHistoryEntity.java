package com.thoth.adapter.out.persistence.entity;

import com.thoth.domain.valueobject.MaintenanceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "maintenance_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceHistoryEntity {

    @Id
    @Column(name = "maintenance_id")
    private UUID maintenanceId;

    @Column(name = "equipment_id", nullable = false)
    private UUID equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false)
    private MaintenanceType maintenanceType;

    @Column(name = "performed_date", nullable = false)
    private LocalDateTime performedDate;

    @Column(name = "technician_name", nullable = false)
    private String technicianName;

    @Column(name = "technician_id")
    private UUID technicianId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "next_scheduled_date")
    private LocalDate nextScheduledDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "signature_base64", columnDefinition = "TEXT")
    private String signatureBase64;

    @Column(name = "signed_by", length = 100)
    private String signedBy;

    @OneToMany(mappedBy = "maintenanceHistory", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<PartReplacedEntity> partsReplaced = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}