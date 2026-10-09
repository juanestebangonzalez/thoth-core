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

    public static final String WINDOWS = "WINDOWS";

    /** Ediciones (software) permitidas; solo aplican cuando el sistema operativo es WINDOWS. */
    public static final List<String> OS_EDITIONS = List.of("WINDOWS 10", "WINDOWS 11");

    /** Versiones permitidas de WINDOWS (actualizaciones anuales/semestrales). */
    public static final List<String> WINDOWS_VERSIONS = List.of("26H2", "26H1", "25H2", "24H2", "23H2");

    /** Tipos de licencia del sistema operativo. */
    public static final List<String> LICENSE_TYPES = List.of("OEM", "RETAIL", "VOLUMEN");

    public static final String NO_APLICA = "NO APLICA";

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

    /** Normaliza la edicion: mayusculas, espacios simples y 'WINDOWS11' -> 'WINDOWS 11'; vacio -> null. */
    public static String normalizeEdition(String value) {
        String n = normalize(value);
        if (n == null) return null;
        return n.replaceAll("\\s+", " ").replaceAll("^WINDOWS\\s*(\\d+)$", "WINDOWS $1");
    }

    /** Mensaje de error si la edicion no es del catalogo (null si es valida o vacia). */
    public static String validateEdition(String edition) {
        String n = normalizeEdition(edition);
        if (n == null || OS_EDITIONS.contains(n)) return null;
        return "Software (edicion del sistema operativo) invalido: '" + edition + "'. Valores permitidos: "
            + String.join(", ", OS_EDITIONS);
    }

    /** Mensaje de error si hay edicion y el sistema operativo no es WINDOWS (null si es valido). */
    public static String validateEditionForOs(String operatingSystem, String edition) {
        if (normalizeEdition(edition) == null) return null;
        if (WINDOWS.equals(normalize(operatingSystem))) return null;
        return "La edicion del sistema operativo (" + String.join(" / ", OS_EDITIONS)
            + ") solo aplica cuando el sistema operativo es WINDOWS";
    }

    /** Si el SO es WINDOWS la version debe ser del catalogo; para otros SO es texto libre (null si valida). */
    public static String validateVersionForOs(String operatingSystem, String version) {
        String v = normalize(version);
        if (v == null || !WINDOWS.equals(normalize(operatingSystem))) return null;
        if (WINDOWS_VERSIONS.contains(v)) return null;
        return "Version de WINDOWS invalida: '" + version + "'. Valores permitidos: " + String.join(", ", WINDOWS_VERSIONS);
    }

    /** Mensaje de error si el tipo de licencia no es del catalogo (null si es valido o vacio). */
    public static String validateLicenseType(String licenseType) {
        String n = normalize(licenseType);
        if (n == null || LICENSE_TYPES.contains(n)) return null;
        return "Tipo de licencia invalido: '" + licenseType + "'. Valores permitidos: " + String.join(", ", LICENSE_TYPES);
    }

    /** Vencimiento de la licencia segun su tipo: OEM/RETAIL -> NO APLICA (perpetua); VOLUMEN u otro -> null. */
    public static String licenseExpiration(String licenseType) {
        String n = normalize(licenseType);
        return ("OEM".equals(n) || "RETAIL".equals(n)) ? NO_APLICA : null;
    }

    public static String invalidMessage(String value) {
        return "Sistema operativo invalido: '" + value + "'. Valores permitidos: " + String.join(", ", VALUES);
    }
}
