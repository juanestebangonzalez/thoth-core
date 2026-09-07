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
    @NotBlank(message = "Building is required")
    private String building;
    
    @NotBlank(message = "Floor is required")
    private String floor;
    
    @NotBlank(message = "Office is required")
    private String office;
}