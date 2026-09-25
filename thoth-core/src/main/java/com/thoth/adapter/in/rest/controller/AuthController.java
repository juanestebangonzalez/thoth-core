package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.ChangePasswordRequest;
import com.thoth.adapter.in.rest.dto.request.PasswordResetRequest;
import com.thoth.adapter.in.rest.dto.request.LoginRequest;
import com.thoth.adapter.in.rest.dto.request.RegisterRequest;
import com.thoth.adapter.in.rest.dto.response.AuthResponse;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import com.thoth.application.service.AuthService;
import com.thoth.application.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Auth Management API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserJpaRepository userRepository;
    private final AuditService auditService;

    private String resolveUserId(String username) {
        return userRepository.findByUsername(username)
            .map(u -> u.getId().toString())
            .orElse(null);
    }

    @PostMapping("/register")
    @Operation(summary = "Register User")
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
    @Operation(summary = "Login")
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
    @Operation(summary = "Change Password (forced after reset)")
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
    @Operation(summary = "Request password reset (notifies admin)")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest body) {
        String username = body.username();
        String email = body.email();

        AuthService.PasswordResetRequestCommand command = new AuthService.PasswordResetRequestCommand(username, email);
        AuthService.SimpleResult result = authService.requestPasswordReset(command);

        // Log audit entry only for valid matches so admin can see real requests
        if (authService.isValidResetRequest(username, email)) {
            auditService.log("PASSWORD_RESET_REQUEST", "AUTH", null, username,
                "Solicitud de restablecimiento de contrasena para: " + username + " (" + email + ")", "system");
        }

        return ResponseEntity.ok(Map.of("message", result.message()));
    }
}