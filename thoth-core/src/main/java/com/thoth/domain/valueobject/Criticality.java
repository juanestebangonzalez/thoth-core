package com.thoth.domain.valueobject;

import java.util.Locale;

/** Criticidad del equipo segun su centro de costo (valor calculado, no se persiste). */
public final class Criticality {

    public static final String ALTA = "ALTA";
    public static final String MEDIA = "MEDIA";
    public static final String BAJA = "BAJA";

    private Criticality() {}

    /** ASISTENCIAL -> ALTA, ADMINISTRATIVO -> MEDIA, cualquier otro o vacio -> BAJA. */
    public static String fromCostCenter(String costCenter) {
        if (costCenter == null || costCenter.isBlank()) return BAJA;
        String c = costCenter.trim().toUpperCase(Locale.ROOT);
        if (c.contains("ASISTENCIAL")) return ALTA;
        if (c.contains("ADMINISTRATIVO")) return MEDIA;
        return BAJA;
    }
}
