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
    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;
    
    @NotBlank(message = "Maintenance type is required")
    private String type;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Severity is required")
    private String severity;
    
    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;
}