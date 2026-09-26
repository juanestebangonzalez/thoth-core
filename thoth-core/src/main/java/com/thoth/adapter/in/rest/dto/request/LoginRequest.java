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
public class LoginRequest {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(max = 100)
    private String password;
}