package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DT-20: DTO tipado para solicitud de reset de contraseña.
 */
public record PasswordResetRequest(
    @NotBlank(message = "El usuario es obligatorio")
    String username,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo invalido")
    String email
) {}
