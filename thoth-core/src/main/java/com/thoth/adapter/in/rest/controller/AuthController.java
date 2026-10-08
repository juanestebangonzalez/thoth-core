package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.ChangePasswordRequest;
import com.thoth.adapter.in.rest.dto.request.PasswordResetRequest;
import com.thoth.adapter.in.rest.dto.request.LoginRequest;
import com.thoth.adapter.in.rest.dto.request.PasswordPolicy;
import com.thoth.adapter.in.rest.dto.request.RegisterRequest;
import com.thoth.adapter.in.rest.dto.request.ResetPasswordRequest;
import com.thoth.adapter.in.rest.dto.response.AuthResponse;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import com.thoth.application.service.AuthService;
import com.thoth.application.service.AuditService;
import com.thoth.application.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion, registro y recuperacion de contrasena")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserJpaRepository userRepository;
    private final AuditService auditService;
    private final PasswordResetService passwordResetService;

    /** Mensaje generico cuando el correo esta activo (no revela si el usuario existe). */
    static final String RESET_MAIL_MESSAGE =
        "Si los datos son correctos, recibiras un correo con un enlace para restablecer tu contrasena.";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PasswordPolicy.PATTERN);

    private String resolveUserId(String username) {
        return userRepository.findByUsername(username)
            .map(u -> u.getId().toString())
            .orElse(null);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.RegisterCommand command = new AuthService.RegisterCommand(
            request.getUsername(), request.getPassword(), request.getEmail()
        );
        AuthService.AuthResult result = authService.register(command);
        AuthResponse response = AuthResponse.builder()
            .userId(result.success() ? resolveUserId(result.username()) : null)
            .token(result.token()).username(result.username()).role(result.role())
            .message(result.message()).passwordChangeRequired(result.passwordChangeRequired()).build();
        if (!result.success()) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginCommand command = new AuthService.LoginCommand(request.getUsername(), request.getPassword());
        AuthService.AuthResult result = authService.login(command);
        AuthResponse response = AuthResponse.builder()
            .userId(result.success() ? resolveUserId(result.username()) : null)
            .token(result.token()).username(result.username()).role(result.role())
            .message(result.message()).passwordChangeRequired(result.passwordChangeRequired()).build();
        if (!result.success()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contrasena (obligatorio tras un restablecimiento)")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        AuthService.ChangePasswordCommand command = new AuthService.ChangePasswordCommand(
            request.getUsername(), request.getCurrentPassword(), request.getNewPassword()
        );
        AuthService.AuthResult result = authService.changePassword(command);
        AuthResponse response = AuthResponse.builder()
            .userId(result.success() ? resolveUserId(result.username()) : null)
            .token(result.token()).username(result.username()).role(result.role())
            .message(result.message()).passwordChangeRequired(result.passwordChangeRequired()).build();
        if (!result.success()) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-password-reset")
    @Operation(summary = "Solicitar restablecimiento de contrasena",
               description = "Con el correo activo (MAIL_ENABLED=true) envia un enlace al usuario; "
                   + "si no, deja la solicitud en la auditoria para el administrador. La respuesta es siempre la misma.")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest body) {
        String username = body.username();
        String email = body.email();

        if (passwordResetService.isMailEnabled()) {
            passwordResetService.requestReset(username, email);
            return ResponseEntity.ok(Map.of("message", RESET_MAIL_MESSAGE));
        }

        AuthService.PasswordResetRequestCommand command = new AuthService.PasswordResetRequestCommand(username, email);
        AuthService.SimpleResult result = authService.requestPasswordReset(command);

        // Log audit entry only for valid matches so admin can see real requests
        if (authService.isValidResetRequest(username, email)) {
            auditService.log("PASSWORD_RESET_REQUEST", "AUTH", null, username,
                "Solicitud de restablecimiento de contrasena para: " + username + " (" + email + ")", "system");
        }

        return ResponseEntity.ok(Map.of("message", result.message()));
    }

    @GetMapping("/reset-password/validate")
    @Operation(summary = "Validar enlace de restablecimiento de contrasena")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam(required = false) String token) {
        return ResponseEntity.ok(Map.of("valid", passwordResetService.isTokenValid(token)));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contrasena con el enlace recibido por correo")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody(required = false) ResetPasswordRequest body) {
        if (body == null || body.token() == null || body.token().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", PasswordResetService.MSG_INVALID_TOKEN));
        }
        String newPassword = body.newPassword();
        if (newPassword == null || newPassword.length() < PasswordPolicy.MIN_LENGTH
                || newPassword.length() > PasswordPolicy.MAX_LENGTH) {
            return ResponseEntity.badRequest().body(Map.of("message", PasswordPolicy.LENGTH_MESSAGE));
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", PasswordPolicy.PATTERN_MESSAGE));
        }
        PasswordResetService.ResetResult result = passwordResetService.resetPassword(body.token(), newPassword);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of("message", result.message()));
        }
        return ResponseEntity.ok(Map.of("message", result.message()));
    }
}
