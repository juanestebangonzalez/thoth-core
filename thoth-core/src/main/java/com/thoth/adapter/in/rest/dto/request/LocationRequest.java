package com.thoth.adapter.in.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ubicacion del equipo. Todos los campos son opcionales.
 * building = sede, office = area, floor = piso (legado, ya no se captura en el formulario).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    private String building;
    private String floor;
    private String office;
}
