package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    @NotBlank(message = "El edificio es obligatorio")
    private String building;
    
    @NotBlank(message = "El piso es obligatorio")
    private String floor;
    
    @NotBlank(message = "La oficina es obligatoria")
    private String office;
}