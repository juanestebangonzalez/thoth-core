package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 6, message = "La nueva contrasena debe tener al menos 6 caracteres")
    private String newPassword;
}