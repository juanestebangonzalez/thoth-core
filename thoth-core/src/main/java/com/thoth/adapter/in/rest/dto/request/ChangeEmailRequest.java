package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DT-20: DTO tipado para cambio de email.
 */
public record ChangeEmailRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo invalido")
    String email
) {}
