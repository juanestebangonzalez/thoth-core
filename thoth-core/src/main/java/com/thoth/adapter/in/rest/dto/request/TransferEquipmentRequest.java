package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransferEquipmentRequest(
    @NotBlank(message = "La sede destino es obligatoria")
    @Size(max = 100, message = "La sede destino no puede exceder 100 caracteres")
    String toBuilding,

    @Size(max = 50, message = "El piso no puede exceder 50 caracteres")
    String toFloor,

    @Size(max = 50, message = "La oficina no puede exceder 50 caracteres")
    String toOffice,

    @NotBlank(message = "El motivo del traslado es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    String reason,

    /** Trasladar tambien los monitores asociados (null = true). */
    Boolean includeMonitors
) {
    /** Constructor de compatibilidad (sin includeMonitors: se trasladan los monitores). */
    public TransferEquipmentRequest(String toBuilding, String toFloor, String toOffice, String reason) {
        this(toBuilding, toFloor, toOffice, reason, null);
    }

    /** true salvo que se indique explicitamente false. */
    public boolean shouldIncludeMonitors() {
        return includeMonitors == null || includeMonitors;
    }
}
