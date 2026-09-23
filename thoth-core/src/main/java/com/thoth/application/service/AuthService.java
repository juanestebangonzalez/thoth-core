package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import com.thoth.config.security.JwtTokenProvider;
import com.thoth.application.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public record RegisterCommand(String username, String password, String email) {}
    public record LoginCommand(String username, String password) {}
    public record ChangePasswordCommand(String username, String currentPassword, String newPassword) {}
    public record PasswordResetRequestCommand(String username, String email) {}
    public record AuthResult(String token, String username, String role, String message, boolean success, boolean passwordChangeRequired) {}
    public record SimpleResult(String message, boolean success) {}

    public AuthResult register(RegisterCommand command) {
        // SEC-007 (user enumeration, MEDIUM): registration intentionally keeps
        // distinct "username taken" / "email taken" messages for usability -
        // the accepted mitigation is RateLimitingFilter (SEC-008) on this
        // endpoint rather than degrading the UX with a generic message.
        if (userRepository.existsByUsernameIgnoreCase(command.username())) {
            return new AuthResult(null, null, null, "El nombre de usuario ya existe", false, false);
        }
        if (userRepository.existsByEmailIgnoreCase(command.email())) {
            return new AuthResult(null, null, null, "El correo electronico ya esta registrado", false, false);
        }
        if (userRepository.existsByEmail(command.email())) {
            return new AuthResult(null, null, null, "El correo electronico ya esta registrado", false, false);
        }

        UserEntity user = UserEntity.builder()
            .username(command.username())
            .password(passwordEncoder.encode(command.password()))
            .email(command.email())
            .role(UserEntity.UserRole.USER)
            .enabled(true)
            .passwordChangeRequired(false)
            .createdAt(LocalDateTime.now())
            .build();
        userRepository.save(user);
        try { permissionService.assignDefaultPermissions(user.getId(), UserEntity.UserRole.USER.name()); } catch (Exception ignored) {};

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResult(token, user.getUsername(), user.getRole().name(), "Usuario registrado exitosamente", true, false);
    }

    public AuthResult login(LoginCommand command) {
        Optional<UserEntity> optUser = userRepository.findByUsername(command.username());
        if (optUser.isEmpty()) {
            return new AuthResult(null, null, null, "Invalid username or password", false, false);
        }

        UserEntity user = optUser.get();
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            return new AuthResult(null, null, null, "Invalid username or password", false, false);
        }
        if (!user.isEnabled()) {
            return new AuthResult(null, null, null, "La cuenta esta desactivada", false, false);
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResult(token, user.getUsername(), user.getRole().name(),
            user.isPasswordChangeRequired() ? "Debes cambiar tu contrasena" : "Inicio de sesion exitoso",
            true, user.isPasswordChangeRequired());
    }

    public AuthResult changePassword(ChangePasswordCommand command) {
        Optional<UserEntity> optUser = userRepository.findByUsername(command.username());
        if (optUser.isEmpty()) {
            return new AuthResult(null, null, null, "Usuario no encontrado", false, false);
        }

        UserEntity user = optUser.get();
        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            return new AuthResult(null, null, null, "Contrasena actual incorrecta", false, false);
        }

        if (command.newPassword() == null || command.newPassword().length() < 6) {
            return new AuthResult(null, null, null, "La nueva contrasena debe tener al menos 6 caracteres", false, false);
        }

        user.setPassword(passwordEncoder.encode(command.newPassword()));
        user.setPasswordChangeRequired(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResult(token, user.getUsername(), user.getRole().name(),
            "Contrasena actualizada exitosamente", true, false);
    }

    /**
     * Validates username+email match and returns a generic success message.
     * The audit log records the request so the admin can see it and use the
     * existing "reset password" action from the user management panel.
     * Returns the same message regardless of whether the user exists (anti-enumeration).
     */
    public SimpleResult requestPasswordReset(PasswordResetRequestCommand command) {
        // Always return success to prevent user enumeration
        String genericMessage = "Si los datos son correctos, el administrador sera notificado de tu solicitud.";

        Optional<UserEntity> optUser = userRepository.findByUsername(command.username());
        if (optUser.isEmpty()) {
            return new SimpleResult(genericMessage, true);
        }

        UserEntity user = optUser.get();
        // Verify email matches (case-insensitive)
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(command.email())) {
            return new SimpleResult(genericMessage, true);
        }

        // Valid match - the audit log entry is created by the controller
        return new SimpleResult(genericMessage, true);
    }

    /**
     * Check if user+email match exists (used by controller to decide whether to audit log).
     */
    public boolean isValidResetRequest(String username, String email) {
        Optional<UserEntity> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) return false;
        UserEntity user = optUser.get();
        return user.getEmail() != null && user.getEmail().equalsIgnoreCase(email) && user.isEnabled();
    }
}