package com.thoth.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceRecordDTO(
    UUID maintenanceId,
    UUID equipmentId,
    String maintenanceType,
    String description,
    String severity,
    LocalDate scheduledDate,
    LocalDate completedDate
) {}
