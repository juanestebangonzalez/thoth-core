package com.thoth.adapter.in.rest.dto.request;

/**
 * Cuerpo de POST /api/v1/auth/reset-password: token recibido por correo y nueva contrasena.
 * La validacion se hace en el controlador para responder siempre {"message": ...}.
 */
public record ResetPasswordRequest(
    String token,
    String newPassword
) {}
