package com.thoth.adapter.in.rest.dto.request;

/**
 * DT-08: politica de contrasenas del sistema, en un unico lugar.
 *
 * Antes cada DTO declaraba su propio minimo (6 caracteres, sin exigencia de
 * composicion). Centralizarla evita que se vuelvan a separar y deja un unico
 * punto donde endurecerla en el futuro.
 *
 * Las constantes deben ser compile-time constants porque se usan dentro de
 * anotaciones de validacion.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {}

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 100;

    /** Al menos una letra y al menos un numero. */
    public static final String PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9]).+$";

    public static final String LENGTH_MESSAGE =
        "La contrasena debe tener entre 10 y 100 caracteres";

    public static final String PATTERN_MESSAGE =
        "La contrasena debe incluir al menos una letra y al menos un numero";
}
