package com.thoth.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MaintenanceHistoryDTO(
    UUID maintenanceId,
    UUID equipmentId,
    String maintenanceType,
    LocalDateTime performedDate,
    String technicianName,
    UUID technicianId,
    String reason,
    String description,
    LocalDate nextScheduledDate,
    List<PartReplacedDTO> partsReplaced,
    Integer partsCount,
    String createdBy,
    String signatureBase64,
    String signedBy,
    LocalDateTime createdAt
) {}