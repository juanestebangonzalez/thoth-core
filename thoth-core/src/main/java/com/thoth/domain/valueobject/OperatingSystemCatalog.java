package com.thoth.domain.valueobject;

import java.util.List;
import java.util.Locale;

/**
 * Valores permitidos para el sistema operativo de un equipo.
 */
public final class OperatingSystemCatalog {

    public static final List<String> VALUES = List.of(
        "WINDOWS", "LINUX", "MACOS", "CHROMEOS", "ANDROID", "IOS", "OTRO", "N/A"
    );

    public static final int OS_VERSION_MAX_LENGTH = 100;

    private OperatingSystemCatalog() {}

    /** true si el valor es vacio/nulo o uno de los sistemas operativos permitidos. */
    public static boolean isValid(String value) {
        String n = normalize(value);
        return n == null || VALUES.contains(n);
    }

    /** Recorta y pasa a mayusculas; vacio -> null. */
    public static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static String invalidMessage(String value) {
        return "Sistema operativo invalido: '" + value + "'. Valores permitidos: " + String.join(", ", VALUES);
    }
}
