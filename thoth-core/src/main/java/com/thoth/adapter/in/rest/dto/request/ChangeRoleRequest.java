package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DT-20: DTO tipado para cambio de rol de usuario.
 */
public record ChangeRoleRequest(
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "ADMIN|TECHNICIAN|USER|VIEWER", message = "Rol invalido. Use: ADMIN, TECHNICIAN, USER, VIEWER")
    String role
) {}
