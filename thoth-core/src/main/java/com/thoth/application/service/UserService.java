package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;

    public record UserInfo(
        UUID id, String username, String email, String role, boolean enabled, String createdAt, boolean passwordChangeRequired
    ) {}

    public record ResetPasswordResult(String newPassword, String message, boolean success) {}
    public record UpdateResult(String message, boolean success) {}

    public List<UserInfo> listAll() {
        return userRepository.findAll().stream()
            .map(u -> new UserInfo(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().name(),
                u.isEnabled(),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : "",
                u.isPasswordChangeRequired()
            ))
            .toList();
    }

    public UpdateResult changeRole(UUID userId, String newRole) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new UpdateResult("Usuario no encontrado", false);
        }
        try {
            UserEntity.UserRole role = UserEntity.UserRole.valueOf(newRole.toUpperCase());
            user.setRole(role);
            userRepository.save(user);
            // Mantiene los permisos granulares en linea con el nuevo rol (ver SEC-009):
            // sin esto, un usuario promovido a ADMIN/TECHNICIAN quedaria sin filas de
            // permiso y las guardas basadas en PermissionService lo bloquearian igual.
            permissionService.assignDefaultPermissions(userId, role.name());
            return new UpdateResult("Rol actualizado a " + role.name(), true);
        } catch (IllegalArgumentException e) {
            return new UpdateResult("Rol invalido. Use: ADMIN, TECHNICIAN, USER, VIEWER", false);
        }
    }

    public UpdateResult toggleEnabled(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new UpdateResult("Usuario no encontrado", false);
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return new UpdateResult(
            user.isEnabled() ? "Usuario activado" : "Usuario desactivado",
            true
        );
    }

    public ResetPasswordResult resetPassword(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ResetPasswordResult(null, "Usuario no encontrado", false);
        }
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(true);
        userRepository.save(user);
        return new ResetPasswordResult(
            newPassword,
            "Contrasena reseteada. El usuario debera cambiarla en su proximo inicio de sesion.",
            true
        );
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}