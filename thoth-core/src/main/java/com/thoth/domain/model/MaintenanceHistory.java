package com.thoth.domain.model;

import com.thoth.domain.valueobject.MaintenanceType;
import com.thoth.domain.valueobject.PartReplaced;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceHistory {

    private UUID maintenanceId;
    private UUID equipmentId;
    private MaintenanceType maintenanceType;
    private LocalDateTime performedDate;
    private String technicianName;
    private UUID technicianId;
    private String reason;
    private String description;
    private LocalDate nextScheduledDate;
    @Builder.Default
    private List<PartReplaced> partsReplaced = new ArrayList<>();
    private LocalDateTime createdAt;
    private String createdBy;
    private String signatureBase64;
    private String signedBy;

    public static MaintenanceHistory create(
            UUID equipmentId,
            MaintenanceType type,
            String technicianName,
            String reason,
            String description,
            LocalDate nextScheduledDate,
            List<PartReplaced> parts,
            String createdBy) {

        if (equipmentId == null) throw new IllegalArgumentException("Equipment ID cannot be null");
        if (type == null) throw new IllegalArgumentException("Maintenance type cannot be null");
        if (technicianName == null || technicianName.isBlank()) throw new IllegalArgumentException("Technician name is required");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Reason is required");

        return MaintenanceHistory.builder()
            .maintenanceId(UUID.randomUUID())
            .equipmentId(equipmentId)
            .maintenanceType(type)
            .performedDate(LocalDateTime.now())
            .technicianName(technicianName.trim())
            .reason(reason.trim())
            .description(description != null ? description.trim() : "")
            .nextScheduledDate(nextScheduledDate)
            .partsReplaced(parts != null ? parts : new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .createdBy(createdBy != null ? createdBy : "SYSTEM")
            .build();
    }

    public boolean hasPartsReplaced() {
        return partsReplaced != null && !partsReplaced.isEmpty();
    }

    public int getPartsCount() {
        return partsReplaced != null ? partsReplaced.size() : 0;
    }
}