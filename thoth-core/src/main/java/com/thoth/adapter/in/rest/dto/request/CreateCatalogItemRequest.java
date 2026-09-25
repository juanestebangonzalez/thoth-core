package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DT-20: DTO tipado para creación/actualización de elementos de catálogo
 * (Sede, DeviceType, MaintenanceCategory).
 */
public record CreateCatalogItemRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String name,

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    String description,

    @Size(max = 255, message = "La direccion no puede exceder 255 caracteres")
    String address,

    @Size(max = 20, message = "El telefono no puede exceder 20 caracteres")
    String phone,

    Boolean active
) {}
