package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMaintenanceRequest {
    @NotNull(message = "La fecha de finalizacion es obligatoria")
    private LocalDate completionDate;
    
    @NotBlank(message = "Notes are required")
    private String notes;
}