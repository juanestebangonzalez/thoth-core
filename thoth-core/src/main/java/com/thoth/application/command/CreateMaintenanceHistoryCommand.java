package com.thoth.application.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateMaintenanceHistoryCommand(
    UUID equipmentId,
    String maintenanceType,
    String technicianName,
    UUID technicianId,
    String reason,
    String description,
    LocalDate nextScheduledDate,
    List<PartCommand> partsReplaced,
    String createdBy,
    String signatureBase64,
    String signedBy
) {
    public record PartCommand(
        String partName,
        String partSerialNumber,
        String reason
    ) {}
}