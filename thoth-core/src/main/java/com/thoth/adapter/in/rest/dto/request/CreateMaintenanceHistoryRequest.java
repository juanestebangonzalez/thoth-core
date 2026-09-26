package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "El ID del equipo es obligatorio")
    private UUID equipmentId;

    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    private String maintenanceType;

    @NotBlank(message = "El nombre del tecnico es obligatorio")
    @Size(max = 100)
    private String technicianName;

    private UUID technicianId;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 2000)
    private String reason;

    @Size(max = 2000)
    private String description;
    private LocalDate nextScheduledDate;

    @Valid
    private List<PartReplacedRequest> partsReplaced;

    @Size(max = 500000)
    private String signatureBase64;
    private String signedBy;
}
