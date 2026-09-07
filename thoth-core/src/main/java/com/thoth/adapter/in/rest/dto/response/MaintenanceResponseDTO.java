package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceResponseDTO {
    private UUID maintenanceId;
    private UUID equipmentId;
    private String type;
    private String description;
    private String severity;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}