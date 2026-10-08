package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta / edicion de un periferico del equipo. */
public record PeripheralRequest(
    @NotBlank(message = "El tipo de periferico es obligatorio")
    @Size(max = 100, message = "El tipo de periferico no puede exceder 100 caracteres")
    String type,

    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    String brand
) {}
