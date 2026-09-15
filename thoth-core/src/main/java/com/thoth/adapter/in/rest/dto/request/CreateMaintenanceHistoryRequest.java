package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceHistoryRequest {
    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;

    @NotBlank(message = "Maintenance type is required (PREVENTIVE or CORRECTIVE)")
    private String maintenanceType;

    @NotBlank(message = "Technician name is required")
    private String technicianName;

    private UUID technicianId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;
    private LocalDate nextScheduledDate;

    @Valid
    private List<PartReplacedRequest> partsReplaced;

    private String signatureBase64;
    private String signedBy;
}