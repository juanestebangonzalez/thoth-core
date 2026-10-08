package com.thoth.domain.valueobject;

import java.util.Locale;

public enum MaintenanceType {
    PREVENTIVE,
    CORRECTIVE;

    /**
     * true si el tipo guardado corresponde a un mantenimiento preventivo.
     * Acepta el codigo historico en ingles (PREVENTIVE) y el del catalogo en espanol (PREVENTIVO).
     */
    public static boolean isPreventive(String type) {
        String t = normalize(type);
        return "PREVENTIVE".equals(t) || "PREVENTIVO".equals(t);
    }

    /**
     * true si el tipo guardado corresponde a un mantenimiento correctivo.
     * Acepta el codigo historico en ingles (CORRECTIVE) y el del catalogo en espanol (CORRECTIVO).
     */
    public static boolean isCorrective(String type) {
        String t = normalize(type);
        return "CORRECTIVE".equals(t) || "CORRECTIVO".equals(t);
    }

    private static String normalize(String type) {
        return type == null ? null : type.trim().toUpperCase(Locale.ROOT);
    }
}
