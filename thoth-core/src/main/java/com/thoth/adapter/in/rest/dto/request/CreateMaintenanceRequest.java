package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceRequest {
    @NotNull(message = "El ID del equipo es obligatorio")
    private UUID equipmentId;
    
    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    private String type;
    
    @NotBlank(message = "La descripcion es obligatoria")
    private String description;
    
    @NotBlank(message = "La severidad es obligatoria")
    private String severity;
    
    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDate scheduledDate;
}