package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRequest {
    @NotNull(message = "El ID del equipo es obligatorio")
    private UUID equipmentId;
}