package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50)
    private String username;

    /**
     * DT-08: minimo 10 caracteres, con al menos una letra y al menos un numero.
     * El minimo anterior era de 6 caracteres sin ninguna exigencia de
     * composicion, lo que admitia contrasenas como "123456".
     */
    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH,
          message = PasswordPolicy.LENGTH_MESSAGE)
    @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.PATTERN_MESSAGE)
    private String password;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Formato de correo electronico invalido")
    private String email;
}
