package com.thoth.adapter.in.rest.controller;

import com.thoth.application.service.UserService;
import com.thoth.application.service.AuditService;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Gestion de usuarios (solo ADMIN)")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuditService auditService;
    private final UserJpaRepository userJpaRepository;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<UserService.UserInfo>> listAll() {
        return ResponseEntity.ok(userService.listAll());
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Cambiar rol de un usuario")
    public ResponseEntity<UserService.UpdateResult> changeRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body, Principal principal) {
        String newRole = body.get("role");
        UserService.UpdateResult result = userService.changeRole(id, newRole);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(result);
        }
        if (result.success()) {
            auditService.log("CHANGE_ROLE", "USERS", id.toString(), null, "Nuevo rol: " + newRole, principal != null ? principal.getName() : "system");
        }
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/toggle-enabled")
    @Operation(summary = "Activar o desactivar un usuario")
    public ResponseEntity<UserService.UpdateResult> toggleEnabled(@PathVariable UUID id) {
        UserService.UpdateResult result = userService.toggleEnabled(id);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(result);
        }
        if (result.success()) {
            auditService.log("TOGGLE_ENABLED", "USERS", id.toString(), null, result.message(), "system");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Resetear contrasena y generar una temporal")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable UUID id, Principal principal) {
        UserService.ResetPasswordResult result = userService.resetPassword(id);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of("message", result.message(), "success", false));
        }
        // DT-07: Se registra en auditoría la acción de reset.
        auditService.log("RESET_PASSWORD", "USERS", id.toString(), null,
            "Contrasena temporal generada. El usuario debera cambiarla al iniciar sesion.",
            principal != null ? principal.getName() : "system");
        return ResponseEntity.ok(Map.of(
            "message", result.message(),
            "success", true,
            "newPassword", result.newPassword()
        ));
    }

    @PatchMapping("/{id}/email")
    @Operation(summary = "Cambiar email de un usuario")
    public ResponseEntity<UserService.UpdateResult> changeEmail(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body, Principal principal) {
        String newEmail = body.get("email");
        UserService.UpdateResult result = userService.changeEmail(id, newEmail);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(result);
        }
        auditService.log("CHANGE_EMAIL", "USERS", id.toString(), null, "Nuevo email: " + newEmail, principal != null ? principal.getName() : "system");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario permanentemente")
    public ResponseEntity<?> deleteUser(@PathVariable java.util.UUID id) {
        return userJpaRepository.findById(id).map(user -> {
            if (user.getUsername().equals("admin")) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "No se puede eliminar el usuario admin"));
            }
            userJpaRepository.delete(user);
            auditService.log("DELETE_USER", "USERS", id.toString(), user.getUsername(), "Usuario eliminado", "admin");
            return ResponseEntity.ok(java.util.Map.of("message", "Usuario eliminado: " + user.getUsername()));
        }).orElse(ResponseEntity.notFound().build());
    }
}