package com.thoth.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reglas de validacion compartidas (formulario, importacion y dominio) para los datos
 * del responsable y de red del equipo. Los metodos validate* devuelven el mensaje de error
 * en espanol o null si el valor es valido (null/vacio siempre es valido: campos opcionales).
 */
public final class EquipmentFieldRules {

    public static final int RESPONSIBLE_POSITION_MAX = 100;
    public static final int RESPONSIBLE_DOCUMENT_MAX = 30;
    public static final int RESPONSIBLE_EMAIL_MAX = 150;
    public static final String IP_DHCP = "DHCP";
    public static final String IP_FIJA = "FIJA";

    public static final String PHONE_REGEX = "^\\d{10}$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";
    public static final String IPV4_REGEX =
        "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";

    private static final Pattern PHONE = Pattern.compile(PHONE_REGEX);
    private static final Pattern EMAIL = Pattern.compile(EMAIL_REGEX);
    private static final Pattern IPV4 = Pattern.compile(IPV4_REGEX);

    private EquipmentFieldRules() {}

    /** Texto recortado; vacio -> null. */
    public static String clean(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    public static String upper(String value) {
        String v = clean(value);
        return v != null ? v.toUpperCase(Locale.ROOT) : null;
    }

    public static String validateResponsiblePosition(String value) {
        String v = clean(value);
        if (v != null && v.length() > RESPONSIBLE_POSITION_MAX) {
            return "El cargo del responsable no puede superar " + RESPONSIBLE_POSITION_MAX + " caracteres";
        }
        return null;
    }

    public static String validateResponsibleDocument(String value) {
        String v = clean(value);
        if (v != null && v.length() > RESPONSIBLE_DOCUMENT_MAX) {
            return "El documento del responsable no puede superar " + RESPONSIBLE_DOCUMENT_MAX + " caracteres";
        }
        return null;
    }

    public static String validatePhone(String value) {
        String v = clean(value);
        if (v != null && !PHONE.matcher(v).matches()) {
            return "El celular del responsable debe tener exactamente 10 digitos (solo numeros)";
        }
        return null;
    }

    public static String validateEmail(String value) {
        String v = clean(value);
        if (v == null) return null;
        if (v.length() > RESPONSIBLE_EMAIL_MAX) {
            return "El correo del responsable no puede superar " + RESPONSIBLE_EMAIL_MAX + " caracteres";
        }
        if (!EMAIL.matcher(v).matches()) {
            return "El correo del responsable no tiene un formato valido";
        }
        return null;
    }

    public static String validateIpAddress(String value) {
        String v = clean(value);
        if (v != null && !IPV4.matcher(v).matches()) {
            return "La direccion IP '" + v + "' no es una IPv4 valida (ej: 192.168.1.10)";
        }
        return null;
    }

    /** Normaliza la asignacion de IP (DHCP | FIJA). Acepta tambien ESTATICA/STATIC como FIJA. */
    public static String normalizeIpAssignment(String value) {
        String v = upper(value);
        if (v == null) return null;
        if (v.equals("ESTATICA") || v.equals("ESTÁTICA") || v.equals("STATIC") || v.equals("FIXED")) return IP_FIJA;
        return v;
    }

    public static String validateIpAssignment(String value) {
        String v = normalizeIpAssignment(value);
        if (v != null && !v.equals(IP_DHCP) && !v.equals(IP_FIJA)) {
            return "La asignacion de IP '" + clean(value) + "' no es valida (use DHCP o FIJA)";
        }
        return null;
    }
}
